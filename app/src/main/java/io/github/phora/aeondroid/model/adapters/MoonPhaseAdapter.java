package io.github.phora.aeondroid.model.adapters;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.phora.aeondroid.calculations.EphemerisUtils;
import io.github.phora.aeondroid.PhaseUtils;
import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.model.MoonPhase;

/**
 * Created by phora on 9/13/15.
 */
public class MoonPhaseAdapter extends BaseAdapter {

    private final Context mContext;
    private final ArrayList<MoonPhase> mPhases;
    private int phaseSelection = -1;

    public MoonPhaseAdapter(Context context, ArrayList<MoonPhase> phases) {
        super();
        mContext = context;
        mPhases = phases;
    }

    @Override
    public int getCount() {
        return mPhases.size();
    }

    @Override
    public MoonPhase getItem(int i) {
        return mPhases.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MoonPhase mp = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.moonphase_item, parent, false);
        }

        TextView stampView = (TextView)convertView.findViewById(R.id.MoonPhase_Time);
        TextView phaseView = (TextView)convertView.findViewById(R.id.MoonPhase_PhaseString);
        ImageView phaseImage = (ImageView)convertView.findViewById(R.id.MoonPhase_Image);

        stampView.setText(EphemerisUtils.DATE_FMT.format(mp.getTimeStamp()));
        phaseView.setText(PhaseUtils.getPhaseString(mContext, mp));
        phaseImage.setImageResource(PhaseUtils.getPhaseImage(mp));

        if (phaseSelection == position) {
            TypedValue typedValue = new TypedValue();
            mContext.getTheme().resolveAttribute(R.attr.PlanetaryHours_Current, typedValue, false);
            if (typedValue.type == TypedValue.TYPE_REFERENCE) {
                convertView.setBackgroundResource(typedValue.resourceId);
            } else {
                convertView.setBackgroundColor(typedValue.data);
            }
        }
        else {
            convertView.setBackgroundResource(0);
        }

        return convertView;
    }

    public int getPhaseSelection() {
        return phaseSelection;
    }

    public void setPhaseSelection(int hourSelection) {
        boolean callInvalidate = (this.phaseSelection != hourSelection);
        this.phaseSelection = hourSelection;
        if (callInvalidate) {
            notifyDataSetChanged();
        }
    }
}
