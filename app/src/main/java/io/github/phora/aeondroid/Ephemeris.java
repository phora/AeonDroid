package io.github.phora.aeondroid;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.github.phora.aeondroid.model.MoonPhase;
import io.github.phora.aeondroid.model.PlanetaryHour;
import swisseph.DblObj;
import swisseph.SweConst;
import swisseph.SweDate;
import swisseph.SwissEph;

/**
 * Created by phora on 9/8/15.
 */
public class Ephemeris {
    private SwissEph sw;
    private double[] observer;

    public Ephemeris(String search_path) {
        sw = new SwissEph(search_path);
    }

    public Ephemeris(String search_path, double longitude, double latitude, double height) {
        sw = new SwissEph(search_path);
        this.observer = new double[]{longitude, latitude, height};
    }

    public Ephemeris(String search_path, double[] observer) {
        sw = new SwissEph(search_path);
        if (observer.length == 3) {
            this.observer = observer;
        }
        else {
            this.observer = new double[]{0, 0, 0};
        }
    }

    public double[] getObserver() {
        return observer;
    }

    public void setObserver(double longitude, double latitude, double height) {
        this.observer = new double[]{longitude, latitude, height};
    }

    public Double getMoonSunDiff(double day) {
        StringBuffer sb = new StringBuffer();

        double[] resarray = new double[6];
        double[] resarray2 = new double[6];

        int retcode = sw.swe_calc_ut(day, SweConst.SE_MOON,
                SweConst.SEFLG_SWIEPH,
                resarray, sb);
        int retcode2 = sw.swe_calc_ut(day, SweConst.SE_SUN,
                SweConst.SEFLG_SWIEPH,
                resarray2, sb);

        if (retcode == SweConst.ERR)
            return null;
        if (retcode2 == SweConst.ERR)
            return null;

        return EphemerisUtils.angleSubtract(resarray2[0], resarray[0]);
    }

    public Date predictMoonPhase(double cycles, int offset, double target_angle) {
        if (target_angle < -180 || target_angle > 180) {
            return null;
        }
        cycles = ((int)cycles)+offset;
        double diff = Double.POSITIVE_INFINITY;
        while (Math.abs(diff) >= 1E-3) {
            double angle_diff = getMoonSunDiff(EphemerisUtils.moonCyclesToJulian(cycles));
            angle_diff = EphemerisUtils.angleSubtract(angle_diff, target_angle);
            cycles += angle_diff / 360.;
            diff = angle_diff;
        }
        return SweDate.getDate(EphemerisUtils.moonCyclesToJulian(cycles));
    }

    public Date predictMoonPhase(Date date, int offset, double target_angle) {
        double cycles_with_excess = EphemerisUtils.dateToMoonCycles(date);

        return predictMoonPhase(cycles_with_excess, offset, target_angle);
    }

