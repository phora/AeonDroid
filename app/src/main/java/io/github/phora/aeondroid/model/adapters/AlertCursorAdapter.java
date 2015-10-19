package io.github.phora.aeondroid.model.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;

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
        return null;
    }

    @Override
    protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup viewGroup) {
        return View.inflate(context, R.layout.alert_item, null);
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {

    }

    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup viewGroup) {
        return View.inflate(context, R.layout.step_item, null);
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {

    }
}
