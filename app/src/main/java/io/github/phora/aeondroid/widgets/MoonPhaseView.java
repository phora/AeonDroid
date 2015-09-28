package io.github.phora.aeondroid.widgets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.github.phora.aeondroid.model.MoonPhase;
import io.github.phora.aeondroid.workers.AeonDroidService;
import io.github.phora.aeondroid.Events;
import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.model.adapters.MoonPhaseAdapter;

public class MoonPhaseView extends LinearLayout implements BroadcastReceivable, AeonDroidServiceable {

    private AeonDroidService mAeonDroidService;
    private List<ReceiverFilterPair> backingStore;
    private boolean _delayedRefresh;

    private Date timeStamp = null;

    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private MoonPhaseAdapter mAdapter;
    private View mEmptyView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MoonPhaseView(Context context, AeonDroidService aeonDroidService, Date date) {
        super(context);
        View.inflate(context, R.layout.fragment_moonphase, this);
        onFinishInflate();

        backingStore = new LinkedList<>();
        IntentFilter filterRefresh = new IntentFilter(Events.REFRESH_MOON_PHASE);
        IntentFilter filterHighlight = new IntentFilter(Events.FOUND_PHASE);
        RefreshReceiver refreshReceiver = new RefreshReceiver();
        HighlightReceiver highlightReceiver = new HighlightReceiver();
        backingStore.add(new ReceiverFilterPair(refreshReceiver, filterRefresh));
        backingStore.add(new ReceiverFilterPair(highlightReceiver, filterHighlight));

        timeStamp = date;
        mAeonDroidService = aeonDroidService;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mListView = (ListView) findViewById(android.R.id.list);
        mEmptyView = findViewById(android.R.id.empty);
        mListView.setEmptyView(mEmptyView);
    }

    private void refreshContents() {
        if (timeStamp != null) {
            ArrayList<MoonPhase> mpl = mAeonDroidService.getEphemeris().getMoonCycle(timeStamp);
            mAdapter = new MoonPhaseAdapter(getContext(), mpl);
        }
        else {
            ArrayList<MoonPhase> mpl = mAeonDroidService.getMoonPhases();
            mAdapter = new MoonPhaseAdapter(getContext(), mpl);
        }
        mListView.setAdapter(mAdapter);
    }

    @Override
    public boolean hasReceivers() {
        return true;
    }

    @Override
    public List<ReceiverFilterPair> getReceivers() {
        return backingStore;
    }

    @Override
    public void setServiceReference(AeonDroidService aeonDroidService) {
        mAeonDroidService = aeonDroidService;
    }

    private class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Intent peekIntent = new Intent(context, AeonDroidService.class);
            //AeonDroidService.AeonDroidBinder adb = (AeonDroidService.AeonDroidBinder)peekService(context, peekIntent);

            if (mAeonDroidService != null && mListView != null) {
                Log.d("MPFragment", "Refreshing phases succeeds!");
                refreshContents();
            }
            else {
                Log.d("MPFragment", "Can't get phases, binder to service is null");
                _delayedRefresh = true;
            }
        }
    }

    private class HighlightReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int index = intent.getIntExtra(Events.EXTRA_LPHASE_INDEX, -1);
            if (_delayedRefresh && mAeonDroidService != null && mListView != null) {
                refreshContents();
                _delayedRefresh = false;
            }
            if (mAdapter != null) {
                mAdapter.setPhaseSelection(index);
            }
        }
    }
}
