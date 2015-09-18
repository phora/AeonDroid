package io.github.phora.aeondroid.model;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import io.github.phora.aeondroid.EphemerisUtils;
import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.drawables.PlanetIndicator;
import swisseph.SweDate;

/**
 * Created by phora on 9/9/15.
 */
public class PlanetaryHoursAdapter extends BaseAdapter {

    private int hourSelection = -1;
    private int hourStyle = 0;

    private Context mContext;
    private ArrayList<PlanetaryHour> mPhours;
    private PlanetIndicator pi;

    public PlanetaryHoursAdapter(Context context, ArrayList<PlanetaryHour> phours) {
        super();
        mContext = context;
        mPhours = phours;
        pi = PlanetIndicator.getInstance(mContext);
    }

    @Override
    public int getCount() {
        return mPhours.size();
    }

    @Override
    public PlanetaryHour getItem(int i) {
        return mPhours.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PlanetaryHour ph = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.phours_item, parent, false);
        }

        ImageView pHoursIcon = (ImageView)convertView.findViewById(R.id.PlanetaryHours_Icon);
        TextView pHoursName = (TextView)convertView.findViewById(R.id.PlanetaryHours_Name);
        TextView pHoursTime = (TextView)convertView.findViewById(R.id.PlanetaryHours_Time);
        TextView pHoursTimeEnd = (TextView)convertView.findViewById(R.id.PlanetaryHours_TimeEnd);
        View pDayStripe = convertView.findViewById(R.id.PlanetaryHours_DayStripe);

        String[] planets = mContext.getResources().getStringArray(R.array.PlanetNames);
        Date d = SweDate.getDate(ph.getHourStamp());
        Date ed = SweDate.getDate(ph.getHourStamp()+ph.getHourLength());

        if (hourSelection == position) {
            TypedValue typedValue = new TypedValue();
            mContext.getTheme().resolveAttribute(R.attr.PlanetaryHours_Current, typedValue, true);
            if (typedValue.type == TypedValue.TYPE_REFERENCE) {
                convertView.setBackgroundResource(typedValue.resourceId);
            } else {
                convertView.setBackgroundColor(typedValue.data);
            }
        }
        else {
            convertView.setBackgroundResource(0);
        }

        String starts_fmt = mContext.getString(R.string.PHoursAdapter_StartsAt);
        String ends_fmt = mContext.getString(R.string.PHoursAdapter_EndsAt);

        pHoursTime.setText(String.format(starts_fmt, EphemerisUtils.DATE_FMT.format(d)));
        pHoursTimeEnd.setText(String.format(ends_fmt, EphemerisUtils.DATE_FMT.format(ed)));
        pHoursName.setText(planets[ph.getPlanetType()]);
        //Log.d("PlanetaryHoursAdapter", "Is night?: " + ph.isNight());
        if (!ph.isNight()) {
            pDayStripe.setVisibility(View.INVISIBLE);
        }
        else {
            pDayStripe.setVisibility(View.VISIBLE);
        }
        //Log.d("PlanetaryHoursAdapter", "Drawable set: "+ph.getPlanetType());

        if (hourStyle == 0) {
            pHoursIcon.setImageDrawable(pi.getChakraDrawable(ph.getPlanetType()));
        }
        else if (hourStyle == 1) {
            pHoursIcon.setImageResource(pi.getPlanetSymbol(ph.getPlanetType()));
        }

        return convertView;
    }

    public void setHourSelection(int hourSelection) {
        boolean call_invalidate = (this.hourSelection != hourSelection);
        this.hourSelection = hourSelection;
        if (call_invalidate) {
            notifyDataSetChanged();
        }
    }

    public int getHourSelection() {
        return hourSelection;
    }

    public int getHourStyle() {
        return hourStyle;
    }

    public void setHourStyle(int hourStyle) {
        boolean call_invalidate = (this.hourStyle != hourStyle);
        this.hourStyle = hourStyle;
        if (call_invalidate) {
            notifyDataSetChanged();
        }
    }
}
