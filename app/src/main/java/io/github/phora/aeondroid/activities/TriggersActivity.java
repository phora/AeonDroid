package io.github.phora.aeondroid.activities;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseBooleanArray;
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
    private EditButtonListener mEditButtonListener;
    private ExpandableListView.OnGroupClickListener mDontExpandOnClick = new ExpandableListView.OnGroupClickListener() {
        @Override
        public boolean onGroupClick(ExpandableListView expandableListView, View view, int pos, long id) {
            expandableListView.setItemChecked(pos, !expandableListView.isItemChecked(pos));
            //Log.d("TriggersActivity", "Selection length: " + listView.getCheckedItemCount());
            return true;
        }
    };

    private static final int NEW_TRIGGER = 0;
    private static final int EDITING_TRIGGER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triggers);

        context = this;

        final ExpandableListView listView = getExpandableListView();
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setClickable(true);
        listView.setLongClickable(true);
        listView.setItemsCanFocus(false);
        listView.setOnGroupClickListener(mDontExpandOnClick);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Cursor cursor = (Cursor) listView.getItemAtPosition(pos);
                int attInt = cursor.getInt(cursor.getColumnIndex(DBHelper.ATRIGGER_TYPE));
                AlertTriggerType att = AlertTriggerType.intToATT(attInt);

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
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteItems = menu.findItem(R.id.action_delete);
        MenuItem groupItems = menu.findItem(R.id.action_group);

        ExpandableListView expandableListView = getExpandableListView();
        boolean moreThanOne = expandableListView.getCheckedItemCount() > 0;

        deleteItems.setEnabled(moreThanOne);

        SparseBooleanArray sba = expandableListView.getCheckedItemPositions();
        boolean isOrHasChild = false;

        for (int i = 0; i < sba.size(); i++) {
            if (!sba.valueAt(i)) {
                continue;
            }
            long packedPos = expandableListView.getExpandableListPosition(sba.keyAt(i));
            int posType = ExpandableListView.getPackedPositionType(packedPos);
            Cursor cursor = (Cursor) expandableListView.getItemAtPosition(sba.keyAt(i));
            AlertTriggerType att = AlertTriggerType.intToATT(cursor.getInt(cursor.getColumnIndex(DBHelper.ATRIGGER_TYPE)));
            isOrHasChild = (posType == ExpandableListView.PACKED_POSITION_TYPE_CHILD ||
                    att == AlertTriggerType.ATRIGGER_GROUP);
            if (isOrHasChild) {
                break;
            }
        }

        groupItems.setEnabled(!isOrHasChild && moreThanOne);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        final ExpandableListView expandableListView = getExpandableListView();
        final int itemCount = expandableListView.getCheckedItemCount();
        final SparseBooleanArray sba = expandableListView.getCheckedItemPositions();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_group) {
            //show dialog showing groups where to move
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final ExpandableListView putInWhichGroup = new ExpandableListView(this);
            putInWhichGroup.setGroupIndicator(null);
            putInWhichGroup.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            putInWhichGroup.setOnGroupClickListener(mDontExpandOnClick);
            new AsyncTask<Void, Void, Cursor>() {
                @Override
                protected Cursor doInBackground(Void... voids) {
                    return DBHelper.getInstance(context).getAllTriggerGroups();
                }

                @Override
                protected void onPostExecute(Cursor cursor) {
                    AlertTriggerCursorAdapter atca = new AlertTriggerCursorAdapter(cursor, context, null);
                    putInWhichGroup.setAdapter(atca);
                }
            }.execute();
            builder.setView(putInWhichGroup);
            builder.setMessage(getString(R.string.TriggersActivity_GroupTriggersMessage, getString(R.string.AppName)));
            builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int buttonId) {
                    Long[] itemIds = new Long[itemCount];
                    int itemIdIdx = 0;
                    for (int i = 0; i < sba.size(); i++) {
                        if (!sba.valueAt(i)) {
                            continue;
                        }
                        Cursor cursor = (Cursor)expandableListView.getItemAtPosition(sba.keyAt(i));
                        long itemId = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID));
                        itemIds[itemIdIdx] = itemId;
                        itemIdIdx++;
                    }
                    //add triggers to that group
                    //then refresh

                    int groupItemPos = putInWhichGroup.getSelectedItemPosition();
                    long groupId;
                    if (groupItemPos != -1) {
                        Cursor cursor = (Cursor) putInWhichGroup.getItemAtPosition(groupItemPos);
                        groupId = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID));
                    }
                    else {
                        groupId = -1;
                    }

                    new BatchGroupTask(groupId).execute(itemIds);
                }
            });
            builder.setNegativeButton(R.string.Cancel, null);
            builder.create().show();
            return true;
        }
        else if (id == R.id.action_delete) {
            //show dialog showing confirm dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String msg = getString(R.string.TriggersActivity_BatchDeleteConfirm, itemCount);
            builder.setMessage(msg);
            builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int buttonId) {
                    Long[] itemIds = new Long[itemCount];
                    int itemIdIdx = 0;
                    for (int i = 0; i < sba.size(); i++) {
                        if (!sba.valueAt(i)) {
                            continue;
                        }
                        Cursor cursor = (Cursor)expandableListView.getItemAtPosition(sba.keyAt(i));
                        long itemId = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID));
                        itemIds[itemIdIdx] = itemId;
                        itemIdIdx++;
                    }
                    //then run the batch delete, which calls the refresh when done
                    new BatchDeleteTask().execute(itemIds);
                }
            });
            builder.setNegativeButton(R.string.Cancel, null);
            builder.create().show();
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
                AlertTriggerType att = AlertTriggerType.intToATT(data.getIntExtra(EditTriggerActivity.EXTRA_TYPE, 1));

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
        Intent intent = new Intent(this, EditTriggerActivity.class);
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

            Intent intent = new Intent(context, EditTriggerActivity.class);

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

    private class BatchDeleteTask extends AsyncTask<Long,Void,Void> {
        @Override
        protected Void doInBackground(Long... longs) {
            DBHelper.getInstance(context).deleteTriggers(longs);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new LoadTriggersTask().execute();
        }
    }

    private class BatchGroupTask extends AsyncTask<Long,Void,Boolean> {

        private Long groupId;

        public BatchGroupTask(Long groupId) {
            this.groupId = groupId;
        }

        @Override
        protected Boolean doInBackground(Long... longs) {
            DBHelper dbHelper = DBHelper.getInstance(TriggersActivity.this);
            if (groupId == null || groupId < 1) {
                groupId = dbHelper.createTrigger(AlertTriggerType.ATRIGGER_GROUP, null, null, null, true);
            }
            if (groupId >= 1) {
                if (longs.length == 1) {
                    dbHelper.addTriggerToGroup(groupId, longs[0]);
                    return true;
                }
                else if (longs.length > 1) {
                    dbHelper.addTriggersToGroup(groupId, longs);
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean shouldRefresh) {
            if (shouldRefresh) {
                new LoadTriggersTask().execute();
            }
        }
    }
}
