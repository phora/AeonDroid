package io.github.phora.aeondroid.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.github.phora.aeondroid.calculations.EphemerisUtils;
import io.github.phora.aeondroid.Events;
import io.github.phora.aeondroid.PhaseUtils;
import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.model.MoonPhase;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RightNowFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RightNowFragment extends Fragment implements BroadcastReceivable {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RightNow.
     */
    // TODO: Rename and change types and number of parameters
    public static RightNowFragment newInstance(String param1, String param2) {
        RightNowFragment fragment = new RightNowFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public RightNowFragment() {
        // Required empty public constructor
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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_right_now, container, false);
        mSunRow = v.findViewById(R.id.RightNow_SunRow);
        mMoonRow = v.findViewById(R.id.RightNow_MoonRow);
        mMercuryRow = v.findViewById(R.id.RightNow_MercuryRow);
        mVenusRow = v.findViewById(R.id.RightNow_VenusRow);
        mMarsRow = v.findViewById(R.id.RightNow_MarsRow);
        mJupiterRow = v.findViewById(R.id.RightNow_JupiterRow);
        mSaturnRow = v.findViewById(R.id.RightNow_SaturnRow);
        mUranusRow = v.findViewById(R.id.RightNow_UranusRow);
        mNeptuneRow = v.findViewById(R.id.RightNow_NeptuneRow);
        mPlutoRow = v.findViewById(R.id.RightNow_PlutoRow);

        mSunMeasure = (TextView)v.findViewById(R.id.RightNow_SunMeasure);
        mMoonMeasure = (TextView)v.findViewById(R.id.RightNow_MoonMeasure);
        mMercuryMeasure = (TextView)v.findViewById(R.id.RightNow_MercuryMeasure);
        mVenusMeasure = (TextView)v.findViewById(R.id.RightNow_VenusMeasure);
        mMarsMeasure = (TextView)v.findViewById(R.id.RightNow_MarsMeasure);
        mJupiterMeasure = (TextView)v.findViewById(R.id.RightNow_JupiterMeasure);
        mSaturnMeasure = (TextView)v.findViewById(R.id.RightNow_SaturnMeasure);
        mUranusMeasure = (TextView)v.findViewById(R.id.RightNow_UranusMeasure);
        mNeptuneMeasure = (TextView)v.findViewById(R.id.RightNow_NeptuneMeasure);
        mPlutoMeasure = (TextView)v.findViewById(R.id.RightNow_PlutoMeasure);
        
        mMoonPhase = (TextView)v.findViewById(R.id.RightNow_MoonPhase);
        mMoonVOC = (TextView)v.findViewById(R.id.RightNow_MoonVOC);
        return v;
    }


    @Override
    public boolean hasReceivers() {
        return true;
    }

    @Override
    public List<ReceiverFilterPair> getReceivers() {
        return backingStore;
    }

    private class HighlightReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int planetType = intent.getIntExtra(Events.EXTRA_HOUR_TYPE, -1);
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
                return mVenusRow;
            case 2:
                return mMercuryRow;
            case 3:
                return mMoonRow;
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
                return mMercuryMeasure;
            case 2:
                return mVenusMeasure;
            case 3:
                return mMoonMeasure;
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
                String strDate = EphemerisUtils.DATE_FMT.format(d);

                //int sign = intent.getIntExtra(Events.EXTRA_VOC_TO, 0);
                //String strSign = context.getResources().getStringArray(R.array.SignNames)[sign];

                mMoonVOC.setText(String.format(fmt, strDate));
            }
            else {
                String fmt = context.getString(R.string.Moon_PendingVOC);

                Date d = new Date(intent.getLongExtra(Events.EXTRA_VOC_FROM_DATE, 0));
                String strDate = EphemerisUtils.DATE_FMT.format(d);

                //int sign = intent.getIntExtra(Events.EXTRA_VOC_FROM, 0);
                //String strSign = context.getResources().getStringArray(R.array.SignNames)[sign];

                mMoonVOC.setText(String.format(fmt, strDate));
            }
        }
    }
}
