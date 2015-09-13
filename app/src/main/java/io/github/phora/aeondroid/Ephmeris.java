package io.github.phora.aeondroid;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.github.phora.aeondroid.model.PlanetaryHour;
import io.github.phora.aeondroid.model.PlanetaryHoursAdapter;
import swisseph.DblObj;
import swisseph.SweConst;
import swisseph.SweDate;
import swisseph.SwissEph;

/**
 * Created by phora on 9/8/15.
 */
public class Ephmeris {
    SwissEph sw;

    private double[] observer;

    public Ephmeris(String search_path) {
        sw = new SwissEph(search_path);
    }

    public Ephmeris(String search_path, double longitude, double latitude, double height) {
        sw = new SwissEph(search_path);
        this.observer = new double[]{longitude, latitude, height};
    }

    public Ephmeris(String search_path, double[] observer) {
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
            cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR)-2);
            next_sunrise = sunrise.doubleValue();
            sunrise = getBodyRise(cal.getTime(), SweConst.SE_SUN);
            sunset = getBodyRise(cal.getTime(), SweConst.SE_SUN);
        }
        else {
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
        Log.d("Ephmeris", PlanetaryHoursAdapter.DATE_FMT.format(date));
        Log.d("Ephmeris", PlanetaryHoursAdapter.DATE_FMT.format(cal.getTime()));

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
}
