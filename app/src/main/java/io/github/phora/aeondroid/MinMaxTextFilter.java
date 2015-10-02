package io.github.phora.aeondroid;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by phora on 10/1/15.
 */
public class MinMaxTextFilter implements InputFilter {
    private double min;
    private double max;
    private boolean includeCeiling;

    public MinMaxTextFilter(double min, double max, boolean includeCeiling) {
        if (min >= max) {
            throw new IllegalArgumentException("The min can't be greater than the max!");
        }
        this.min = min;
        this.max = max;
        this.includeCeiling = includeCeiling;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dStart, int dEnd) {
        try {
            String newVal = dest.toString().substring(0, dStart) + dest.toString().substring(dEnd, dest.toString().length());
            // Add the new string in
            newVal = newVal.substring(0, dStart) + source.toString() + newVal.substring(dStart, newVal.length());

            double input = Double.valueOf(newVal);
            if (isInRange(input)) {
                return null;
            }
        }
        catch (NumberFormatException e) {

        }
        return "";
    }

    private boolean isInRange(double value) {
        if (includeCeiling) {
            return min <= value && value <= max;
        }
        else {
            return min <= value && value < max;
        }
    }
}
