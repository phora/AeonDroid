package io.github.phora.aeondroid;

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

import io.github.phora.aeondroid.activities.MainActivity;
import io.github.phora.aeondroid.drawables.PlanetIndicator;
import io.github.phora.aeondroid.model.MoonPhase;
import io.github.phora.aeondroid.model.PlanetaryHour;
import swisseph.SweDate;

public class AeonDroidService extends Service {

    private Ephemeris ephemeris = null;
    private ArrayList<PlanetaryHour> planetaryHours = null;
    private int lastIndex = -1;
    private boolean usingGPS;
    private boolean gpsAvailable;

    private final static int NOTI_REQUEST_CODE = 116;
    private final static int NOTIFICATION_ID = 117;
    private final AeonDroidBinder myBinder = new AeonDroidBinder();
    private LocUpdater locUpdater;
    private CheckPlanetaryHoursThread cpht = null;
    private NotificationManager notificationManager;
    private LocalBroadcastManager localBroadcastManager;
    private ArrayList<MoonPhase> moonPhases;

    private PendingIntent contentIntent;
    private boolean _firstRun = true;
    private PlanetIndicator pi;



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

    public ArrayList<MoonPhase> getMoonPhases() {
        if (moonPhases == null) {
            refreshMoonPhases();
        }
        return moonPhases;
    }

    public void refreshMoonPhases() {
        refreshMoonPhases(new Date());
    }

    public void refreshMoonPhases(Date d) {
        moonPhases = ephemeris.getMoonCycle(d);
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

        new CopyAssetFiles(".*\\.se1", getApplicationContext()).copy();

        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotification();
        localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        pi = PlanetIndicator.getInstance(this);

        ephemeris = new Ephemeris(getApplicationContext().getFilesDir() + File.separator + "/ephe", 0, 0, 0);
        locUpdater = new LocUpdater();
    }

    private void createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);
        Intent startIntent = new Intent(this, MainActivity.class);
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
            cpht = new CheckPlanetaryHoursThread(1000);
            if (dummy != null) {
                dummy.interrupt();
            }

            boolean observer_different;
            double[] cur_obv = ephemeris.getObserver();
            observer_different = (cur_obv[0] != longitude) || (cur_obv[1] != latitude) || (cur_obv[2] != altitude);

            ephemeris.setObserver(longitude, latitude, altitude);
            refreshPlanetaryHours();

            if ((usingGPS != prevUsingGPS) || observer_different || _firstRun) {
                _firstRun = false;
                Intent intent = new Intent();
                intent.setAction(Events.REFRESH_HOURS);
                localBroadcastManager.sendBroadcastSync(intent);
            }

            cpht.start();
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
            ephemeris.setObserver(location.getLongitude(), location.getLatitude(), 0);

            Thread dummy = cpht;
            cpht = new CheckPlanetaryHoursThread(1000);
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
                    Intent intent = new Intent();

                    if (planetaryHours == null) {
                        lastIndex = -1;
                        intent.setAction(Events.REFRESH_HOURS);
                        refreshPlanetaryHours(d);
                        localBroadcastManager.sendBroadcastSync(intent);
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

                    if (!found_hour) {
                        lastIndex = -1;
                        intent.setAction(Events.REFRESH_HOURS);
                        refreshPlanetaryHours(d);
                        localBroadcastManager.sendBroadcastSync(intent);
                    } else {
                        intent.setAction(Events.FOUND_HOUR);

                        if (hour_different) {
                            PlanetaryHour ph = planetaryHours.get(lastIndex);
                            int planet_type = ph.getPlanetType();

                            String[] planets = getResources().getStringArray(R.array.PlanetNames);
                            String planetname = planets[planet_type];
                            String content_text = String.format(getString(R.string.ADService_PHourIs), planetname);

                            String starts_fmt = getString(R.string.PHoursAdapter_StartsAt);
                            String ends_fmt = getString(R.string.PHoursAdapter_EndsAt);

                            Date sd = SweDate.getDate(ph.getHourStamp());
                            Date ed = SweDate.getDate(ph.getHourStamp() + ph.getHourLength());

                            String starts_at  = String.format(starts_fmt, EphemerisUtils.DATE_FMT.format(sd));
                            String ends_at = String.format(ends_fmt, EphemerisUtils.DATE_FMT.format(ed));

                            String finresult = String.format("%s\n%s", starts_at, ends_at);

                            NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(AeonDroidService.this)
                                    .setContentTitle(content_text)
                                    .setTicker(content_text)
                                    .setContentText(finresult)
                                    .setContentIntent(contentIntent)
                                    .setOngoing(true);

                            //.setSmallIcon(R.mipmap.ic_launcher)

                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            int hoursStyle = Integer.valueOf(sp.getString("phoursIndicatorDrawer", "0"));

                            if (hoursStyle == 0) {
                                notifyBuilder.setSmallIcon(R.mipmap.ic_launcher);
                            }
                            else if (hoursStyle == 1) {
                                notifyBuilder.setSmallIcon(pi.getChakraNoti(planet_type));
                            }
                            else {
                                notifyBuilder.setSmallIcon(pi.getPlanetNoti(planet_type));
                            }

                            notificationManager.notify(NOTIFICATION_ID, notifyBuilder.build());
                        }

                        intent.putExtra(Events.EXTRA_HOUR_INDEX, lastIndex);
                        localBroadcastManager.sendBroadcast(intent);
                    }
                }
            } catch (InterruptedException e) {

            }
        }
    }
}
