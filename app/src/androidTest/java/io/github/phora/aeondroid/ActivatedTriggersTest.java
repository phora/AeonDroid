package io.github.phora.aeondroid;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;
import android.test.RenamingDelegatingContext;
import android.util.SparseArray;

import java.io.File;
import java.util.Date;
import java.util.Set;

import io.github.phora.aeondroid.calculations.Ephemeris;
import io.github.phora.aeondroid.calculations.TriggerUtil;
import io.github.phora.aeondroid.model.AspectConfig;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ActivatedTriggersTest extends ApplicationTestCase<Application> {
    private static final String TEST_FILE_PREFIX = "test_";
    public ActivatedTriggersTest() {
        super(Application.class);
    }

    protected SparseArray<AspectConfig> orbsConfig;
    protected SparseArray<AspectConfig> orbsConfig2;

    protected Ephemeris ephemeris;
    protected DBHelper dbHelper;
    protected Context context;

    protected Date newD;
    protected Date waxD;
    protected Date fullD;
    protected Date d;
    protected Date d2;

    protected Date dForAspectCheck;

    protected double[] currentChart;
    protected double[] natalChart;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();

        context = new RenamingDelegatingContext(getContext().getApplicationContext(), TEST_FILE_PREFIX);
        dbHelper = new DBHelper(context);
        orbsConfig = dbHelper.getOrbs();
        orbsConfig2 = dbHelper.getOrbs();
        dbHelper.close();
        //show inconjuncts in this copy of it
        orbsConfig2.valueAt(9).setShown(true);

        ephemeris = new Ephemeris(context.getApplicationContext().getFilesDir() + File.separator + "ephe", context);
        ephemeris.setObserver(0, 0, 0, "UTC");

        //2015-09-13 06:41:16 UTC - New Moon
        newD = new Date(1442126476000L);

        //2015-09-20 00:25:54 UTC - Waxing Crescent Moon
        waxD = new Date(1442708754114L);

        //2015-09-28 02:50:29 UTC - Full Moon
        fullD = new Date(1443408629000L);

        //2015-10-04 00:25:54 UTC - Waning Gibbous Moon
        d = new Date(1443918354114L);

        //2015-10-04 01:25:54 UTC - Waning Gibbous Moon
        d2 = new Date(1443921954114L);

        dForAspectCheck = new Date(1444154725055L);

        //generated for ~2015-10-06 18:45:16 UTC
        currentChart = new double[]{
                193.15205930965595, // Sun     @ Libra 13*9"7 (2nd decanate, Aquarius)
                124.77077833867101, // Moon    @ Leo 4*46"14 (1st decanate, Leo)
                181.63956496559175, // Mercury @ Libra 1*38"22 (1st decanate, Libra)
                148.3980108181602,  // Venus   @ Leo 28*23"52 (3rd decanate, Aries)
                157.26105726368579, // Mars    @ Virgo 7*15"39 (1st decanate, Virgo)
                162.00459243502624, // Jupiter @ Virgo 12*0"16 (2nd decanate, Capricorn)
                241.51387766412387, // Saturn  @ Sagittarius 1*30"49 (1st decanate, Sagittarius)
                18.72495590514388,  // Uranus  @ Aries 18*43"29 (2nd decanate, Leo)
                337.5047262172735,  // Neptune @ Pisces 7*30"17 (1st decanate, Pisces)
                283.00803669644336, // Pluto   @ Capricorn 13*0"28 (2nd decanate, Taurus)
        };

        //generated for ~1986-10-31 18:45:16 UTC
        natalChart = new double[]{
                218.00919931760305, // Sun     @ Scorpio 8*0"33 (1st decanate, Scorpio)
                197.78438955318217, // Moon    @ Libra 17*47"3 (2nd decanate, Aquarius)
                239.00223258967307, // Mercury @ Scorpio 29*0"8 (3rd decanate, Cancer)
                225.4734932352908,  // Venus   @ Scorpio 15*28"24 (2nd decanate, Pisces)
                313.4067835157731,  // Mars    @ Aquarius 13*24"24 (2nd decanate, Gemini)
                343.06920900149214, // Jupiter @ Pisces 13*4"9 (2nd decanate, Cancer)
                248.30570429513304, // Saturn  @ Sagittarius 8*18"20 (1st decanate, Sagittarius)
                260.0605024493464,  // Uranus  @ Sagittarius 20*3"37 (3rd decanate, Leo)
                273.63025195200686, // Neptune @ Capricorn 3*37"48 (1st decanate, Capricorn)
                217.3220705570839,  // Pluto   @ Scorpio 7*19"19 (1st decanate, Scorpio)
        };
    }

    public void testDayType() {
        //we always want a fresh DB for each test case since we'll put in different item sets for each test
        dbHelper = new DBHelper(context);

        //only trigger on sundays, dependent on the current date
        long sundayCurDay = dbHelper.createTrigger(AlertTriggerType.DAY_TYPE, 0L, null, 0L, true);
        //only trigger on sundays, if the sun rose for the appropriate sunrise time on a sunday
        long sundaySunriseDay = dbHelper.createTrigger(AlertTriggerType.DAY_TYPE, 0L, null, 1L, true);


        //we fill the natal and current chart with null and null since we're not testing for these
        Set<Long> triggeredItems = TriggerUtil.getActivatedTriggers(dbHelper, ephemeris, orbsConfig, null, null, d);
        dbHelper.close();

        assertTrue(triggeredItems.contains(sundayCurDay));
        assertFalse(triggeredItems.contains(sundaySunriseDay));
    }

    public void testMoonPhase() {
        //we always want a fresh DB for each test case since we'll put in different item sets for each test
        dbHelper = new DBHelper(context);

        //hopefully the variable names of these are self explanatory
        long newMoon  = dbHelper.createTrigger(AlertTriggerType.MOON_PHASE, 0L, null, null, true);
        long waxCres  = dbHelper.createTrigger(AlertTriggerType.MOON_PHASE, 1L, null, null, true);
        long fullMoon = dbHelper.createTrigger(AlertTriggerType.MOON_PHASE, 4L, null, null, true);
        long wanGib   = dbHelper.createTrigger(AlertTriggerType.MOON_PHASE, 5L, null, null, true);

        //we fill the natal and current chart with null and null since we're not testing for these
        Set<Long> triggeredNew = TriggerUtil.getActivatedTriggers(dbHelper, ephemeris, orbsConfig, null, null, newD);
        Set<Long> triggeredWaxCrescent = TriggerUtil.getActivatedTriggers(dbHelper, ephemeris, orbsConfig, null, null, waxD);
        Set<Long> triggeredFull = TriggerUtil.getActivatedTriggers(dbHelper, ephemeris, orbsConfig, null, null, fullD);
        Set<Long> triggeredWanGib = TriggerUtil.getActivatedTriggers(dbHelper, ephemeris, orbsConfig, null, null, d);
        dbHelper.close();

        //make sure the new moon trigger is ONLY triggering on a new moon
        assertTrue(triggeredNew.contains(newMoon));
        assertFalse(triggeredNew.contains(waxCres));
        assertFalse(triggeredNew.contains(fullMoon));
        assertFalse(triggeredNew.contains(wanGib));

        //make sure the wax cres moon trigger is ONLY triggering on a wax cres moon
        assertFalse(triggeredWaxCrescent.contains(newMoon));
        assertTrue(triggeredWaxCrescent.contains(waxCres));
        assertFalse(triggeredWaxCrescent.contains(fullMoon));
        assertFalse(triggeredWaxCrescent.contains(wanGib));

        //make sure the full moon trigger is ONLY triggering on a full moon
        assertFalse(triggeredFull.contains(newMoon));
        assertFalse(triggeredFull.contains(waxCres));
        assertTrue(triggeredFull.contains(fullMoon));
        assertFalse(triggeredFull.contains(wanGib));
        
        //make sure the wan gibbous moon trigger is ONLY triggering on a wan gibbous moon
        assertFalse(triggeredWanGib.contains(newMoon));
        assertFalse(triggeredWanGib.contains(waxCres));
        assertFalse(triggeredWanGib.contains(fullMoon));
        assertTrue(triggeredWanGib.contains(wanGib));
    }

    public void testPlanetSign() {
        dbHelper = new DBHelper(context);
        dbHelper.close();
    }

    public void testPlanetaryHour() {
        dbHelper = new DBHelper(context);
        long moonHour = dbHelper.createTrigger(AlertTriggerType.PLANETARY_HOUR, 1L, null, null, true);

        Set<Long> triggeredItems = TriggerUtil.getActivatedTriggers(dbHelper, ephemeris, orbsConfig, null, null, d);
        dbHelper.close();

        assertTrue(triggeredItems.contains(moonHour));
    }

    public void testDateTime() {
        dbHelper = new DBHelper(context);

        long dateAndTime = dbHelper.createTrigger(AlertTriggerType.DATETIME, 1443918354114L, null, 0L, true);
        long justDate = dbHelper.createTrigger(AlertTriggerType.DATETIME, 1443921954114L, null, 1L, true);
        long justTime = dbHelper.createTrigger(AlertTriggerType.DATETIME, 1443918354114L, null, 2L, true);

        Set<Long> triggeredDT = TriggerUtil.getActivatedTriggers(dbHelper, ephemeris, orbsConfig, null, null, d);
        Set<Long> triggeredDate = TriggerUtil.getActivatedTriggers(dbHelper, ephemeris, orbsConfig, null, null, d2);
        Set<Long> triggeredTime = TriggerUtil.getActivatedTriggers(dbHelper, ephemeris, orbsConfig, null, null, waxD);
        dbHelper.close();

        assertTrue(triggeredDT.contains(dateAndTime));
        assertTrue(triggeredDate.contains(justDate));
        assertTrue(triggeredTime.contains(justTime));
    }

    public void testAspect() {
        dbHelper = new DBHelper(context);

        // Jupiter on Natal Mars, inconjunct/quincunx
        long jupiterOnNatalMarsInc = dbHelper.createTrigger(AlertTriggerType.ASPECT, 5L, 4., 9L, true);
        // Jupiter on Natal Mars, conjunct
        long jupiterOnNatalMarsCon = dbHelper.createTrigger(AlertTriggerType.ASPECT, 5L, 4., 0L, true);

        // Mars on Natal Jupiter, opposition
        long MarsOnNatalJupiterOpp = dbHelper.createTrigger(AlertTriggerType.ASPECT, 4L, 5., 10L, true);
        // Mars on Natal Jupiter, trine
        long MarsOnNatalJupiterTrn = dbHelper.createTrigger(AlertTriggerType.ASPECT, 4L, 5., 6L, true);

        Set<Long> triggeredItems = TriggerUtil.getActivatedTriggers(dbHelper, ephemeris, orbsConfig,
                currentChart, natalChart, dForAspectCheck);
        Set<Long> triggeredItems2 = TriggerUtil.getActivatedTriggers(dbHelper, ephemeris, orbsConfig2,
                currentChart, natalChart, dForAspectCheck);
        dbHelper.close();

        //should report false for this set because inconjunct by default isn't displayed
        assertFalse(triggeredItems.contains(jupiterOnNatalMarsInc));
        assertFalse(triggeredItems.contains(jupiterOnNatalMarsCon));
        assertTrue(triggeredItems.contains(MarsOnNatalJupiterOpp));
        assertFalse(triggeredItems.contains(MarsOnNatalJupiterTrn));

        //should report true for this set because inconjunct is now displayed
        assertTrue(triggeredItems2.contains(jupiterOnNatalMarsInc));
        assertFalse(triggeredItems2.contains(jupiterOnNatalMarsCon));
        assertTrue(triggeredItems2.contains(MarsOnNatalJupiterOpp));
        assertFalse(triggeredItems2.contains(MarsOnNatalJupiterTrn));
    }

    public void testGroupTriggered() {
        dbHelper = new DBHelper(context);

        long moonHour = dbHelper.createTrigger(AlertTriggerType.PLANETARY_HOUR, 1L, null, null, true);
        long wanGib   = dbHelper.createTrigger(AlertTriggerType.MOON_PHASE, 5L, null, null, true);

        long sundaySunriseDay = dbHelper.createTrigger(AlertTriggerType.DAY_TYPE, 0L, null, 1L, true);

        long sundayCurDay = dbHelper.createTrigger(AlertTriggerType.DAY_TYPE, 0L, null, 0L, true);

        long group1 = dbHelper.createTrigger(AlertTriggerType.ATRIGGER_GROUP, null, null, null, true);
        long group2 = dbHelper.createTrigger(AlertTriggerType.ATRIGGER_GROUP, null, null, null, true);
        long group3 = dbHelper.createTrigger(AlertTriggerType.ATRIGGER_GROUP, null, null, null, true);

        dbHelper.addTriggersToGroup(group1, moonHour, wanGib);
        dbHelper.addTriggersToGroup(group2, moonHour, sundaySunriseDay);
        dbHelper.addTriggersToGroup(group3, moonHour, sundayCurDay);

        Set<Long> triggeredItems = TriggerUtil.getActivatedTriggers(dbHelper, ephemeris, orbsConfig,
                null, null, d);
        dbHelper.close();

        assertTrue(triggeredItems.contains(group1));
        assertFalse(triggeredItems.contains(group2));
        assertTrue(triggeredItems.contains(group3));
    }
}