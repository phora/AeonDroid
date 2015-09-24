package io.github.phora.aeondroid.model.adapters;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.drawables.PlanetIndicator;
import io.github.phora.aeondroid.model.AspectConfig;

/**
 * Created by ${CUSER} on 9/23/15.
 */
public class OrbsAdapter extends BaseAdapter {
    private final Context mContext;
    private SparseArray<AspectConfig> mOrbConfig;
    private CompoundButton.OnCheckedChangeListener mCheckListener;

    public OrbsAdapter(Context context, SparseArray<AspectConfig> orbConfig,
                       CompoundButton.OnCheckedChangeListener checkListener) {
        mContext = context;
        mOrbConfig = orbConfig;
        mCheckListener = checkListener;
    }

    public SparseArray<AspectConfig> getOrbConfig() {
        return mOrbConfig;
    }

    @Override
    public int getCount() {
        return mOrbConfig.size();
    }

    @Override
    public Integer getItem(int i) {
        return mOrbConfig.keyAt(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.orb_item, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.Orb_Indicator);
        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.Orb_Visible);
        TextView nameView = (TextView) convertView.findViewById(R.id.Orb_Name);
        TextView textView = (TextView) convertView.findViewById(R.id.Orb_Value);

        imageView.setImageResource(PlanetIndicator.getInstance(mContext).getAspectSymbol(position));
        checkBox.setChecked(mOrbConfig.valueAt(position).isShown());
        checkBox.setOnCheckedChangeListener(mCheckListener);
        nameView.setText(AspectConfig.ASPECT_NAMES[position]);
        textView.setText(String.valueOf(mOrbConfig.valueAt(position).getOrb()));

        convertView.setTag(position);
        checkBox.setTag(position);

        return convertView;
    }
}
