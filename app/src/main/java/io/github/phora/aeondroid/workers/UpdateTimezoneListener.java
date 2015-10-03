package io.github.phora.aeondroid.workers;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.FileNotFoundException;

import io.github.phora.aeondroid.calculations.ZoneTab;

/**
 * Created by phora on 10/3/15.
 */
class UpdateTimezoneListener implements SharedPreferences.OnSharedPreferenceChangeListener {
    private String longitudePref;
    private String latitudePref;
    private String timezonePref;
    private Context context;
    private PostTimezoneUpdateListener postTimezoneUpdateListener;

    public UpdateTimezoneListener(Context context,
                                  String longitudePref, String latitudePref, String timezonePref,
                                  PostTimezoneUpdateListener postTimezoneUpdateListener) {

        this.longitudePref = longitudePref;
        this.latitudePref = latitudePref;
        this.timezonePref = timezonePref;
        this.postTimezoneUpdateListener = postTimezoneUpdateListener;
        this.context = context.getApplicationContext();
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!key.equals(longitudePref) && !key.equals(latitudePref) && !key.equals(timezonePref)) {
            return;
        }
        else if (key.equals(longitudePref) || key.equals(latitudePref)) {
            Double longitude = Double.valueOf(sharedPreferences.getString(longitudePref, "0.0"));
            Double latitude = Double.valueOf(sharedPreferences.getString(latitudePref, "0.0"));

            try {
                String currentTimezone = sharedPreferences.getString(timezonePref, "");
                String detectedTimezone = null;
                ZoneTab zt = ZoneTab.getInstance(context);
                ZoneTab.ZoneInfo zi = zt.nearestTZ(latitude, longitude);
                if (zi != null) {
                    detectedTimezone = zi.getTz();
                }
                if (!currentTimezone.equals(detectedTimezone)) {
                    sharedPreferences.edit().putString(timezonePref, detectedTimezone).commit();
                }
            } catch (FileNotFoundException e) {

            }
        }
        else {
            if (postTimezoneUpdateListener != null) {
                postTimezoneUpdateListener.onTimezoneUpdate();
            }
        }
    }
}
