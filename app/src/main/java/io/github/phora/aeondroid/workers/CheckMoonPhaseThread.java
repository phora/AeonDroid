package io.github.phora.aeondroid.workers;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Date;

import io.github.phora.aeondroid.Events;
import io.github.phora.aeondroid.PhaseUtils;
import io.github.phora.aeondroid.model.MoonPhase;

/**
 * Created by phora on 9/23/15.
 */
class CheckMoonPhaseThread extends Thread {
    private AeonDroidService aeonDroidService;
    private int sleepVal;
    private int lastIndex = -1;

    public CheckMoonPhaseThread(AeonDroidService aeonDroidService, int milliseconds) {
        this.aeonDroidService = aeonDroidService;
        this.sleepVal = milliseconds;
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Thread.sleep(sleepVal);

                Intent intent = new Intent();
                Date d = new Date();
                ArrayList<MoonPhase> moonPhases = aeonDroidService.moonPhases;

                if (moonPhases == null) {
                    lastIndex = -1;
                    intent.setAction(Events.REFRESH_MOON_PHASE);
                    aeonDroidService.refreshMoonPhases(d);
                    aeonDroidService.localBroadcastManager.sendBroadcast(intent);
                    continue;
                }

                boolean foundPhase = false;
                int count = moonPhases.size();

                if (lastIndex == -1) {
                    for (int i = 0; i < count - 1; i++) {
                        MoonPhase mp = moonPhases.get(i);
                        MoonPhase mp2 =moonPhases.get(i + 1);

                        Date dmp = mp.getTimeStamp();
                        Date dmp2 = mp2.getTimeStamp();

                        if (dmp.compareTo(d) <= 0 && dmp2.compareTo(d) >= 0) {
                            lastIndex = i;
                            foundPhase = true;
                            break;
                        }
                    }
                } else {
                    for (int i = lastIndex; i < count - 1; i++) {
                        MoonPhase mp = moonPhases.get(i);
                        MoonPhase mp2 = moonPhases.get(i + 1);

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
                    aeonDroidService.localBroadcastManager.sendBroadcast(intent);
                    //update sign info from here?
                } else {
                    lastIndex = -1;
                    aeonDroidService.refreshMoonPhases(d);
                    intent.setAction(Events.REFRESH_MOON_PHASE);
                    aeonDroidService.localBroadcastManager.sendBroadcast(intent);
                }

                intent = new Intent();
                intent.setAction(Events.PHASE_DETAILS);
                MoonPhase now = aeonDroidService.ephemeris.makeMoonPhaseForDate(d, moonPhases.get(4).getTimeStamp());
                intent.putExtra(Events.EXTRA_LPHASE_TYPE, PhaseUtils.phaseToInt(now.getPhaseType()));
                intent.putExtra(Events.EXTRA_LPHASE_WAXING, now.isWaxing());
                aeonDroidService.localBroadcastManager.sendBroadcast(intent);
            }
        } catch (InterruptedException e) {

        }
    }
}
