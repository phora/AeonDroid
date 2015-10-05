package io.github.phora.aeondroid.widgets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.github.phora.aeondroid.calculations.Ephemeris;
import io.github.phora.aeondroid.model.SunsetSunriseInfo;
import io.github.phora.aeondroid.workers.AeonDroidService;
import io.github.phora.aeondroid.Events;
import io.github.phora.aeondroid.model.PlanetaryHour;
import io.github.phora.aeondroid.model.adapters.PlanetaryHoursAdapter;
import io.github.phora.aeondroid.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlanetaryHoursView extends LinearLayout implements BroadcastReceivable, AeonDroidServiceable {

    private AeonDroidService mAeonDroidService;
    private boolean _autoScrolledOnce = false;
    private PlanetaryHoursAdapter pha;
    private Date timeStamp = null;

    private List<ReceiverFilterPair> backingStore;

    private boolean _styleLateSet;
    private boolean _delayedRefresh;
    private int _delayedHourStyle;
    private ListView mListView;
    private View mEmpty;

    public PlanetaryHoursView(Context context, AeonDroidService aeonDroidService, Date date) {
        super(context);
        View.inflate(context, R.layout.phours_view, this);
        onFinishInflate();

        backingStore = new LinkedList<>();

        IntentFilter filterHighlight = new IntentFilter(Events.FOUND_HOUR);
        IntentFilter filterRefresh = new IntentFilter(Events.REFRESH_HOURS);

        HighlightReceiver highlightReceiver = new HighlightReceiver();
        RefreshReceiver refreshReceiver = new RefreshReceiver();

        backingStore.add(new ReceiverFilterPair(refreshReceiver, filterRefresh));
        backingStore.add(new ReceiverFilterPair(highlightReceiver, filterHighlight));

        timeStamp = date;
        mAeonDroidService = aeonDroidService;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mListView = (ListView) findViewById(android.R.id.list);
        mEmpty = findViewById(android.R.id.empty);
        mListView.setEmptyView(mEmpty);
        reTheme();
    }

    private void reTheme() {
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

    private void refreshContents() {
        Context context = getContext();
        if (timeStamp != null) {
            ArrayList<PlanetaryHour> phl = Ephemeris.getDefaultEphemeris(context).getPlanetaryHours(timeStamp);
            pha = new PlanetaryHoursAdapter(context, phl);
        }
        else {
            SunsetSunriseInfo ssi = mAeonDroidService.getSunsetSunriseInfo();
            ArrayList<PlanetaryHour> phl = Ephemeris.getDefaultEphemeris(context).getPlanetaryHours(ssi);
            pha = new PlanetaryHoursAdapter(context, phl);
        }
        mListView.setAdapter(pha);
        if (_styleLateSet) {
            _styleLateSet = false;
            pha.setHourStyle(_delayedHourStyle);
        }
    }

    @Override
    public boolean hasReceivers() {
        return backingStore != null;
    }

    @Override
    public List<ReceiverFilterPair> getReceivers() {
        return backingStore;
    }

    @Override
    public void setServiceReference(AeonDroidService aeonDroidService) {
        mAeonDroidService = aeonDroidService;
    }

    private class HighlightReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("PHView", "Received highlight update");
            int selectedRow = intent.getIntExtra(Events.EXTRA_HOUR_INDEX, -1);
            if (_delayedRefresh && mAeonDroidService != null && mListView != null) {
                refreshContents();
                _delayedRefresh = false;
            }
            if (pha != null) {
                pha.setHourSelection(selectedRow);
                if (_styleLateSet) {
                    _styleLateSet = false;
                    pha.setHourStyle(_delayedHourStyle);
                }
                if (!_autoScrolledOnce && mListView != null) {
                    try {
                        Log.d("PHView", "Attempting to scroll to "+selectedRow);
                        mListView.setSelectionFromTop(selectedRow, mListView.getHeight() / 2);
                        _autoScrolledOnce = true;
                        Log.d("PHView", "Scrolling success");
                    } catch (IllegalStateException e) {
                        Log.d("PHView", "Don't scroll yet, the view's not ready");
                    }
                }
            }
        }
    }

    private class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            _autoScrolledOnce = false;
            Log.d("PHView", "Received refresh update");

            //Intent peekIntent = new Intent(context, AeonDroidService.class);
            //AeonDroidService.AeonDroidBinder adb = (AeonDroidService.AeonDroidBinder)peekService(context, peekIntent);

            if (mAeonDroidService != null && mListView != null) {
                Log.d("PHView", "Refreshing hours succeeds!");
                refreshContents();
            } else {
                Log.d("PHView", "Can't get hours, binder to service is null");
                _delayedRefresh = true;
            }
        }
    }
}