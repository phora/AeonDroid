package io.github.phora.aeondroid.model;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.drawables.PlanetIndicator;

/**
 * Created by phora on 9/21/15.
 */
public class AspectAdapter extends BaseAdapter {

    private Context mContext;
    private SparseArray<AspectConfig> mOrbConfig;
    private List<AspectEntry> mAspects;

    public AspectAdapter(Context context, SparseArray<AspectConfig> orbConfig, List<AspectEntry> aspects) {
        super();
        mContext = context;
        mOrbConfig = orbConfig;
        mAspects = aspects;
    }

    @Override
    public int getCount() {
        return mAspects.size();
    }

    @Override
    public AspectEntry getItem(int i) {
        return mAspects.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AspectEntry ae = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.aspect_item, parent, false);
        }

        ImageView img = (ImageView)convertView.findViewById(R.id.AspectItem_Indicator);

        if (position == 0) {
            img.setImageResource(0);
            return convertView;
        }
        else {
            boolean isHeader = ae.isHeader();
            if (isHeader) {
                boolean useFrom = position % 11 == 0;
                int res;
                if (useFrom) {
                    res = PlanetIndicator.getInstance(mContext).getPlanetChartSymbol(ae.getFromPlanetType());
                }
                else {
                    res = PlanetIndicator.getInstance(mContext).getPlanetChartSymbol(ae.getToPlanetType());
                }
                img.setImageResource(res);
            }
            else {
                double aspectDist = ae.getAspectDist();
                int aspectPos = AspectConfig.getClosestAspect(aspectDist, mOrbConfig, true);
                double closestAspect = mOrbConfig.keyAt(aspectPos);
                AspectConfig aspectConfig = mOrbConfig.valueAt(aspectPos);
                boolean met_reqs = aspectConfig.isShown() && (Math.abs(closestAspect-aspectDist) <= aspectConfig.getOrb());
                int res = 0;
                if (met_reqs) {
                    res = PlanetIndicator.getInstance(mContext).getAspectSymbol(aspectPos);
                }
                img.setImageResource(res);
            }
        }
        return convertView;
    }
}
