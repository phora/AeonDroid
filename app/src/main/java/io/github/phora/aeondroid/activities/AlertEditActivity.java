package io.github.phora.aeondroid.activities;

import android.os.Bundle;
import android.app.Activity;

import io.github.phora.aeondroid.R;

public class AlertEditActivity extends Activity {

    private long alertId = -1;
    private String alertName;

    public final static String EXTRA_ALERT_ID = "EXTRA_ALERT_ID";
    public final static String EXTRA_ALERT_NAME = "EXTRA_ALERT_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_edit);

        if (getIntent() != null) {
            alertId = getIntent().getLongExtra(EXTRA_ALERT_ID, -1);
            alertName = getIntent().getStringExtra(EXTRA_ALERT_NAME);
        }
    }

}
