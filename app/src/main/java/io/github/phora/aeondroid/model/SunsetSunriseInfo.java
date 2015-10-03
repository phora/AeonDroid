package io.github.phora.aeondroid.model;

import java.util.Date;

import io.github.phora.aeondroid.calculations.EphemerisUtils;
import swisseph.SweDate;

/**
 * Created by phora on 9/18/15.
 */
public class SunsetSunriseInfo {
    private final SweDate calcTime;
    private final double dayHourLength;
    private final double nightHourLength;
    private Double sunset;
    private Double sunrise;
    private Double nextSunrise;
    private int dayOffset;

    public SunsetSunriseInfo(Double sunrise, Double sunset, Double nextSunrise, SweDate calcTime) {
        this.sunset = sunset;
        this.sunrise = sunrise;
        this.nextSunrise = nextSunrise;
        this.calcTime = calcTime;
        this.dayOffset = SweDate.getDayOfWeekNr(sunrise);

        this.dayHourLength = (sunset - sunrise)/12;
        this.nightHourLength = (nextSunrise - sunset)/12;
    }

    public int getDayOffset() {
        return dayOffset;
    }

    public Double getSunset() {
        return sunset;
    }

    public Double getSunrise() {
        return sunrise;
    }

    public Double getNextSunrise() {
        return nextSunrise;
    }

    public SweDate getCalcTime() {
        return calcTime;
    }

    public double getDayHourLength() {
        return dayHourLength;
    }

    public double getNightHourLength() {
        return nightHourLength;
    }

    public int calculatePlanetHourNum(Date d) {
        return calculatePlanetHourNum(EphemerisUtils.dateToSweDate(d).getJulDay());
    }

    public int calculatePlanetHourNum(Double d) {
        if (sunrise <= d && d < sunset) {
            return (int)((d - sunrise)/dayHourLength);
        }
        else if (sunset <= d && d < nextSunrise) {
            return (int)((d - sunset)/nightHourLength)+12;
        }
        else if (nextSunrise <= d) {
            return -1;
        }
        else {
            return -2;
        }
    }

    public PlanetaryHour calculatePlanetHour(Date d) {
        return calculatePlanetHour(EphemerisUtils.dateToSweDate(d).getJulDay());
    }

    public PlanetaryHour calculatePlanetHour(Double d) {
        double timestamp;
        PlanetaryHour.HourClass hclass = PlanetaryHour.HourClass.NORMAL;
        int nHours;
        boolean isNight = false;

        if (sunrise <= d && d < sunset) {
            nHours = (int)((d - sunrise)/dayHourLength);
            timestamp = nHours*dayHourLength+sunrise;
            if (nHours == 0) {
                hclass = PlanetaryHour.HourClass.SUNRISE;
            }
        }
        else if (sunset <= d && d < nextSunrise) {
            nHours = (int)((d - sunset)/nightHourLength);
            isNight = true;
            timestamp = (nHours*nightHourLength)+sunset;
            if (nHours == 0) {
                hclass = PlanetaryHour.HourClass.SUNSET;
            }
        }
        else {
            return null;
        }

        if (isNight) {
            return new PlanetaryHour(true, hclass,
                    (nHours+12+PlanetaryHour.WDAYS_TO_POFFSETS[dayOffset]) % 7,
                    timestamp, nightHourLength);
        }
        else {
            return new PlanetaryHour(false, hclass,
                    (nHours+PlanetaryHour.WDAYS_TO_POFFSETS[dayOffset]) % 7,
                    timestamp, dayHourLength);
        }
    }
}
