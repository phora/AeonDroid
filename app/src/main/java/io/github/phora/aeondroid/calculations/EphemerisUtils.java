package io.github.phora.aeondroid.calculations;

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import io.github.phora.aeondroid.R;
import swisseph.SweDate;

/**
 * Created by phora on 9/13/15.
 */
public class EphemerisUtils {
    public final static double LUNAR_MONTH_DAYS = 29.53058868;
    public final static double LAST_NM = 2415021.077777778;
    public final static double SOLAR_YEAR_DAYS = 365.2421934027778;

    public final static DateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String degreesToSignString(Context context, double degrees) {
        String fmt = "%1$s %2$d*%3$d\"%4$d";

        int sign         = (int)(degrees / 30);
        int subdegrees   = (int)(degrees % 30);
        double minutes   = (degrees-(int)degrees)*60;
        int minutesAsInt = (int)minutes;
        int seconds      = (int)((minutes - (int)minutes)*60);

        String[] signs = context.getResources().getStringArray(R.array.SignNames);

        return String.format(fmt, signs[sign], subdegrees, minutesAsInt, seconds);
    }

    public static SweDate dateToSweDate(Date d) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(d.getTime());
        //cal.setTimeZone();

        double hourDouble = (double)cal.get(Calendar.HOUR_OF_DAY) +
                ((double)cal.get(Calendar.MINUTE)/60.) +
                ((double)cal.get(Calendar.SECOND)/3600.) +
                ((double)cal.get(Calendar.MILLISECOND)/3600000.);

        SweDate sd = new SweDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1,
                cal.get(Calendar.DAY_OF_MONTH), hourDouble, SweDate.SE_GREG_CAL);
        return sd;
    }

    public static double dateToMoonCycles(Date forecastDate) {
        return dateToMoonCycles(forecastDate, LAST_NM);
    }

    public static double dateToMoonCycles(Date forecastDate, double originDate) {
        double daysSinceLastCycle = dateToSweDate(forecastDate).getJulDay() - originDate;
        double cycleWithPart = daysSinceLastCycle / LUNAR_MONTH_DAYS;
        return cycleWithPart;
    }

    public static double moonCyclesToJulian(double cycles) {
        return moonCyclesToJulian(cycles, LAST_NM);
    }

    public static double moonCyclesToJulian(double cycles, double originDate) {
        double day = cycles*LUNAR_MONTH_DAYS+originDate;
        return day;
    }

    public static double angleSubtract(double target, double source) {
        double diff = (target - source);
        if (diff > 180.) {
            diff -= 360.;
        }
        else if (diff < -180.) {
            diff += 360.;
        }
        return diff;
    }

    public static SweDate dateToSweDate(Date date, String origTimezone, int replaceHour) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(origTimezone));
        cal.setTimeInMillis(date.getTime());
        cal.set(Calendar.HOUR_OF_DAY, replaceHour);

        Calendar greenwich = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        greenwich.setTimeInMillis(cal.getTimeInMillis());

        double hourDouble = (double)greenwich.get(Calendar.HOUR_OF_DAY) +
                ((double)greenwich.get(Calendar.MINUTE)/60.) +
                ((double)greenwich.get(Calendar.SECOND)/3600.) +
                ((double)greenwich.get(Calendar.MILLISECOND)/3600000.);

        SweDate sd = new SweDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1,
                cal.get(Calendar.DAY_OF_MONTH), hourDouble, SweDate.SE_GREG_CAL);
        return sd;
    }
}
