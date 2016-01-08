package io.github.phora.aeondroid.activities;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import net.i2p.android.ext.floatingactionbutton.AddFloatingActionButton;

import io.github.phora.aeondroid.DBHelper;
import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.model.adapters.StepReorderAdapter;
import io.github.phora.aeondroid.widgets.FABAnimator;

public class AlertEditActivity extends ListActivity {

    private long alertId = -1;

    public final static String EXTRA_ALERT_ID = "EXTRA_ALERT_ID";
    public final static String EXTRA_STEP_PAIR_IDS = "EXTRA_STEP_PAIR_IDS";
    public final static String EXTRA_STEP_PAIR_ORDERS = "EXTRA_STEP_PAIR_ORDERS";
    public final static String EXTRA_ALERT_NAME = "EXTRA_ALERT_NAME";

    private final static int CREATED_STEP = 1;

    private Context context;

    private EditText mAlertName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_edit);

        context = this;

        mAlertName = (EditText) findViewById(R.id.AlertEdit_Name);
        if (getIntent() != null) {
            alertId = getIntent().getLongExtra(EXTRA_ALERT_ID, -1);
            mAlertName.setText(getIntent().getStringExtra(EXTRA_ALERT_NAME));
        }

        final ListView listView = getListView();

        AddFloatingActionButton fab = (AddFloatingActionButton) findViewById(R.id.fab);
        listView.setOnScrollListener(new FABAnimator(context, fab));

        new LoadStepsForAlertTask().execute();
    }

    public void cancelEdit(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void finishEdit(View view) {
        Intent intent = new Intent();

        intent.putExtra(EXTRA_ALERT_NAME, mAlertName.getText().toString());
        intent.putExtra(EXTRA_ALERT_ID, alertId);

        StepReorderAdapter stepReorderAdapter = (StepReorderAdapter) getListAdapter();
        LongSparseArray<int[]> pendingStepChanges = stepReorderAdapter.getPendingStepChanges();
        int pendingStepChangeSize = pendingStepChanges.size();
        long[] stepPairIds = new long[pendingStepChangeSize];
        int[] stepPairOrders = new int[pendingStepChangeSize];

        for (int i = 0; i < pendingStepChangeSize; i++) {
            stepPairIds[i] = pendingStepChanges.keyAt(i);
            stepPairOrders[i] = pendingStepChanges.valueAt(i)[1];
        }

        intent.putExtra(EXTRA_STEP_PAIR_IDS, stepPairIds);
        intent.putExtra(EXTRA_STEP_PAIR_ORDERS, stepPairOrders);

        setResult(RESULT_OK, intent);
        finish();
    }

    public void addStep(View view) {
        Intent intent = new Intent(AlertEditActivity.this, StepEditActivity.class);
        startActivityForResult(intent, CREATED_STEP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATED_STEP) {
            if (resultCode == RESULT_OK) {
                int reps = data.getIntExtra(StepEditActivity.EXTRA_REPS, 0);
                String url = data.getStringExtra(StepEditActivity.EXTRA_URL);
                String desc= data.getStringExtra(StepEditActivity.EXTRA_DESC);
                String img = data.getStringExtra(StepEditActivity.EXTRA_IMAGE);
                int color = data.getIntExtra(StepEditActivity.EXTRA_COLOR, 0);

                new CreateStepTask(reps, url, desc, img, color).execute();
            }
        }
    }

    private class LoadStepsForAlertTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Void... voids) {
            return DBHelper.getInstance(context).stepsForAlert(alertId);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            StepReorderAdapter stepReorderAdapter = (StepReorderAdapter) getListAdapter();
            if (stepReorderAdapter == null) {
                setListAdapter(new StepReorderAdapter(context, cursor, false));
            }
            else {
                stepReorderAdapter.changeCursor(cursor);
            }
        }
    }

    private class CreateStepTask  extends AsyncTask<Void, Void, Void> {
        private final int reps;
        private final String url;
        private final String desc;
        private final String img;
        private final int color;

        public CreateStepTask(int reps, String url, String desc, String img, int color) {
            this.reps = reps;
            this.url = url;
            this.desc = desc;
            this.img = img;
            this.color = color;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            DBHelper sqlhelper = DBHelper.getInstance(context);
            long stepId = sqlhelper.createAlertStep(url, img, desc, color, reps);
            sqlhelper.linkAlertStep(alertId, stepId, 999);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new LoadStepsForAlertTask().execute();
        }
    }
}
