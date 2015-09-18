package io.github.phora.aeondroid;

import swisseph.SweDate;

/**
 * Created by phora on 9/18/15.
 */
public class SunsetSunriseInfo {
    private final SweDate calcTime;
    private Double sunset;
    private Double sunrise;
    private Double nextSunrise;

    public SunsetSunriseInfo(Double sunrise, Double sunset, Double nextSunrise, SweDate calcTime) {
        this.sunset = sunset;
        this.sunrise = sunrise;
        this.nextSunrise = nextSunrise;
        this.calcTime = calcTime;
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
}
