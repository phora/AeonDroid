package io.github.phora.aeondroid.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import io.github.phora.aeondroid.AeonDroidService;
import io.github.phora.aeondroid.Events;
import io.github.phora.aeondroid.fragments.BroadcastReceivable;
import io.github.phora.aeondroid.fragments.MoonPhaseFragment;
import io.github.phora.aeondroid.fragments.PlanetaryHoursFragment;
import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.fragments.ReceiverFilterPair;
import io.github.phora.aeondroid.fragments.RightNowFragment;

public class MainActivity extends FragmentActivity {

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIsDark()) {
            setTheme(R.style.AppThemeDark);
        }
        else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager)findViewById(R.id.pager);
        mainTabsAdapter = new MainTabsAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mainTabsAdapter);

        Intent intent = new Intent(getApplicationContext(), AeonDroidService.class);
        startService(intent);
    }

    public AeonDroidService getServiceReference() {
        return serviceReference;
    }

    private AeonDroidService serviceReference;
    private boolean isBound;
    private MainTabsAdapter mainTabsAdapter;

    private ServiceConnection myConnection =  new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // called when the connection with the service has been
            // established. gives us the service object to use so we can
            // interact with the service.we have bound to a explicit
            // service that we know is running in our own process, so we can
            // cast its IBinder to a concrete class and directly access it.
            Log.i("MainActivity", "Bound service connected");
            serviceReference = ((AeonDroidService.AeonDroidBinder) service).getService();
            isBound = true;
            serviceReference.recheckGps();

            // we need to do this in case the refresh event wasn't fired from the service
            Log.i("MainActivity", "Forcing refresh retrieval");
            forceRefresh();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // called when the connection with the service has been
            // unexpectedly disconnected -- its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            Log.i("MainActivity", "Problem: bound service disconnected");
            serviceReference = null;
            isBound = false;
        }
    };

    private void forceRefresh() {
        Intent intent = new Intent();
        intent.setAction(Events.REFRESH_HOURS);
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        lbm.sendBroadcast(intent);
        intent = new Intent();
        intent.setAction(Events.REFRESH_MOON_PHASE);
        lbm.sendBroadcast(intent);
    }

    private boolean getIsDark() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isdark = preferences.getBoolean("isDark", false);
        return isdark;
    }

    private void doUnbindService() {
        Toast.makeText(this, "Unbinding...", Toast.LENGTH_SHORT).show();
        unbindService(myConnection);
        isBound = false;
    }

    //    bind to the service
    private void doBindService() {
        Toast.makeText(this, "Binding...", Toast.LENGTH_SHORT).show();
        if (!isBound) {
            Intent bindIntent = new Intent(this, AeonDroidService.class);
            isBound = bindService(bindIntent, myConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        Intent intent = new Intent();
        intent.setAction(Events.REFRESH_HOURS);
        lbm.sendBroadcastSync(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());

        for (int i = 0; i < mainTabsAdapter.getCount(); i++) {
            try {
                BroadcastReceivable br = (BroadcastReceivable) mainTabsAdapter.getItem(i);
                if (br.hasReceivers()) {
                    for (ReceiverFilterPair rfp : br.getReceivers()) {
                        rfp.unregister(lbm);
                    }
                }
            }
            catch (ClassCastException e) {
                continue;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());

        for (int i = 0; i < mainTabsAdapter.getCount(); i++) {
            try {
                BroadcastReceivable br = (BroadcastReceivable) mainTabsAdapter.getItem(i);
                if (br.hasReceivers()) {
                    for (ReceiverFilterPair rfp : br.getReceivers()) {
                        rfp.register(lbm);
                    }
                }
            } catch (ClassCastException e) {
                continue;
            }
        }

        if (!isBound) {
            doBindService();
        }
        else {
            forceRefresh();
        }
        if (isBound && serviceReference != null) {
            // we call this in case we manually changed our gps settings
            serviceReference.recheckGps();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        doBindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        doUnbindService();
    }

    @Override
    public void onBackPressed() {
       /* we customise the back button so that the activity pauses
        instead of finishing*/
        moveTaskToBack(true);
    }

    //    the activity is being destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("MainActivity", "Destroying activity...");
       /* it's not just being destroyed to rebuild due to orientation
        change but genuinely being destroyed...for ever*/
        if (isFinishing()) {
            Log.i("MainActivity", "activity is finishing");
//            stop service as activity being destroyed and we won't use it any more
            Intent intentStopService = new Intent(this, AeonDroidService.class);
            stopService(intentStopService);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class MainTabsAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> pages;

        public MainTabsAdapter(FragmentManager fm) {
            super(fm);
            pages = new ArrayList<Fragment>();
            pages.add(null);
            pages.add(null);
            pages.add(null);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fraggy = pages.get(position);
            switch (position) {
                case 0:
                    if (fraggy == null) {
                        fraggy = PlanetaryHoursFragment.newInstance();
                        pages.set(0, fraggy);
                        return fraggy;
                    }
                    else {
                        return fraggy;
                    }
                case 1:
                    if (fraggy == null) {
                        fraggy = MoonPhaseFragment.newInstance();
                        pages.set(1, fraggy);
                        return fraggy;
                    }
                    else {
                        return fraggy;
                    }
                case 2:
                    if (fraggy == null) {
                        fraggy = RightNowFragment.newInstance(null, null);
                        pages.set(2, fraggy);
                        return fraggy;
                    }
                    else {
                        return fraggy;
                    }
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
                case 0:
                    return getString(R.string.planetary_hours);
                case 1:
                    return getString(R.string.moon_phases);
                case 2:
                    return getString(R.string.right_now);
                default:
                    return null;
            }
        }
    }
}
