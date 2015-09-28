package io.github.phora.aeondroid.workers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Date;

import io.github.phora.aeondroid.Events;
import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.calculations.EphemerisUtils;
import io.github.phora.aeondroid.drawables.PlanetIndicator;
import io.github.phora.aeondroid.model.PlanetaryHour;
import swisseph.SweDate;

/**
 * Created by phora on 9/23/15.
 */
class CheckPlanetaryHoursThread extends Thread {
    private AeonDroidService aeonDroidService;
    private PlanetIndicator pi;
    private int sleepVal;
    private int lastIndex = -1;

    public CheckPlanetaryHoursThread(AeonDroidService aeonDroidService, int milliseconds) {
        this.aeonDroidService = aeonDroidService;
        this.sleepVal = milliseconds;
        this.pi = PlanetIndicator.getInstance(aeonDroidService);
        setUncaughtExceptionHandler(Thread.getDefaultUncaughtExceptionHandler());
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

                ArrayList<PlanetaryHour> planetaryHours = aeonDroidService.planetaryHours;

                if (planetaryHours == null) {
                    lastIndex = -1;
                    intent.setAction(Events.REFRESH_HOURS);
                    aeonDroidService.refreshPlanetaryHours(d);
                    aeonDroidService.localBroadcastManager.sendBroadcastSync(intent);
                    continue;
                }
                int itemCount = planetaryHours.size();

                if (lastIndex == -1) {
                    for (int i = 0; i < itemCount; i++) {
                        PlanetaryHour ph = planetaryHours.get(i);
                        Date hourD = SweDate.getDate(ph.getHourStamp());
                        Date hourEndD = SweDate.getDate(ph.getHourStamp() + ph.getHourLength());
                        if (hourD.compareTo(d) <= 0 && hourEndD.compareTo(d) >= 0) {
                            hourDifferent = (lastIndex != i);
                            lastIndex = i;
                            foundHour = true;
                            break;
                        }
                    }
                } else {
                    for (int i = lastIndex; i < itemCount; i++) {
                        PlanetaryHour ph = planetaryHours.get(i);
                        Date hourD = SweDate.getDate(ph.getHourStamp());
                        Date hourEndD = SweDate.getDate(ph.getHourStamp() + ph.getHourLength());
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
                    aeonDroidService.refreshPlanetaryHours(d);
                    aeonDroidService.localBroadcastManager.sendBroadcast(intent);
                } else {
                    intent.setAction(Events.FOUND_HOUR);

                    PlanetaryHour ph = planetaryHours.get(lastIndex);
                    int planetType = ph.getPlanetType();

                    if (hourDifferent) {
                        String[] planets = aeonDroidService.getResources().getStringArray(R.array.PlanetNames);
                        String planetname = planets[planetType];
                        String contentText = String.format(aeonDroidService.getString(R.string.ADService_PHourIs), planetname);

                        String startsFmt = aeonDroidService.getString(R.string.PHoursAdapter_StartsAt);
                        String endsFmt = aeonDroidService.getString(R.string.PHoursAdapter_EndsAt);

                        Date sd = SweDate.getDate(ph.getHourStamp());
                        Date ed = SweDate.getDate(ph.getHourStamp() + ph.getHourLength());

                        String startsAt = String.format(startsFmt, EphemerisUtils.DATE_FMT.format(sd));
                        String endsAt = String.format(endsFmt, EphemerisUtils.DATE_FMT.format(ed));

                        String finresult = String.format("%s\n%s", startsAt, endsAt);

                        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(aeonDroidService)
                                .setContentTitle(contentText)
                                .setTicker(contentText)
                                .setContentText(finresult)
                                .setContentIntent(aeonDroidService.contentIntent)
                                .setOngoing(true);

                        //.setSmallIcon(R.mipmap.ic_launcher)

                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(aeonDroidService.getApplicationContext());
                        int hoursStyle = Integer.valueOf(sp.getString("phoursIndicatorDrawer", "0"));

                        if (hoursStyle == 0) {
                            notifyBuilder.setSmallIcon(R.mipmap.ic_launcher);
                        } else if (hoursStyle == 1) {
                            notifyBuilder.setSmallIcon(pi.getChakraNoti(planetType));
                        } else {
                            notifyBuilder.setSmallIcon(pi.getPlanetNoti(planetType));
                        }

                        aeonDroidService.getNotificationManager().notify(AeonDroidService.NOTIFICATION_ID, notifyBuilder.build());
                    }
                    intent.putExtra(Events.EXTRA_HOUR_TYPE, planetType);
                    intent.putExtra(Events.EXTRA_HOUR_INDEX, lastIndex);
                    aeonDroidService.localBroadcastManager.sendBroadcast(intent);
                }
            }
        } catch (InterruptedException e) {

        }
    }
}
