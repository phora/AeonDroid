package io.github.phora.aeondroid.model;

import java.util.Date;

/**
 * Created by phora on 9/13/15.
 */
public class MoonPhase {
    private boolean mWaxing;
    private double  mIllumination;
    public enum PhaseType {
        NEW, CRESCENT, QUARTER, GIBBOUS, FULL
    };
    private Date mTimeStamp;
    private PhaseType mPhaseType;

    public MoonPhase(boolean waxing, double illumination, PhaseType phaseType, Date timeStamp) {
        this.mWaxing = waxing;
        this.mIllumination = illumination;
        this.mPhaseType = phaseType;
        this.mTimeStamp = timeStamp;
    }

    public boolean isWaxing() {
        return mWaxing;
    }

    public void setWaxing(boolean waxing) {
        this.mWaxing = waxing;
    }

    public double getIllumination() {
        return mIllumination;
    }

    public void setIllumination(double illumination) {
        this.mIllumination = illumination;
    }

    public PhaseType getPhaseType() {
        return mPhaseType;
    }

    public void setPhaseType(PhaseType phaseType) {
        this.mPhaseType = phaseType;
    }

    public Date getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.mTimeStamp = timeStamp;
    }
}
