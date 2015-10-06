package io.github.phora.aeondroid.calculations;

import android.database.Cursor;
import android.util.SparseArray;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import io.github.phora.aeondroid.AlertTriggerType;
import io.github.phora.aeondroid.DBHelper;
import io.github.phora.aeondroid.PhaseUtils;
import io.github.phora.aeondroid.model.AspectConfig;
import io.github.phora.aeondroid.model.MoonPhase;
import io.github.phora.aeondroid.model.PlanetaryHour;
import io.github.phora.aeondroid.model.SunsetSunriseInfo;
import swisseph.SweDate;

import static io.github.phora.aeondroid.AlertTriggerType.ATRIGGER_GROUP;
import static io.github.phora.aeondroid.AlertTriggerType.intToATT;

/**
 * Created by phora on 10/5/15.
 */
public class TriggerUtil {
    //TODO: Read in a list of objects representing triggers instead of querying the DB directly.
    //While this might use more RAM, it'd be better than requerying the database every second

    //TODO: Work on the class that will represent alerts, so the alerts don't need to be queried from DB.

    //TODO: There has to be a better way to rewrite this function to use less arguments, but still be unit-testable

    //TODO: If it's possible, try to precalculate most of the foreseeable intersections of the datetimes
    //I can see this working for just about everything but the aspects, since that requires some trig

    public static Set<Long> getActivatedTriggers(DBHelper dbHelper, Ephemeris ephemeris, SparseArray<AspectConfig> aspectConfigs,
                                              double[] planetPoses, double[] natalChart, Date d) {
        Double julified = EphemerisUtils.dateToSweDate(d).getJulDay();
        long millified = d.getTime();

        SunsetSunriseInfo ssi = ephemeris.getSunriseandSunset(d, ephemeris.getTimezone());
        PlanetaryHour ph = ssi.calculatePlanetHour(julified);
        MoonPhase mp = ephemeris.makeMoonPhaseForDate(d);

        LinkedList<Long> checkLast = new LinkedList<>();
        HashSet<Long> triggeredItems = new HashSet<>();
        HashSet<Long> triggeredGroups = new HashSet<>();

        Cursor cursor = dbHelper.getAllEnabledTriggers();

        while(cursor.moveToNext()) {
            long triggerId = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID));
            AlertTriggerType att = intToATT(cursor.getInt(cursor.getColumnIndex(DBHelper.ATRIGGER_TYPE)));
            boolean isEnabled = cursor.getInt(cursor.getColumnIndex(DBHelper.ATRIGGER_ENABLED)) == 1;

