package io.github.phora.aeondroid.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.github.phora.aeondroid.AeonDroidService;
import io.github.phora.aeondroid.Events;
import io.github.phora.aeondroid.model.PlanetaryHour;
import io.github.phora.aeondroid.model.PlanetaryHoursAdapter;
import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.activities.MainActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlanetaryHoursFragment extends ListFragment implements BroadcastReceivable {

    private boolean _autoScrolledOnce = false;
    private PlanetaryHoursAdapter pha;

    private List<ReceiverFilterPair> backingStore;

    private boolean _styleLateSet;
    private int _delayedHourStyle;

    public PlanetaryHoursFragment() {
        backingStore = new LinkedList<>();

        IntentFilter filterHighlight = new IntentFilter(Events.FOUND_HOUR);
        IntentFilter filterRefresh = new IntentFilter(Events.REFRESH_HOURS);

        HighlightReceiver highlightReceiver = new HighlightReceiver();
        RefreshReceiver refreshReceiver = new RefreshReceiver();

        backingStore.add(new ReceiverFilterPair(refreshReceiver, filterRefresh));
        backingStore.add(new ReceiverFilterPair(highlightReceiver, filterHighlight));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_phours, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            _autoScrolledOnce = false;
            Context app = getContext().getApplicationContext();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
            int hoursStyle = Integer.valueOf(preferences.getString("phoursIndicator", "0"));
            if (pha != null) {
                pha.setHourStyle(hoursStyle);
            }
            else {
                _styleLateSet = true;
                _delayedHourStyle = hoursStyle;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public static Fragment newInstance() {
        Fragment fragment = new PlanetaryHoursFragment();
        return fragment;
    }

    @Override
    public boolean hasReceivers() {
        return backingStore != null;
    }

    @Override
    public List<ReceiverFilterPair> getReceivers() {
        return backingStore;
    }

    private class HighlightReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("PHFragment", "Received highlight update");
            int selected_row = intent.getIntExtra(Events.EXTRA_HOUR_INDEX, -1);
            if (!_autoScrolledOnce) {
                try {
                    Log.d("PHFragment", "Attempting to scroll to "+selected_row);
                    getListView().setSelectionFromTop(selected_row, getListView().getHeight() / 2);
                    _autoScrolledOnce = true;
                    Log.d("PHFragment", "Scrolling success");
                } catch (IllegalStateException e) {
                    Log.d("PHFragment", "Don't scroll yet, the view's not ready");
                }
            }
            if (pha != null) {
                pha.setHourSelection(selected_row);
                if (_styleLateSet) {
                    _styleLateSet = false;
                    pha.setHourStyle(_delayedHourStyle);
                }
            }
        }
    }

    private class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            _autoScrolledOnce = false;
            Log.d("PHFragment", "Received refresh update");

            Intent peekIntent = new Intent(context, AeonDroidService.class);
            AeonDroidService.AeonDroidBinder adb = (AeonDroidService.AeonDroidBinder)peekService(context, peekIntent);

            if (adb != null && getActivity() != null) {
                ArrayList<PlanetaryHour> phl = adb.getService().getPlanetaryHours();
                pha = new PlanetaryHoursAdapter(getActivity(), phl);
                setListAdapter(pha);
                if (_styleLateSet) {
                    _styleLateSet = false;
                    pha.setHourStyle(_delayedHourStyle);
                }
            } else {
                Log.d("PHFragment", "Can't get hours, binder to service is null");
            }
        }
    }
}