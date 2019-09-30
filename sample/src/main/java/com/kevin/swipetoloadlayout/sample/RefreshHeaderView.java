package com.kevin.swipetoloadlayout.sample;

import android.content.Context;
import android.util.AttributeSet;

import com.kevin.swipetoloadlayout.SwipeRefreshTrigger;
import com.kevin.swipetoloadlayout.SwipeTrigger;
import com.kevin.swipetoloadlayout.sample.widget.TwoBallRotationProgressBar;


/**
 * RefreshHeaderView
 *
 * @author zwenkai@foxmail.com, Created on 2019-09-27 16:58:15
 * Major Function：<b></b>
 * <p/>
 * Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
public class RefreshHeaderView extends TwoBallRotationProgressBar implements SwipeRefreshTrigger, SwipeTrigger {

    public RefreshHeaderView(Context context) {
        super(context);
    }

    public RefreshHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onRefresh() {
    }

    @Override
    public void onPrepare() {
        startAnimator();
    }

    @Override
    public void onMove(int yScrolled, boolean isComplete, boolean automatic) {
    }

    @Override
    public void onRelease() {
    }

    @Override
    public void onComplete() {
    }

    @Override
    public void onReset() {
        stopAnimator();
    }
}