package io.github.phora.aeondroid.model.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;

import io.github.phora.aeondroid.AlertTriggerType;
import io.github.phora.aeondroid.DBHelper;
import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.calculations.EphemerisUtils;
import io.github.phora.aeondroid.model.AspectConfig;

import static io.github.phora.aeondroid.AlertTriggerType.*;

/**
 * Created by phora on 9/30/15.
 */
public class AlertTriggerCursorAdapter extends CursorTreeAdapter {
    private Context mContext;
    private View.OnClickListener mEditButtonListener;

    public AlertTriggerCursorAdapter(Cursor cursor, Context context,
                                     View.OnClickListener editButtonListener) {
        super(cursor, context);
        mContext = context;
        mEditButtonListener = editButtonListener;
    }

    public AlertTriggerCursorAdapter(Cursor cursor, Context context, boolean autoRequery,
                                     View.OnClickListener editButtonListener) {
        super(cursor, context, autoRequery);
        mContext = context;
        mEditButtonListener = editButtonListener;
    }

    @Override
    protected Cursor getChildrenCursor(Cursor cursor) {
        long group_id = cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID));
        boolean hasArg = !cursor.isNull(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG1));

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

    private void fillOutItem (AlertTriggerType att, boolean hasArg1, boolean hasArg2, boolean hasSpecificity,
                              View view, Context context, Cursor cursor, boolean isChild) {
        TextView title = (TextView) view.findViewById(R.id.TriggerItem_Type);

        Long argument1 = null;
        Double argument2 = null;
        Long specificity = null;

        if (hasArg1) {
            argument1 = cursor.getLong(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG1));
        }
        if (hasArg2) {
            argument2 = cursor.getDouble(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG2));
        }
        if (hasSpecificity) {
            specificity = cursor.getLong(cursor.getColumnIndex(DBHelper.ATRIGGER_SPECIFICITY));
        }

        ImageButton editButton = (ImageButton) view.findViewById(R.id.TriggerItem_EditBtn);
        editButton.setTag(cursor.getPosition());

        if (att != ATRIGGER_GROUP && !isChild && mEditButtonListener != null) {
            editButton.setOnClickListener(mEditButtonListener);
            editButton.setVisibility(View.VISIBLE);
        }
        else {
            editButton.setOnClickListener(null);
            editButton.setVisibility(View.GONE);
        }

        CheckBox checkbox = (CheckBox) view.findViewById(R.id.TriggerItem_SelectBox);
        if (isChild) {
            checkbox.setVisibility(View.GONE);
        }
        else {
            checkbox.setVisibility(View.VISIBLE);
        }

        String[] typeNames = context.getResources().getStringArray(R.array.TriggerTypeNames);
        title.setText(typeNames[att.attToInt()]);

        TextView argument1View = (TextView) view.findViewById(R.id.TriggerItem_Argument1);
        TextView argument2View = (TextView) view.findViewById(R.id.TriggerItem_Argument2);
        TextView specificityView = (TextView) view.findViewById(R.id.TriggerItem_Specificity);

        if (hasArg1) {
            argument1View.setVisibility(View.VISIBLE);
            String[] labelNames = null;
            switch(att) {
                case DAY_TYPE:
                    labelNames = context.getResources().getStringArray(R.array.PlanetDayNames);
                    break;
                case MOON_PHASE:
                    labelNames = context.getResources().getStringArray(R.array.QuickPickPhase);
                    break;
                case PLANET_SIGN:
                    labelNames = context.getResources().getStringArray(R.array.PlanetChartNames);
                    break;
                case PLANETARY_HOUR:
                    labelNames = context.getResources().getStringArray(R.array.PlanetNames);
                    break;
                case DATETIME:
                    if (specificity != null) {
                        switch(specificity.intValue()) {
                            case 0:
                                argument1View.setText(EphemerisUtils.DATETIME_FMT.format(new Date(argument1)));
                                break;
                            case 1:
                                argument1View.setText(EphemerisUtils.DATE_FMT.format(new Date(argument1)));
                                break;
                            case 2:
                                argument1View.setText(EphemerisUtils.TIME_FMT.format(new Date(argument1)));
                                break;
                        }
                    }
                    break;
                case ASPECT:
                    labelNames = context.getResources().getStringArray(R.array.PlanetChartNames);
                    break;
            }
            if (labelNames != null) {
                argument1View.setText(labelNames[argument1.intValue()]);
            }
        }
        else {
            argument1View.setVisibility(View.GONE);
        }

        if (hasArg2) {
            argument2View.setVisibility(View.VISIBLE);
            switch(att) {
                case PLANET_SIGN:
                    argument2View.setText(EphemerisUtils.degreesToSignString(context, argument2));
                    break;
                case ASPECT:
                    String[] arg2labels = context.getResources().getStringArray(R.array.PlanetChartNames);
                    argument2View.setText(arg2labels[argument2.intValue()]);
                    break;
            }
        }
        else {
            argument2View.setVisibility(View.GONE);
        }

        if (hasSpecificity) {
            specificityView.setVisibility(View.VISIBLE);
            String[] specificityLabels = null;
            switch(att) {
                case DAY_TYPE:
                    specificityLabels = context.getResources().getStringArray(R.array.DayTypeSpecificityLabels);
                    break;
                case DATETIME:
                    specificityLabels = context.getResources().getStringArray(R.array.DateOrTime);
                    break;
                case PLANET_SIGN:
                    specificityLabels = context.getResources().getStringArray(R.array.PlanetPosSpecificityLabels);
                    break;
                case ASPECT:
                    specificityView.setText(AspectConfig.ASPECT_NAMES[specificity.intValue()]);
                    break;
            }
            if (specificityLabels != null) {
                specificityView.setText(specificityLabels[specificity.intValue()]);
            }
        }
        else {
            specificityView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        ImageView groupIndicator = (ImageView) view.findViewById(R.id.TriggerItem_GroupIndicator);

        int triggerType = cursor.getInt(cursor.getColumnIndex(DBHelper.ATRIGGER_TYPE));
        AlertTriggerType att = intToATT(triggerType);

        boolean hasArg1 = !cursor.isNull(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG1));
        boolean hasArg2 = !cursor.isNull(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG2));
        boolean hasSpecificity = !cursor.isNull(cursor.getColumnIndex(DBHelper.ATRIGGER_SPECIFICITY));

        if (hasArg1) {
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

        fillOutItem(att, hasArg1, hasArg2, hasSpecificity, view, context, cursor, false);
    }

    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup viewGroup) {
        return View.inflate(context, R.layout.trigger_item, null);
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        int triggerType = cursor.getInt(cursor.getColumnIndex(DBHelper.ATRIGGER_TYPE));
        AlertTriggerType att = intToATT(triggerType);

        boolean hasArg1 = !cursor.isNull(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG1));
        boolean hasArg2 = !cursor.isNull(cursor.getColumnIndex(DBHelper.ATRIGGER_ARG2));
        boolean hasSpecificity = !cursor.isNull(cursor.getColumnIndex(DBHelper.ATRIGGER_SPECIFICITY));

        fillOutItem(att, hasArg1, hasArg2, hasSpecificity, view, context, cursor, true);
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
