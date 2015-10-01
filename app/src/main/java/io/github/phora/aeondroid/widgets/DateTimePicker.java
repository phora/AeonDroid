package io.github.phora.aeondroid.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.calculations.ZoneTab;

/**
 * Created by phora on 10/1/15.
 */
public class DateTimePicker extends LinearLayout {
    private Calendar calendar;
    private DatePicker datePicker;
    private ListView hoursList;
    private ListView minutesList;
    private ListView secondsList;

    private String latRef;
    private String lonRef;
    private String showToastsRef;
    private TextView tzView;
    private DatePicker.OnDateChangedListener datePickerListener;

    private boolean _dontDoubleFire = false;

    public DateTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.datetime_widget, this);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DateTimeWidget,
                0, 0);

        latRef = a.getString(R.styleable.DateTimeWidget_latitudeReference);
        lonRef = a.getString(R.styleable.DateTimeWidget_longitudeReference);
        showToastsRef = a.getString(R.styleable.DateTimeWidget_showToastsReference);
        datePickerListener = new DateChangedListener();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        double lat = Double.valueOf(sharedPreferences.getString(latRef, "0.0"));
        double lon = Double.valueOf(sharedPreferences.getString(lonRef, "0.0"));
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
        Log.d("DatetimePicker", "Found timezone " + timezone);
        calendar = new GregorianCalendar(TimeZone.getTimeZone(timezone));

        tzView = (TextView)findViewById(R.id.DTWidget_TimeZone);
        tzView.setText(getContext().getString(R.string.DetectedTimezone, timezone));
        datePicker  = (DatePicker)findViewById(R.id.DTWidget_Date);
        hoursList   = (ListView)findViewById(R.id.DTWidget_Hours);
        minutesList = (ListView)findViewById(R.id.DTWidget_Minutes);
        secondsList = (ListView)findViewById(R.id.DTWidget_Seconds);

        final ViewFlipper viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper);
        if (viewFlipper != null) {
            showToast = sharedPreferences.getBoolean(showToastsRef, true);
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

        datePicker.init(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                datePickerListener);
        hoursList.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_single_choice,
                getContext().getResources().getStringArray(R.array.hours)));
        hoursList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                if (_dontDoubleFire) {
                    return;
                }
                Log.d("DateTimePicker", "New hour: " + pos);
                calendar.set(Calendar.HOUR_OF_DAY, pos);
                Log.d("DateTimePicker", "Current datetime: " + calendar.getTime());
            }
        });

        minutesList.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_single_choice,
                getContext().getResources().getStringArray(R.array.minutes_seconds)));
        minutesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                if (_dontDoubleFire) {
                    return;
                }
                Log.d("DateTimePicker", "New minute: " + pos);
                calendar.set(Calendar.MINUTE, pos);
                Log.d("DateTimePicker", "Current datetime: " + calendar.getTime());
            }
        });

        secondsList.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_single_choice,
                getContext().getResources().getStringArray(R.array.minutes_seconds)));
        secondsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                if (_dontDoubleFire) {
                    return;
                }
                Log.d("DateTimePicker", "New second: "+pos);
                calendar.set(Calendar.SECOND, pos);
                Log.d("DateTimePicker", "Current datetime: " + calendar.getTime());
            }
        });

        if (showToast && viewFlipper != null) {
            Toast.makeText(getContext(), R.string.DTPref_Paging, Toast.LENGTH_SHORT).show();
        }
    }

    public void setTimeInMillis(long ms) {
        _dontDoubleFire = true;
        calendar.setTimeInMillis(ms);
        datePicker.updateDate(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        hoursList.setItemChecked(calendar.get(Calendar.HOUR_OF_DAY), true);
        hoursList.setSelectionFromTop(calendar.get(Calendar.HOUR_OF_DAY), hoursList.getHeight() / 2);

        minutesList.setItemChecked(calendar.get(Calendar.MINUTE), true);
        minutesList.setSelectionFromTop(calendar.get(Calendar.MINUTE), minutesList.getHeight() / 2);

        secondsList.setItemChecked(calendar.get(Calendar.SECOND), true);
        secondsList.setSelectionFromTop(calendar.get(Calendar.SECOND), secondsList.getHeight() / 2);

        Log.d("DateTimePicker", "Current datetime: " + calendar.getTime());
        _dontDoubleFire = false;
    }

    public long getTimeInMillis() {
        Log.d("DateTimePicker", "Current datetime: "+calendar.getTime());
        return calendar.getTimeInMillis();
    }

    private class DateChangedListener implements DatePicker.OnDateChangedListener {
        @Override
        public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
            if (_dontDoubleFire) {
                return;
            }
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            Log.d("DateTimePicker", "Current datetime: " + calendar.getTime());
        }
    }
}
