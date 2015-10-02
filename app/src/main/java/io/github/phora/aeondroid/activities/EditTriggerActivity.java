package io.github.phora.aeondroid.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.widgets.DateTimePicker;
import io.github.phora.aeondroid.widgets.PlanetPositionEdit;

public class EditTriggerActivity extends Activity {

    public static final String EXTRA_ID="EXTRA_ID";
    public static final String EXTRA_TYPE="EXTRA_TYPE";
    public static final String EXTRA_ARG1="EXTRA_ARG1";
    public static final String EXTRA_ARG2="EXTRA_ARG2";
    public static final String EXTRA_SPECIFICITY="EXTRA_SPECIFICITY";
    public static final String EXTRA_ENABLED="EXTRA_ENABLED";

    private long mItemId = -1;

    /* Type selector + pager for args for type*/
    private Spinner     mEditItemType;
    private ViewFlipper mEditArgsFlipper;
    private CheckedTextView mEditTrigEnabled;

    /* Trigger argument widgets */
    private Spinner   mDayType;
    private Checkable mOnlyFromSunrise;

    private Spinner mPhase;

    private Spinner             mPlanet;
    private PlanetPositionEdit  mPlanetPos;

    private Spinner mPlanetHour;

    private DateTimePicker mDateTime;
    private Spinner        mDateSpecificity;

    private Spinner  mNatalPlanet;
    private Spinner  mAspectType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_trigger);

        mEditItemType = (Spinner)findViewById(R.id.EditTrigger_Type);
        mEditArgsFlipper = (ViewFlipper)findViewById(R.id.EditTrigger_Args);
        mEditTrigEnabled = (CheckedTextView)findViewById(R.id.EditTrigger_Enabled);

        mEditItemType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                mEditArgsFlipper.setDisplayedChild(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mDayType = (Spinner)findViewById(R.id.EditTrigger_DayType);
        mOnlyFromSunrise = (Checkable)findViewById(R.id.EditTrigger_OnlyFromSunrise);

        mPhase = (Spinner)findViewById(R.id.EditTrigger_Phase);

        mPlanet = (Spinner)findViewById(R.id.EditTrigger_Planet);
        mPlanetPos = (PlanetPositionEdit)findViewById(R.id.EditTrigger_PlanetPos);

        mPlanetHour = (Spinner)findViewById(R.id.EditTrigger_PlanetaryHour);

        mDateTime = (DateTimePicker)findViewById(R.id.EditTrigger_DT);
        mDateSpecificity = (Spinner)findViewById(R.id.EditTrigger_SpecificityChoice);
        mDateSpecificity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                mDateTime.lockPage(pos-1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mNatalPlanet = (Spinner)findViewById(R.id.EditTrigger_NatalPlanet);
        mAspectType = (Spinner)findViewById(R.id.EditTrigger_AspectType);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (getIntent() != null) {
            Intent data = getIntent();
            mItemId =  data.getLongExtra(EXTRA_ID, -1);
            if (mItemId != -1) {
                // always greater than 0 from the intent, since 0 is reserved for trigger groups
                // due to this, we map them to the type boxes by bumping the value down by one
                int type = data.getIntExtra(EXTRA_TYPE, 1)-1;
                mEditItemType.setSelection(type);

                Double arg1 = data.getDoubleExtra(EXTRA_ARG1, 0);
                Double arg2 = data.getDoubleExtra(EXTRA_ARG2, 0);
                Double specificity = data.getDoubleExtra(EXTRA_SPECIFICITY, 0);

                switch (type) {
                    case 0:
                        mDayType.setSelection(arg1.intValue());
                        mOnlyFromSunrise.setChecked(specificity.intValue() == 1);
                        break;
                    case 1:
                        mPhase.setSelection(arg1.intValue());
                        break;
                    case 2:
                        mPlanet.setSelection(arg1.intValue());
                        mPlanetPos.setFullDegreeInput(sharedPreferences.getBoolean("UseFullDegreeInput", false));
                        mPlanetPos.setSloppy(specificity.intValue() == 1);
                        mPlanetPos.setDegreeValue(arg2);
                        break;
                    case 3:
                        mPlanetHour.setSelection(arg1.intValue());
                        break;
                    case 4:
                        mDateTime.setTimeInMillis(arg1.longValue());
                        mDateSpecificity.setSelection(specificity.intValue());
                        break;
                    case 5:
                        mPlanet.setSelection(arg1.intValue());
                        mNatalPlanet.setSelection(arg2.intValue());
                        mAspectType.setSelection(specificity.intValue());
                        break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_trigger, menu);
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

    public void cancelEdit(View view) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, mItemId);
        intent.putExtra(EXTRA_TYPE, mEditItemType.getSelectedItemPosition()+1);
        intent.putExtra(EXTRA_ENABLED, mEditTrigEnabled.isChecked());
        switch(mEditItemType.getSelectedItemPosition()) {
            case 0:
                intent.putExtra(EXTRA_ARG1, (long) mDayType.getSelectedItemPosition());
                if (mOnlyFromSunrise.isChecked()) {
                    intent.putExtra(EXTRA_SPECIFICITY, 1L);
                }
                else {
                    intent.putExtra(EXTRA_SPECIFICITY, 0L);
                }
                break;
            case 1:
                intent.putExtra(EXTRA_ARG1, (long)mPhase.getSelectedItemPosition());
                break;
            case 2:
                intent.putExtra(EXTRA_ARG1, (long)mPlanet.getSelectedItemPosition());
                intent.putExtra(EXTRA_ARG2, mPlanetPos.degreeValue());
                if (mPlanetPos.isSloppy()) {
                    intent.putExtra(EXTRA_SPECIFICITY, 1L);
                }
                else {
                    intent.putExtra(EXTRA_SPECIFICITY, 0L);
                }
                break;
            case 3:
                intent.putExtra(EXTRA_ARG1, (long)mPlanetHour.getSelectedItemPosition());
                break;
            case 4:
                intent.putExtra(EXTRA_ARG1, mDateTime.getTimeInMillis());
                intent.putExtra(EXTRA_SPECIFICITY, (long)mDateSpecificity.getSelectedItemPosition());
                break;
            case 5:
                intent.putExtra(EXTRA_ARG1, (long)mPlanet.getSelectedItemPosition());
                intent.putExtra(EXTRA_ARG2, (double)mNatalPlanet.getSelectedItemPosition());
                intent.putExtra(EXTRA_SPECIFICITY, (long)mAspectType.getSelectedItemPosition());
                break;
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    public void finishEdit(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
