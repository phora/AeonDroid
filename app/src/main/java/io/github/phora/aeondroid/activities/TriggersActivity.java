package io.github.phora.aeondroid.activities;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
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
                Cursor cursor = (Cursor)listView.getItemAtPosition(pos);
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
                    }
                    else {
                        DBHelper.getInstance(context).deleteTrigger(alertTriggerId);
                        Toast.makeText(context, R.string.TriggersActivity_RemovedTrigger, Toast.LENGTH_SHORT).show();
                    }

                }
                else if (listItemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
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

    public void addTrigger(View view) {
        DBHelper.getInstance(this).createTrigger(AlertTriggerType.DAY_TYPE, 0., null, 1., true);
        new LoadTriggersTask().execute();
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
                setListAdapter(new AlertTriggerCursorAdapter(cursor, context, false));
            }
            else {
                AlertTriggerCursorAdapter atca = (AlertTriggerCursorAdapter)getExpandableListAdapter();
                atca.changeCursor(cursor);
            }
        }
    }
}
