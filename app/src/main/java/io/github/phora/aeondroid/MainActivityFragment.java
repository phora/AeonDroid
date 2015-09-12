package io.github.phora.aeondroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import swisseph.SweDate;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends ListFragment {

    private boolean _autoScrolledOnce;

    public MainActivityFragment() {
    }

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity ma = (MainActivity)getActivity();
        IntentFilter filter_highlight = new IntentFilter(Events.FOUND_HOUR);
        IntentFilter filter_refresh = new IntentFilter(Events.REFRESH_HOURS);

        getActivity().registerReceiver(new HighlightReceiver(), filter_highlight);
        getActivity().registerReceiver(new RefreshReceiver(),   filter_refresh);
    }

    public synchronized void refreshFragment() {
        MainActivity ma = (MainActivity)getActivity();
        if (ma != null && ma.getServiceReference() != null) {
            ArrayList<PlanetaryHour> phl = ma.getServiceReference().getPlanetaryHours();
            PlanetaryHoursAdapter pha = new PlanetaryHoursAdapter(ma, phl);
            setListAdapter(pha);
        }
    }

    private class HighlightReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int selected_row = intent.getIntExtra(Events.EXTRA_HOUR_INDEX, -1);
            if (!_autoScrolledOnce) {
                getListView().setSelection(selected_row);
                _autoScrolledOnce = true;
            }
            PlanetaryHoursAdapter pha = (PlanetaryHoursAdapter)getListAdapter();
            pha.setHourSelection(selected_row);
        }
    }

    private class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MainActivity ma = (MainActivity)getActivity();
            ArrayList<PlanetaryHour> phl = ma.getServiceReference().getPlanetaryHours();
            PlanetaryHoursAdapter pha = new PlanetaryHoursAdapter(getActivity(), phl);
            setListAdapter(pha);
        }
    }
}