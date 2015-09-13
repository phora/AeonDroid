package io.github.phora.aeondroid.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import io.github.phora.aeondroid.Events;
import io.github.phora.aeondroid.model.PlanetaryHour;
import io.github.phora.aeondroid.model.PlanetaryHoursAdapter;
import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.activities.MainActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends ListFragment {

    private boolean _autoScrolledOnce;
    private HighlightReceiver highlightReceiver;
    private RefreshReceiver refreshReceiver;

    private IntentFilter filterHighlight;
    private IntentFilter filterRefresh;

    public MainActivityFragment() {
        filterHighlight = new IntentFilter(Events.FOUND_HOUR);
        filterRefresh = new IntentFilter(Events.REFRESH_HOURS);

        highlightReceiver = new HighlightReceiver();
        refreshReceiver = new RefreshReceiver();
    }

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            getActivity().unregisterReceiver(highlightReceiver);
            getActivity().unregisterReceiver(refreshReceiver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().registerReceiver(highlightReceiver, filterHighlight);
            getActivity().registerReceiver(refreshReceiver, filterRefresh);
        }
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