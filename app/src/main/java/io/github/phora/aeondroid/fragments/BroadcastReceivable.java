package io.github.phora.aeondroid.fragments;

import java.util.List;

import io.github.phora.aeondroid.AeonDroidService;

/**
 * Created by phora on 9/16/15.
 */
public interface BroadcastReceivable {
    boolean hasReceivers();
    List<ReceiverFilterPair> getReceivers();
}
