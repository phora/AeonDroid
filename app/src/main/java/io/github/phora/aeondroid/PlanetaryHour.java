package io.github.phora.aeondroid;

/**
 * Created by phora on 9/9/15.
 */
public class PlanetaryHour {
    public enum HourClass {
        SUNRISE,
        SUNSET,
        NORMAL
    }

    public static int[] WDAYS_TO_POFFSETS = new int[]{0, 3, 6, 2, 5, 1, 4};

    private boolean mIsNight;
    private HourClass mHourType;
    private int mPlanetType;
    private double mHourStamp;
    private double mHourLength;

    public PlanetaryHour(boolean isNight, HourClass hourType, int planetType,
                         double hourStamp, double hourLength) {
        this.mIsNight = isNight;
        this.mHourType = hourType;
        this.mPlanetType = planetType;
        this.mHourStamp = hourStamp;
        this.mHourLength = hourLength; //do we need this?
    }

    public boolean isNight() {
        return mIsNight;
    }

    public void setisNight(boolean mIsNight) {
        this.mIsNight = mIsNight;
    }

    public HourClass getHourType() {
        return mHourType;
    }

    public void setHourType(HourClass mHourType) {
        this.mHourType = mHourType;
    }

    public int getPlanetType() {
        return mPlanetType;
    }

    public void setPlanetType(int mPlanetType) {
        this.mPlanetType = mPlanetType;
    }

    public double getHourStamp() {
        return mHourStamp;
    }

    public void setHourStamp(double mHourStamp) {
        this.mHourStamp = mHourStamp;
    }

    public double getHourLength() {
        return mHourLength;
    }

    public void setHourLength(double mHourLength) {
        this.mHourLength = mHourLength;
    }
}
