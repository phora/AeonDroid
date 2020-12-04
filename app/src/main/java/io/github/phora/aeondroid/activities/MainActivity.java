package io.github.phora.aeondroid.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import io.github.phora.aeondroid.widgets.MainTabsAdapter;
import io.github.phora.aeondroid.workers.AeonDroidService;
import io.github.phora.aeondroid.Events;
import io.github.phora.aeondroid.widgets.BroadcastReceivable;
import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.widgets.ReceiverFilterPair;

public class MainActivity extends FragmentActivity {

    private ViewPager viewPager;
    private Context context;

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

        context = this;
        mainTabsAdapter = new MainTabsAdapter(MainActivity.this, null);
        viewPager.setAdapter(mainTabsAdapter);
    }

    public AeonDroidService getServiceReference() {
        return serviceReference;
    }

    private AeonDroidService serviceReference;
    private boolean isBound;
    private MainTabsAdapter mainTabsAdapter;

    private class RecheckGPSTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog pd;
        boolean doRefresh;
        private Handler mHandler = new Handler(Looper.getMainLooper());

        public RecheckGPSTask(boolean doRefresh) {
            this.doRefresh = doRefresh;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    serviceReference.recheckGps();
                }
            });
            serviceReference.recheckBirthplace();
            return null;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(context);
            pd.setIndeterminate(true);
            pd.setMessage(getString(R.string.MainActivity_CheckingCalcs));
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pd.dismiss();
            // we need to do this in case the refresh event wasn't fired from the service
            if (doRefresh) {
                Log.i("MainActivity", "Forcing refresh retrieval");
                forceRefresh();
            }
        }
    }

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
            mainTabsAdapter.setServiceReference(serviceReference);
            //isBound = true;
            new RecheckGPSTask(true).execute();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // called when the connection with the service has been
            // unexpectedly disconnected -- its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            Log.i("MainActivity", "Problem: bound service disconnected");
            serviceReference = null;
            mainTabsAdapter.setServiceReference(null);
            //isBound = false;
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
                    Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
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

        Intent intent = new Intent(getApplicationContext(), AeonDroidService.class);
        startService(intent);

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());

        Log.d("MainActivity", "Registering broadcast receivers");
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

        boolean prevIsBound = isBound;
        if (!isBound) {
            doBindService();
        }

        if (isBound && serviceReference != null && prevIsBound == isBound) {
            // we call this in case we manually changed our gps settings
            Log.d("MainActivity", "Check if we need to redo our calculations because locations changed");
            new RecheckGPSTask(false).execute();
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
        instead of finishing */
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
        } else if (id == R.id.action_triggers) {
            Intent i = new Intent(this, TriggersActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.action_alerts) {
            Intent i = new Intent(this, AlertsCRUDActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.action_about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            View view = LayoutInflater.from(this).inflate(R.layout.about_dialog, null);
            String verName;
            try {
                verName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            }
            catch (PackageManager.NameNotFoundException e) {
                verName = null;
            }
            TextView textView = (TextView)view.findViewById(R.id.AboutDialog_Version);
            textView.setText(getString(R.string.About_Version, verName));

            builder.setTitle(getString(R.string.About_App, getString(R.string.AppName)));
            builder.setView(view);
            builder.setNegativeButton(R.string.OK, null);
            builder.create().show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
