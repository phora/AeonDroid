package io.github.phora.aeondroid.workers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import io.github.phora.aeondroid.CopyAssetFiles;
import io.github.phora.aeondroid.Events;
import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.activities.MainActivity;
import io.github.phora.aeondroid.calculations.Ephemeris;
import io.github.phora.aeondroid.calculations.EphemerisUtils;
import io.github.phora.aeondroid.drawables.PlanetIndicator;
import io.github.phora.aeondroid.model.MoonPhase;
import io.github.phora.aeondroid.model.PlanetaryHour;
import swisseph.SweConst;

public class AeonDroidService extends Service {

    Ephemeris ephemeris = null;
    volatile ArrayList<PlanetaryHour> planetaryHours = null;
    volatile ArrayList<MoonPhase> moonPhases;

    private volatile double[] natalChart;

    private int lastIndex = -1;
    private boolean usingGPS;
    private boolean gpsAvailable;

    private final static int NOTI_REQUEST_CODE = 116;
    final static int NOTIFICATION_ID = 117;
    public  final static int[] planetsList = new int[] {
            SweConst.SE_SUN, SweConst.SE_MERCURY, SweConst.SE_VENUS,
            SweConst.SE_MOON, SweConst.SE_MARS, SweConst.SE_JUPITER,
            SweConst.SE_SATURN, SweConst.SE_URANUS, SweConst.SE_NEPTUNE,
            SweConst.SE_PLUTO
    };
    private final AeonDroidBinder myBinder = new AeonDroidBinder();
    private LocUpdater locUpdater;

    private CheckPlanetaryHoursThread cpht = null;
    private CheckMoonPhaseThread      cmpt = null;
    private CheckPlanetsPosThread     cppt = null;
    private CheckVoidOfCourseThread cvct = null;
    private GenerateAspectTableThread gatt = null;

    NotificationManager   notificationManager;
    LocalBroadcastManager localBroadcastManager;

    PendingIntent contentIntent;
    private boolean _firstRun = true;

    public double[] getNatalChart() {
        return natalChart;
    }

    public Ephemeris getEphemeris() {
        return ephemeris;
    }

    public void setEphemeris(Ephemeris ephemeris) {
        this.ephemeris = ephemeris;
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

    public synchronized ArrayList<PlanetaryHour> getPlanetaryHours() {
        if (planetaryHours == null) {
            refreshPlanetaryHours();
        }
        return planetaryHours;
    }

    public synchronized void refreshPlanetaryHours(Date d) {
        planetaryHours = ephemeris.getPlanetaryHours(d);
    }

    public synchronized void refreshPlanetaryHours() {
        planetaryHours = ephemeris.getPlanetaryHours(new Date());
    }

    public synchronized ArrayList<MoonPhase> getMoonPhases() {
        if (moonPhases == null) {
            refreshMoonPhases();
        }
        return moonPhases;
    }

    public synchronized void refreshMoonPhases() {
        refreshMoonPhases(new Date());
    }

    public synchronized void refreshMoonPhases(Date d) {
        moonPhases = ephemeris.getMoonCycle(d);
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public LocalBroadcastManager getLocalBroadcastManager() {
        return localBroadcastManager;
    }

    public class AeonDroidBinder extends Binder {
        public AeonDroidService getService() {
            return AeonDroidService.this;
        }
    }

    public AeonDroidService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        new CopyAssetFiles(".*\\.se1", "ephe", getApplicationContext()).copy();
        new CopyAssetFiles("zone\\.tab", "", getApplicationContext()).copy();

        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotification();
        localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        ephemeris = new Ephemeris(getApplicationContext().getFilesDir() + File.separator + "ephe",
                this,
                0, 0, 0);
        locUpdater = new LocUpdater();

        cmpt = new CheckMoonPhaseThread(this, 1000);
        cmpt.start();

        cppt = new CheckPlanetsPosThread(this, 1500);
        cppt.start();

        cvct = new CheckVoidOfCourseThread(this, 1000);
        cvct.start();
    }

    private void createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.AppName))
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);
        Intent startIntent = new Intent(this, MainActivity.class);
        startIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        contentIntent = PendingIntent.getActivity(this,
                NOTI_REQUEST_CODE, startIntent, 0);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
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
            locationManager.removeUpdates(locUpdater);
            Log.d("ADService", "Using manually entered location");
            double longitude = Double.valueOf(preferences.getString("CurrentLoc.Longitude", "0"));
            double latitude = Double.valueOf(preferences.getString("CurrentLoc.Latitude", "0"));
            double altitude = Double.valueOf(preferences.getString("CurrentLoc.Altitude", "0"));

            Thread dummy = cpht;
            cpht = new CheckPlanetaryHoursThread(this, 1000);
            if (dummy != null) {
                dummy.interrupt();
            }

            boolean observerDifferent;
            double[] curObv = ephemeris.getObserver();
            observerDifferent = (curObv[0] != longitude) || (curObv[1] != latitude) || (curObv[2] != altitude);

            ephemeris.setObserver(longitude, latitude, altitude);
            refreshPlanetaryHours();

            if ((usingGPS != prevUsingGPS) || observerDifferent || _firstRun) {
                _firstRun = false;
                Intent intent = new Intent();
                intent.setAction(Events.REFRESH_HOURS);
                localBroadcastManager.sendBroadcast(intent);
            }

            cpht.start();
        }
    }

    public void recheckBirthplace() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //double longitude = Double.valueOf(preferences.getString("BirthLoc.Longitude", "0.0"));
        //double latitude = Double.valueOf(preferences.getString("BirthLoc.Latitude", "0.0"));

        long birthday_ms = preferences.getLong("BirthTime", 0);
        double birthday_days = EphemerisUtils.dateToSweDate(new Date(birthday_ms)).getJulDay();

        //stop thread here

        int count = planetsList.length;
        natalChart = new double[count];

        for (int i = 0; i < count; i++) {
            natalChart[i] = ephemeris.getBodyPos(birthday_days, planetsList[i]);
        }

        //start thread here
    }

    @Override
    public void onDestroy() {
        Thread dummy = cpht;
        cpht = null;
        dummy.interrupt();

        dummy = cmpt;
        cmpt = null;
        dummy.interrupt();

        dummy = cppt;
        cppt = null;
        dummy.interrupt();

        dummy = cvct;
        cvct = null;
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
            ephemeris.setObserver(location.getLongitude(), location.getLatitude(), 0);

            Thread dummy = cpht;
            cpht = new CheckPlanetaryHoursThread(AeonDroidService.this, 1000);
            if (dummy != null) {
                dummy.interrupt();
            }

            refreshPlanetaryHours();

            Intent intent = new Intent();
            intent.setAction(Events.REFRESH_HOURS);
            localBroadcastManager.sendBroadcast(intent);

            cpht.start();
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

}