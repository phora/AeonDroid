package io.github.phora.aeondroid.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.phora.aeondroid.EphemerisUtils;
import io.github.phora.aeondroid.R;

/**
 * Created by phora on 9/13/15.
 */
public class MoonPhaseAdapter extends ArrayAdapter<MoonPhase> {
    
    public MoonPhaseAdapter(Context context, ArrayList<MoonPhase> phases) {
        super(context, 0, phases);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MoonPhase mp = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.moonphase_item, parent, false);
        }

        TextView stampView = (TextView)convertView.findViewById(R.id.MoonPhase_Time);
        TextView phaseView = (TextView)convertView.findViewById(R.id.MoonPhase_PhaseString);

        stampView.setText(EphemerisUtils.DATE_FMT.format(mp.getTimeStamp()));
        phaseView.setText(getPhaseString(mp));

        return convertView;
    }

    private String getPhaseString(MoonPhase mp) {
        Context context = getContext();
        String fmt = context.getString(R.string.waxwanefmt);

        if (mp.getPhaseType() == MoonPhase.PhaseType.NEW) {
            return context.getString(R.string.new_moon);
        }
        else if (mp.getPhaseType() == MoonPhase.PhaseType.FULL) {
            return context.getString(R.string.full_moon);
        }
        else if (mp.getPhaseType() == MoonPhase.PhaseType.QUARTER) {
            if (mp.isWaxing()) {
                return String.format(fmt, context.getString(R.string.first_quarter),
                        context.getString(R.string.quarter_sfx));
            }
            else {
                return String.format(fmt, context.getString(R.string.last_quarter),
                        context.getString(R.string.quarter_sfx));
            }
        }
        else {
            String part;
            if (mp.getPhaseType() == MoonPhase.PhaseType.CRESCENT) {
                part = context.getString(R.string.crescent_moon);
            }
            else {
                part = context.getString(R.string.gibbous_moon);
            }

            if (mp.isWaxing()) {
                return String.format(fmt, context.getString(R.string.waxing_moon), part);
            }
            else {
                return String.format(fmt, context.getString(R.string.waning_moon), part);
            }
        }
    }
}
