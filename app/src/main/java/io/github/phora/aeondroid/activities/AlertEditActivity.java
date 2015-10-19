package io.github.phora.aeondroid.activities;

import android.os.Bundle;
import android.app.Activity;

import io.github.phora.aeondroid.R;

public class AlertEditActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_edit);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
