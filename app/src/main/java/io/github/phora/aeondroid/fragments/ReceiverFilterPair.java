package io.github.phora.aeondroid.fragments;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by phora on 9/16/15.
 */
public class ReceiverFilterPair {
    BroadcastReceiver receiver;
    IntentFilter filter;

    public ReceiverFilterPair(BroadcastReceiver receiver, IntentFilter filter) {
        this.receiver = receiver;
        this.filter = filter;
    }

    public void register(LocalBroadcastManager bm) {
        bm.registerReceiver(receiver, filter);
    }

    public void unregister(LocalBroadcastManager bm) {
        bm.unregisterReceiver(receiver);
    }
}
