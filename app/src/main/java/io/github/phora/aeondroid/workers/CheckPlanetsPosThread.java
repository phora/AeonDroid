package io.github.phora.aeondroid.workers;

import android.content.Intent;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Date;

import io.github.phora.aeondroid.Events;
import io.github.phora.aeondroid.calculations.Ephemeris;
import io.github.phora.aeondroid.calculations.EphemerisUtils;

/**
 * Created by phora on 9/23/15.
 */
class CheckPlanetsPosThread extends Thread {
    private AeonDroidService aeonDroidService;
    private int sleepVal;

    public CheckPlanetsPosThread(AeonDroidService aeonDroidService, int milliseconds) {
        this.aeonDroidService = aeonDroidService;
        this.sleepVal = milliseconds;
        setUncaughtExceptionHandler(Thread.getDefaultUncaughtExceptionHandler());
    }

    public void run() {
        try {
            while (!isInterrupted()) {
                Thread.sleep(this.sleepVal);

                Intent intent = new Intent();
                intent.setAction(Events.PLANET_POS);

                int count = AeonDroidService.planetsList.length;
                Ephemeris ephemeris = aeonDroidService.ephemeris;
                if (aeonDroidService.planetPos == null) {
                    aeonDroidService.planetPos = new double[count];
                }
                double[] results = aeonDroidService.planetPos;
                Date d = new Date();
                double julified = EphemerisUtils.dateToSweDate(d).getJulDay();

                for (int i = 0; i < count; i++) {
                    results[i] = ephemeris.getBodyPos(julified, AeonDroidService.planetsList[i]);
                }

                intent.putExtra(Events.EXTRA_PLANET_POS, results);
                aeonDroidService.localBroadcastManager.sendBroadcast(intent);
            }
        } catch (InterruptedException e) {

        }
    }
}
