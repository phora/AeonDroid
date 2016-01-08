package io.github.phora.aeondroid.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import io.github.phora.aeondroid.R;
import yuku.ambilwarna.AmbilWarnaDialog;

public class StepEditActivity extends Activity {

    public static final String EXTRA_STEP_ID = "EXTRA_STEP_ID";
    public static final String EXTRA_ALERT_ID = "EXTRA_ALERT_ID";
    public static final String EXTRA_REPS = "EXTRA_REPS";
    public static final String EXTRA_URL = "EXTRA_URL";
    public static final String EXTRA_DESC = "EXTRA_DESC";
    public static final String EXTRA_IMAGE = "EXTRA_IMAGE";
    public static final String EXTRA_COLOR = "EXTRA_COLOR";
    
    private static final int CHANGED_IMAGE = 1;

    private long stepId = -1;

    //ignored except in the context of creating a new step
    private long alertId = -1;

    private EditText mRepEdit;
    private EditText mUrlEdit;
    private EditText mDescEdit;
    private ImageView mImageView;
    private View mColorView;

    private Uri imageSrc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_edit);


        mRepEdit = (EditText)findViewById(R.id.StepEdit_Reps);
        mUrlEdit = (EditText)findViewById(R.id.StepEdit_URL);
        mDescEdit = (EditText)findViewById(R.id.StepEdit_Desc);
        mImageView = (ImageView)findViewById(R.id.StepEdit_Img);
        mColorView = findViewById(R.id.StepEdit_ColorView);

        Intent intent = getIntent();
        if (intent != null) {
            stepId = intent.getLongExtra(EXTRA_STEP_ID, -1);
            alertId = intent.getLongExtra(EXTRA_ALERT_ID, -1);

            mRepEdit.setText(String.valueOf(intent.getIntExtra(EXTRA_REPS, 0)));
            mUrlEdit.setText(intent.getStringExtra(EXTRA_URL));
            mDescEdit.setText(intent.getStringExtra(EXTRA_DESC));
            String uri = intent.getStringExtra(EXTRA_IMAGE);
            if (uri == null) {
                imageSrc = Uri.parse("");
            }
            else {
                imageSrc = Uri.parse(uri);
            }
            mImageView.setImageURI(imageSrc);
            int curColor = intent.getIntExtra(EXTRA_COLOR, 0);
            mColorView.setBackgroundColor(curColor);
            mColorView.setTag(curColor);
        }
    }

    public void changeImage(View view) {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, CHANGED_IMAGE);
    }

    public void cancelEdit(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CHANGED_IMAGE)
        {
            if (resultCode == RESULT_OK)
            {
                imageSrc = data.getData();
                mImageView.setImageURI(imageSrc);
            }
        }
    }

    public void finishEdit(View view) {
        Intent intent = new Intent();

        intent.putExtra(EXTRA_STEP_ID, stepId);
        intent.putExtra(EXTRA_ALERT_ID, alertId);

        intent.putExtra(EXTRA_REPS, Integer.valueOf(mRepEdit.getText().toString()));
        intent.putExtra(EXTRA_URL, mUrlEdit.getText().toString());
        intent.putExtra(EXTRA_DESC, mDescEdit.getText().toString());
        intent.putExtra(EXTRA_IMAGE, imageSrc.toString());

        intent.putExtra(EXTRA_COLOR, (Integer)mColorView.getTag());

        setResult(RESULT_OK, intent);
        finish();
    }

    public void editColor(View view) {
        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(this, mColorView.getSolidColor(),
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        mColorView.setBackgroundColor(color);
                        mColorView.setTag(color);
                    }
                });
        ambilWarnaDialog.show();
    }
}
