package io.github.phora.aeondroid.calculations;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by phora on 9/18/15.
 */
public class ZoneTab {

    public class ZoneInfo {
        private String country;
        private double latitude;
        private double longitude;
        private String tz;

        public ZoneInfo(String country, String tz, double latitude, double longitude) {
            this.country = country;
            this.tz = tz;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getCountry() {
            return country;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getTz() {
            return tz;
        }
    }

    ArrayList<ZoneInfo> zones;

    private static Pattern latlongPattern = Pattern.compile("([^\\d])(\\d+)([^\\d])(\\d+)");
    private static ZoneTab sInstance = null;

    public static synchronized ZoneTab getInstance(Context context) throws FileNotFoundException {
        if (sInstance == null) {
            String fpath = context.getApplicationContext().getFilesDir() + File.separator + "zone.tab";
            sInstance = new ZoneTab(fpath);
        }
        return sInstance;
    }

    private ZoneTab(String filepath) throws FileNotFoundException {
        Scanner in = new Scanner(new FileReader(filepath));
        zones = new ArrayList<>();

        while (in.hasNext()) {
            String line = in.nextLine();
            if (line.startsWith("#")) {
                continue;
            }

            String[] values = line.split("[ \t]");
            if (values.length >= 3) {
                String country = values[0];
                String tz = values[2];
                double[] coords = latlong(values[1]);
                zones.add(new ZoneInfo(country, tz, coords[0], coords[1]));
            }
        }
    }

    public ZoneInfo nearestTZ(double lat, double lon, String... ignore) {
        ZoneInfo best = null;
        double m = Double.POSITIVE_INFINITY;

        for (ZoneInfo zi: zones) {
            String name = zi.getTz();
            boolean skip = false;
            for (String blEntry: ignore) {
                if (name.contains(blEntry)) {
                    skip = true;
                    break;
                }
            }
            if (skip) {
                continue;
            }
            double dist = distance(lat, lon, zi.getLatitude(), zi.getLongitude());
            if (dist < m) {
                m = dist;
                best = zi;
            }
        }

        return best;
    }

    public ZoneInfo nearestTZ(double lat, double lon) {
        ZoneInfo best = null;
        double m = Double.POSITIVE_INFINITY;

        for (ZoneInfo zi: zones) {
            double dist = distance(lat, lon, zi.getLatitude(), zi.getLongitude());
            if (dist < m) {
                m = dist;
                best = zi;
            }
        }

        return best;
    }

    public double distance(double lat_1, double long_1, double lat_2, double long_2) {
        lat_1 = lat_1 * Math.PI / 180.0;
        long_1 = long_1 * Math.PI / 180.0;
        lat_2 = lat_2 * Math.PI / 180.0;
        long_2 = long_2 * Math.PI / 180.0;

        Double dlong = long_2 - long_1;
        Double dlat = lat_2 - lat_1;

        double angle = Math.pow(Math.sin(dlat / 2), 2) +
                       Math.cos(lat_1) * Math.cos(lat_2)  *
                       Math.pow(Math.sin(dlong / 2), 2);

        return 2 * Math.asin(Math.min(1, Math.sqrt(angle)));
    }

    public double[] latlong(String coords) {
        Matcher m = latlongPattern.matcher(coords);
        if (m.matches()) {
            return new double[]{coordStrToVal(m.group(1), m.group(2)), coordStrToVal(m.group(3), m.group(4))};
        }
        else {
            return null;
        }
    }

    public double coordStrToVal(String sign, String digits) {
        int digitLen = digits.length();

        int dir;
        double d, m, s;

        if (digitLen == 4) {
            d = Double.valueOf(digits.substring(0, 2));
            m = Double.valueOf(digits.substring(2, 4));
            s = 0;
        }
        else if (digitLen == 5) {
            d = Double.valueOf(digits.substring(0, 3));
            m = Double.valueOf(digits.substring(3, 5));
            s = 0;
        }
        else if (digitLen == 6) {
            d = Double.valueOf(digits.substring(0, 2));
            m = Double.valueOf(digits.substring(2, 4));
            s = Double.valueOf(digits.substring(4, 6));
        }
        else if (digitLen == 7) {
            d = Double.valueOf(digits.substring(0, 3));
            m = Double.valueOf(digits.substring(3, 5));
            s = Double.valueOf(digits.substring(5, 7));
        }
        else {
            throw new IndexOutOfBoundsException("Not implemented");
        }

        if (sign.equals("+")) {
            dir = 1;
        }
        else {
            dir = -1;
        }

        return dms(dir, d, m, s);
    }

    public double dms(int dir, double d, double m, double s) {
        return dir*(d+(m+s/60)/60);
    }

}
