package io.github.phora.aeondroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends FragmentActivity {

    private Ephmeris ephmeris;
    private LocUpdater locUpdater;
    private boolean usingGPS;

    public boolean isGpsAvailable() {
        return gpsAvailable;
    }

    public void setGpsAvailable(boolean gpsAvailable) {
        this.gpsAvailable = gpsAvailable;
    }

    private boolean gpsAvailable;

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

        new CopyAssetFiles(".*\\.se1", getApplicationContext()).copy();

        ephmeris = new Ephmeris(getApplicationContext().getFilesDir() + File.separator + "/ephe", 0, 0, 0);
        locUpdater = new LocUpdater();
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        usingGPS = preferences.getBoolean("CurrentLoc.Auto", false);
        gpsAvailable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (usingGPS && gpsAvailable) {
                Log.d("MainActivity", "Using GPS location");
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 5000, 10, locUpdater);
        }
        else {
            Log.d("MainActivity", "Using manually entered location");
            double longitude = Double.valueOf(preferences.getString("CurrentLoc.Longitude", "0"));
            double latitude = Double.valueOf(preferences.getString("CurrentLoc.Latitude", "0"));
            ephmeris.setObserver(longitude, latitude, 0.);
        }
    }

    private boolean getIsDark() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isdark = preferences.getBoolean("isDark", false);
        return isdark;
    }

    public Ephmeris getEphmeris() {
        return ephmeris;
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

    public boolean isUsingGPS() {
        return usingGPS;
    }

    public void setUsingGPS(boolean usingGPS) {
        this.usingGPS = usingGPS;
    }

    private class LocUpdater implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Log.d("LocUpdater", "New location, refreshing displayed data");
            Toast.makeText(MainActivity.this, "New location, refreshing displayed data", Toast.LENGTH_SHORT).show();
            ephmeris.setObserver(location.getLongitude(), location.getLatitude(), 0);
            MainActivityFragment maf = (MainActivityFragment)getSupportFragmentManager().findFragmentById(R.id.Fragment_PHours);
            maf.refreshFragment();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }
}
