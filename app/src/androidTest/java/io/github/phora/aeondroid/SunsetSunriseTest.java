package io.github.phora.aeondroid;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;

import io.github.phora.aeondroid.calculations.Ephemeris;
import io.github.phora.aeondroid.calculations.EphemerisUtils;
import io.github.phora.aeondroid.model.SunsetSunriseInfo;
import swisseph.SweDate;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class SunsetSunriseTest extends ApplicationTestCase<Application> {
    public SunsetSunriseTest() {
        super(Application.class);
    }

    protected double[] greenwich = new double[]{0, 0, 0};
    protected double[] seattle   = new double[]{-122.3320700, 47.6062100, 56};
    protected double[] mandurah  = new double[]{115.7426282, -32.5366794, 0};

    protected Ephemeris ephemeris;

    protected Calendar day0;
    protected Calendar day1;
    protected Calendar day2;
    protected Calendar day3;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();

        Context appContext = getContext().getApplicationContext();
        new CopyAssetFiles(".*\\.se1", "ephe", appContext).copy();
        new CopyAssetFiles("zone\\.tab", "", appContext).copy();

        ephemeris = new Ephemeris(appContext.getFilesDir() + File.separator + "ephe",
                appContext);

        day0 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        day0.set(Calendar.YEAR, 2015);
        day0.set(Calendar.MONTH, Calendar.SEPTEMBER);
        day0.set(Calendar.DAY_OF_MONTH, 17);
        day0.set(Calendar.HOUR_OF_DAY, 23);
        day0.set(Calendar.MINUTE, 39);
        
        day1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        day1.set(Calendar.YEAR, 2015);
        day1.set(Calendar.MONTH, Calendar.SEPTEMBER);
        day1.set(Calendar.DAY_OF_MONTH, 18);
        day1.set(Calendar.HOUR_OF_DAY, 23);
        day1.set(Calendar.MINUTE, 39);

        day2 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        day2.set(Calendar.YEAR, 2015);
        day2.set(Calendar.MONTH, Calendar.SEPTEMBER);
        day2.set(Calendar.DAY_OF_MONTH, 19);
        day2.set(Calendar.HOUR_OF_DAY, 23);
        day2.set(Calendar.MINUTE, 39);

        day3 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        day3.set(Calendar.YEAR, 2015);
        day3.set(Calendar.MONTH, Calendar.SEPTEMBER);
        day3.set(Calendar.DAY_OF_MONTH, 21);
        day3.set(Calendar.HOUR_OF_DAY, 0);
        day3.set(Calendar.MINUTE, 39);
    }

    //TODO: come back to this file and add comments to show what all the julian times mean
    //TODO: come back to this file and annotate what the dayOffset means

    public void testGMT0() {
        ephemeris.setObserver(greenwich[0], greenwich[1], greenwich[2], "Africa/Accra");

        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(day0.getTime(), "Africa/Accra");

        assertEquals(2457282.743877315, test_ssi.getSunrise(), 1E-4);
        assertEquals(2457283.248611111, test_ssi.getSunset(), 1E-4);
        assertEquals(2457283.7436226853, test_ssi.getNextSunrise(), 1E-4);
        assertEquals(4, test_ssi.getDayOffset());
    }

    public void testGMT1() {
        ephemeris.setObserver(greenwich[0], greenwich[1], greenwich[2], "Africa/Accra");

        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(day1.getTime(), "Africa/Accra");

        assertEquals(2457283.7436226853, test_ssi.getSunrise(), 1E-4);
        assertEquals(2457284.2483680556, test_ssi.getSunset(), 1E-4);
        assertEquals(2457284.7433796297, test_ssi.getNextSunrise(), 1E-4);
        assertEquals(5, test_ssi.getDayOffset());
    }

    public void testGMT2() {
        ephemeris.setObserver(greenwich[0], greenwich[1], greenwich[2], "Africa/Accra");
        SweDate sd = EphemerisUtils.dateToSweDate(day2.getTime(), "Africa/Accra", 12);

        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(day2.getTime(), "Africa/Accra");

        assertEquals(2457284.7433796297, test_ssi.getSunrise(), 1E-4);
        assertEquals(2457285.248125, test_ssi.getSunset(), 1E-4);
        assertEquals(2457285.743136574, test_ssi.getNextSunrise(), 1E-4);
        assertEquals(6, test_ssi.getDayOffset());
    }

    public void testGMT3() {
        ephemeris.setObserver(greenwich[0], greenwich[1], greenwich[2], "Africa/Accra");
        SweDate sd = EphemerisUtils.dateToSweDate(day3.getTime(), "Africa/Accra", 12);

        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(day3.getTime(), "Africa/Accra");

        assertEquals(2457285.743136574, test_ssi.getSunrise(), 1E-4);
        assertEquals(2457286.2478703703, test_ssi.getSunset(), 1E-4);
        assertEquals(2457286.7428935184, test_ssi.getNextSunrise(), 1E-4);
        assertEquals(0, test_ssi.getDayOffset());
    }

    public void testPST0() {
        ephemeris.setObserver(seattle[0], seattle[1], seattle[2], "America/Vancouver");

        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(day0.getTime(), "America/Vancouver");

        assertEquals(2457283.075625, test_ssi.getSunrise(), 1E-4);
        assertEquals(2457283.5957060186, test_ssi.getSunset(), 1E-4);
        assertEquals(2457284.076550926, test_ssi.getNextSunrise(), 1E-4);
        assertEquals(4, test_ssi.getDayOffset());
    }

    public void testPST1() {
        ephemeris.setObserver(seattle[0], seattle[1], seattle[2], "America/Vancouver");

        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(day1.getTime(), "America/Vancouver");

        assertEquals(2457284.076550926, test_ssi.getSunrise(), 1E-4);
        assertEquals(2457284.5942824073, test_ssi.getSunset(), 1E-4);
        assertEquals(2457285.077488426, test_ssi.getNextSunrise(), 1E-4);
        assertEquals(5, test_ssi.getDayOffset());
    }

    public void testPST2() {
        ephemeris.setObserver(seattle[0], seattle[1], seattle[2], "America/Vancouver");

        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(day2.getTime(), "America/Vancouver");

        assertEquals(2457285.077488426, test_ssi.getSunrise(), 1E-4);
        assertEquals(2457285.5928587965, test_ssi.getSunset(), 1E-4);
        assertEquals(2457286.078425926, test_ssi.getNextSunrise(), 1E-4);
        assertEquals(6, test_ssi.getDayOffset());
    }

    public void testPST3() {
        ephemeris.setObserver(seattle[0], seattle[1], seattle[2], "America/Vancouver");

        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(day3.getTime(), "America/Vancouver");

        assertEquals(2457286.078425926, test_ssi.getSunrise(), 1E-4);
        assertEquals(2457286.591423611, test_ssi.getSunset(), 1E-4);
        assertEquals(2457287.0793634257, test_ssi.getNextSunrise(), 1E-4);
        assertEquals(0, test_ssi.getDayOffset());
    }

    public void testMandurah0() {
        ephemeris.setObserver(mandurah[0], mandurah[1], mandurah[2], "Asia/Ust-Nera");

        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(day0.getTime(), "Asia/Ust-Nera");

        assertEquals(2457283.425474537, test_ssi.getSunrise(), 1E-4);
        assertEquals(2457283.9240046297, test_ssi.getSunset(), 1E-4);
        assertEquals(2457284.424548611, test_ssi.getNextSunrise(), 1E-4);
        assertEquals(5, test_ssi.getDayOffset());
    }

    public void testMandurah1() {
        ephemeris.setObserver(mandurah[0], mandurah[1], mandurah[2], "Asia/Ust-Nera");
        
        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(day1.getTime(), "Asia/Ust-Nera");

        assertEquals(2457284.424548611, test_ssi.getSunrise(), 1E-4);
        assertEquals(2457284.9244444445, test_ssi.getSunset(), 1E-4);
        assertEquals(2457285.423611111, test_ssi.getNextSunrise(), 1E-4);
        assertEquals(6, test_ssi.getDayOffset());
    }

    public void testMandurah2() {
        ephemeris.setObserver(mandurah[0], mandurah[1], mandurah[2], "Asia/Ust-Nera");
        SweDate sd = EphemerisUtils.dateToSweDate(day2.getTime(), "Asia/Ust-Nera", 12);
        
        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(day2.getTime(), "Asia/Ust-Nera");

        assertEquals(2457285.423611111, test_ssi.getSunrise(), 1E-4);
        assertEquals(2457285.9248842592, test_ssi.getSunset(), 1E-4);
        assertEquals(2457286.422673611, test_ssi.getNextSunrise(), 1E-4);
        assertEquals(0, test_ssi.getDayOffset());
    }

    public void testMandurah3() {
        ephemeris.setObserver(mandurah[0], mandurah[1], mandurah[2], "Asia/Ust-Nera");

        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(day3.getTime(), "Asia/Ust-Nera");

        assertEquals(2457286.422673611, test_ssi.getSunrise(), 1E-4);
        assertEquals(2457286.925324074, test_ssi.getSunset(), 1E-4);
        assertEquals(2457287.4217476854, test_ssi.getNextSunrise(), 1E-4);
        assertEquals(1, test_ssi.getDayOffset());
    }

    public void testQuickCalcHour() {
        SweDate sd = EphemerisUtils.dateToSweDate(day3.getTime(), "America/Vancouver", 12);
        SunsetSunriseInfo ssi = new SunsetSunriseInfo(2457286.078425926, 2457286.591423611, 2457287.0793634257, "America/Vancouver", sd);

        assertEquals(ssi.calculatePlanetHourNum(ssi.getSunrise()-0.1), -2);
        assertEquals(ssi.calculatePlanetHourNum(ssi.getNextSunrise()+0.1), -1);

        double startTime = ssi.getSunrise();
        for (int i = 0; i < 48; i++) {
            assertEquals(ssi.calculatePlanetHourNum(startTime), i / 4);
            startTime += ssi.getDayHourLength() / 4;
        }
        startTime = ssi.getSunset();
        for (int i = 0; i < 48; i++) {
            assertEquals(ssi.calculatePlanetHourNum(startTime), i / 4 + 12);
            startTime += ssi.getNightHourLength() / 4;
        }
    }
}