package io.github.phora.aeondroid;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import swisseph.SweDate;

public class AeonDroidService extends Service {

    private Ephmeris ephmeris = null;
    private ArrayList<PlanetaryHour> planetaryHours = null;
    private int lastIndex = -1;
    private boolean usingGPS;
    private boolean gpsAvailable;

    public final static int NOTIFICATION_ID = 117;
    private final AeonDroidBinder myBinder = new AeonDroidBinder();
    private LocUpdater locUpdater;
    private CheckPlanetaryHoursThread cpht = null;
    private NotificationManager notificationManager;

    public Ephmeris getEphmeris() {
        return ephmeris;
    }

    public void setEphmeris(Ephmeris ephmeris) {
        this.ephmeris = ephmeris;
    }

    public boolean isGpsAvailable() {
        return gpsAvailable;
    }

    public void setGpsAvailable(boolean gpsAvailable) {
        this.gpsAvailable = gpsAvailable;
    }

    public boolean isUsingGPS() {
        return usingGPS;
    }

    public void setUsingGPS(boolean usingGPS) {
        this.usingGPS = usingGPS;
    }

    public ArrayList<PlanetaryHour> getPlanetaryHours() {
        if (planetaryHours == null) {
            refreshPlanetaryHours();
        }
        return planetaryHours;
    }

    public void refreshPlanetaryHours(Date d) {
        planetaryHours = ephmeris.getPlanetaryHours(d);
    }

    public void refreshPlanetaryHours() {
        planetaryHours = ephmeris.getPlanetaryHours(new Date());
    }

    public class AeonDroidBinder extends Binder {
        AeonDroidService getService() {
            return AeonDroidService.this;
        }
    }

    public AeonDroidService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        new CopyAssetFiles(".*\\.se1", getApplicationContext()).copy();

        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        ephmeris = new Ephmeris(getApplicationContext().getFilesDir() + File.separator + "/ephe", 0, 0, 0);
        locUpdater = new LocUpdater();

        recheckGps();

        cpht = new CheckPlanetaryHoursThread(1000);
        cpht.start();
    }

    public void recheckGps() {
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prevUsingGPS = usingGPS;
        usingGPS = preferences.getBoolean("CurrentLoc.Auto", false);
        gpsAvailable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (usingGPS && gpsAvailable) {
            Log.d("ADService", "Using GPS location");
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 10, locUpdater);
        }
        else {
            Log.d("ADService", "Using manually entered location");
            double longitude = Double.valueOf(preferences.getString("CurrentLoc.Longitude", "0"));
            double latitude = Double.valueOf(preferences.getString("CurrentLoc.Latitude", "0"));
            ephmeris.setObserver(longitude, latitude, 0.);

            if (usingGPS != prevUsingGPS) {
                Intent intent = new Intent();
                intent.setAction(Events.UPDATED_LOCATION);
                sendBroadcast(intent);
            }
        }
    }

    @Override
    public void onDestroy() {
        Thread dummy = cpht;
        cpht = null;
        dummy.interrupt();
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    private class LocUpdater implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Log.d("LocUpdater", "New location, refreshing displayed data");
            Toast.makeText(AeonDroidService.this, "New location, refreshing displayed data", Toast.LENGTH_SHORT).show();
            ephmeris.setObserver(location.getLongitude(), location.getLatitude(), 0);
            Intent intent = new Intent();
            intent.setAction(Events.UPDATED_LOCATION);
            sendBroadcast(intent);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {
            if (s.equals(LocationManager.GPS_PROVIDER)) {
                gpsAvailable = true;
            }
        }

        @Override
        public void onProviderDisabled(String s) {
            if (s.equals(LocationManager.GPS_PROVIDER)) {
                gpsAvailable = false;
            }
        }
    }

    private class CheckPlanetaryHoursThread extends Thread {
        private int sleepVal;
        private int lastIndex = -1;

        public CheckPlanetaryHoursThread(int milliseconds) {
            this.sleepVal = milliseconds;
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    Thread.sleep(this.sleepVal);

                    final Date d = new Date();
                    boolean found_hour = false;
                    boolean hour_different = false;

                    if (planetaryHours == null) {
                        continue;
                    }
                    int item_count = planetaryHours.size();

                    if (lastIndex == -1) {
                        for (int i=0; i<item_count;i++) {
                            PlanetaryHour ph = planetaryHours.get(i);
                            Date hour_d = SweDate.getDate(ph.getHourStamp());
                            Date hour_end_d = SweDate.getDate(ph.getHourStamp()+ph.getHourLength());
                            if (hour_d.compareTo(d) <= 0 && hour_end_d.compareTo(d) >= 0) {
                                hour_different = (lastIndex != i);
                                lastIndex = i;
                                found_hour = true;
                                break;
                            }
                        }
                    }
                    else {
                        for (int i=lastIndex; i<item_count;i++) {
                            PlanetaryHour ph = planetaryHours.get(i);
                            Date hour_d = SweDate.getDate(ph.getHourStamp());
                            Date hour_end_d = SweDate.getDate(ph.getHourStamp()+ph.getHourLength());
                            if (hour_d.compareTo(d) <= 0 && hour_end_d.compareTo(d) >= 0) {
                                hour_different = (lastIndex != i);
                                lastIndex = i;
                                found_hour = true;
                                break;
                            }
                        }
                    }

                    Intent intent = new Intent();
                    if (!found_hour) {
                        intent.setAction(Events.REFRESH_HOURS);
                        lastIndex = -1;
                        refreshPlanetaryHours(d);
                        sendBroadcast(intent);
                    } else if (hour_different) {
                        intent.setAction(Events.FOUND_HOUR);
                        PlanetaryHour ph = planetaryHours.get(lastIndex);
                        String[] planets = getResources().getStringArray(R.array.PlanetNames);
                        String planetname = planets[ph.getPlanetType()];
                        String content_text = String.format("Current planetary hour is %1$s", planetname);
                        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(AeonDroidService.this)
                                .setContentTitle(getString(R.string.app_name))
                                .setContentText(content_text)
                                .setSmallIcon(R.mipmap.ic_launcher);
                        notificationManager.notify(NOTIFICATION_ID, notifyBuilder.build());
                        intent.putExtra(Events.EXTRA_HOUR_INDEX, lastIndex);
                        sendBroadcast(intent);
                    }
                }
            } catch (InterruptedException e) {

            }
        }
    }
}
