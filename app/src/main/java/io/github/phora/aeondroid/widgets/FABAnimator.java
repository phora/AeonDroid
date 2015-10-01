package io.github.phora.aeondroid.widgets;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import io.github.phora.aeondroid.R;

/**
 * Created by phora on 9/25/15.
 */
public class FABAnimator implements AbsListView.OnScrollListener {
    private int mPosition;
    private int mOffset;
    private Animation fabDisappear;
    private Animation fabAppear;
    private View mFAB;

    public FABAnimator(Context context, View fab) {
        fabDisappear = AnimationUtils.loadAnimation(context, R.anim.fab_disappear);
        fabAppear = AnimationUtils.loadAnimation(context, R.anim.fab_appear);
        mFAB = fab;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            return;
        }
        int position = absListView.getFirstVisiblePosition();
        View v = absListView.getChildAt(0);
        int offset = (v == null) ? 0 : v.getTop();


        if (mPosition < position || (mPosition == position && mOffset > offset)) {
            if (mFAB.getVisibility() != View.GONE) {
                mFAB.startAnimation(fabDisappear);
                mFAB.setVisibility(View.GONE);
            }
            // Scrolled down?
        } else {
            if (mFAB.getVisibility() != View.VISIBLE) {
                mFAB.setVisibility(View.VISIBLE);
                mFAB.startAnimation(fabAppear);
            }
            // Scrolled up
        }
        mPosition = position;
        mOffset = offset;
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItems) {

    }
}
