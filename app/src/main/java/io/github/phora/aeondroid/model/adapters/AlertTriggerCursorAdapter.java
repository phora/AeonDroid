package io.github.phora.aeondroid.model.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.phora.aeondroid.AlertTriggerType;
import io.github.phora.aeondroid.DBHelper;
import io.github.phora.aeondroid.R;

/**
 * Created by phora on 9/30/15.
 */
public class AlertTriggerCursorAdapter extends CursorTreeAdapter {
    private Context mContext;

    public AlertTriggerCursorAdapter(Cursor cursor, Context context) {
        super(cursor, context);
        mContext = context;
    }

    public AlertTriggerCursorAdapter(Cursor cursor, Context context, boolean autoRequery) {
        super(cursor, context, autoRequery);
        mContext = context;
    }

    @Override
    protected Cursor getChildrenCursor(Cursor cursor) {
        long group_id = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID));
        boolean hasArg = cursor.isNull(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG1));

        if (!hasArg) {
            return DBHelper.getInstance(mContext).getSubtriggers(group_id);
        }
        else {
            return null;
        }
    }

    @Override
    protected View newGroupView(Context context, Cursor cursor, final boolean isExpanded, final ViewGroup viewGroup) {
        return View.inflate(context, R.layout.trigger_item, null);
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        TextView title = (TextView) view.findViewById(R.id.TriggerItem_Type);
        ImageView groupIndicator = (ImageView) view.findViewById(R.id.TriggerItem_GroupIndicator);

        int triggerType = cursor.getInt(cursor.getColumnIndex(DBHelper.ATRIGGER_TYPE));
        AlertTriggerType att = AlertTriggerType.intToAtrigger(triggerType);

        if (!cursor.isNull(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG1))) {
            groupIndicator.setImageResource(0);
        }
        else {
            if (isExpanded) {
                groupIndicator.setImageResource(R.drawable.base_chakra);
            }
            else {
                groupIndicator.setImageResource(R.drawable.crown_chakra);
            }
        }

        title.setText(att.toString());
    }

    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup viewGroup) {
        return View.inflate(context, R.layout.trigger_item, null);
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        TextView title = (TextView) view.findViewById(R.id.TriggerItem_Type);
        int triggerType = cursor.getInt(cursor.getColumnIndex(DBHelper.ATRIGGER_TYPE));
        AlertTriggerType att = AlertTriggerType.intToAtrigger(triggerType);
        title.setText(att.toString());
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, final ViewGroup parent) {
        View result = super.getGroupView(groupPosition, isExpanded, convertView, parent);
        ImageView groupIndicator = (ImageView) result.findViewById(R.id.TriggerItem_GroupIndicator);
        groupIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View btn) {
                ExpandableListView elv = (ExpandableListView)parent;
                if (isExpanded) {
                    elv.collapseGroup(groupPosition);
                }
                else {
                    elv.expandGroup(groupPosition);
                }
            }
        });
        return result;
    }
}
