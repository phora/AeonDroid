package io.github.phora.aeondroid.model.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.ImageButton;
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

        editAlert.setTag(cursor.getPosition());
        linkTriggers.setTag(cursor.getPosition());

        alertName.setText(cursor.getString(cursor.getColumnIndex(DBHelper.ALERT_LABEL)));

        editAlert.setOnClickListener(editButtonListener);
        linkTriggers.setOnClickListener(linkButtonListener);
    }

    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup viewGroup) {
        return View.inflate(context, R.layout.step_item, null);
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        ImageButton editStep = (ImageButton)view.findViewById(R.id.StepItem_Edit);

        editStep.setTag(0, cursor);
        editStep.setTag(1, cursor.getPosition());
        editStep.setOnClickListener(stepEditButtonListener);
    }
}
