package io.github.phora.aeondroid.model.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.phora.aeondroid.DBHelper;
import io.github.phora.aeondroid.R;

/**
 * Created by phora on 10/9/15.
 */
public class AlertCursorAdapter extends CursorTreeAdapter {
    private Context mContext;
    private final View.OnClickListener linkButtonListener;
    private final View.OnClickListener editButtonListener;
    private final View.OnClickListener stepEditButtonListener;

    public AlertCursorAdapter(Cursor cursor, Context context,
                              View.OnClickListener linkButtonListener,
                              View.OnClickListener editButtonListener,
                              View.OnClickListener stepEditButtonListener) {
        super(cursor, context);
        mContext = context;
        this.linkButtonListener = linkButtonListener;
        this.editButtonListener = editButtonListener;
        this.stepEditButtonListener = stepEditButtonListener;
    }

    public AlertCursorAdapter(Cursor cursor, Context context, boolean autoRequery,
                              View.OnClickListener linkButtonListener,
                              View.OnClickListener editButtonListener,
                              View.OnClickListener stepEditButtonListener) {
        super(cursor, context, autoRequery);
        mContext = context;
        this.linkButtonListener = linkButtonListener;
        this.editButtonListener = editButtonListener;
        this.stepEditButtonListener = stepEditButtonListener;
    }

    @Override
    protected Cursor getChildrenCursor(Cursor cursor) {
        long alertId = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID));
        return DBHelper.getInstance(mContext).stepsForAlert(alertId);
    }

    @Override
    protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup viewGroup) {
        return View.inflate(context, R.layout.alert_item, null);
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        TextView alertName = (TextView) view.findViewById(R.id.AlarmItem_Name);
        ImageButton editAlert = (ImageButton) view.findViewById(R.id.AlarmItem_Edit);
        ImageButton linkTriggers = (ImageButton) view.findViewById(R.id.AlarmItem_Link);
        ImageView groupIndicator = (ImageView) view.findViewById(R.id.AlarmItem_GroupIndicator);

        editAlert.setTag(cursor.getPosition());
        linkTriggers.setTag(cursor.getPosition());

        alertName.setText(cursor.getString(cursor.getColumnIndex(DBHelper.ALERT_LABEL)));

        editAlert.setOnClickListener(editButtonListener);
        linkTriggers.setOnClickListener(linkButtonListener);

        if (isExpanded) {
            groupIndicator.setImageResource(R.drawable.base_chakra);
        }
        else {
            groupIndicator.setImageResource(R.drawable.crown_chakra);
        }
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, final ViewGroup parent) {
        View result = super.getGroupView(groupPosition, isExpanded, convertView, parent);
        ImageView groupIndicator = (ImageView) result.findViewById(R.id.AlarmItem_GroupIndicator);
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

    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup viewGroup) {
        return View.inflate(context, R.layout.step_item, null);
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        ImageView stepImage = (ImageView) view.findViewById(R.id.StepItem_Image);
        View colorPreview = view.findViewById(R.id.StepItem_Color);
        ImageButton editStep = (ImageButton)view.findViewById(R.id.StepItem_Edit);
        TextView stepUri = (TextView) view.findViewById(R.id.StepItem_Uri);
        TextView descPreview = (TextView) view.findViewById(R.id.StepItem_Desc);

        editStep.setTag(R.id.AlertCursorAdapter_ChildCursor, cursor);
        editStep.setTag(R.id.AlertCursorAdapter_ChildCursorPos, cursor.getPosition());
        editStep.setOnClickListener(stepEditButtonListener);

        Uri imageUri = Uri.parse(cursor.getString(cursor.getColumnIndex(DBHelper.STEP_IMAGE)));
        String otherUri = cursor.getString(cursor.getColumnIndex(DBHelper.STEP_LINK));
        String desc = cursor.getString(cursor.getColumnIndex(DBHelper.STEP_DESCRIPTION));
        int color = cursor.getInt(cursor.getColumnIndex(DBHelper.STEP_COLOR));

        stepImage.setImageURI(imageUri);
        stepUri.setText(otherUri);
        colorPreview.setBackgroundColor(color);

        int descLength = desc.length();
        if (descLength > 50) {
            String genPreview = String.format("%s ... %s", desc.substring(0, 25),
                    desc.substring(descLength-25));
            descPreview.setText(genPreview);
        }
        else {
            descPreview.setText(desc.substring(0, Math.min(50, descLength)));
        }
    }
}
