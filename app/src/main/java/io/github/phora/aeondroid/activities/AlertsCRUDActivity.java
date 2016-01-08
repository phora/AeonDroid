package io.github.phora.aeondroid.activities;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CursorTreeAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;

import net.i2p.android.ext.floatingactionbutton.AddFloatingActionButton;

import io.github.phora.aeondroid.DBHelper;
import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.model.adapters.AlertCursorAdapter;
import io.github.phora.aeondroid.widgets.FABAnimator;

public class AlertsCRUDActivity extends ExpandableListActivity {

    private static final int EDITED_ALERT_AND_STEPS = 1;
    private static final int EDITED_STEP = 2;

    private Context context;
    private View.OnClickListener mLinkButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //take us to the triggers activity with a triggers for alert cursor
            Intent intent = new Intent(AlertsCRUDActivity.this, EditTriggerActivity.class);
            startActivity(intent);
        }
    };
    private View.OnClickListener mEditButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //take us to the edit steps activity, which will have name+steps on there
            //for re-ordering
            Intent intent = new Intent(AlertsCRUDActivity.this, AlertEditActivity.class);
            Cursor c = ((AlertCursorAdapter)getExpandableListAdapter()).getCursor();
            int pos = (Integer)view.getTag();
            c.moveToPosition(pos);

            long alertId = c.getLong(c.getColumnIndex(DBHelper.COLUMN_ID));
            String alertName = c.getString(c.getColumnIndex(DBHelper.ALERT_LABEL));

            intent.putExtra(AlertEditActivity.EXTRA_ALERT_ID, alertId);
            intent.putExtra(AlertEditActivity.EXTRA_ALERT_NAME, alertName);

            startActivityForResult(intent, EDITED_ALERT_AND_STEPS);
        }
    };
    private View.OnClickListener mStepEditButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //take us to the step edit activity, where we actually edit the parameters
            Intent intent = new Intent(AlertsCRUDActivity.this, StepEditActivity.class);
            AlertCursorAdapter aca = (AlertCursorAdapter)getExpandableListAdapter();
            Cursor c = (Cursor) view.getTag(R.id.AlertCursorAdapter_ChildCursor);
            int pos = (Integer) view.getTag(R.id.AlertCursorAdapter_ChildCursorPos);
            c.moveToPosition(pos);

            long stepId = c.getLong(c.getColumnIndex(DBHelper.COLUMN_ID));
            int repetitions = c.getInt(c.getColumnIndex(DBHelper.STEP_REPITITIONS));
            String url = c.getString(c.getColumnIndex(DBHelper.STEP_LINK));
            String desc = c.getString(c.getColumnIndex(DBHelper.STEP_DESCRIPTION));
            String imageUri = c.getString(c.getColumnIndex(DBHelper.STEP_IMAGE));
            int color = c.getInt(c.getColumnIndex(DBHelper.STEP_COLOR));

            intent.putExtra(StepEditActivity.EXTRA_STEP_ID, stepId);
            intent.putExtra(StepEditActivity.EXTRA_REPS, repetitions);
            intent.putExtra(StepEditActivity.EXTRA_URL, url);
            intent.putExtra(StepEditActivity.EXTRA_DESC, desc);
            intent.putExtra(StepEditActivity.EXTRA_IMAGE, imageUri);
            intent.putExtra(StepEditActivity.EXTRA_COLOR, color);

            startActivityForResult(intent, EDITED_STEP);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts_edit);

        context = this;

        final ExpandableListView listView = getExpandableListView();
        listView.setClickable(true);
        listView.setLongClickable(true);
        listView.setItemsCanFocus(false);

        AddFloatingActionButton fab = (AddFloatingActionButton) findViewById(R.id.fab);

        listView.setOnScrollListener(new FABAnimator(context, fab));

        new LoadAlertsTask().execute();
    }

    public void addAlert(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        builder.setView(editText);
        builder.setMessage(R.string.AlertsEditActivity_NameAlert);
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String alertName = editText.getText().toString();
                if (!TextUtils.isEmpty(alertName)) {
                    new AddAlertTask(alertName).execute();
                }
            }
        });
        builder.setNegativeButton(R.string.Cancel, null);
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDITED_ALERT_AND_STEPS) {
            if (resultCode == RESULT_OK) {
                long alertId = data.getLongExtra(AlertEditActivity.EXTRA_ALERT_ID, -1);
                String name = data.getStringExtra(AlertEditActivity.EXTRA_ALERT_NAME);
                long[] ids = data.getLongArrayExtra(AlertEditActivity.EXTRA_STEP_PAIR_IDS);
                int[] orders = data.getIntArrayExtra(AlertEditActivity.EXTRA_STEP_PAIR_ORDERS);

                new UpdateAlertAndStepOrdersTask(alertId, name, ids, orders).execute();
            }
        }
        else if (requestCode == EDITED_STEP) {
            if (resultCode == RESULT_OK) {
                long stepId = data.getLongExtra(StepEditActivity.EXTRA_STEP_ID, -1);
                int reps = data.getIntExtra(StepEditActivity.EXTRA_REPS, 0);
                String url = data.getStringExtra(StepEditActivity.EXTRA_URL);
                String desc = data.getStringExtra(StepEditActivity.EXTRA_DESC);
                String img = data.getStringExtra(StepEditActivity.EXTRA_IMAGE);
                int color = data.getIntExtra(StepEditActivity.EXTRA_COLOR, 0);

                new UpdateStepTask(stepId, url, img, desc, color, reps).execute();
            }
        }
    }



    private class UpdateAlertAndStepOrdersTask extends AsyncTask<Void, Void, Void> {
        private final long[] ids;
        private final int[] orders;
        private final String name;
        private final long alertId;

        public UpdateAlertAndStepOrdersTask(long alertId, String name, long[] ids, int[] orders) {
            this.name = name;
            this.alertId = alertId;
            this.ids = ids;
            this.orders = orders;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            DBHelper.getInstance(context).renameAlert(alertId, name);
            DBHelper.getInstance(context).updateLinkAlertStepOrders(ids, orders);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new LoadAlertsTask().execute();
        }
    }

    private class LoadAlertsTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Void... voids) {
            return DBHelper.getInstance(context).allAlerts();
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            CursorTreeAdapter adapter = (CursorTreeAdapter) AlertsCRUDActivity.this.getExpandableListAdapter();
            if (adapter == null) {
                adapter = new AlertCursorAdapter(cursor, context, mLinkButtonListener, mEditButtonListener, mStepEditButtonListener);
                AlertsCRUDActivity.this.setListAdapter(adapter);
            }
            else {
                adapter.changeCursor(cursor);
            }
        }
    }

    private class AddAlertTask extends AsyncTask<Void, Void, Void> {
        private String alertName;

        public AddAlertTask(String alertName) {
            this.alertName = alertName;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            DBHelper.getInstance(context).createAlert(alertName);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new LoadAlertsTask().execute();
        }
    }

    private class UpdateStepTask extends AsyncTask<Void, Void, Void> {
        private final long id;
        private final int reps;
        private final String url;
        private final String desc;
        private final String img;
        private final int color;

        public UpdateStepTask(long id, String url, String img, String desc, int color, int reps) {
            this.id = id;
            this.reps = reps;
            this.url = url;
            this.desc = desc;
            this.img = img;
            this.color = color;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            DBHelper.getInstance(context).updateAlertStep(id, url, img, desc, color, reps);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new LoadAlertsTask().execute();
        }
    }
}
