package io.github.phora.aeondroid.widgets;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.github.phora.aeondroid.calculations.EphemerisUtils;
import io.github.phora.aeondroid.Events;
import io.github.phora.aeondroid.PhaseUtils;
import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.model.MoonPhase;
import io.github.phora.aeondroid.workers.AeonDroidService;

public class RightNowView extends LinearLayout implements BroadcastReceivable, AeonDroidServiceable {
    private AeonDroidService mAeonDroidService;
    private Date timeStamp = null;

    private List<ReceiverFilterPair> backingStore;
    private View lastView = null;

    private View mSunRow;
    private View mMoonRow;
    private View mMercuryRow;
    private View mVenusRow;
    private View mMarsRow;
    private View mJupiterRow;
    private View mSaturnRow;
    private View mUranusRow;
    private View mNeptuneRow;
    private View mPlutoRow;
    
    private TextView mMoonPhase;

    private TextView mSunMeasure;
    private TextView mMoonMeasure;
    private TextView mMercuryMeasure;
    private TextView mVenusMeasure;
    private TextView mMarsMeasure;
    private TextView mJupiterMeasure;
    private TextView mSaturnMeasure;
    private TextView mUranusMeasure;
    private TextView mNeptuneMeasure;
    private TextView mPlutoMeasure;
    private TextView mMoonVOC;

    public RightNowView(Context context, AeonDroidService aeonDroidService, Date date) {
        // Required empty public constructor
        super(context);
        View.inflate(context, R.layout.right_now_view, this);
        onFinishInflate();

        backingStore = new LinkedList<>();
        IntentFilter intentFilter = new IntentFilter(Events.FOUND_HOUR);
        HighlightReceiver highlightReceiver = new HighlightReceiver();
        backingStore.add(new ReceiverFilterPair(highlightReceiver, intentFilter));

        IntentFilter phaseFilter = new IntentFilter(Events.PHASE_DETAILS);
        PhaseReceiver phaseReceiver = new PhaseReceiver();
        backingStore.add(new ReceiverFilterPair(phaseReceiver, phaseFilter));

        IntentFilter measureFilter = new IntentFilter(Events.PLANET_POS);
        MeasureReceiver measureReceiver = new MeasureReceiver();
        backingStore.add(new ReceiverFilterPair(measureReceiver, measureFilter));

        IntentFilter vocFilter = new IntentFilter(Events.VOC_STATUS);
        VoCReceiver vocReceiver = new VoCReceiver();
        backingStore.add(new ReceiverFilterPair(vocReceiver, vocFilter));

        timeStamp = date;
        mAeonDroidService = aeonDroidService;
    }
    

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // Inflate the layout for this fragment
        mSunRow = findViewById(R.id.RightNow_SunRow);
        mMoonRow = findViewById(R.id.RightNow_MoonRow);
        mMercuryRow = findViewById(R.id.RightNow_MercuryRow);
        mVenusRow = findViewById(R.id.RightNow_VenusRow);
        mMarsRow = findViewById(R.id.RightNow_MarsRow);
        mJupiterRow = findViewById(R.id.RightNow_JupiterRow);
        mSaturnRow = findViewById(R.id.RightNow_SaturnRow);
        mUranusRow = findViewById(R.id.RightNow_UranusRow);
        mNeptuneRow = findViewById(R.id.RightNow_NeptuneRow);
        mPlutoRow = findViewById(R.id.RightNow_PlutoRow);

        mSunMeasure = (TextView)findViewById(R.id.RightNow_SunMeasure);
        mMoonMeasure = (TextView)findViewById(R.id.RightNow_MoonMeasure);
        mMercuryMeasure = (TextView)findViewById(R.id.RightNow_MercuryMeasure);
        mVenusMeasure = (TextView)findViewById(R.id.RightNow_VenusMeasure);
        mMarsMeasure = (TextView)findViewById(R.id.RightNow_MarsMeasure);
        mJupiterMeasure = (TextView)findViewById(R.id.RightNow_JupiterMeasure);
        mSaturnMeasure = (TextView)findViewById(R.id.RightNow_SaturnMeasure);
        mUranusMeasure = (TextView)findViewById(R.id.RightNow_UranusMeasure);
        mNeptuneMeasure = (TextView)findViewById(R.id.RightNow_NeptuneMeasure);
        mPlutoMeasure = (TextView)findViewById(R.id.RightNow_PlutoMeasure);
        
