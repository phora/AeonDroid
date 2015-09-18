package io.github.phora.aeondroid;

import android.content.Context;

import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.model.MoonPhase;
import io.github.phora.aeondroid.model.MoonPhase.PhaseType;

/**
 * Created by ${CUSER} on 9/17/15.
 */
public class PhaseUtils {
    public static int phaseToInt(PhaseType pt) {
        switch(pt) {
            case NEW:
                return 0;
            case CRESCENT:
                return 1;
            case QUARTER:
                return 2;
            case GIBBOUS:
                return 3;
            case FULL:
                return 4;
            default:
                return -1;
        }
    }

    public static PhaseType phaseFromInt(int i) {
        switch(i) {
            case 0:
                return PhaseType.NEW;
            case 1:
                return PhaseType.CRESCENT;
            case 2:
                return PhaseType.QUARTER;
            case 3:
                return PhaseType.GIBBOUS;
            case 4:
                return PhaseType.FULL;
            default:
                return null;
        }
    }

    public static int getPhaseImage(MoonPhase mp) {
        if (mp.getPhaseType() == PhaseType.NEW) {
            return R.drawable.moon_new;
        }
        else if (mp.getPhaseType() == PhaseType.FULL) {
            return R.drawable.moon_full;
        }
        else if (mp.getPhaseType() == PhaseType.QUARTER) {
            if (mp.isWaxing()) {
                return R.drawable.moon_first_quarter;
            }
            else {
                return R.drawable.moon_last_quarter;
            }
        }
        else {
            if (mp.getPhaseType() == PhaseType.CRESCENT) {
                if (mp.isWaxing()) {
                    return R.drawable.moon_waxing_crescent;
                }
                else {
                    return R.drawable.moon_waning_crescent;
                }
            }
            else {
                if (mp.isWaxing()) {
                    return R.drawable.moon_waxing_gibbous;
                }
                else {
                    return R.drawable.moon_waning_gibbous;
                }
            }
        }
    }


    public static String getPhaseString(Context context, PhaseType pt, boolean waxing) {
        String fmt = context.getString(R.string.waxwanefmt);

        if (pt == PhaseType.NEW) {
            return context.getString(R.string.new_moon);
        }
        else if (pt == PhaseType.FULL) {
            return context.getString(R.string.full_moon);
        }
        else if (pt == PhaseType.QUARTER) {
            if (waxing) {
                return String.format(fmt, context.getString(R.string.first_quarter),
                        context.getString(R.string.quarter_sfx));
            }
            else {
                return String.format(fmt, context.getString(R.string.last_quarter),
                        context.getString(R.string.quarter_sfx));
            }
        }
        else {
            String part;
            if (pt == PhaseType.CRESCENT) {
                part = context.getString(R.string.crescent_moon);
            }
            else {
                part = context.getString(R.string.gibbous_moon);
            }

            if (waxing) {
                return String.format(fmt, context.getString(R.string.waxing_moon), part);
            }
            else {
                return String.format(fmt, context.getString(R.string.waning_moon), part);
            }
        }
    }

    public static String getPhaseString(Context context, MoonPhase mp) {
        String fmt = context.getString(R.string.waxwanefmt);

        if (mp.getPhaseType() == PhaseType.NEW) {
            return context.getString(R.string.new_moon);
        }
        else if (mp.getPhaseType() == PhaseType.FULL) {
            return context.getString(R.string.full_moon);
        }
        else if (mp.getPhaseType() == PhaseType.QUARTER) {
            if (mp.isWaxing()) {
                return String.format(fmt, context.getString(R.string.first_quarter),
                        context.getString(R.string.quarter_sfx));
            }
            else {
                return String.format(fmt, context.getString(R.string.last_quarter),
                        context.getString(R.string.quarter_sfx));
            }
        }
        else {
            String part;
            if (mp.getPhaseType() == PhaseType.CRESCENT) {
                part = context.getString(R.string.crescent_moon);
            }
            else {
                part = context.getString(R.string.gibbous_moon);
            }

            if (mp.isWaxing()) {
                return String.format(fmt, context.getString(R.string.waxing_moon), part);
            }
            else {
                return String.format(fmt, context.getString(R.string.waning_moon), part);
            }
        }
    }
}
