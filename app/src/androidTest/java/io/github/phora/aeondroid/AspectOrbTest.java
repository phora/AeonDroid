package io.github.phora.aeondroid;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;
import android.util.SparseArray;

import io.github.phora.aeondroid.model.AspectConfig;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class AspectOrbTest extends ApplicationTestCase<Application> {
    public AspectOrbTest() {
        super(Application.class);
    }

    protected SparseArray<AspectConfig> orbsConfig;
    protected DBHelper dbHelper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();

        Context appContext = getContext().getApplicationContext();
        dbHelper = DBHelper.getInstance(appContext);
        orbsConfig = dbHelper.getOrbs();
        // this test assumes a stock configuration
    }

    public void testClosestOrb() {
        double feedValue = 122;
        int expected = 120;
        int answer = AspectConfig.getClosestAspect(feedValue, orbsConfig, false);

        assertEquals(expected, answer);
     }

    public void testClosestOrbWithTie() {
        // sextile wins because it has a larger orb
        double feedValue = 66;
        int expected = 60;
        int answer = AspectConfig.getClosestAspect(feedValue, orbsConfig, false);
        assertEquals(expected, answer);

        // conjunction wins because it has a larger orb
        feedValue = 15;
        expected = 0;
        answer = AspectConfig.getClosestAspect(feedValue, orbsConfig, false);
        assertEquals(expected, answer);

        // case where both trine and square have same orbs
        feedValue = 105;
        expected = 90;
        answer = AspectConfig.getClosestAspect(feedValue, orbsConfig, false);
        assertEquals(expected, answer);

        // opposition wins because it has a larger orb
        feedValue = 165;
        expected = 180;
        answer = AspectConfig.getClosestAspect(feedValue, orbsConfig, false);
        assertEquals(expected, answer);
    }
}