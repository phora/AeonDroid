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
import io.github.phora.aeondroid.calculations.Ephemeris;
import io.github.phora.aeondroid.calculations.EphemerisUtils;
import io.github.phora.aeondroid.drawables.PlanetIndicator;
import io.github.phora.aeondroid.model.MoonPhase;
import io.github.phora.aeondroid.model.PlanetaryHour;
import io.github.phora.aeondroid.model.VoidOfCourseInfo;
import swisseph.SweConst;
import swisseph.SweDate;

public class AeonDroidService extends Service {

    private Ephemeris ephemeris = null;
    private volatile ArrayList<PlanetaryHour> planetaryHours = null;
    private volatile ArrayList<MoonPhase> moonPhases;
    private int lastIndex = -1;
    private boolean usingGPS;
    private boolean gpsAvailable;

    private final static int NOTI_REQUEST_CODE = 116;
    private final static int NOTIFICATION_ID = 117;
    private final AeonDroidBinder myBinder = new AeonDroidBinder();
    private LocUpdater locUpdater;
    private CheckPlanetaryHoursThread cpht = null;
    private CheckMoonPhaseThread cmpt = null;
    private CheckPlanetsPosThread cppt = null;
    private CheckVoidOfCourseThread cvct = null;
    private NotificationManager notificationManager;
    private LocalBroadcastManager localBroadcastManager;

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
        pi = PlanetIndicator.getInstance(this);

        ephemeris = new Ephemeris(getApplicationContext().getFilesDir() + File.separator + "ephe",
                this,
                0, 0, 0);
        locUpdater = new LocUpdater();

        cmpt = new CheckMoonPhaseThread(1000);
        cmpt.start();

        cppt = new CheckPlanetsPosThread(1500);
        cppt.start();

        cvct = new CheckVoidOfCourseThread(1000);
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
            cpht = new CheckPlanetaryHoursThread(1000);
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

    private class CheckPlanetsPosThread extends Thread {
        private int sleepVal;

        private final int[] planetsList = new int[] {
            SweConst.SE_SUN, SweConst.SE_MERCURY, SweConst.SE_VENUS,
            SweConst.SE_MOON, SweConst.SE_MARS, SweConst.SE_JUPITER,
            SweConst.SE_SATURN, SweConst.SE_URANUS, SweConst.SE_NEPTUNE,
            SweConst.SE_PLUTO
        };

        public CheckPlanetsPosThread(int milliseconds) {
            this.sleepVal = milliseconds;
        }

