package io.github.phora.aeondroid.widgets;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Date;

import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.workers.AeonDroidService;

/**
 * Created by phora on 9/29/15.
 */
public class MainTabsAdapter extends PagerAdapter {

    private Context mContext;
    private ArrayList<View> pages;
    private Date mDate;

    public MainTabsAdapter(Context context, AeonDroidService aeonDroidService) {
        mContext = context;
        mDate = null;
        pages = new ArrayList<View>();
        pages.add(new PlanetaryHoursView(context, aeonDroidService, mDate));
        pages.add(new MoonPhaseView(context, aeonDroidService, mDate));
        pages.add(new RightNowView(context, aeonDroidService, mDate));
        pages.add(new AspectsView(context, aeonDroidService, mDate));
    }

    public MainTabsAdapter(Context context, AeonDroidService aeonDroidService, Date date) {
        mContext = context;
        mDate = date;
        pages = new ArrayList<View>();
        pages.add(new PlanetaryHoursView(context, aeonDroidService, mDate));
        pages.add(new MoonPhaseView(context, aeonDroidService, mDate));
        pages.add(new RightNowView(context, aeonDroidService, mDate));
        pages.add(new AspectsView(context, aeonDroidService, mDate));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = pages.get(position);
        container.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(pages.get(position));
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.PanelTitle_PlanetaryHours);
            case 1:
                return mContext.getString(R.string.PanelTitle_MoonPhases);
            case 2:
                return mContext.getString(R.string.PanelTitle_RightNow);
            case 3:
                return mContext.getString(R.string.PanelTitle_Aspects);
            default:
                return null;
        }
    }

    public View getItem(int i) {
        return pages.get(i);
    }

    public void setServiceReference(AeonDroidService serviceReference) {
        for (View v: pages) {
            try {
                ((AeonDroidServiceable)v).setServiceReference(serviceReference);
            } catch (ClassCastException e) {
                continue;
            }
        }
    }
}
