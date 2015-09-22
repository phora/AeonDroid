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

/**
 * Created by phora on 9/21/15.
 */
public class DatetimePreference extends DialogPreference {
    private static final long DEFAULT_VALUE = 0;
    private Calendar calendar;
    private DatePicker datePicker;
    private ListView hoursList;
    private ListView minutesList;
    private ListView secondsList;

    private String latRef;
    private String lonRef;
    private String showToastsRef;
    private TextView tzView;

    public DatetimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DateTimePreference,
                0, 0);

        latRef = a.getString(R.styleable.DateTimePreference_latitudeReference);
        lonRef = a.getString(R.styleable.DateTimePreference_longitudeReference);
        showToastsRef = a.getString(R.styleable.DateTimePreference_showToastsReference);
        setDialogLayoutResource(R.layout.preference_datetime);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        double lat = Double.valueOf(getSharedPreferences().getString(latRef, "0.0"));
        double lon = Double.valueOf(getSharedPreferences().getString(lonRef, "0.0"));
        boolean showToast = false;
        String timezone;
        try {
            ZoneTab.ZoneInfo zi = ZoneTab.getInstance(getContext()).nearestTZ(lat, lon);
            if (zi != null) {
                timezone = zi.getTz();
            }
            else {
                timezone = "UTC";
            }
        } catch (FileNotFoundException e) {
            timezone = "UTC";
        }
        Log.d("DatetimePreference", "Found timezone " + timezone);
        calendar = new GregorianCalendar(TimeZone.getTimeZone(timezone));
        calendar.setTimeInMillis(getPersistedLong(DEFAULT_VALUE));

        tzView = (TextView)view.findViewById(R.id.DTPref_TimeZone);
        tzView.setText(getContext().getString(R.string.DetectedTimezone, timezone));
        datePicker  = (DatePicker)view.findViewById(R.id.DTPref_Date);
        hoursList   = (ListView)view.findViewById(R.id.DTPref_Hours);
        minutesList = (ListView)view.findViewById(R.id.DTPref_Minutes);
        secondsList = (ListView)view.findViewById(R.id.DTPref_Seconds);

        final ViewFlipper viewFlipper = (ViewFlipper)view.findViewById(R.id.viewFlipper);
        if (viewFlipper != null) {
            showToast = getSharedPreferences().getBoolean(showToastsRef, true);
            viewFlipper.setOnTouchListener(new View.OnTouchListener() {
                private float firstX;
                private float MIN_SWIPE = 10.0f;
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        firstX = motionEvent.getX();
                    }
                    else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        float lastX = motionEvent.getX();
                        if (firstX - lastX > MIN_SWIPE) {
                            viewFlipper.showNext();
                            return false;
                        }
                        else if (lastX - firstX > MIN_SWIPE) {
                            viewFlipper.showPrevious();
                            return false;
                        }
                        else {
                            Toast.makeText(getContext(), R.string.DTPref_Paging, Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    }
                    return true;
                }
            });
        }

        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        hoursList.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_single_choice,
                getContext().getResources().getStringArray(R.array.hours)));
        hoursList.setItemChecked(calendar.get(Calendar.HOUR_OF_DAY), true);
        hoursList.setSelectionFromTop(calendar.get(Calendar.HOUR_OF_DAY), hoursList.getHeight() / 2);

        minutesList.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_single_choice,
                getContext().getResources().getStringArray(R.array.minutes_seconds)));
        minutesList.setItemChecked(calendar.get(Calendar.MINUTE), true);
        minutesList.setSelectionFromTop(calendar.get(Calendar.MINUTE), minutesList.getHeight() / 2);

        secondsList.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_single_choice,
                getContext().getResources().getStringArray(R.array.minutes_seconds)));
        secondsList.setItemChecked(calendar.get(Calendar.SECOND), true);
        secondsList.setSelectionFromTop(calendar.get(Calendar.SECOND), secondsList.getHeight() / 2);

        if (showToast && viewFlipper != null) {
            Toast.makeText(getContext(), R.string.DTPref_Paging, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            calendar.set(Calendar.YEAR, datePicker.getYear());
            calendar.set(Calendar.MONTH, datePicker.getMonth());
            calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
            calendar.set(Calendar.HOUR_OF_DAY, hoursList.getCheckedItemPosition());
            calendar.set(Calendar.MINUTE, minutesList.getCheckedItemPosition());
            calendar.set(Calendar.SECOND, secondsList.getCheckedItemPosition());

            long newTime = calendar.getTimeInMillis();

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
