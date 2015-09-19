package io.github.phora.aeondroid;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;

import swisseph.SweDate;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class SunsetSunriseTest extends ApplicationTestCase<Application> {
    public SunsetSunriseTest() {
        super(Application.class);
    }

    protected double[] greenwich = new double[]{0, 0, 0};
    protected double[] seattle = new double[]{-122.3320700, 47.6062100, 56};

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
        ephemeris = new Ephemeris(appContext.getFilesDir() + File.separator + "ephe",
                appContext.getFilesDir() + File.separator + "zone.tab",
                greenwich);

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
        day3.set(Calendar.DAY_OF_MONTH, 20);
        day3.set(Calendar.HOUR_OF_DAY, 23);
        day3.set(Calendar.MINUTE, 39);
    }

    public void testGMT0() {
        ephemeris.setObserver(greenwich[0], greenwich[1], greenwich[2]);
        SweDate sd = EphemerisUtils.dateToSweDate(day0.getTime(), ephemeris.getTimezone(), 12);

        SunsetSunriseInfo ssi = new SunsetSunriseInfo(2457282.743877315, 2457283.248611111, 2457283.7436226853, sd);
        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(sd);

        assertEquals(ssi.getSunrise(), test_ssi.getSunrise(), 1E-4);
        assertEquals(ssi.getSunset(), test_ssi.getSunset(), 1E-4);
        assertEquals(ssi.getNextSunrise(), test_ssi.getNextSunrise(), 1E-4);
    }

    public void testGMT1() {
        ephemeris.setObserver(greenwich[0], greenwich[1], greenwich[2]);
        SweDate sd = EphemerisUtils.dateToSweDate(day1.getTime(), ephemeris.getTimezone(), 12);

        SunsetSunriseInfo ssi = new SunsetSunriseInfo(2457283.7436226853, 2457284.2483680556, 2457284.7433796297, sd);
        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(sd);

        assertEquals(ssi.getSunrise(), test_ssi.getSunrise(), 1E-4);
        assertEquals(ssi.getSunset(), test_ssi.getSunset(), 1E-4);
        assertEquals(ssi.getNextSunrise(), test_ssi.getNextSunrise(), 1E-4);
    }

    public void testGMT2() {
        ephemeris.setObserver(greenwich[0], greenwich[1], greenwich[2]);
        SweDate sd = EphemerisUtils.dateToSweDate(day2.getTime(), ephemeris.getTimezone(), 12);

        SunsetSunriseInfo ssi = new SunsetSunriseInfo(2457284.7433796297, 2457285.248125, 2457285.743136574, sd);
        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(sd);

        assertEquals(ssi.getSunrise(), test_ssi.getSunrise(), 1E-4);
        assertEquals(ssi.getSunset(), test_ssi.getSunset(), 1E-4);
        assertEquals(ssi.getNextSunrise(), test_ssi.getNextSunrise(), 1E-4);
    }

    public void testGMT3() {
        ephemeris.setObserver(greenwich[0], greenwich[1], greenwich[2]);
        SweDate sd = EphemerisUtils.dateToSweDate(day3.getTime(), ephemeris.getTimezone(), 12);

        SunsetSunriseInfo ssi = new SunsetSunriseInfo(2457285.743136574, 2457286.2478703703, 2457286.7428935184, sd);
        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(sd);

        assertEquals(ssi.getSunrise(), test_ssi.getSunrise(), 1E-4);
        assertEquals(ssi.getSunset(), test_ssi.getSunset(), 1E-4);
        assertEquals(ssi.getNextSunrise(), test_ssi.getNextSunrise(), 1E-4);
    }

    public void testPST0() {
        ephemeris.setObserver(seattle[0], seattle[1], seattle[2]);
        SweDate sd = EphemerisUtils.dateToSweDate(day0.getTime(), ephemeris.getTimezone(), 12);

        SunsetSunriseInfo ssi = new SunsetSunriseInfo(2457283.075625, 2457283.5957060186, 2457284.076550926, sd);
        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(sd);

        assertEquals(ssi.getSunrise(), test_ssi.getSunrise(), 1E-4);
        assertEquals(ssi.getSunset(), test_ssi.getSunset(), 1E-4);
        assertEquals(ssi.getNextSunrise(), test_ssi.getNextSunrise(), 1E-4);
    }

    public void testPST1() {
        ephemeris.setObserver(seattle[0], seattle[1], seattle[2]);
        SweDate sd = EphemerisUtils.dateToSweDate(day1.getTime(), ephemeris.getTimezone(), 12);

        SunsetSunriseInfo ssi = new SunsetSunriseInfo(2457284.076550926, 2457284.5942824073, 2457285.077488426, sd);
        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(sd);

        assertEquals(ssi.getSunrise(), test_ssi.getSunrise(), 1E-4);
        assertEquals(ssi.getSunset(), test_ssi.getSunset(), 1E-4);
        assertEquals(ssi.getNextSunrise(), test_ssi.getNextSunrise(), 1E-4);
    }

    public void testPST2() {
        ephemeris.setObserver(seattle[0], seattle[1], seattle[2]);
        SweDate sd = EphemerisUtils.dateToSweDate(day2.getTime(), ephemeris.getTimezone(), 12);

        SunsetSunriseInfo ssi = new SunsetSunriseInfo(2457285.077488426, 2457285.5928587965, 2457286.078425926, sd);
        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(sd);

        assertEquals(ssi.getSunrise(), test_ssi.getSunrise(), 1E-4);
        assertEquals(ssi.getSunset(), test_ssi.getSunset(), 1E-4);
        assertEquals(ssi.getNextSunrise(), test_ssi.getNextSunrise(), 1E-4);
    }

    public void testPST3() {
        ephemeris.setObserver(seattle[0], seattle[1], seattle[2]);
        SweDate sd = EphemerisUtils.dateToSweDate(day3.getTime(), ephemeris.getTimezone(), 12);

        SunsetSunriseInfo ssi = new SunsetSunriseInfo(2457286.078425926, 2457286.591423611, 2457287.0793634257, sd);
        SunsetSunriseInfo test_ssi = ephemeris.getSunriseandSunset(sd);

        assertEquals(ssi.getSunrise(), test_ssi.getSunrise(), 1E-4);
        assertEquals(ssi.getSunset(), test_ssi.getSunset(), 1E-4);
        assertEquals(ssi.getNextSunrise(), test_ssi.getNextSunrise(), 1E-4);
    }
}