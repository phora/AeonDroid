package io.github.phora.aeondroid.calculations;

import android.content.Context;
import android.util.Log;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;

import io.github.phora.aeondroid.model.SunsetSunriseInfo;
import io.github.phora.aeondroid.model.VoidOfCourseInfo;
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
    private ZoneTab zt;
    private ZoneTab.ZoneInfo zi;
    private double[] observer;
    private Context context;

    public Ephemeris(String search_path, Context context) {
        this.context = context.getApplicationContext();
        sw = new SwissEph(search_path);
        try {
            zt = ZoneTab.getInstance(this.context);
        } catch (FileNotFoundException e) {
            zt = null;
        }
    }

    public String getTimezone() {
        return zi.getTz();
    }

    public Ephemeris(String searchPath, Context context, double longitude, double latitude, double height) {
        this.context = context.getApplicationContext();
        sw = new SwissEph(searchPath);
        try {
            zt = ZoneTab.getInstance(this.context);
        } catch (FileNotFoundException e) {
            zt = null;
        }
        this.observer = new double[]{longitude, latitude, height};
        if (zt != null) {
            zi = zt.nearestTZ(this.observer[1], this.observer[2]);
        }
    }

    public Ephemeris(String searchPath, Context context, double[] observer) {
        this.context = context;
        sw = new SwissEph(searchPath);
        try {
            zt = ZoneTab.getInstance(this.context);
        } catch (FileNotFoundException e) {
            zt = null;
        }

        if (observer != null && observer.length == 3) {
            this.observer = observer;
        }
        else {
            this.observer = new double[]{0, 0, 0};
        }
        if (zt != null) {
            zi = zt.nearestTZ(this.observer[1], this.observer[2]);
        }
    }

    public double[] getObserver() {
        return observer;
    }

    public void setObserver(double longitude, double latitude, double height) {
        this.observer = new double[]{longitude, latitude, height};
        zi = zt.nearestTZ(latitude, longitude);
    }

    public synchronized Double getBodyPos(double day, int celestial_body) {
        StringBuffer sb = new StringBuffer();

        double[] resArray = new double[6];

        int retCode = sw.swe_calc_ut(day, celestial_body,
                SweConst.SEFLG_SWIEPH,
                resArray, sb);

        if (retCode == SweConst.ERR) {
            Log.e("Ephemeris", sb.toString());
            return null;
        }
        else {
            return resArray[0];
        }
    }

    public synchronized Double getMoonSunDiff(double day) {
        StringBuffer sb = new StringBuffer();

        double[] resArray = new double[6];
        double[] resArray2 = new double[6];

        int retCode = sw.swe_calc_ut(day, SweConst.SE_MOON,
                SweConst.SEFLG_SWIEPH,
                resArray, sb);
        int retCode2 = sw.swe_calc_ut(day, SweConst.SE_SUN,
                SweConst.SEFLG_SWIEPH,
                resArray2, sb);

        if (retCode == SweConst.ERR) {
            Log.e("Ephemeris", sb.toString());
            return null;
        }
        if (retCode2 == SweConst.ERR) {
            Log.e("Ephemeris", sb.toString());
            return null;
        }

        return EphemerisUtils.angleSubtract(resArray2[0], resArray[0]);
    }

    public synchronized Date predictMoonPhase(double cycles, int offset, double targetAngle) {
        if (targetAngle < -180 || targetAngle > 180) {
            return null;
        }
        cycles = ((int)cycles)+offset;
        double diff = Double.POSITIVE_INFINITY;
        while (Math.abs(diff) >= 1E-3) {
            double angleDiff = getMoonSunDiff(EphemerisUtils.moonCyclesToJulian(cycles));
            angleDiff = EphemerisUtils.angleSubtract(angleDiff, targetAngle);
            cycles += angleDiff / 360.;
            diff = angleDiff;
        }
        return SweDate.getDate(EphemerisUtils.moonCyclesToJulian(cycles));
    }

    public synchronized Date predictMoonPhase(Date date, int offset, double targetAngle) {
        double cyclesWithExcess = EphemerisUtils.dateToMoonCycles(date);

        return predictMoonPhase(cyclesWithExcess, offset, targetAngle);
    }

    public synchronized Double getBodyRise(Double date, int celestialBody) {
        //Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        //cal.setTime(date);

        StringBuffer sb = new StringBuffer();
        DblObj dblobj = new DblObj();

        //SweDate sd = new SweDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH), 12, SweDate.SE_GREG_CAL);
        //detect temperature?
        //detect pressure?

        int retcode = sw.swe_rise_trans(date, celestialBody, null,
                SweConst.SEFLG_SWIEPH, //ephmeris to use
                SweConst.SE_CALC_RISE, //we want rise
                this.observer, 0, 0, //geographic info. also, detect pressure and temperature?
                dblobj, sb); //return value and buffer for errors

        if (retcode != SweConst.OK) {
            Log.e("Ephemeris", sb.toString());
            return null;
        }
        else
            return dblobj.val;
    }

    public synchronized Double getBodySet(Double date, int celestialBody) {
        //Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        //cal.setTime(date);

        StringBuffer sb = new StringBuffer();
        DblObj dblobj = new DblObj();

        //SweDate sd = new SweDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH), 12, SweDate.SE_GREG_CAL);
        //detect temperature?
        //detect pressure?
        int retcode = sw.swe_rise_trans(date, celestialBody, null,
                SweConst.SEFLG_SWIEPH, //ephmeris to use
                SweConst.SE_CALC_SET, //we want rise
                this.observer, 0, 0, //geographic info. also, detect pressure and temperature?
                dblobj, sb); //return value and buffer for errors

        if (retcode != SweConst.OK) {
            Log.e("Ephemeris", sb.toString());
            return null;
        }
        else
            return dblobj.val;
    }

    public synchronized SunsetSunriseInfo getSunriseandSunset(Date date, String timezone) {
        SweDate sd = EphemerisUtils.dateToSweDate(date, timezone, 12);
        SunsetSunriseInfo ssi = getSunriseandSunset(sd);

        double julified = EphemerisUtils.dateToSweDate(date).getJulDay();
        double calcTime = ssi.getCalcTime().getJulDay();

        Log.d("Ephemeris", ssi.getSunrise()+" < "+julified+" < "+ssi.getNextSunrise());

        if (julified <= ssi.getSunrise()) {
            Log.d("Ephemeris", "Date before sunrise, getting yesterday");
            sd = new SweDate(calcTime-1, SweDate.SE_GREG_CAL);
            ssi = getSunriseandSunset(sd);
        }
        else if (julified >= ssi.getNextSunrise()) {
            Log.d("Ephemeris", "Date after next sunrise, getting tomorrow");
            sd = new SweDate(calcTime+1, SweDate.SE_GREG_CAL);
            ssi = getSunriseandSunset(sd);
        }

        return ssi;
    }

    public synchronized SunsetSunriseInfo getSunriseandSunset(SweDate sd) {
        Double sunrise = getBodyRise(sd.getJulDay() - 1, SweConst.SE_SUN);
        Double sunset       = getBodySet(sd.getJulDay(), SweConst.SE_SUN);
        Double nextSunrise = getBodyRise(sd.getJulDay(), SweConst.SE_SUN);

        return new SunsetSunriseInfo(sunrise, sunset, nextSunrise, sd);
    }

    public synchronized ArrayList<PlanetaryHour> getPlanetaryHours(SunsetSunriseInfo ssi) {
        Double sunrise, sunset, nextSunrise;

        sunrise = ssi.getSunrise();
        sunset = ssi.getSunset();
        nextSunrise = ssi.getNextSunrise();

        double dayLength = sunset - sunrise;
        double nightLength = nextSunrise - sunset;

        double dayHourLength = dayLength/12.;
        double nightHourLength = nightLength/12.;

        Log.d("Ephmeris", String.format("Hours details: %.5f %.5f %.5f", sunrise, sunset, nextSunrise));

        ArrayList<PlanetaryHour> hours = new ArrayList<PlanetaryHour>();
        int dayOffset = SweDate.getDayOfWeekNr(sunrise);
        Log.d("Ephmeris", "Day offset: " + dayOffset);
        //Log.d("Ephmeris", EphemerisUtils.DATETIME_FMT.format(date));
        //Log.d("Ephmeris", EphemerisUtils.DATETIME_FMT.format(cal.getTime()));

        for (int i=0;i<12;i++) {
            double timestamp = ((double)i)*dayHourLength+sunrise;
            PlanetaryHour.HourClass hclass;
            if (i == 0)
                hclass = PlanetaryHour.HourClass.SUNRISE;
            else
                hclass = PlanetaryHour.HourClass.NORMAL;
            hours.add(new PlanetaryHour(false, hclass,
                    (i+PlanetaryHour.WDAYS_TO_POFFSETS[dayOffset])%7,
                    timestamp, dayHourLength));
        }
        for (int i=0;i<12;i++) {
            double timestamp = ((double)i)*nightHourLength+sunset;
            PlanetaryHour.HourClass hclass;
            if (i == 0)
                hclass = PlanetaryHour.HourClass.SUNSET;
            else
                hclass = PlanetaryHour.HourClass.NORMAL;
            hours.add(new PlanetaryHour(true, hclass,
                    (i+12+PlanetaryHour.WDAYS_TO_POFFSETS[dayOffset])%7,
                    timestamp, nightHourLength));
        }

        return hours;
    }

    public synchronized ArrayList<PlanetaryHour> getPlanetaryHours(Date date) {
        SunsetSunriseInfo ssi = getSunriseandSunset(date, zi.getTz());

        return getPlanetaryHours(ssi);
    }

    public synchronized MoonPhase makeMoonPhaseForDate(Date date) {
        return makeMoonPhaseForDate(date, predictMoonPhase(date, 0, 180));
    }

    public synchronized MoonPhase makeMoonPhaseForDate(Date date, Date fullMoon) {
        StringBuffer sb = new StringBuffer();
        double[] resarray = new double[20];
        double julday = EphemerisUtils.dateToSweDate(date).getJulDay();
        //crashes here?
        int retcode = sw.swe_pheno_ut(julday, SweConst.SE_MOON,
                SweConst.SEFLG_SWIEPH,
                resarray,
                sb);
        if (retcode == SweConst.OK) {
            boolean waxing = date.compareTo(fullMoon) <= 0;
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

    public synchronized VoidOfCourseInfo predictVoidOfCourse(Date date) {
        double cycles = EphemerisUtils.dateToMoonCycles(date);
        double moonPos = getBodyPos(EphemerisUtils.dateToSweDate(date).getJulDay(), SweConst.SE_MOON);

        double closestSign = ((int)(moonPos/30) + 1)*30;
        double lowerSign = ((int)(moonPos/30))*30;

        double lowerCycles = cycles+(EphemerisUtils.angleSubtract(lowerSign, moonPos))/360;
        cycles += ((closestSign - moonPos) / 360);

        double diff = Double.POSITIVE_INFINITY;

        while (Math.abs(diff) >= 1E-8) {
            double closestSignEstimate = getBodyPos(EphemerisUtils.moonCyclesToJulian(cycles), SweConst.SE_MOON);
            diff = EphemerisUtils.angleSubtract(closestSign, closestSignEstimate);
            cycles += (diff / 360);
        }

        diff = Double.POSITIVE_INFINITY;
        while (Math.abs(diff) >= 1E-8) {
            double closestSignEstimate = getBodyPos(EphemerisUtils.moonCyclesToJulian(lowerCycles), SweConst.SE_MOON);
            diff = EphemerisUtils.angleSubtract(lowerSign, closestSignEstimate);
            lowerCycles += (diff / 360);
        }

        double vocCycles = cycles;
        boolean hasAspects = false;
        int[] otherPlanets = new int[]{
                SweConst.SE_SUN, SweConst.SE_MERCURY, SweConst.SE_VENUS,
                SweConst.SE_MARS, SweConst.SE_JUPITER, SweConst.SE_SATURN,
                SweConst.SE_URANUS, SweConst.SE_NEPTUNE, SweConst.SE_PLUTO
        };
        double[] aspectsToCheck = new double[]{0, 60, 90, 120, 180};

        while (!hasAspects && (vocCycles > lowerCycles)) {
            vocCycles -= (1. / 36000);
            double julified = EphemerisUtils.moonCyclesToJulian(vocCycles);
            moonPos = getBodyPos(julified, SweConst.SE_MOON);

            for (int planet: otherPlanets) {
                double otherAngle = getBodyPos(julified, planet);
                for (double angle: aspectsToCheck) {
                    double angleGap = Math.abs(EphemerisUtils.angleSubtract(moonPos, otherAngle));
                    if (Math.abs(EphemerisUtils.angleSubtract(angle, angleGap)) <= 5E-3) {
                        hasAspects = true;
                        break;
                    }
                }
            }
        }

        if (hasAspects) {
            Date startDate = SweDate.getDate(EphemerisUtils.moonCyclesToJulian(vocCycles));
            Date endDate = SweDate.getDate(EphemerisUtils.moonCyclesToJulian(cycles));
            return new VoidOfCourseInfo(startDate, (int) (lowerSign / 30), endDate, (int) (closestSign / 30));
        }
        else {
            return null;
        }
    }

    public synchronized ArrayList<MoonPhase> getMoonCycle(Date date) {
        double moonCycles = EphemerisUtils.dateToMoonCycles(date);

        Date newMoonStart = predictMoonPhase(moonCycles, 0, 0);
        Date waxingCrescent = predictMoonPhase(moonCycles, 0, -45);
        Date firstQuarter = predictMoonPhase(moonCycles, 0, -90);
        Date waxingGibbous = predictMoonPhase(moonCycles, 0, -135);
        Date fullMoon = predictMoonPhase(moonCycles, 0, 180);
        Date waningGibbous = predictMoonPhase(moonCycles, 1, 135);
        Date lastQuarter = predictMoonPhase(moonCycles, 1, 90);
        Date waningCrescent = predictMoonPhase(moonCycles, 1, 45);
        Date newMoonEnd = predictMoonPhase(moonCycles, 1, 0);

        Date[] tmp = new Date[]{newMoonStart, waxingCrescent, firstQuarter,
                waxingGibbous, fullMoon, waningGibbous,
                lastQuarter, waningCrescent, newMoonEnd};

        ArrayList<MoonPhase> output = new ArrayList<>();

        for (Date d: tmp) {
            MoonPhase mp = makeMoonPhaseForDate(d, fullMoon);
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
