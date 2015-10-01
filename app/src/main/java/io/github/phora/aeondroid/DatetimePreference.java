package io.github.phora.aeondroid;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import io.github.phora.aeondroid.calculations.ZoneTab;
import io.github.phora.aeondroid.widgets.DateTimePicker;

/**
 * Created by phora on 9/21/15.
 */
public class DatetimePreference extends DialogPreference {
    private static final long DEFAULT_VALUE = 0;

    private DateTimePicker dtp;

    public DatetimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_datetime);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        dtp = (DateTimePicker) view.findViewById(R.id.DTPref);

        dtp.setTimeInMillis(getPersistedLong(DEFAULT_VALUE));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            /*
            calendar.set(Calendar.YEAR, datePicker.getYear());
            calendar.set(Calendar.MONTH, datePicker.getMonth());
            calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
            calendar.set(Calendar.HOUR_OF_DAY, hoursList.getCheckedItemPosition());
            calendar.set(Calendar.MINUTE, minutesList.getCheckedItemPosition());
            calendar.set(Calendar.SECOND, secondsList.getCheckedItemPosition());
            */

            long newTime = dtp.getTimeInMillis();

            if (!callChangeListener(newTime)) {
                return;
            }

            setDateTime(newTime);
        }
    }

    protected void setDateTime(long timeInMillis) {
        persistLong(timeInMillis);
        notifyDependencyChange(shouldDisableDependents());
        notifyChanged();
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        long datetime;

        if (defaultValue == null) {
            datetime = restorePersistedValue ? getPersistedLong(DEFAULT_VALUE) : DEFAULT_VALUE;
        } else if (defaultValue instanceof Long) {
            datetime = restorePersistedValue ? getPersistedLong((Long) defaultValue) : (Long) defaultValue;
        } else if (defaultValue instanceof Calendar) {
            datetime = restorePersistedValue ? getPersistedLong(((Calendar) defaultValue).getTimeInMillis()) : ((Calendar) defaultValue).getTimeInMillis();
        } else {
            datetime = restorePersistedValue ? getPersistedLong(DEFAULT_VALUE) : DEFAULT_VALUE;
        }

        setDateTime(datetime);
    }
}
