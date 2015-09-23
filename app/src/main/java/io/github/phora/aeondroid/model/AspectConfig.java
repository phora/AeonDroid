package io.github.phora.aeondroid.model;

import android.util.SparseArray;

import io.github.phora.aeondroid.R;

/**
 * Created by phora on 9/22/15.
 */
public class AspectConfig {
    public final static int[] ASPECT_VALUES = {0, 30, 45,
            60, 72, 90, 120,
            135, 144, 150, 180};
    public final static int[] ASPECT_NAMES = {
            R.string.Aspects_Conjunction, R.string.Aspects_SemiSextile, R.string.Aspects_SemiSquare,
            R.string.Aspects_Sextile, R.string.Aspects_Quintile, R.string.Aspects_Square,
            R.string.Aspects_Trine, R.string.Aspects_Sesquisquare, R.string.Aspects_Biquintile,
            R.string.Aspects_Quincunx, R.string.Aspects_Opposition};
    public final static double[] DEFAULT_ORBS = {10, 3, 3,
            6, 2, 8, 8,
            3, 2, 3, 10};

    public final static boolean[] DEFAULT_VISIBILITY = {true, false, false,
            true, true, true, true,
            false, false, false, true};

    private boolean shown;
    private double orb;
    private int displayName = 0;

    public AspectConfig(boolean shown, double orb, int displayName) {
        this.shown = shown;
        if (orb < 0) {
            throw new IllegalArgumentException("Orbs can't be negative!");
        }
        else if (orb > 29) {
            throw new IllegalArgumentException("Orbs can't be greater than a sixth of a circle!");
        }
        this.orb = orb;
        this.displayName = displayName;
    }

    public AspectConfig(boolean shown, double orb) {
        this.shown = shown;
        if (orb < 0) {
            throw new IllegalArgumentException("Orbs can't be negative!");
        }
        else if (orb > 29) {
            throw new IllegalArgumentException("Orbs can't be greater than a sixth of a circle!");
        }
        this.orb = orb;
    }

    public static int getClosestAspect(double aspect, SparseArray<AspectConfig> orbConfigs, boolean returnPos) {
        int low = 0;
        int high = orbConfigs.size() - 1;
        if (high < 0) {
            throw new IllegalArgumentException("The array can't be empty!");
        }
        while (low < high) {
            int mid = (high + low) >> 1;
            // division by two and right bit shift by one do the same thing, however
            // bit shifting is more efficient since that requires less instructions
            assert(mid < high);

            double keyAt1 = orbConfigs.keyAt(mid);
            double keyAt2 = orbConfigs.keyAt(mid+1);

            double d1 = Math.abs(keyAt1 - aspect);
            double d2 = Math.abs(keyAt2 - aspect);
            if (d2 < d1)
            {
                low = mid+1;
            }
            else if (d2 == d1) {
                //use the orbs as a tiebreaker
                AspectConfig aspectConfig1 = orbConfigs.valueAt(mid);
                AspectConfig aspectConfig2 = orbConfigs.valueAt(mid + 1);

                double floor1 = Math.abs(aspect - (keyAt1 - aspectConfig1.getOrb()));
                double floor2 = Math.abs(aspect - (keyAt2 - aspectConfig2.getOrb()));

                double ceil1 = Math.abs(aspect - (keyAt1 + aspectConfig1.getOrb()));
                double ceil2 = Math.abs(aspect - (keyAt2 + aspectConfig2.getOrb()));

                double minDist = Math.min(Math.min(Math.min(floor1, floor2), ceil1), ceil2);

                if (minDist == ceil1 || minDist == floor1 ||  d1 <= aspectConfig1.getOrb()) {
                    return (int)keyAt1;
                }
                else if (minDist == ceil2 || minDist == floor2 ||  d2 <= aspectConfig2.getOrb()) {
                    return (int)keyAt2;
                }
                else {
                    low = mid+1;
                }
            }
            else
            {
                high = mid;
            }
        }
        if (returnPos) {
            return high;
        }
        else {
            return orbConfigs.keyAt(high);
        }
    }

    public boolean isShown() {
        return shown;
    }

    public void setShown(boolean shown) {
        this.shown = shown;
    }

    public double getOrb() {
        return orb;
    }

    public void setOrb(double orb) {
        if (orb < 0) {
            throw new IllegalArgumentException("Orbs can't be negative!");
        }
        else if (orb > 29) {
            throw new IllegalArgumentException("Orbs can't be greater than a sixth of a circle!");
        }
        this.orb = orb;
    }

    public int getDisplayName() {
        return displayName;
    }
}
