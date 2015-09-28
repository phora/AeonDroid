package io.github.phora.aeondroid.widgets;

import java.util.List;

/**
 * Created by phora on 9/16/15.
 */
public interface BroadcastReceivable {
    boolean hasReceivers();
    List<ReceiverFilterPair> getReceivers();
}
