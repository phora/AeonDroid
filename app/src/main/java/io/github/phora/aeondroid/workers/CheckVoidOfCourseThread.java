package io.github.phora.aeondroid.workers;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Date;

import io.github.phora.aeondroid.Events;
import io.github.phora.aeondroid.calculations.Ephemeris;
import io.github.phora.aeondroid.model.VoidOfCourseInfo;

/**
 * Created by phora on 9/23/15.
 */
class CheckVoidOfCourseThread extends Thread {
    private AeonDroidService aeonDroidService;
    private int sleepVal;
    private VoidOfCourseInfo voci;

    public CheckVoidOfCourseThread(AeonDroidService aeonDroidService, int sleepVal) {
        this.aeonDroidService = aeonDroidService;
        this.sleepVal = sleepVal;
    }

    @Override
    public void run() {
        try {
            Date d;
            d = new Date();

            Ephemeris ephemeris = aeonDroidService.ephemeris;

            voci = ephemeris.predictVoidOfCourse(d);

            while (!isInterrupted()) {
                Thread.sleep(sleepVal);
                d = new Date();
                if (voci == null) {
                    Log.d("VoCThread", "Nope, couldn't find anything");
                } else {
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
                        aeonDroidService.localBroadcastManager.sendBroadcast(intent);
                    } else if (d.compareTo(sd) <= 0) {
                        Log.d("VoCThread", "Pending VoC");
                        intent.setAction(Events.VOC_STATUS);
                        intent.putExtra(Events.EXTRA_VOC_IN, false);
                        //intent.putExtra(Events.EXTRA_VOC_FROM, voci.getSignFrom());
                        intent.putExtra(Events.EXTRA_VOC_FROM_DATE, voci.getStartDate().getTime());
                        //intent.putExtra(Events.EXTRA_VOC_TO, voci.getSignTo());
                        //intent.putExtra(Events.EXTRA_VOC_TO_DATE, voci.getEndDate().getTime());
                        aeonDroidService.localBroadcastManager.sendBroadcast(intent);
                    } else {
                        Log.d("VoCThread", "Need to recalculate VoC");
                        voci = ephemeris.predictVoidOfCourse(d);
                    }
                }
            }
        } catch (InterruptedException e) {

        }
    }
}
