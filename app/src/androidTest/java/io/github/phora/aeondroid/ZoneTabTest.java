package io.github.phora.aeondroid;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;

import java.io.File;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import swisseph.SweDate;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ZoneTabTest extends ApplicationTestCase<Application> {
    public ZoneTabTest() {
        super(Application.class);
    }

    protected double[] greenwich = new double[]{0, 0, 0};
    protected double[] seattle = new double[]{-122.3320700, 47.6062100, 56};

    protected ZoneTab zoneTab;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();

        Context appContext = getContext().getApplicationContext();
        new CopyAssetFiles("zone\\.tab", "", appContext).copy();
        zoneTab = new ZoneTab(appContext.getFilesDir() + File.separator + "zone.tab");
    }

    public void testLatLong() {
        double[] expected = new double[]{-12.783333333333333, 45.233333333333334};
        double[] answer = zoneTab.latlong("-1247+04514");

        assertEquals(expected[0], answer[0], 1E-5);
        assertEquals(expected[1], answer[1], 1E-5);

        expected = new double[]{-69.00611111111111, 39.590000000000003};
        answer = zoneTab.latlong("-690022+0393524");
     }

    public void testNearestTZ() {
        String expected_name = "America/Indiana/Vincennes";
        String expected_name2 = "America/Chicago";

        ZoneTab.ZoneInfo zi = zoneTab.nearest_tz(39.2975, -94.7139);
        assertEquals(expected_name, zi.getTz());

        zi = zoneTab.nearest_tz(39.2975, -94.7139, "Indiana");
        assertEquals(expected_name2, zi.getTz());
    }
}