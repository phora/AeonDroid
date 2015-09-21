package io.github.phora.aeondroid.model;

import java.util.Date;

/**
 * Created by phora on 9/18/15.
 */
public class VoidOfCourseInfo {
    private Date startDate;
    private int signFrom;
    private Date endDate;
    private int signTo;

    public VoidOfCourseInfo(Date startDate, int signFrom, Date endDate, int signTo) {
        this.startDate = startDate;
        this.signFrom = signFrom;
        this.endDate = endDate;
        this.signTo = signTo;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public int getSignFrom() {
        return signFrom;
    }

    public void setSignFrom(int signFrom) {
        this.signFrom = signFrom;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getSignTo() {
        return signTo;
    }

    public void setSignTo(int signTo) {
        this.signTo = signTo;
    }
}
