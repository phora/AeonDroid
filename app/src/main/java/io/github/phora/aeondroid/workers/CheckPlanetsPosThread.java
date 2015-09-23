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
    }

    public void run() {
        try {
            while (!isInterrupted()) {
                Thread.sleep(this.sleepVal);

                Intent intent = new Intent();
                intent.setAction(Events.PLANET_POS);

                int count = aeonDroidService.planetsList.length;
                Ephemeris ephemeris = aeonDroidService.ephemeris;
                double[] results = new double[count];
                Date d = new Date();
                double julified = EphemerisUtils.dateToSweDate(d).getJulDay();

                for (int i = 0; i < count; i++) {
                    results[i] = ephemeris.getBodyPos(julified, aeonDroidService.planetsList[i]);
                }

                /*if (aeonDroidService.gatt != null) {
                    Message message = new Message();
                    message.obj = results;
                    aeonDroidService.gatt.aspectHandler.sendMessage(message);
                }*/

                intent.putExtra(Events.EXTRA_PLANET_POS, results);
                aeonDroidService.localBroadcastManager.sendBroadcast(intent);
            }
        } catch (InterruptedException e) {

        }
    }
}
