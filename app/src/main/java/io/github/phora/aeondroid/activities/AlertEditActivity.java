package io.github.phora.aeondroid.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import io.github.phora.aeondroid.R;

public class AlertEditActivity extends ListActivity {

    private long alertId = -1;

    public final static String EXTRA_ALERT_ID = "EXTRA_ALERT_ID";
    public final static String EXTRA_ALERT_NAME = "EXTRA_ALERT_NAME";
    private EditText mAlertName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_edit);

        mAlertName = (EditText) findViewById(R.id.AlertEdit_Name);
        if (getIntent() != null) {
            alertId = getIntent().getLongExtra(EXTRA_ALERT_ID, -1);
            mAlertName.setText(getIntent().getStringExtra(EXTRA_ALERT_NAME));
        }
    }


    public void cancelEdit(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void finishEdit(View view) {
        Intent intent = new Intent();

        intent.putExtra(EXTRA_ALERT_NAME, mAlertName.getText().toString());
        intent.putExtra(EXTRA_ALERT_ID, alertId);

        setResult(RESULT_OK, intent);
        finish();
    }
}
