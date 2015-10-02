package io.github.phora.aeondroid.activities;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import net.i2p.android.ext.floatingactionbutton.FloatingActionsMenu;

import io.github.phora.aeondroid.AlertTriggerType;
import io.github.phora.aeondroid.DBHelper;
import io.github.phora.aeondroid.widgets.FABAnimator;
import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.model.adapters.AlertTriggerCursorAdapter;

public class TriggersActivity extends ExpandableListActivity {

    private Context context;
    private EditButtonListener mEditButtonListener;

    private static final int NEW_TRIGGER = 0;
    private static final int EDITING_TRIGGER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triggers);

        context = this;

        final ExpandableListView listView = getExpandableListView();
        listView.setClickable(true);
        listView.setLongClickable(true);
        listView.setItemsCanFocus(false);
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int pos, long id) {
                listView.setItemChecked(pos, !listView.isItemChecked(pos));
                //Log.d("TriggersActivity", "Selection length: " + listView.getCheckedItemCount());
                return true;
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Cursor cursor = (Cursor) listView.getItemAtPosition(pos);
                int attInt = cursor.getInt(cursor.getColumnIndex(DBHelper.ATRIGGER_TYPE));
                AlertTriggerType att = AlertTriggerType.intToAtrigger(attInt);

                int listItemType = ExpandableListView.getPackedPositionType(id);
                long alertTriggerId = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID));

                if (listItemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    boolean reallyAGroup = (att == AlertTriggerType.ATRIGGER_GROUP);
                    if (reallyAGroup) {
                        DBHelper.getInstance(context).removeSubtriggers(alertTriggerId);
                        DBHelper.getInstance(context).deleteTrigger(alertTriggerId);
                        Toast.makeText(context, R.string.TriggersActivity_RemovedTriggerGroup, Toast.LENGTH_SHORT).show();
                    } else {
                        DBHelper.getInstance(context).deleteTrigger(alertTriggerId);
                        Toast.makeText(context, R.string.TriggersActivity_RemovedTrigger, Toast.LENGTH_SHORT).show();
                    }

                } else if (listItemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    long groupId = cursor.getLong(cursor.getColumnIndex(DBHelper.SUBTRIGGER_GID));
                    DBHelper.getInstance(context).removeSubtrigger(groupId, alertTriggerId);
                    Toast.makeText(context, R.string.TriggersActivity_RemovedSubtrigger, Toast.LENGTH_SHORT).show();
                }
                new LoadTriggersTask().execute();
                return true;
            }
        });

        FloatingActionsMenu fab = (FloatingActionsMenu) findViewById(R.id.fab);

        listView.setOnScrollListener(new FABAnimator(context, fab));

        mEditButtonListener = new EditButtonListener();

        new LoadTriggersTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_triggers, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_TRIGGER) {
            if (resultCode == RESULT_OK) {
                Long arg1 = null;
                Double arg2 = null;
                Long specificity = null;
                AlertTriggerType att = AlertTriggerType.intToAtrigger(data.getIntExtra(EditTriggerActivity.EXTRA_TYPE, 1));

                //some of the trigger arguments maybe null
                if (data.hasExtra(EditTriggerActivity.EXTRA_ARG1)) {
                    arg1 = data.getLongExtra(EditTriggerActivity.EXTRA_ARG1, 0);
                }
                if (data.hasExtra(EditTriggerActivity.EXTRA_ARG2)) {
                    arg2 = data.getDoubleExtra(EditTriggerActivity.EXTRA_ARG2, 0);
                }
                if (data.hasExtra(EditTriggerActivity.EXTRA_SPECIFICITY)) {
                    specificity = data.getLongExtra(EditTriggerActivity.EXTRA_SPECIFICITY, 0);
                }

                boolean enabled = data.getBooleanExtra(EditTriggerActivity.EXTRA_ENABLED, false);

                DBHelper.getInstance(this).createTrigger(att, arg1, arg2, specificity, enabled);
                new LoadTriggersTask().execute();
            }
        }
        else if (requestCode == EDITING_TRIGGER) {
            if (resultCode == RESULT_OK) {
                Long arg1 = null;
                Double arg2 = null;
                Long specificity = null;

                long id = data.getLongExtra(EditTriggerActivity.EXTRA_ID, -1);
                //some of the trigger arguments maybe null
                if (data.hasExtra(EditTriggerActivity.EXTRA_ARG1)) {
                    arg1 = data.getLongExtra(EditTriggerActivity.EXTRA_ARG1, 0);
                }
                if (data.hasExtra(EditTriggerActivity.EXTRA_ARG2)) {
                    arg2 = data.getDoubleExtra(EditTriggerActivity.EXTRA_ARG2, 0);
                }
                if (data.hasExtra(EditTriggerActivity.EXTRA_SPECIFICITY)) {
                    specificity = data.getLongExtra(EditTriggerActivity.EXTRA_SPECIFICITY, 0);
                }

                boolean enabled = data.getBooleanExtra(EditTriggerActivity.EXTRA_ENABLED, false);

                DBHelper.getInstance(this).updateTriggerParams(id, arg1, arg2, specificity, enabled);
                new LoadTriggersTask().execute();
            }
        }
    }

    public void addTrigger(View view) {
        Intent intent = new Intent(TriggersActivity.this, EditTriggerActivity.class);
        startActivityForResult(intent, NEW_TRIGGER);
    }

    public void addTriggerGroup(View view) {
        DBHelper.getInstance(this).createTrigger(AlertTriggerType.ATRIGGER_GROUP, null, null, null, true);
        new LoadTriggersTask().execute();
    }

    private class LoadTriggersTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Void... voids) {
            return DBHelper.getInstance(context).getAllTriggers();
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            ExpandableListAdapter ela = getExpandableListAdapter();
            if (ela == null) {
                setListAdapter(new AlertTriggerCursorAdapter(cursor, context, false, mEditButtonListener));
            }
            else {
                AlertTriggerCursorAdapter atca = (AlertTriggerCursorAdapter)getExpandableListAdapter();
                atca.changeCursor(cursor);
            }
        }
    }

    private class EditButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int cursorPos = (Integer)view.getTag();
            Cursor cursor = ((AlertTriggerCursorAdapter)getExpandableListAdapter()).getCursor();
            cursor.moveToPosition(cursorPos);

            long colId = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID));
            int itemType = cursor.getInt(cursor.getColumnIndex(DBHelper.ATRIGGER_TYPE));
            Long arg1 = cursor.getLong(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG1));
            Double arg2 = cursor.getDouble(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG2));
            Long specificity = cursor.getLong(cursor.getColumnIndex(DBHelper.ATRIGGER_SPECIFICITY));
            boolean enabled = cursor.getInt(cursor.getColumnIndex(DBHelper.ATRIGGER_ENABLED)) == 1;

            Intent intent = new Intent(TriggersActivity.this, EditTriggerActivity.class);

            /* fill intent with data */
            intent.putExtra(EditTriggerActivity.EXTRA_ID, colId);
            intent.putExtra(EditTriggerActivity.EXTRA_TYPE, itemType);
            intent.putExtra(EditTriggerActivity.EXTRA_ARG1, arg1);
            intent.putExtra(EditTriggerActivity.EXTRA_ARG2, arg2);
            intent.putExtra(EditTriggerActivity.EXTRA_SPECIFICITY, specificity);
            intent.putExtra(EditTriggerActivity.EXTRA_ENABLED, enabled);
            /* /fill intent with data */

            startActivityForResult(intent, EDITING_TRIGGER);
        }
    }
}