            if (att == ATRIGGER_GROUP) {
                checkLast.add(triggerId);
                continue;
            }
            else if (!isEnabled) {
                continue;
            }
            else {
                switch(att) {
                    case DAY_TYPE: {
                        boolean fromSunrise = cursor.getLong(cursor.getColumnIndex(DBHelper.ATRIGGER_SPECIFICITY)) == 1;
                        long targetDay = cursor.getLong(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG1));
                        if ((fromSunrise && ssi.getDayOffset() == targetDay)
                                || (!fromSunrise && SweDate.getDayOfWeekNr(julified) == targetDay)) {
                            triggeredItems.add(triggerId);
                        }
                    }
                    break;
                    case MOON_PHASE: {
                        int quickPhase = cursor.getInt(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG1));
                        boolean neededWaxing = quickPhase <= 4;
                        int adjustNum = quickPhase;
                        if (adjustNum > 4) {
                            //map it back down to 3 for gibbous, 2 for quarter, and so on
                            adjustNum = 8 - adjustNum;
                        }
                        MoonPhase.PhaseType neededPhase = PhaseUtils.phaseFromInt(adjustNum);
                        if ((neededPhase == MoonPhase.PhaseType.FULL || neededPhase == MoonPhase.PhaseType.NEW)
                                && neededPhase == mp.getPhaseType()) {
                            triggeredItems.add(triggerId);
                        }
                        else if (neededWaxing == mp.isWaxing() && neededPhase == mp.getPhaseType()) {
                            triggeredItems.add(triggerId);
                        }
                    }
                    break;
                    case PLANET_SIGN: {
                        boolean isSloppy = cursor.getLong(cursor.getColumnIndex(DBHelper.ATRIGGER_SPECIFICITY)) == 1;
                        Double idealAngle = cursor.getDouble(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG2));
                        int planet = cursor.getInt(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG1));
                        Double currentAngle = planetPoses[planet];
                        if ((isSloppy && ((idealAngle / 12) == (currentAngle / 12))) ||
                                Math.abs(EphemerisUtils.angleSubtract(idealAngle, currentAngle)) <= 5E-3) {
                            triggeredItems.add(triggerId);
                        }
                    }
                    break;
                    case PLANETARY_HOUR: {
                        int hourType = cursor.getInt(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG1));
                        if (hourType == ph.getPlanetType()) {
                            triggeredItems.add(triggerId);
                        }
                    }
                    break;
                    case DATETIME: {
                        long millis = cursor.getLong(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG1));
                        int dateOrTimeOpt = cursor.getInt(cursor.getColumnIndex(DBHelper.ATRIGGER_SPECIFICITY));
                        switch(dateOrTimeOpt) {
                            case 0:
                                if (Math.abs(millified - millis)/1000 == 0) {
                                    //we don't care about millisecond specificity
                                    triggeredItems.add(triggerId);
                                }
                                break;
                            case 1: {
                                    Calendar calendar = Calendar.getInstance();
                                    Calendar fedDateCal = Calendar.getInstance();
                                    calendar.setTimeInMillis(millis);
                                    fedDateCal.setTimeInMillis(millified);

                                    if ((calendar.get(Calendar.YEAR) == fedDateCal.get(Calendar.YEAR))
                                         && (calendar.get(Calendar.MONTH) == fedDateCal.get(Calendar.MONTH))
                                         && (calendar.get(Calendar.DAY_OF_MONTH) == fedDateCal.get(Calendar.DAY_OF_MONTH))) {
                                        triggeredItems.add(triggerId);
                                    }
                                }
                                break;
                            case 2: {
                                Calendar calendar = Calendar.getInstance();
                                Calendar fedDateCal = Calendar.getInstance();
                                calendar.setTimeInMillis(millis);
                                fedDateCal.setTimeInMillis(millified);

                                if ((calendar.get(Calendar.HOUR_OF_DAY) == fedDateCal.get(Calendar.HOUR_OF_DAY))
                                        && (calendar.get(Calendar.MINUTE) == fedDateCal.get(Calendar.MINUTE))
                                        && (calendar.get(Calendar.SECOND) == fedDateCal.get(Calendar.SECOND))) {
                                    triggeredItems.add(triggerId);
                                }
                            }
                                break;
                        }
                    }
                    break;
                    case ASPECT: {
                        int planet = cursor.getInt(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG1));
                        int natalPlanet = cursor.getInt(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG2));
                        double currentDegreeDistance = Math.abs(planetPoses[planet] - natalChart[natalPlanet]);

                        int wantedDegreePos = cursor.getInt(cursor.getColumnIndex(DBHelper.ATRIGGER_SPECIFICITY));
                        double degrees = aspectConfigs.keyAt(wantedDegreePos);
                        AspectConfig aspectConfig = aspectConfigs.valueAt(wantedDegreePos);
                        if (aspectConfig.isShown() && (Math.abs(degrees-currentDegreeDistance) <= aspectConfig.getOrb())) {
                            triggeredItems.add(triggerId);
                        }
                    }
                    break;
                }
            }
        }
        cursor.close();

        for (Long groupId: checkLast) {
            cursor = dbHelper.getSubtriggers(groupId);
            boolean allTriggered = true;
            while(cursor.moveToNext()) {
                long itemId = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID));
                if (!triggeredItems.contains(itemId)) {
                    allTriggered = false;
                    break;
                }
            }
            cursor.close();
            if (allTriggered) {
                triggeredGroups.add(groupId);
            }
        }

        triggeredItems.addAll(triggeredGroups);

        //fetch triggered alarms
        //then have those alarms do... things
        return triggeredItems;
    }
}
