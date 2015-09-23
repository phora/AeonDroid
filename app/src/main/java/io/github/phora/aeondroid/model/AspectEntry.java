package io.github.phora.aeondroid.model;

/**
 * Created by phora on 9/21/15.
 */
public class AspectEntry {
    private int fromPlanetType;
    private int toPlanetType;

    private int fromChartId; // -1 and 0 are reserved for realtime chart and birth chart, respectively
    private int toChartId; // -1 and 0 are reserved for realtime chart and birth chart, respectively

    private double fromPlanetPos; // -1 denotes a header
    private double toPlanetPos;   // -1 denotes a header

    public AspectEntry(int fromPlanetType, int toPlanetType, int fromChartId, int toChartId, double fromPlanetPos, double toPlanetPos) {
        this.fromPlanetType = fromPlanetType;
        this.toPlanetType = toPlanetType;

        this.fromChartId = fromChartId;
        this.toChartId = toChartId;

        this.fromPlanetPos = fromPlanetPos;
        this.toPlanetPos = toPlanetPos;
    }

    public int getFromPlanetType() {
        return fromPlanetType;
    }

    public boolean isHeader() {
        return fromPlanetPos == -1 ||toPlanetPos == -1;
    }

    public int getToPlanetType() {
        return toPlanetType;
    }

    public double getFromPlanetPos() {
        return fromPlanetPos;
    }

    public double getToPlanetPos() {
        return toPlanetPos;
    }

    public double getAspectDist() {
        return Math.abs(fromPlanetPos-toPlanetPos);
    }
}