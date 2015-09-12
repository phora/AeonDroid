package io.github.phora.aeondroid;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

    private final static int NOTI_REQUEST_CODE = 116;
    private LocUpdateReceiver locUpdateReceiver;
    private IntentFilter filterLocUpdate;

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

        Intent intent = new Intent(this, AeonDroidService.class);
        startService(intent);
        sendNotification();

        filterLocUpdate = new IntentFilter(Events.UPDATED_LOCATION);
        locUpdateReceiver = new LocUpdateReceiver();
        registerReceiver(locUpdateReceiver, filterLocUpdate);
    }

    public AeonDroidService getServiceReference() {
        return serviceReference;
    }

    private AeonDroidService serviceReference;
    private boolean isBound;

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

            Intent intent = new Intent();
            intent.setAction(Events.REFRESH_HOURS);
            sendBroadcast(intent);
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

    private void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setTicker("")
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);
        Intent startIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                NOTI_REQUEST_CODE, startIntent, 0);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(AeonDroidService.NOTIFICATION_ID, notification);
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
    protected void onPause() {
        super.onPause();
        unregisterReceiver(locUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isBound && serviceReference != null) {
            serviceReference.recheckGps();
        }
        registerReceiver(locUpdateReceiver, filterLocUpdate);
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

    private class LocUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MainActivityFragment maf = (MainActivityFragment)getSupportFragmentManager().findFragmentById(R.id.Fragment_PHours);
            maf.refreshFragment();
        }
    }
}
