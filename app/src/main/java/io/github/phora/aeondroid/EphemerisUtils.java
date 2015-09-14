package io.github.phora.aeondroid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import swisseph.SweDate;

/**
 * Created by phora on 9/13/15.
 */
public class EphemerisUtils {
    public final static double LUNAR_MONTH_DAYS = 29.53058868;
    public final static double LAST_NM = 2415021.077777778;
    public final static double SOLAR_YEAR_DAYS = 365.2421934027778;
    public final static DateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static SweDate dateToSweDate(Date d) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));;
        cal.setTime(d);

        double hour_double = (double)cal.get(Calendar.HOUR_OF_DAY) +
                ((double)cal.get(Calendar.MINUTE)/60.) +
                ((double)cal.get(Calendar.SECOND)/3600.) +
                ((double)cal.get(Calendar.MILLISECOND)/3600000.);

        SweDate sd = new SweDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1,
                cal.get(Calendar.DAY_OF_MONTH), hour_double, SweDate.SE_GREG_CAL);
        return sd;
    }

    public static double dateToMoonCycles(Date forecast_date) {
        return dateToMoonCycles(forecast_date, LAST_NM);
    }

    public static double dateToMoonCycles(Date forecast_date, double origin_date) {
        double days_since_last_cycle = dateToSweDate(forecast_date).getJulDay() - origin_date;
        double cycle_with_part = days_since_last_cycle / LUNAR_MONTH_DAYS;
        return cycle_with_part;
    }

    public static double moonCyclesToJulian(double cycles) {
        return moonCyclesToJulian(cycles, LAST_NM);
    }

    public static double moonCyclesToJulian(double cycles, double origin_date) {
        double day = cycles*LUNAR_MONTH_DAYS+origin_date;
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
}
