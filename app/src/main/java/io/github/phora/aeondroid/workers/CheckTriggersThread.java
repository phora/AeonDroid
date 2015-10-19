package io.github.phora.aeondroid.workers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.SparseArray;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import io.github.phora.aeondroid.AlertTriggerType;
import io.github.phora.aeondroid.DBHelper;
import io.github.phora.aeondroid.calculations.Ephemeris;
import io.github.phora.aeondroid.calculations.EphemerisUtils;
import io.github.phora.aeondroid.calculations.TriggerUtil;
import io.github.phora.aeondroid.model.AspectConfig;
import io.github.phora.aeondroid.model.MoonPhase;
import io.github.phora.aeondroid.model.PlanetaryHour;
import io.github.phora.aeondroid.model.SunsetSunriseInfo;
import swisseph.SweDate;

import static io.github.phora.aeondroid.AlertTriggerType.*;

/**
 * Created by phora on 10/5/15.
 */
public class CheckTriggersThread extends Thread {

    private AeonDroidService aeonDroidService;
    private int sleepVal;

    public CheckTriggersThread(AeonDroidService aeonDroidService, int milliseconds) {

        this.aeonDroidService = aeonDroidService;
        this.sleepVal = milliseconds;
    }

    @Override
    public void run() {
        try {
            DBHelper dbHelper = DBHelper.getInstance(aeonDroidService);
            SparseArray<AspectConfig> aspectConfigs = dbHelper.getOrbs();

            while(!isInterrupted()) {
                Thread.sleep(sleepVal);

                Date d = new Date();
                //read in the natal chart

                //get relevant data
                Set<Long> triggerIds = TriggerUtil.getActivatedTriggers(dbHelper, aeonDroidService.ephemeris, aspectConfigs,
                        aeonDroidService.planetPos, aeonDroidService.natalChart, d);

                if (!triggerIds.isEmpty()) {
                    Long[] triggerIdsAsArray = triggerIds.toArray(new Long[triggerIds.size()]);
                    //TODO: Set up ONE noti for all of the alerts.
                    //TODO: When the notification is tapped, a list will appear
                    //TODO: Tapping on a list item will show the full text of it OR
                    //TODO: Open the file that is associated with the triggered alert
                    //TODO: That being said, this is probably more useful than having crazy alert options like LED and such
                    //TODO: Since there maybe multiple working reminders that were triggered that we WANT to have all available
                    //TODO: At once, instead of a swarm of notis
                }
            }
        } catch (InterruptedException e) {

        }
    }
}
