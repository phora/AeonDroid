package io.github.phora.aeondroid.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ViewFlipper;

import io.github.phora.aeondroid.MinMaxTextFilter;
import io.github.phora.aeondroid.R;

/**
 * Created by ${CUSER} on 10/1/15.
 */
public class PlanetPositionEdit extends LinearLayout {

    private boolean fullDegreeInput = false;

    private ViewFlipper viewFlipper;
    private Spinner     signSpinner;
    private EditText    smallDegreeEdit;
    private EditText    fullDegreeEdit;
    private Checkable   fullSloppyCheck;
    private Checkable   fullDegreeCheck;

    public PlanetPositionEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.planet_position_edit, this);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.PlanetPositionEdit,
                0, 0);

        fullDegreeInput = a.getBoolean(R.styleable.PlanetPositionEdit_fullDegreeInput, false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        fullDegreeCheck = (Checkable) findViewById(R.id.PlanetPosEdit_TypeFullDegree);
        viewFlipper = (ViewFlipper) findViewById(R.id.PlanetPosEdit_Flipper);
        signSpinner = (Spinner) findViewById(R.id.PlanetPosEdit_Sign);
        smallDegreeEdit = (EditText)findViewById(R.id.PlanetPosEdit_SmallDegree);
        fullDegreeEdit = (EditText)findViewById(R.id.PlanetPosEdit_FullDegree);
        fullSloppyCheck = (Checkable)findViewById(R.id.PlanetPosEdit_Sloppy);

        if (!isInEditMode()) {
            ((View)fullDegreeCheck).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    fullDegreeCheck.toggle();
                    fullDegreeInput = fullDegreeCheck.isChecked();
                    if (fullDegreeInput) {
                        viewFlipper.setDisplayedChild(1);
                    }
                    else {
                        viewFlipper.setDisplayedChild(0);
                    }
                }
            });
            fullDegreeCheck.setChecked(fullDegreeInput);

            signSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                    if (!fullDegreeInput) {
                        double smallDegree = 0;
                        try {
                            smallDegree=Double.valueOf(smallDegreeEdit.getText().toString());
                        } catch (NumberFormatException e) {
                        }
                        double value = pos * 30 + smallDegree;
                        fullDegreeEdit.setText(String.valueOf(value));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            smallDegreeEdit.setFilters(new InputFilter[]{new MinMaxTextFilter(0., 30., false)});
            smallDegreeEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (!fullDegreeInput) {
                        double smallDegree = 0;
                        try {
                            smallDegree=Double.valueOf(charSequence.toString());
                        } catch (NumberFormatException e) {
                        }
                        double value = signSpinner.getSelectedItemPosition() * 30 + smallDegree;
                        fullDegreeEdit.setText(String.valueOf(value));
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            fullDegreeEdit.setFilters(new InputFilter[]{new MinMaxTextFilter(0., 360., false)});
            fullDegreeEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (fullDegreeInput) {
                        double value = Double.valueOf(charSequence.toString());
                        signSpinner.setSelection((int)(value / 30));
                        smallDegreeEdit.setText(String.valueOf(value % 30.));
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
    }

    public boolean isSloppy() {
        if (fullDegreeInput) {
            return fullSloppyCheck.isChecked();
        }
        else {
            return TextUtils.isEmpty(smallDegreeEdit.getText()) || fullSloppyCheck.isChecked();
        }
    }

    public void setSloppy(boolean sloppy) {
        fullSloppyCheck.setChecked(sloppy);
    }

    public double degreeValue() {
        if (fullDegreeInput) {
            String valueTyped = fullDegreeEdit.getText().toString();
            if (!TextUtils.isEmpty(valueTyped)) {
                return Double.valueOf(valueTyped);
            }
            else {
                return 0;
            }
        }
        else {
            double baseSign = signSpinner.getSelectedItemPosition() * 30;
            String valueTyped = smallDegreeEdit.getText().toString();
            if (!TextUtils.isEmpty(valueTyped)) {
                return baseSign+Double.valueOf(valueTyped);
            }
            else {
                return baseSign;
            }
        }
    }

    public void setDegreeValue(double degrees) {
        fullDegreeEdit.setText(String.valueOf(degrees));

        int signMapped = (int)(degrees / 30);
        signSpinner.setSelection(signMapped);

        double smallDegree = degrees % 30.;
        smallDegreeEdit.setText(String.valueOf(smallDegree));
    }

    public boolean isFullDegreeInput() {
        return fullDegreeInput;
    }

    public void setFullDegreeInput(boolean fullDegreeInput) {
        this.fullDegreeInput = fullDegreeInput;
    }
}