        mMoonPhase = (TextView)findViewById(R.id.RightNow_MoonPhase);
        mMoonVOC = (TextView)findViewById(R.id.RightNow_MoonVOC);
    }


    @Override
    public boolean hasReceivers() {
        return true;
    }

    @Override
    public List<ReceiverFilterPair> getReceivers() {
        return backingStore;
    }

    @Override
    public void setServiceReference(AeonDroidService aeonDroidService) {
        mAeonDroidService = aeonDroidService;
    }

    private class HighlightReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int planetType = intent.getIntExtra(Events.EXTRA_HOUR_TYPE, -1);
            if (mSunRow == null) {
                return;
            }
            if (lastView != null) {
                lastView.setBackgroundResource(0);
            }
            View planetRow = getPlanetRow(planetType);
            lastView = planetRow;
            if (getContext() != null && lastView != null) {
                TypedValue tv = new TypedValue();
                getContext().getTheme().resolveAttribute(R.attr.PlanetaryHours_Current, tv, true);
                lastView.setBackgroundColor(tv.data);
            }
        }
    }

    private View getPlanetRow(int planetType) {
        switch (planetType) {
            case 0:
                return mSunRow;
            case 1:
                return mMoonRow;
            case 2:
                return mMercuryRow;
            case 3:
                return mVenusRow;
            case 4:
                return mSaturnRow;
            case 5:
                return mJupiterRow;
            case 6:
                return mMarsRow;
            default:
                return null;
        }
    }

    private TextView getPlanetMeasure(int planetType) {
        switch (planetType) {
            case 0:
                return mSunMeasure;
            case 1:
                return mMoonMeasure;
            case 2:
                return mMercuryMeasure;
            case 3:
                return mVenusMeasure;
            case 4:
                return mMarsMeasure;
            case 5:
                return mJupiterMeasure;
            case 6:
                return mSaturnMeasure;
            case 7:
                return mUranusMeasure;
            case 8:
                return mNeptuneMeasure;
            case 9:
                return mPlutoMeasure;
            default:
                return null;
        }
    }

    private class PhaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MoonPhase.PhaseType pt = PhaseUtils.phaseFromInt(intent.getIntExtra(Events.EXTRA_LPHASE_TYPE, -1));
            boolean waxing = intent.getBooleanExtra(Events.EXTRA_LPHASE_WAXING, false);
            if (pt != null && mMoonPhase != null) {
                String phaseString = PhaseUtils.getPhaseString(context, pt, waxing);
                mMoonPhase.setText(phaseString);
            }
        }
    }

    private class MeasureReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            double[] data = intent.getDoubleArrayExtra(Events.EXTRA_PLANET_POS);
            if (data != null) {
                int count = data.length;
                for (int i = 0; i<count; i++) {
                    String measureStr = EphemerisUtils.degreesToSignString(context, data[i]);
                    TextView tv = getPlanetMeasure(i);
                    if (tv != null) {
                        tv.setText(measureStr);
                    }
                }
            }
        }
    }

    private class VoCReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mMoonVOC == null) {
                return;
            }
            boolean isVoC = intent.getBooleanExtra(Events.EXTRA_VOC_IN, false);
            if (isVoC) {
                String fmt = context.getString(R.string.Moon_CurrentlyVOC);

                Date d = new Date(intent.getLongExtra(Events.EXTRA_VOC_TO_DATE, 0));
                String strDate = EphemerisUtils.DATETIME_FMT.format(d);

                //int sign = intent.getIntExtra(Events.EXTRA_VOC_TO, 0);
                //String strSign = context.getResources().getStringArray(R.array.SignNames)[sign];

                mMoonVOC.setText(String.format(fmt, strDate));
            }
            else {
                String fmt = context.getString(R.string.Moon_PendingVOC);

                Date d = new Date(intent.getLongExtra(Events.EXTRA_VOC_FROM_DATE, 0));
                String strDate = EphemerisUtils.DATETIME_FMT.format(d);

                //int sign = intent.getIntExtra(Events.EXTRA_VOC_FROM, 0);
                //String strSign = context.getResources().getStringArray(R.array.SignNames)[sign];

                mMoonVOC.setText(String.format(fmt, strDate));
            }
        }
    }
}