    public Double getBodyRise(Date date, int celestial_body) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);

        StringBuffer sb = new StringBuffer();
        DblObj dblobj = new DblObj();

        SweDate sd = new SweDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH), 12, SweDate.SE_GREG_CAL);
        //detect temperature?
        //detect pressure?

        int retcode = sw.swe_rise_trans(sd.getJulDay(), celestial_body, null,
                SweConst.SEFLG_SWIEPH, //ephmeris to use
                SweConst.SE_CALC_RISE, //we want rise
                this.observer, 0, 0, //geographic info. also, detect pressure and temperature?
                dblobj, sb); //return value and buffer for errors

        if (retcode != SweConst.OK)
            return null;
        else
            return dblobj.val;
    }

    public Double getBodySet(Date date, int celestial_body) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);

        StringBuffer sb = new StringBuffer();
        DblObj dblobj = new DblObj();

        SweDate sd = new SweDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH), 12, SweDate.SE_GREG_CAL);
        //detect temperature?
        //detect pressure?
        int retcode = sw.swe_rise_trans(sd.getJulDay(), celestial_body, null,
                SweConst.SEFLG_SWIEPH, //ephmeris to use
                SweConst.SE_CALC_SET, //we want rise
                this.observer, 0, 0, //geographic info. also, detect pressure and temperature?
                dblobj, sb); //return value and buffer for errors

        if (retcode != SweConst.OK)
            return null;
        else
            return dblobj.val;
    }

    public ArrayList<PlanetaryHour> getPlanetaryHours(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR)+1);

        Double sunrise, sunset, next_sunrise;

        sunrise = getBodyRise(date, SweConst.SE_SUN);
        if (date.before(SweDate.getDate(sunrise))) {
            Log.d("Ephmeris", "Getting the hours for the day before, sun didn't rise yet");
            cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR)-2);
            next_sunrise = sunrise.doubleValue();
            sunrise = getBodyRise(cal.getTime(), SweConst.SE_SUN);
            sunset = getBodyRise(cal.getTime(), SweConst.SE_SUN);
        }
        else {
            Log.d("Ephmeris", "Getting the hours for the current day, sun rose");
            sunset = getBodySet(date, SweConst.SE_SUN);
            next_sunrise = getBodyRise(cal.getTime(), SweConst.SE_SUN);
        }

        double day_length = sunset - sunrise;
        double night_length = next_sunrise - sunset;

        double day_hour_length = day_length/12.;
        double night_hour_length = night_length/12.;

        ArrayList<PlanetaryHour> hours = new ArrayList<PlanetaryHour>();
        int day_offset = SweDate.getDayOfWeekNr(sunrise);
        Log.d("Ephmeris", "Day offset: " + day_offset);
        Log.d("Ephmeris", EphemerisUtils.DATE_FMT.format(date));
        Log.d("Ephmeris", EphemerisUtils.DATE_FMT.format(cal.getTime()));

        for (int i=0;i<12;i++) {
            double timestamp = ((double)i)*day_hour_length+sunrise;
            PlanetaryHour.HourClass hclass;
            if (i == 0)
                hclass = PlanetaryHour.HourClass.SUNRISE;
            else
                hclass = PlanetaryHour.HourClass.NORMAL;
            hours.add(new PlanetaryHour(false, hclass,
                    (i+PlanetaryHour.WDAYS_TO_POFFSETS[day_offset])%7,
                    timestamp, day_hour_length));
        }
        for (int i=0;i<12;i++) {
            double timestamp = ((double)i)*night_hour_length+sunset;
            PlanetaryHour.HourClass hclass;
            if (i == 0)
                hclass = PlanetaryHour.HourClass.SUNSET;
            else
                hclass = PlanetaryHour.HourClass.NORMAL;
            hours.add(new PlanetaryHour(true, hclass,
                    (i+12+PlanetaryHour.WDAYS_TO_POFFSETS[day_offset])%7,
                    timestamp, night_hour_length));
        }

        return hours;
    }

    public MoonPhase makeMoonPhaseForDate(Date date) {
        return makeMoonPhaseForDate(date, predictMoonPhase(date, 0, 180));
    }

    public MoonPhase makeMoonPhaseForDate(Date date, Date full_moon) {
        StringBuffer sb = new StringBuffer();
        double[] resarray = new double[20];
        double julday = EphemerisUtils.dateToSweDate(date).getJulDay();
        //crashes here?
        int retcode = sw.swe_pheno_ut(julday, SweConst.SE_MOON,
                SweConst.SEFLG_SWIEPH,
                resarray,
                sb);
        if (retcode == SweConst.OK) {
            boolean waxing = date.compareTo(full_moon) <= 0;
            double illumination = resarray[1]*100;
            double elongation = resarray[2];

            MoonPhase.PhaseType pt;

            if (elongation <= 5.) {
                pt = MoonPhase.PhaseType.NEW;
            }
            else if (elongation <= 88.) {
                pt = MoonPhase.PhaseType.CRESCENT;
            }
            else if (elongation <= 94.) {
                pt = MoonPhase.PhaseType.QUARTER;
            }
            else if (elongation <= 175.) {
                pt = MoonPhase.PhaseType.GIBBOUS;
            }
            else {
                pt = MoonPhase.PhaseType.FULL;
            }

            return new MoonPhase(waxing, illumination, pt, date);
        }
        else {
            return null;
        }
    }

    public ArrayList<MoonPhase> getMoonCycle(Date date) {
        double moon_cycles = EphemerisUtils.dateToMoonCycles(date);

        Date new_moon_start = predictMoonPhase(moon_cycles, 0, 0);
        Date waxing_crescent = predictMoonPhase(moon_cycles, 0, -45);
        Date first_quarter = predictMoonPhase(moon_cycles, 0, -90);
        Date waxing_gibbous = predictMoonPhase(moon_cycles, 0, -135);
        Date full_moon = predictMoonPhase(moon_cycles, 0, 180);
        Date waning_gibbous = predictMoonPhase(moon_cycles, 1, 135);
        Date last_quarter = predictMoonPhase(moon_cycles, 1, 90);
        Date waning_crescent = predictMoonPhase(moon_cycles, 1, 45);
        Date new_moon_end = predictMoonPhase(moon_cycles, 1, 0);

        Date[] tmp = new Date[]{new_moon_start, waxing_crescent, first_quarter,
                waxing_gibbous, full_moon, waning_gibbous,
                last_quarter, waning_crescent, new_moon_end};

        ArrayList<MoonPhase> output = new ArrayList<>();

        for (Date d: tmp) {
            MoonPhase mp = makeMoonPhaseForDate(d, full_moon);
            if (mp != null) {
                output.add(mp);
            }
            else {
                return null;
            }
        }

        return output;
    }
}