        public void run() {
            try {
                while (!isInterrupted()) {
                    Thread.sleep(this.sleepVal);

                    Intent intent = new Intent();
                    intent.setAction(Events.PLANET_POS);

                    int count = planetsList.length;
                    double[] results = new double[count];
                    Date d = new Date();
                    double julified = EphemerisUtils.dateToSweDate(d).getJulDay();

                    for (int i = 0; i < count; i++) {
                        results[i] = ephemeris.getBodyPos(julified, planetsList[i]);
                    }

                    intent.putExtra(Events.EXTRA_PLANET_POS, results);
                    localBroadcastManager.sendBroadcast(intent);
                }
            } catch (InterruptedException e) {

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
                    boolean foundHour = false;
                    boolean hourDifferent = false;
                    Intent intent = new Intent();

                    if (planetaryHours == null) {
                        lastIndex = -1;
                        intent.setAction(Events.REFRESH_HOURS);
                        refreshPlanetaryHours(d);
                        localBroadcastManager.sendBroadcastSync(intent);
                        continue;
                    }
                    int itemCount = planetaryHours.size();

                    if (lastIndex == -1) {
                        for (int i=0; i<itemCount;i++) {
                            PlanetaryHour ph = planetaryHours.get(i);
                            Date hourD = SweDate.getDate(ph.getHourStamp());
                            Date hourEndD = SweDate.getDate(ph.getHourStamp()+ph.getHourLength());
                            if (hourD.compareTo(d) <= 0 && hourEndD.compareTo(d) >= 0) {
                                hourDifferent = (lastIndex != i);
                                lastIndex = i;
                                foundHour = true;
                                break;
                            }
                        }
                    }
                    else {
                        for (int i=lastIndex; i<itemCount;i++) {
                            PlanetaryHour ph = planetaryHours.get(i);
                            Date hourD = SweDate.getDate(ph.getHourStamp());
                            Date hourEndD = SweDate.getDate(ph.getHourStamp()+ph.getHourLength());
                            if (hourD.compareTo(d) <= 0 && hourEndD.compareTo(d) >= 0) {
                                hourDifferent = (lastIndex != i);
                                lastIndex = i;
                                foundHour = true;
                                break;
                            }
                        }
                    }

                    if (!foundHour) {
                        lastIndex = -1;
                        intent.setAction(Events.REFRESH_HOURS);
                        refreshPlanetaryHours(d);
                        localBroadcastManager.sendBroadcast(intent);
                    } else {
                        intent.setAction(Events.FOUND_HOUR);

                        PlanetaryHour ph = planetaryHours.get(lastIndex);
                        int planetType = ph.getPlanetType();

                        if (hourDifferent) {
                            String[] planets = getResources().getStringArray(R.array.PlanetNames);
                            String planetname = planets[planetType];
                            String contentText = String.format(getString(R.string.ADService_PHourIs), planetname);

                            String startsFmt = getString(R.string.PHoursAdapter_StartsAt);
                            String endsFmt = getString(R.string.PHoursAdapter_EndsAt);

                            Date sd = SweDate.getDate(ph.getHourStamp());
                            Date ed = SweDate.getDate(ph.getHourStamp() + ph.getHourLength());

                            String startsAt  = String.format(startsFmt, EphemerisUtils.DATE_FMT.format(sd));
                            String endsAt = String.format(endsFmt, EphemerisUtils.DATE_FMT.format(ed));

                            String finresult = String.format("%s\n%s", startsAt, endsAt);

                            NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(AeonDroidService.this)
                                    .setContentTitle(contentText)
                                    .setTicker(contentText)
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
                                notifyBuilder.setSmallIcon(pi.getChakraNoti(planetType));
                            }
                            else {
                                notifyBuilder.setSmallIcon(pi.getPlanetNoti(planetType));
                            }

                            notificationManager.notify(NOTIFICATION_ID, notifyBuilder.build());
                        }
                        intent.putExtra(Events.EXTRA_HOUR_TYPE, planetType);
                        intent.putExtra(Events.EXTRA_HOUR_INDEX, lastIndex);
                        localBroadcastManager.sendBroadcast(intent);
                    }
                }
            } catch (InterruptedException e) {

            }
        }
    }

    private class CheckMoonPhaseThread extends Thread {
        private int sleepVal;
        private int lastIndex = -1;

        public CheckMoonPhaseThread(int milliseconds) {
            this.sleepVal = milliseconds;
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    Thread.sleep(sleepVal);

                    Intent intent = new Intent();
                    Date d = new Date();

                    if (moonPhases == null) {
                        lastIndex = -1;
                        intent.setAction(Events.REFRESH_MOON_PHASE);
                        refreshMoonPhases(d);
                        localBroadcastManager.sendBroadcast(intent);
                        continue;
                    }

                    boolean foundPhase = false;
                    int count = moonPhases.size();

                    if (lastIndex == -1) {
                        for (int i = 0; i < count-1; i++) {
                            MoonPhase mp = moonPhases.get(i);
                            MoonPhase mp2 = moonPhases.get(i+1);

                            Date dmp = mp.getTimeStamp();
                            Date dmp2 = mp2.getTimeStamp();

                            if (dmp.compareTo(d) <= 0 && dmp2.compareTo(d) >= 0) {
                                lastIndex = i;
                                foundPhase = true;
                                break;
                            }
                        }
                    }
                    else {
                        for (int i = lastIndex; i < count-1; i++) {
                            MoonPhase mp = moonPhases.get(i);
                            MoonPhase mp2 = moonPhases.get(i+1);

                            Date dmp = mp.getTimeStamp();
                            Date dmp2 = mp2.getTimeStamp();

                            if (dmp.compareTo(d) <= 0 && dmp2.compareTo(d) >= 0) {
                                lastIndex = i;
                                foundPhase = true;
                                break;
                            }
                        }
                    }

                    if (foundPhase) {
                        intent.setAction(Events.FOUND_PHASE);
                        intent.putExtra(Events.EXTRA_LPHASE_INDEX, lastIndex);
                        localBroadcastManager.sendBroadcast(intent);
                        //update sign info from here?
                    }
                    else {
                        lastIndex = -1;
                        refreshMoonPhases(d);
                        intent.setAction(Events.REFRESH_MOON_PHASE);
                        localBroadcastManager.sendBroadcast(intent);
                    }

                    intent = new Intent();
                    intent.setAction(Events.PHASE_DETAILS);
                    MoonPhase now = ephemeris.makeMoonPhaseForDate(d, moonPhases.get(4).getTimeStamp());
                    intent.putExtra(Events.EXTRA_LPHASE_TYPE, PhaseUtils.phaseToInt(now.getPhaseType()));
                    intent.putExtra(Events.EXTRA_LPHASE_WAXING, now.isWaxing());
                    localBroadcastManager.sendBroadcast(intent);
                }
            } catch (InterruptedException e) {

            }
        }
    }

    private class CheckVoidOfCourseThread extends Thread {
        private int sleepVal;
        private VoidOfCourseInfo voci;

        public CheckVoidOfCourseThread(int sleepVal) {
            this.sleepVal = sleepVal;
        }

        @Override
        public void run() {
            try {
                Date d;
                d = new Date();

                voci = ephemeris.predictVoidOfCourse(d);

                while (!isInterrupted()) {
                    Thread.sleep(sleepVal);
                    d = new Date();
                    if (voci == null) {
                        Log.d("VoCThread", "Nope, couldn't find anything");
                    }
                    else {
                        Intent intent = new Intent();
                        Date ed = voci.getEndDate();
                        Date sd = voci.getStartDate();
                        if (d.compareTo(sd) >= 0 && d.compareTo(ed) <= 0) {
                            Log.d("VoCThread", "Currently VoC");
                            intent.setAction(Events.VOC_STATUS);
                            intent.putExtra(Events.EXTRA_VOC_IN, true);
                            //intent.putExtra(Events.EXTRA_VOC_FROM, voci.getSignFrom());
                            //intent.putExtra(Events.EXTRA_VOC_FROM_DATE, voci.getStartDate().getTime());
                            //intent.putExtra(Events.EXTRA_VOC_TO, voci.getSignTo());
                            intent.putExtra(Events.EXTRA_VOC_TO_DATE, voci.getEndDate().getTime());
                            localBroadcastManager.sendBroadcast(intent);
                        }
                        else if (d.compareTo(sd) <= 0) {
                            Log.d("VoCThread", "Pending VoC");
                            intent.setAction(Events.VOC_STATUS);
                            intent.putExtra(Events.EXTRA_VOC_IN, false);
                            //intent.putExtra(Events.EXTRA_VOC_FROM, voci.getSignFrom());
                            intent.putExtra(Events.EXTRA_VOC_FROM_DATE, voci.getStartDate().getTime());
                            //intent.putExtra(Events.EXTRA_VOC_TO, voci.getSignTo());
                            //intent.putExtra(Events.EXTRA_VOC_TO_DATE, voci.getEndDate().getTime());
                            localBroadcastManager.sendBroadcast(intent);
                        }
                        else {
                            Log.d("VoCThread", "Need to recalculate VoC");
                            voci = ephemeris.predictVoidOfCourse(d);
                        }
                    }
                }
            } catch (InterruptedException e) {

            }
        }
    }
}