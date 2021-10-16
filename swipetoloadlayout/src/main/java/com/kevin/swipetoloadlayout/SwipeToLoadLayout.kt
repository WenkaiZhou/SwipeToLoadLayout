/*
 * Copyright (c) 2019 Kevin zhou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kevin.swipetoloadlayout

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Scroller
import androidx.core.view.*
import kotlin.math.abs

/**
 * SwipeToLoadLayout
 *
 * @author zwenkai@foxmail.com, Created on 2019-09-27 10:34:58
 * Major Function：<b>SwipeToLoadLayout</b>
 *
 *
 * Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
open class SwipeToLoadLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr), NestedScrollingParent2, NestedScrollingChild2 {

    private val autoScroller: AutoScroller

    private var refreshListener: OnRefreshListener? = null

    private var loadMoreListener: OnLoadMoreListener? = null

    private var headerView: View? = null

    private var targetView: View? = null

    private var footerView: View? = null

    private var headerHeight: Int = 0

    private var footerHeight: Int = 0

    private var hasHeaderView: Boolean = false

    private var hasFooterView: Boolean = false

    /**
     * indicate whether in debug mode
     */
    private var debug: Boolean = false

    private var dragRatio = DEFAULT_DRAG_RATIO

    private var autoLoading: Boolean = false

    /**
     * the threshold of the touch event
     */
    private val touchSlop: Int

    /**
     * state of SwipeToLoadLayout
     */
    private var state = SwipeState.STATE_DEFAULT

    /**
     * target view top offset
     */
    private var headerOffset: Int = 0

    /**
     * target offset
     */
    private var targetOffset: Int = 0

    /**
     * target view bottom offset
     */
    private var footerOffset: Int = 0

    /**
     * init touch action down point.y
     */
    private var initDownY: Float = 0.toFloat()

    /**
     * init touch action down point.x
     */
    private var initDownX: Float = 0.toFloat()

    /**
     * last touch point.y
     */
    private var lastY: Float = 0.toFloat()

    /**
     * last touch point.x
     */
    private var lastX: Float = 0.toFloat()

    /**
     * action touch pointer's id
     */
    private var activePointerId: Int = 0

    /**
     * a switcher indicate whither refresh function is enabled
     */
    var isRefreshEnabled = true

    /**
     * a switcher indicate whiter load more function is enabled
     */
    var isLoadMoreEnabled = true

    /**
     * the style default classic
     */
    private var style = STYLE.CLASSIC

    /**
     * offset to trigger refresh
     * 触发刷新的最小高度
     */
    private var refreshTriggerOffset = 0.toFloat()

    /**
     * offset to trigger load more
     * 触发加载的最小高度
     */
    private var loadMoreTriggerOffset = 0.toFloat()

    /**
     * the max value of top offset
     */
    private var refreshFinalDragOffset: Float = 0.toFloat()

    /**
     * the max value of bottom offset
     */
    private var loadMoreFinalDragOffset: Float = 0.toFloat()

    /**
     * Scrolling duration swiping to refresh -> default
     */
    private var swipingToRefreshToDefaultScrollingDuration =
        DEFAULT_SWIPING_TO_REFRESH_TO_DEFAULT_SCROLLING_DURATION

    /**
     * Scrolling duration state release to refresh -> refreshing
     */
    private var releaseToRefreshToRefreshingScrollingDuration =
        DEFAULT_RELEASE_TO_REFRESHING_SCROLLING_DURATION

    /**
     * Refresh complete delay duration
     */
    private var refreshCompleteDelayDuration = DEFAULT_REFRESH_COMPLETE_DELAY_DURATION

    /**
     * Scrolling duration state refresh complete -> default
     * [.setRefreshing] false
     */
    private var refreshCompleteToDefaultScrollingDuration =
        DEFAULT_REFRESH_COMPLETE_TO_DEFAULT_SCROLLING_DURATION

    /**
     * Scrolling duration state default -> refreshing, mainly for auto refresh
     * [.setRefreshing] true
     */
    private var defaultToRefreshingScrollingDuration =
        DEFAULT_DEFAULT_TO_REFRESHING_SCROLLING_DURATION

    /**
     * Scrolling duration state release to loading more -> loading more
     */
    private var releaseToLoadMoreToLoadingMoreScrollingDuration =
        DEFAULT_RELEASE_TO_LOADING_MORE_SCROLLING_DURATION

    /**
     * Load more complete delay duration
     */
    private var loadMoreCompleteDelayDuration = DEFAULT_LOAD_MORE_COMPLETE_DELAY_DURATION

    /**
     * Scrolling duration state load more complete -> default
     * [.setLoadingMore] false
     */
    private var loadMoreCompleteToDefaultScrollingDuration =
        DEFAULT_LOAD_MORE_COMPLETE_TO_DEFAULT_SCROLLING_DURATION

    /**
     * Scrolling duration swiping to load more -> default
     */
    private var swipingToLoadMoreToDefaultScrollingDuration =
        DEFAULT_SWIPING_TO_LOAD_MORE_TO_DEFAULT_SCROLLING_DURATION

    /**
     * Scrolling duration state default -> loading more, mainly for auto load more
     * [.setLoadingMore] true
     */
    private var defaultToLoadingMoreScrollingDuration =
        DEFAULT_DEFAULT_TO_LOADING_MORE_SCROLLING_DURATION

    /**
     * is current state refreshing
     */
    var isRefreshing: Boolean
        get() = SwipeState.isRefreshing(state)
        set(refreshing) {
            if (!isRefreshEnabled || headerView == null) {
                return
            }
            this.autoLoading = refreshing
            if (refreshing) {
                if (SwipeState.isStateDefault(state)) {
                    setState(SwipeState.STATE_SWIPING_TO_REFRESH)
                    scrollDefaultToRefreshing()
                }
            } else {
                if (SwipeState.isRefreshing(state)) {
                    refreshCallback.onComplete()
                    postDelayed(
                        { scrollRefreshingToDefault() },
                        refreshCompleteDelayDuration.toLong()
                    )
                }
            }
        }

    /**
     *  is current state loading more
     */
    var isLoadingMore: Boolean
        get() = SwipeState.isLoadingMore(state)
        set(loadingMore) {
            if (!isLoadMoreEnabled || footerView == null) {
                return
            }
            this.autoLoading = loadingMore
            if (loadingMore) {
                if (SwipeState.isStateDefault(state)) {
                    setState(SwipeState.STATE_SWIPING_TO_LOAD_MORE)
                    scrollDefaultToLoadingMore()
                }
            } else {
                if (SwipeState.isLoadingMore(state)) {
                    loadMoreCallback.onComplete()
                    postDelayed(
                        { scrollLoadingMoreToDefault() },
                        loadMoreCompleteDelayDuration.toLong()
                    )
                }
            }
        }

    private var refreshCallback: RefreshCallback = RefreshCallback()
    private var loadMoreCallback: LoadMoreCallback = LoadMoreCallback()

    private var totalUnconsumed: Float = 0.toFloat()
    private val nestedScrollingParentHelper by lazy { NestedScrollingParentHelper(this) }
    private val nestedScrollingChildHelper by lazy { NestedScrollingChildHelper(this) }
    private val parentScrollConsumed = IntArray(2)
    private val parentOffsetInWindow = IntArray(2)

    // Used for calls from old versions of onNestedScroll to v3 version of onNestedScroll. This only
    // exists to prevent GC costs that are present before API 21.
    private val nestedScrollingV2ConsumedCompat = IntArray(2)
    private var nestedScrollInProgress: Boolean = false

    /**
     * the style enum
     */
    object STYLE {
        const val CLASSIC = 0
        const val ABOVE = 1 // 刷新、加载View悬浮在内容控件之上
        const val BLEW = 2 // 内容控件悬浮在刷新、加载View之上
        const val SCALE = 3 // 内容控件悬浮在刷新、加载View之上，有滚动视觉差
    }

    init {
        val a =
            context.obtainStyledAttributes(attrs, R.styleable.SwipeToLoadLayout, defStyleAttr, 0)
        for (i in 0 until a.indexCount) {
            when (val attr = a.getIndex(i)) {
                R.styleable.SwipeToLoadLayout_refresh_enabled ->
                    isRefreshEnabled = a.getBoolean(attr, true)
                R.styleable.SwipeToLoadLayout_load_more_enabled ->
                    isLoadMoreEnabled = a.getBoolean(attr, true)
                R.styleable.SwipeToLoadLayout_swipe_style ->
                    setSwipeStyle(a.getInt(attr, STYLE.CLASSIC))
                R.styleable.SwipeToLoadLayout_drag_ratio ->
                    setDragRatio(a.getFloat(attr, DEFAULT_DRAG_RATIO))
                R.styleable.SwipeToLoadLayout_refresh_final_drag_offset ->
                    setRefreshFinalDragOffset(a.getDimensionPixelOffset(attr, 0))
                R.styleable.SwipeToLoadLayout_load_more_final_drag_offset ->
                    setLoadMoreFinalDragOffset(a.getDimensionPixelOffset(attr, 0))
                R.styleable.SwipeToLoadLayout_refresh_trigger_offset ->
                    setRefreshTriggerOffset(a.getDimensionPixelOffset(attr, 0))
                R.styleable.SwipeToLoadLayout_load_more_trigger_offset ->
                    setLoadMoreTriggerOffset(a.getDimensionPixelOffset(attr, 0))
                R.styleable.SwipeToLoadLayout_swiping_to_refresh_to_default_scrolling_duration ->
                    setSwipingToRefreshToDefaultScrollingDuration(
                        a.getInt(attr, DEFAULT_SWIPING_TO_REFRESH_TO_DEFAULT_SCROLLING_DURATION)
                    )
                R.styleable.SwipeToLoadLayout_release_to_refreshing_scrolling_duration ->
                    setReleaseToRefreshingScrollingDuration(
                        a.getInt(attr, DEFAULT_RELEASE_TO_REFRESHING_SCROLLING_DURATION)
                    )
                R.styleable.SwipeToLoadLayout_refresh_complete_delay_duration ->
                    setRefreshCompleteDelayDuration(
                        a.getInt(
                            attr,
                            DEFAULT_REFRESH_COMPLETE_DELAY_DURATION
                        )
                    )
                R.styleable.SwipeToLoadLayout_refresh_complete_to_default_scrolling_duration ->
                    setRefreshCompleteToDefaultScrollingDuration(
                        a.getInt(attr, DEFAULT_REFRESH_COMPLETE_TO_DEFAULT_SCROLLING_DURATION)
                    )
                R.styleable.SwipeToLoadLayout_default_to_refreshing_scrolling_duration ->
                    setDefaultToRefreshingScrollingDuration(
                        a.getInt(attr, DEFAULT_DEFAULT_TO_REFRESHING_SCROLLING_DURATION)
                    )
                R.styleable.SwipeToLoadLayout_swiping_to_load_more_to_default_scrolling_duration ->
                    setSwipingToLoadMoreToDefaultScrollingDuration(
                        a.getInt(attr, DEFAULT_SWIPING_TO_LOAD_MORE_TO_DEFAULT_SCROLLING_DURATION)
                    )
                R.styleable.SwipeToLoadLayout_release_to_loading_more_scrolling_duration ->
                    setReleaseToLoadingMoreScrollingDuration(
                        a.getInt(attr, DEFAULT_RELEASE_TO_LOADING_MORE_SCROLLING_DURATION)
                    )
                R.styleable.SwipeToLoadLayout_load_more_complete_delay_duration ->
                    setLoadMoreCompleteDelayDuration(
                        a.getInt(attr, DEFAULT_LOAD_MORE_COMPLETE_DELAY_DURATION)
                    )
                R.styleable.SwipeToLoadLayout_load_more_complete_to_default_scrolling_duration ->
                    setLoadMoreCompleteToDefaultScrollingDuration(
                        a.getInt(attr, DEFAULT_LOAD_MORE_COMPLETE_TO_DEFAULT_SCROLLING_DURATION)
                    )
                R.styleable.SwipeToLoadLayout_default_to_loading_more_scrolling_duration ->
                    setDefaultToLoadingMoreScrollingDuration(
                        a.getInt(attr, DEFAULT_DEFAULT_TO_LOADING_MORE_SCROLLING_DURATION)
                    )
            }
        }

        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        autoScroller = AutoScroller()

        isNestedScrollingEnabled = true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        when (childCount) {
            1 ->
                targetView = getChildAt(0)
            2 -> {
                val firstChild = getChildAt(0)
                val secondChild = getChildAt(1)
                if (firstChild is SwipeRefreshTrigger) {
                    headerView = firstChild
                    targetView = secondChild
                } else if (secondChild is SwipeLoadMoreTrigger) {
                    targetView = firstChild
                    footerView = secondChild
                }
            }
            3 -> {
                headerView = getChildAt(0)
                targetView = getChildAt(1)
                footerView = getChildAt(2)
            }
            else ->
                throw IllegalStateException("Children num must equal or less than 3.")
        }

        checkNotNull(targetView) { "The child content view must not be null." }

        if (headerView != null && headerView is SwipeTrigger) {
            headerView!!.visibility = View.GONE
        }
        if (footerView != null && footerView is SwipeTrigger) {
            footerView!!.visibility = View.GONE
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // header
        if (headerView != null) {
            val headerView = headerView
            measureChildWithMargins(headerView, widthMeasureSpec, 0, heightMeasureSpec, 0)
            val lp = headerView!!.layoutParams as MarginLayoutParams
            headerHeight = headerView.measuredHeight + lp.topMargin + lp.bottomMargin
            // 如果刷新触发高度小于刷新头部高度，置为头部高度，保证头部完全展示后触发刷新动作
            if (refreshTriggerOffset < headerHeight) {
                refreshTriggerOffset = headerHeight.toFloat()
            }
        }
        // target
        if (targetView != null) {
            val targetView = targetView
            measureChildWithMargins(targetView, widthMeasureSpec, 0, heightMeasureSpec, 0)
        }
        // footer
        if (footerView != null) {
            val footerView = footerView
            measureChildWithMargins(footerView, widthMeasureSpec, 0, heightMeasureSpec, 0)
            val lp = footerView!!.layoutParams as ViewGroup.MarginLayoutParams
            footerHeight = footerView.measuredHeight + lp.topMargin + lp.bottomMargin
            // 如果加载触发高度小于加载尾部高度，置为尾部高度，保证尾部完全展示后触发加载动作
            if (loadMoreTriggerOffset < footerHeight) {
                loadMoreTriggerOffset = footerHeight.toFloat()
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        layoutChildren()

        hasHeaderView = headerView != null
        hasFooterView = footerView != null
    }

    /**
     * LayoutParams of RefreshLoadMoreLayout
     */
    class LayoutParams : MarginLayoutParams {

        constructor(c: Context, attrs: AttributeSet) : super(c, attrs)

        constructor(width: Int, height: Int) : super(width, height)

        constructor(source: ViewGroup.LayoutParams) : super(source)
    }

    /**
     * {@inheritDoc}
     */
    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return LayoutParams(p)
    }

    /**
     * {@inheritDoc}
     */
    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (MotionEventCompat.getActionMasked(ev)) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP ->
                // swipeToRefresh -> finger up -> finger down if the state is still swipeToRefresh
                // in onInterceptTouchEvent ACTION_DOWN event will stop the scroller
                // if the event pass to the child view while ACTION_MOVE(condition is false)
                // in onInterceptTouchEvent ACTION_MOVE the ACTION_UP or ACTION_CANCEL will not be
                // passed to onInterceptTouchEvent and onTouchEvent. Instead It will be passed to
                // child view's onTouchEvent. So we must deal this situation in dispatchTouchEvent
                onActivePointerUp()
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (nestedScrollInProgress) return false
        val action = MotionEventCompat.getActionMasked(event)
        when (action) {
            MotionEvent.ACTION_DOWN -> {

                activePointerId = MotionEventCompat.getPointerId(event, 0)
                lastY = getMotionEventY(event, activePointerId)
                initDownY = lastY
                lastX = getMotionEventX(event, activePointerId)
                initDownX = lastX

                // if it isn't an ing state or default state
                if (SwipeState.isSwipingToRefresh(state) || SwipeState.isSwipingToLoadMore(
                        state
                    ) ||
                    SwipeState.isReleaseToRefresh(state) || SwipeState.isReleaseToLoadMore(
                        state
                    )
                ) {
                    // abort autoScrolling, not trigger the method #autoScrollFinished()
                    autoScroller.abortIfRunning()
                    if (debug) {
                        Log.i(
                            TAG,
                            "Another finger down, abort auto scrolling, let the new finger handle"
                        )
                    }
                }

                if (SwipeState.isSwipingToRefresh(state) || SwipeState.isReleaseToRefresh(
                        state
                    )
                    || SwipeState.isSwipingToLoadMore(state) || SwipeState.isReleaseToLoadMore(
                        state
                    )
                ) {
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (activePointerId == INVALID_POINTER) {
                    return false
                }
                val y = getMotionEventY(event, activePointerId)
                val x = getMotionEventX(event, activePointerId)
                val yInitDiff = y - initDownY
                val xInitDiff = x - initDownX
                lastY = y
                lastX = x
                val moved =
                    Math.abs(yInitDiff) > Math.abs(xInitDiff) && Math.abs(yInitDiff) > touchSlop
                val triggerCondition =
                    // refresh trigger condition
                    yInitDiff > 0 && moved && onCheckCanRefresh() ||
                            //load more trigger condition
                            yInitDiff < 0 && moved && onCheckCanLoadMore()
                if (triggerCondition) {
                    // if the refresh's or load more's trigger condition  is true,
                    // intercept the move action event and pass it to SwipeToLoadLayout#onTouchEvent()
                    return true
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
                lastY = getMotionEventY(event, activePointerId)
                initDownY = lastY
                lastX = getMotionEventX(event, activePointerId)
                initDownX = lastX
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> activePointerId = INVALID_POINTER
        }// let children view handle the ACTION_DOWN;
        // 1. children consumed:
        // if at least one of children onTouchEvent() ACTION_DOWN return true.
        // ACTION_DOWN event will not return to SwipeToLoadLayout#onTouchEvent().
        // but the others action can be handled by SwipeToLoadLayout#onInterceptTouchEvent()
        // 2. children not consumed:
        // if children onTouchEvent() ACTION_DOWN return false.
        // ACTION_DOWN event will return to SwipeToLoadLayout's onTouchEvent().
        // SwipeToLoadLayout#onTouchEvent() ACTION_DOWN return true to consume the ACTION_DOWN event.
        // anyway: handle action down in onInterceptTouchEvent() to init is an good option
        return super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (nestedScrollInProgress) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = MotionEventCompat.getPointerId(event, 0)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                // take over the ACTION_MOVE event from SwipeToLoadLayout#onInterceptTouchEvent()
                // if condition is true
                val y = getMotionEventY(event, activePointerId)
                val x = getMotionEventX(event, activePointerId)

                val yDiff = y - lastY
                val xDiff = x - lastX
                lastY = y
                lastX = x

                if (abs(xDiff) > abs(yDiff) && abs(xDiff) > touchSlop) {
                    return false
                }

                return moveView(yDiff)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val pointerIndex = MotionEventCompat.getActionIndex(event)
                val pointerId = MotionEventCompat.getPointerId(event, pointerIndex)
                if (pointerId != INVALID_POINTER) {
                    activePointerId = pointerId
                }
                lastY = getMotionEventY(event, activePointerId)
                initDownY = lastY
                lastX = getMotionEventX(event, activePointerId)
                initDownX = lastX
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
                lastY = getMotionEventY(event, activePointerId)
                initDownY = lastY
                lastX = getMotionEventX(event, activePointerId)
                initDownX = lastX
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (activePointerId == INVALID_POINTER) {
                    return false
                }
                activePointerId = INVALID_POINTER
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 移动控件整体
     *
     * @return success
     */
    private fun moveView(yDiff: Float): Boolean {
        if (SwipeState.isStateDefault(state)) {
            if (yDiff > 0 && onCheckCanRefresh()) {
                refreshCallback.onPrepare()
                setState(SwipeState.STATE_SWIPING_TO_REFRESH)
            } else if (yDiff < 0 && onCheckCanLoadMore()) {
                loadMoreCallback.onPrepare()
                setState(SwipeState.STATE_SWIPING_TO_LOAD_MORE)
            }
        } else if (SwipeState.isRefreshState(state)) {
            if (targetOffset <= 0) {
                setState(SwipeState.STATE_DEFAULT)
                fixCurrentStatusLayout()
                return false
            }
        } else if (SwipeState.isLoadMoreState(state)) {
            if (targetOffset >= 0) {
                setState(SwipeState.STATE_DEFAULT)
                fixCurrentStatusLayout()
                return false
            }
        }

        if (SwipeState.isRefreshState(state)) {
            if (SwipeState.isSwipingToRefresh(state)
                || SwipeState.isReleaseToRefresh(state)
            ) {
                if (targetOffset >= refreshTriggerOffset) {
                    setState(SwipeState.STATE_RELEASE_TO_REFRESH)
                } else {
                    setState(SwipeState.STATE_SWIPING_TO_REFRESH)
                }
                fingerScroll(yDiff)
            }
        } else if (SwipeState.isLoadMoreState(state)) {
            if (SwipeState.isSwipingToLoadMore(state)
                || SwipeState.isReleaseToLoadMore(state)
            ) {
                if (-targetOffset >= loadMoreTriggerOffset) {
                    setState(SwipeState.STATE_RELEASE_TO_LOAD_MORE)
                } else {
                    setState(SwipeState.STATE_SWIPING_TO_LOAD_MORE)
                }
                fingerScroll(yDiff)
            }
        }
        return true
    }

    /**
     * set debug mode(default value false)
     *
     * @param debug if true log on, false log off
     */
    fun setDebug(debug: Boolean) {
        this.debug = debug
    }

    /**
     * set refresh header view, the view must at lease be an implement of `SwipeRefreshTrigger`.
     * the view can also implement `SwipeTrigger` for more extension functions
     *
     * @param view
     */
    fun setRefreshHeaderView(view: View) {
        if (view is SwipeRefreshTrigger) {
            if (headerView != null && headerView !== view) {
                removeView(headerView)
            }
            if (headerView !== view) {
                this.headerView = view
                addView(view)
            }
        } else {
            Log.e(TAG, "Refresh header view must be an implement of SwipeRefreshTrigger")
        }
    }

    /**
     * set load more footer view, the view must at least be an implement of SwipeLoadTrigger
     * the view can also implement `SwipeTrigger` for more extension functions
     *
     * @param view
     */
    fun setLoadMoreFooterView(view: View) {
        if (view is SwipeLoadMoreTrigger) {
            if (footerView != null && footerView !== view) {
                removeView(footerView)
            }
            if (footerView !== view) {
                this.footerView = view
                addView(footerView)
            }
        } else {
            Log.e(TAG, "Load more footer view must be an implement of SwipeLoadTrigger")
        }
    }

    /**
     * set the style of the refresh header
     *
     * @param style
     */
    fun setSwipeStyle(style: Int) {
        this.style = style
        requestLayout()
    }

    /**
     * set how hard to drag. bigger easier, smaller harder;
     *
     * @param dragRatio default value is [.DEFAULT_DRAG_RATIO]
     */
    fun setDragRatio(dragRatio: Float) {
        this.dragRatio = dragRatio
    }

    /**
     * set the value of [.refreshTriggerOffset].
     * Default value is the refresh header view height [.headerHeight]
     *
     *
     * If the offset you set is smaller than [.headerHeight] or not set,
     * using [.headerHeight] as default value
     *
     * @param offset
     */
    fun setRefreshTriggerOffset(offset: Int) {
        refreshTriggerOffset = offset.toFloat()
    }

    /**
     * set the value of [.loadMoreTriggerOffset].
     * Default value is the load more footer view height [.footerHeight]
     *
     *
     * If the offset you set is smaller than [.footerHeight] or not set,
     * using [.footerHeight] as default value
     *
     * @param offset
     */
    fun setLoadMoreTriggerOffset(offset: Int) {
        loadMoreTriggerOffset = offset.toFloat()
    }

    /**
     * Set the final offset you can swipe to refresh.<br></br>
     * If the offset you set is 0(default value) or smaller than [.refreshTriggerOffset]
     * there no final offset
     *
     * @param offset
     */
    fun setRefreshFinalDragOffset(offset: Int) {
        refreshFinalDragOffset = offset.toFloat()
    }

    /**
     * Set the final offset you can swipe to load more.<br></br>
     * If the offset you set is 0(default value) or smaller than [.loadMoreTriggerOffset],
     * there no final offset
     *
     * @param offset
     */
    fun setLoadMoreFinalDragOffset(offset: Int) {
        loadMoreFinalDragOffset = offset.toFloat()
    }

    /**
     * set [.swipingToRefreshToDefaultScrollingDuration] in milliseconds
     *
     * @param duration
     */
    fun setSwipingToRefreshToDefaultScrollingDuration(duration: Int) {
        this.swipingToRefreshToDefaultScrollingDuration = duration
    }

    /**
     * set [.releaseToRefreshToRefreshingScrollingDuration] in milliseconds
     *
     * @param duration
     */
    fun setReleaseToRefreshingScrollingDuration(duration: Int) {
        this.releaseToRefreshToRefreshingScrollingDuration = duration
    }

    /**
     * set [.refreshCompleteDelayDuration] in milliseconds
     *
     * @param duration
     */
    fun setRefreshCompleteDelayDuration(duration: Int) {
        this.refreshCompleteDelayDuration = duration
    }

    /**
     * set [.refreshCompleteToDefaultScrollingDuration] in milliseconds
     *
     * @param duration
     */
    fun setRefreshCompleteToDefaultScrollingDuration(duration: Int) {
        this.refreshCompleteToDefaultScrollingDuration = duration
    }

    /**
     * set [.defaultToRefreshingScrollingDuration] in milliseconds
     *
     * @param duration
     */
    fun setDefaultToRefreshingScrollingDuration(duration: Int) {
        this.defaultToRefreshingScrollingDuration = duration
    }

    /**
     * set [@swipingToLoadMoreToDefaultScrollingDuration] in milliseconds
     *
     * @param duration
     */
    fun setSwipingToLoadMoreToDefaultScrollingDuration(duration: Int) {
        this.swipingToLoadMoreToDefaultScrollingDuration = duration
    }

    /**
     * set [.releaseToLoadMoreToLoadingMoreScrollingDuration] in milliseconds
     *
     * @param duration
     */
    fun setReleaseToLoadingMoreScrollingDuration(duration: Int) {
        this.releaseToLoadMoreToLoadingMoreScrollingDuration = duration
    }

    /**
     * set [.loadMoreCompleteDelayDuration] in milliseconds
     *
     * @param duration
     */
    fun setLoadMoreCompleteDelayDuration(duration: Int) {
        this.loadMoreCompleteDelayDuration = duration
    }

    /**
     * set [.loadMoreCompleteToDefaultScrollingDuration] in milliseconds
     *
     * @param duration
     */
    fun setLoadMoreCompleteToDefaultScrollingDuration(duration: Int) {
        this.loadMoreCompleteToDefaultScrollingDuration = duration
    }

    /**
     * set [.defaultToLoadingMoreScrollingDuration] in milliseconds
     *
     * @param duration
     */
    fun setDefaultToLoadingMoreScrollingDuration(duration: Int) {
        this.defaultToLoadingMoreScrollingDuration = duration
    }

    /**
     * set an [OnRefreshListener] to listening refresh event
     *
     * @param listener
     */
    fun setOnRefreshListener(listener: OnRefreshListener) {
        this.refreshListener = listener
    }

    /**
     * set an [OnLoadMoreListener] to listening load more event
     *
     * @param listener
     */
    fun setOnLoadMoreListener(listener: OnLoadMoreListener) {
        this.loadMoreListener = listener
    }

    /**
     * Set the current state for better control
     *
     * @param state
     */
    private fun setState(state: Int) {
        this.state = state
        if (debug) {
            SwipeState.printState(state)
        }
    }

    /**
     * copy from [SwipeRefreshLayout.canChildScrollUp]
     *
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    fun canChildScrollUp(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT < 14) {
            if (targetView is AbsListView) {
                val absListView = targetView as AbsListView?
                absListView!!.childCount > 0 && (absListView.firstVisiblePosition > 0 || absListView.getChildAt(
                    0
                ).top < absListView.paddingTop)
            } else {
                ViewCompat.canScrollVertically(targetView, -1) || targetView!!.scrollY > 0
            }
        } else {
            ViewCompat.canScrollVertically(targetView, -1)
        }
    }

    /**
     * Whether it is possible for the child view of this layout to
     * scroll down. Override this if the child view is a custom view.
     *
     * @return
     */
    fun canChildScrollDown(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT < 14) {
            if (targetView is AbsListView) {
                val absListView = targetView as AbsListView?
                absListView!!.childCount > 0 && (absListView.lastVisiblePosition < absListView.childCount - 1 || absListView.getChildAt(
                    absListView.childCount - 1
                ).bottom > absListView.paddingBottom)
            } else {
                ViewCompat.canScrollVertically(targetView, 1) || targetView!!.scrollY < 0
            }
        } else {
            ViewCompat.canScrollVertically(targetView, 1)
        }
    }

    /**
     * @see .onLayout
     */
    private fun layoutChildren() {
        val height = measuredHeight

        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom

        if (targetView == null) {
            return
        }

        // layout header
        if (headerView != null) {
            val headerView = headerView
            val lp = headerView!!.layoutParams as MarginLayoutParams
            val headerLeft = paddingLeft + lp.leftMargin
            val headerTop: Int
            when (style) {
                STYLE.CLASSIC ->
                    // classic
                    headerTop = paddingTop + lp.topMargin - headerHeight + headerOffset
                STYLE.ABOVE ->
                    // classic
                    headerTop = paddingTop + lp.topMargin - headerHeight + headerOffset
                STYLE.BLEW ->
                    // blew
                    headerTop = paddingTop + lp.topMargin
                STYLE.SCALE ->
                    // scale
                    headerTop = paddingTop + lp.topMargin - headerHeight / 2 + headerOffset / 2
                else ->
                    // classic
                    headerTop = paddingTop + lp.topMargin - headerHeight + headerOffset
            }
            val headerRight = headerLeft + headerView.measuredWidth
            val headerBottom = headerTop + headerView.measuredHeight
            headerView.layout(headerLeft, headerTop, headerRight, headerBottom)
        }

        // layout target
        if (targetView != null) {
            val targetView = targetView
            val lp = targetView!!.layoutParams as MarginLayoutParams
            val targetLeft = paddingLeft + lp.leftMargin
            val targetTop: Int

            when (style) {
                STYLE.CLASSIC ->
                    // classic
                    targetTop = paddingTop + lp.topMargin + targetOffset
                STYLE.ABOVE ->
                    // above
                    targetTop = paddingTop + lp.topMargin
                STYLE.BLEW ->
                    // classic
                    targetTop = paddingTop + lp.topMargin + targetOffset
                STYLE.SCALE ->
                    // classic
                    targetTop = paddingTop + lp.topMargin + targetOffset
                else ->
                    // classic
                    targetTop = paddingTop + lp.topMargin + targetOffset
            }
            val targetRight = targetLeft + targetView.measuredWidth
            val targetBottom = targetTop + targetView.measuredHeight

            targetView.layout(targetLeft, targetTop, targetRight, targetBottom)
        }

        // layout footer
        if (footerView != null) {
            val footerView = footerView
            val lp = footerView!!.layoutParams as MarginLayoutParams
            val footerLeft = paddingLeft + lp.leftMargin
            val footerBottom: Int
            when (style) {
                STYLE.CLASSIC ->
                    // classic
                    footerBottom =
                        height - paddingBottom - lp.bottomMargin + footerHeight + footerOffset
                STYLE.ABOVE ->
                    // classic
                    footerBottom =
                        height - paddingBottom - lp.bottomMargin + footerHeight + footerOffset
                STYLE.BLEW ->
                    // blew
                    footerBottom = height - paddingBottom - lp.bottomMargin
                STYLE.SCALE ->
                    // scale
                    footerBottom =
                        height - paddingBottom - lp.bottomMargin + footerHeight / 2 + footerOffset / 2
                else ->
                    // classic
                    footerBottom =
                        height - paddingBottom - lp.bottomMargin + footerHeight + footerOffset
            }
            val footerTop = footerBottom - footerView.measuredHeight
            val footerRight = footerLeft + footerView.measuredWidth

            footerView.layout(footerLeft, footerTop, footerRight, footerBottom)
        }

        if (style == STYLE.CLASSIC || style == STYLE.ABOVE) {
            if (headerView != null) {
                headerView!!.bringToFront()
            }
            if (footerView != null) {
                footerView!!.bringToFront()
            }
        } else if (style == STYLE.BLEW || style == STYLE.SCALE) {
            if (targetView != null) {
                targetView!!.bringToFront()
            }
        }
    }

    private fun fixCurrentStatusLayout() {
        when {
            SwipeState.isRefreshing(state) -> {
                targetOffset = (refreshTriggerOffset + 0.5f).toInt()
                headerOffset = targetOffset
                footerOffset = 0
                layoutChildren()
                invalidate()
            }
            SwipeState.isStateDefault(state) -> {
                targetOffset = 0
                headerOffset = 0
                footerOffset = 0
                layoutChildren()
                invalidate()
            }
            SwipeState.isLoadingMore(state) -> {
                targetOffset = -(loadMoreTriggerOffset + 0.5f).toInt()
                headerOffset = 0
                footerOffset = targetOffset
                layoutChildren()
                invalidate()
            }
        }
    }

    /**
     * scrolling by physical touch with your fingers
     *
     * @param yDiff
     */
    private fun fingerScroll(yDiff: Float) {
        val ratio = dragRatio
        var yScrolled = yDiff * ratio

        // make sure (targetOffset>0 -> targetOffset=0 -> default state)
        // or (targetOffset<0 -> targetOffset=0 -> default state)
        // forbidden fling (targetOffset>0 -> targetOffset=0 ->targetOffset<0 -> default state)
        // or (targetOffset<0 -> targetOffset=0 ->targetOffset>0 -> default state)
        // I am so smart :)

        val tmpTargetOffset = yScrolled + targetOffset
        if (tmpTargetOffset > 0 && targetOffset < 0 || tmpTargetOffset < 0 && targetOffset > 0) {
            yScrolled = (-targetOffset).toFloat()
        }


        if (refreshFinalDragOffset >= refreshTriggerOffset && tmpTargetOffset > refreshFinalDragOffset) {
            yScrolled = refreshFinalDragOffset - targetOffset
        } else if (loadMoreFinalDragOffset >= loadMoreTriggerOffset && -tmpTargetOffset > loadMoreFinalDragOffset) {
            yScrolled = -loadMoreFinalDragOffset - targetOffset
        }

        if (SwipeState.isRefreshState(state)) {
            refreshCallback.onMove(targetOffset, isComplete = false, automatic = false)
        } else if (SwipeState.isLoadMoreState(state)) {
            loadMoreCallback.onMove(targetOffset, isComplete = false, automatic = false)
        }
        updateScroll(yScrolled)
    }

    private fun autoScroll(yScrolled: Float) {
        when {
            SwipeState.isSwipingToRefresh(state) ->
                refreshCallback.onMove(targetOffset, isComplete = false, automatic = true)
            SwipeState.isReleaseToRefresh(state) ->
                refreshCallback.onMove(targetOffset, isComplete = false, automatic = true)
            SwipeState.isRefreshing(state) ->
                refreshCallback.onMove(targetOffset, isComplete = true, automatic = true)
            SwipeState.isSwipingToLoadMore(state) ->
                loadMoreCallback.onMove(targetOffset, isComplete = false, automatic = true)
            SwipeState.isReleaseToLoadMore(state) ->
                loadMoreCallback.onMove(targetOffset, isComplete = false, automatic = true)
            SwipeState.isLoadingMore(state) ->
                loadMoreCallback.onMove(targetOffset, isComplete = true, automatic = true)
        }
        updateScroll(yScrolled)
    }

    /**
     * Process the scrolling(auto or physical) and append the diff values to targetOffset
     * I think it's the most busy and core method. :) a ha ha ha ha...
     *
     * @param yScrolled
     */
    private fun updateScroll(yScrolled: Float) {
        if (yScrolled == 0f) {
            return
        }
        targetOffset += yScrolled.toInt()

        if (SwipeState.isRefreshState(state)) {
            headerOffset = targetOffset
            footerOffset = 0
        } else if (SwipeState.isLoadMoreState(state)) {
            footerOffset = targetOffset
            headerOffset = 0
        }

        if (debug) {
            Log.i(TAG, "targetOffset = $targetOffset")
        }
        layoutChildren()
        invalidate()
    }

    /**
     * on active finger up
     */
    private fun onActivePointerUp() {
        when {
            SwipeState.isSwipingToRefresh(state) ->
                // simply return
                scrollSwipingToRefreshToDefault()
            SwipeState.isSwipingToLoadMore(state) ->
                // simply return
                scrollSwipingToLoadMoreToDefault()
            SwipeState.isReleaseToRefresh(state) -> {
                // return to header height and perform refresh
                refreshCallback.onRelease()
                scrollReleaseToRefreshToRefreshing()
            }
            SwipeState.isReleaseToLoadMore(state) -> {
                // return to footer height and perform loadMore
                loadMoreCallback.onRelease()
                scrollReleaseToLoadMoreToLoadingMore()
            }
        }
    }

    /**
     * on not active finger up
     *
     * @param ev
     */
    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = MotionEventCompat.getActionIndex(ev)
        val pointerId = MotionEventCompat.getPointerId(ev, pointerIndex)
        if (pointerId == activePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            activePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex)
        }
    }

    private fun scrollDefaultToRefreshing() {
        autoScroller.autoScroll(
            (refreshTriggerOffset + 0.5f).toInt(),
            defaultToRefreshingScrollingDuration
        )
    }

    private fun scrollDefaultToLoadingMore() {
        autoScroller.autoScroll(
            -(loadMoreTriggerOffset + 0.5f).toInt(),
            defaultToLoadingMoreScrollingDuration
        )
    }

    private fun scrollSwipingToRefreshToDefault() {
        autoScroller.autoScroll(-headerOffset, swipingToRefreshToDefaultScrollingDuration)
    }

    private fun scrollSwipingToLoadMoreToDefault() {
        autoScroller.autoScroll(-footerOffset, swipingToLoadMoreToDefaultScrollingDuration)
    }

    private fun scrollReleaseToRefreshToRefreshing() {
        autoScroller.autoScroll(
            headerHeight - headerOffset,
            releaseToRefreshToRefreshingScrollingDuration
        )
    }

    private fun scrollReleaseToLoadMoreToLoadingMore() {
        autoScroller.autoScroll(
            -footerOffset - footerHeight,
            releaseToLoadMoreToLoadingMoreScrollingDuration
        )
    }

    private fun scrollRefreshingToDefault() {
        autoScroller.autoScroll(-headerOffset, refreshCompleteToDefaultScrollingDuration)
    }

    private fun scrollLoadingMoreToDefault() {
        autoScroller.autoScroll(-footerOffset, loadMoreCompleteToDefaultScrollingDuration)
    }

    /**
     * invoke when [AutoScroller.finish] is automatic
     */
    private fun autoScrollFinished() {
        val mLastStatus = state

        if (SwipeState.isReleaseToRefresh(state)) {
            setState(SwipeState.STATE_REFRESHING)
            fixCurrentStatusLayout()
            refreshCallback.onRefresh()

        } else if (SwipeState.isRefreshing(state)) {
            setState(SwipeState.STATE_DEFAULT)
            fixCurrentStatusLayout()
            refreshCallback.onReset()

        } else if (SwipeState.isSwipingToRefresh(state)) {
            if (autoLoading) {
                autoLoading = false
                setState(SwipeState.STATE_REFRESHING)
                fixCurrentStatusLayout()
                refreshCallback.onRefresh()
            } else {
                setState(SwipeState.STATE_DEFAULT)
                fixCurrentStatusLayout()
                refreshCallback.onReset()
            }
        } else if (SwipeState.isStateDefault(state)) {

        } else if (SwipeState.isSwipingToLoadMore(state)) {
            if (autoLoading) {
                autoLoading = false
                setState(SwipeState.STATE_LOADING_MORE)
                fixCurrentStatusLayout()
                loadMoreCallback.onLoadMore()
            } else {
                setState(SwipeState.STATE_DEFAULT)
                fixCurrentStatusLayout()
                loadMoreCallback.onReset()
            }
        } else if (SwipeState.isLoadingMore(state)) {
            setState(SwipeState.STATE_DEFAULT)
            fixCurrentStatusLayout()
            loadMoreCallback.onReset()
        } else if (SwipeState.isReleaseToLoadMore(state)) {
            setState(SwipeState.STATE_LOADING_MORE)
            fixCurrentStatusLayout()
            loadMoreCallback.onLoadMore()
        } else {
            throw IllegalStateException("illegal state: " + SwipeState.getState(state))
        }

        if (debug) {
            Log.i(TAG, SwipeState.getState(mLastStatus) + " -> " + SwipeState.getState(state))
        }
    }

    /**
     * check if it can refresh
     *
     * @return
     */
    private fun onCheckCanRefresh(): Boolean {
        return isRefreshEnabled && !canChildScrollUp() && hasHeaderView && refreshTriggerOffset > 0
    }

    /**
     * check if it can load more
     *
     * @return
     */
    private fun onCheckCanLoadMore(): Boolean {
        return isLoadMoreEnabled && !canChildScrollDown() && hasFooterView && loadMoreTriggerOffset > 0
    }

    private fun getMotionEventY(event: MotionEvent, activePointerId: Int): Float {
        val index = MotionEventCompat.findPointerIndex(event, activePointerId)
        return if (index < 0) {
            INVALID_COORDINATE.toFloat()
        } else MotionEventCompat.getY(event, index)
    }

    private fun getMotionEventX(event: MotionEvent, activePointId: Int): Float {
        val index = MotionEventCompat.findPointerIndex(event, activePointId)
        return if (index < 0) {
            INVALID_COORDINATE.toFloat()
        } else MotionEventCompat.getX(event, index)
    }

    /**
     * refresh event callback
     */
    internal inner class RefreshCallback : SwipeTrigger, SwipeRefreshTrigger {
        override fun onPrepare() {
            if (headerView != null
                && headerView is SwipeTrigger
                && SwipeState.isStateDefault(state)
            ) {
                headerView!!.visibility = View.VISIBLE
                (headerView as SwipeTrigger).onPrepare()
            }
        }

        override fun onMove(y: Int, isComplete: Boolean, automatic: Boolean) {
            if (headerView != null
                && headerView is SwipeTrigger
                && SwipeState.isRefreshState(state)
            ) {
                if (headerView!!.visibility != View.VISIBLE) {
                    headerView!!.visibility = View.VISIBLE
                }
                (headerView as SwipeTrigger).onMove(y, isComplete, automatic)
            }
        }

        override fun onRelease() {
            if (headerView != null
                && headerView is SwipeTrigger
                && SwipeState.isReleaseToRefresh(state)
            ) {
                (headerView as SwipeTrigger).onRelease()
            }
        }

        override fun onRefresh() {
            if (headerView != null && SwipeState.isRefreshing(state)) {
                if (headerView is SwipeRefreshTrigger) {
                    (headerView as SwipeRefreshTrigger).onRefresh()
                }
                if (refreshListener != null) {
                    refreshListener!!.onRefresh()
                }
            }
        }

        override fun onComplete() {
            if (headerView != null && headerView is SwipeTrigger) {
                (headerView as SwipeTrigger).onComplete()
            }
        }

        override fun onReset() {
            if (headerView != null
                && headerView is SwipeTrigger
                && SwipeState.isStateDefault(state)
            ) {
                (headerView as SwipeTrigger).onReset()
                headerView!!.visibility = View.GONE
            }
        }
    }

    /**
     * load more event callback
     */
    private inner class LoadMoreCallback : SwipeTrigger, SwipeLoadMoreTrigger {
        override fun onPrepare() {
            if (footerView != null
                && footerView is SwipeTrigger
                && SwipeState.isStateDefault(state)
            ) {
                footerView!!.visibility = View.VISIBLE
                (footerView as SwipeTrigger).onPrepare()
            }
        }

        override fun onMove(y: Int, isComplete: Boolean, automatic: Boolean) {
            if (footerView != null
                && footerView is SwipeTrigger
                && SwipeState.isLoadMoreState(state)
            ) {
                if (footerView!!.visibility != View.VISIBLE) {
                    footerView!!.visibility = View.VISIBLE
                }
                (footerView as SwipeTrigger).onMove(y, isComplete, automatic)
            }
        }

        override fun onRelease() {
            if (footerView != null
                && footerView is SwipeTrigger
                && SwipeState.isReleaseToLoadMore(state)
            ) {
                (footerView as SwipeTrigger).onRelease()
            }
        }

        override fun onLoadMore() {
            if (footerView != null && SwipeState.isLoadingMore(state)) {
                if (footerView is SwipeLoadMoreTrigger) {
                    (footerView as SwipeLoadMoreTrigger).onLoadMore()
                }
                if (loadMoreListener != null) {
                    loadMoreListener!!.onLoadMore()
                }
            }
        }

        override fun onComplete() {
            if (footerView != null && footerView is SwipeTrigger) {
                (footerView as SwipeTrigger).onComplete()
            }
        }

        override fun onReset() {
            if (footerView != null
                && footerView is SwipeTrigger
                && SwipeState.isStateDefault(state)
            ) {
                (footerView as SwipeTrigger).onReset()
                footerView!!.visibility = View.GONE
            }
        }
    }

    private inner class AutoScroller : Runnable {

        private val scroller: Scroller = Scroller(context)

        private var lastY: Int = 0

        private var running = false

        private var abort = false

        override fun run() {
            val finish = !scroller.computeScrollOffset() || scroller.isFinished
            val currY = scroller.currY
            val yDiff = currY - lastY
            if (finish) {
                finish()
            } else {
                lastY = currY
                this@SwipeToLoadLayout.autoScroll(yDiff.toFloat())
                post(this)
            }
        }

        /**
         * remove the post callbacks and reset default values
         */
        private fun finish() {
            lastY = 0
            running = false
            removeCallbacks(this)
            // if abort by user, don't call
            if (!abort) {
                autoScrollFinished()
            }
        }

        /**
         * abort scroll if it is scrolling
         */
        fun abortIfRunning() {
            if (running) {
                if (!scroller.isFinished) {
                    abort = true
                    scroller.forceFinished(true)
                }
                finish()
                abort = false
            }
        }

        /**
         * The param yScrolled here isn't final pos of y.
         * It's just like the yScrolled param in the
         * [.updateScroll]
         *
         * @param yScrolled
         * @param duration
         */
        fun autoScroll(yScrolled: Int, duration: Int) {
            removeCallbacks(this)
            lastY = 0
            if (!scroller.isFinished) {
                scroller.forceFinished(true)
            }
            scroller.startScroll(0, 0, 0, yScrolled, duration)
            post(this)
            running = true
        }
    }

    companion object {

        private val TAG = SwipeToLoadLayout::class.java.simpleName

        private const val DEFAULT_SWIPING_TO_REFRESH_TO_DEFAULT_SCROLLING_DURATION = 200

        private const val DEFAULT_RELEASE_TO_REFRESHING_SCROLLING_DURATION = 200

        private const val DEFAULT_REFRESH_COMPLETE_DELAY_DURATION = 300

        private const val DEFAULT_REFRESH_COMPLETE_TO_DEFAULT_SCROLLING_DURATION = 500

        private const val DEFAULT_DEFAULT_TO_REFRESHING_SCROLLING_DURATION = 500

        private const val DEFAULT_SWIPING_TO_LOAD_MORE_TO_DEFAULT_SCROLLING_DURATION = 200

        private const val DEFAULT_RELEASE_TO_LOADING_MORE_SCROLLING_DURATION = 200

        private const val DEFAULT_LOAD_MORE_COMPLETE_DELAY_DURATION = 300

        private const val DEFAULT_LOAD_MORE_COMPLETE_TO_DEFAULT_SCROLLING_DURATION = 300

        private const val DEFAULT_DEFAULT_TO_LOADING_MORE_SCROLLING_DURATION = 300

        /**
         * how hard to drag
         */
        private const val DEFAULT_DRAG_RATIO = 0.5f

        private const val INVALID_POINTER = -1

        private const val INVALID_COORDINATE = -1
    }

    // NestedScrollingParent 3

    private fun onNestedScroll(
        target: View, dxConsumed: Int, dyConsumed: Int,
        dxUnconsumed: Int, dyUnconsumed: Int, type: Int,
        consumed: IntArray
    ) {
        if (type != ViewCompat.TYPE_TOUCH) {
            return
        }

        // This is a bit of a hack. onNestedScroll is typically called up the hierarchy of nested
        // scrolling parents/children, where each consumes distances before passing the remainder
        // to parents.  In our case, we want to try to run after children, and after parents, so we
        // first pass scroll distances to parents and consume after everything else has.
        val consumedBeforeParents = consumed[1]
        dispatchNestedScroll(
            dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
            parentOffsetInWindow, type, consumed
        )
        val consumedByParents = consumed[1] - consumedBeforeParents
        val unconsumedAfterParents = dyUnconsumed - consumedByParents

        // There are two reasons why scroll distance may be totally consumed.  1) All of the nested
        // scrolling parents up the hierarchy implement NestedScrolling3 and consumed all of the
        // distance or 2) at least 1 nested scrolling parent doesn't implement NestedScrolling3 and
        // for comparability reasons, we are supposed to act like they have.
        //
        // We must assume 2) is the case because we have no way of determining that it isn't, and
        // therefore must fallback to a previous hack that was done before nested scrolling 3
        // existed.
        val remainingDistanceToScroll: Int
        if (unconsumedAfterParents == 0) {
            // The previously implemented hack is to see how far we were offset and assume that that
            // distance is equal to how much all of our parents consumed.
            remainingDistanceToScroll = dyUnconsumed + parentOffsetInWindow[1]
        } else {
            remainingDistanceToScroll = unconsumedAfterParents
        }

        // Not sure why we have to make sure the child can't scroll up... but seems dangerous to
        // remove.
        if (remainingDistanceToScroll < 0 && !canChildScrollUp()) {
            totalUnconsumed += abs(remainingDistanceToScroll).toFloat()
//            moveSpinner(totalUnconsumed)
            val yDiff = -remainingDistanceToScroll.toFloat()
            moveView(yDiff)

            // If we've gotten here, we need to consume whatever is left to consume, which at this
            // point is either equal to 0, or remainingDistanceToScroll.
            consumed[1] += unconsumedAfterParents
        } else {
            val yDiff = -remainingDistanceToScroll.toFloat()
            moveView(yDiff)

            // If we've gotten here, we need to consume whatever is left to consume, which at this
            // point is either equal to 0, or remainingDistanceToScroll.
            consumed[1] += unconsumedAfterParents
        }
    }

    // NestedScrollingParent 2

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return if (type == ViewCompat.TYPE_TOUCH) {
            onStartNestedScroll(child, target, axes)
        } else {
            false
        }
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        // Should always be true because onStartNestedScroll returns false for all type !=
        // ViewCompat.TYPE_TOUCH, but check just in case.
        if (type == ViewCompat.TYPE_TOUCH) {
            onNestedScrollAccepted(child, target, axes)
        }
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        // Should always be true because onStartNestedScroll returns false for all type !=
        // ViewCompat.TYPE_TOUCH, but check just in case.
        if (type == ViewCompat.TYPE_TOUCH) {
            onStopNestedScroll(target)
        }
    }

    override fun onNestedScroll(
        target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
        dyUnconsumed: Int, type: Int
    ) {
        onNestedScroll(
            target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type,
            nestedScrollingV2ConsumedCompat
        )
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        // Should always be true because onStartNestedScroll returns false for all type !=
        // ViewCompat.TYPE_TOUCH, but check just in case.
        if (type == ViewCompat.TYPE_TOUCH) {
            onNestedPreScroll(target, dx, dy, consumed)
        }
    }

    // NestedScrollingParent 1

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
//        return (isEnabled && !mReturningToStart && !mRefreshing
//         nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
        return isEnabled && nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        // Reset the counter of how much leftover scroll needs to be consumed.
        nestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes)
        // Dispatch up to the nested parent
        startNestedScroll(axes and ViewCompat.SCROLL_AXIS_VERTICAL)
        totalUnconsumed = 0f
        nestedScrollInProgress = true
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        // If we are in the middle of consuming, a scroll, then we want to move the spinner back up
        // before allowing the list to scroll
        if (dy > 0 && totalUnconsumed > 0) {
            if (dy > totalUnconsumed) {
                consumed[1] = totalUnconsumed.toInt()
                totalUnconsumed = 0f
            } else {
                totalUnconsumed -= dy.toFloat()
                consumed[1] = dy
            }
//            moveSpinner(totalUnconsumed)
            moveView(-dy.toFloat())
        }

        // If a client layout is using a custom start position for the circle
        // view, they mean to hide it again before scrolling the child view
        // If we get back to totalUnconsumed == 0 and there is more to go, hide
        // the circle so it isn't exposed if its blocking content is moved
//        if (mUsingCustomStart && dy > 0 && totalUnconsumed == 0f
//            && Math.abs(dy - consumed[1]) > 0
//        ) {
//            mCircleView.setVisibility(View.GONE)
//        }

        // Now let our nested parent consume the leftovers
        val parentConsumed = parentScrollConsumed
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0]
            consumed[1] += parentConsumed[1]
        }
    }

    override fun getNestedScrollAxes(): Int {
        return nestedScrollingParentHelper.nestedScrollAxes
    }

    override fun onStopNestedScroll(target: View) {
        nestedScrollingParentHelper.onStopNestedScroll(target)
        nestedScrollInProgress = false
        // Finish the spinner for nested scrolling if we ever consumed any
        // unconsumed nested scroll
        if (totalUnconsumed > 0) {
//            finishSpinner(totalUnconsumed)
            totalUnconsumed = 0f
        }
        // Dispatch up our nested parent
        stopNestedScroll()
    }

    override fun onNestedScroll(
        target: View, dxConsumed: Int, dyConsumed: Int,
        dxUnconsumed: Int, dyUnconsumed: Int
    ) {
        onNestedScroll(
            target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
            ViewCompat.TYPE_TOUCH, nestedScrollingV2ConsumedCompat
        )
    }

    override fun onNestedPreFling(
        target: View, velocityX: Float,
        velocityY: Float
    ): Boolean {
        return dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun onNestedFling(
        target: View, velocityX: Float, velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return dispatchNestedFling(velocityX, velocityY, consumed)
    }

    // NestedScrollingChild 3

    private fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
        dyUnconsumed: Int, offsetInWindow: IntArray?, type: Int,
        consumed: IntArray
    ) {
        if (type == ViewCompat.TYPE_TOUCH) {
            nestedScrollingChildHelper.dispatchNestedScroll(
                dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed, offsetInWindow, type
            )
        }
    }

    // NestedScrollingChild 2

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        return type == ViewCompat.TYPE_TOUCH && startNestedScroll(axes)
    }

    override fun stopNestedScroll(type: Int) {
        if (type == ViewCompat.TYPE_TOUCH) {
            stopNestedScroll()
        }
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        return type == ViewCompat.TYPE_TOUCH && hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
        dyUnconsumed: Int, offsetInWindow: IntArray?, type: Int
    ): Boolean {
        return type == ViewCompat.TYPE_TOUCH && nestedScrollingChildHelper.dispatchNestedScroll(
            dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return type == ViewCompat.TYPE_TOUCH && dispatchNestedPreScroll(
            dx, dy, consumed,
            offsetInWindow
        )
    }

    // NestedScrollingChild 1

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        nestedScrollingChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return nestedScrollingChildHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return nestedScrollingChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        nestedScrollingChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return nestedScrollingChildHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
        dyUnconsumed: Int, offsetInWindow: IntArray?
    ): Boolean {
        return nestedScrollingChildHelper.dispatchNestedScroll(
            dxConsumed, dyConsumed,
            dxUnconsumed, dyUnconsumed, offsetInWindow
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?
    ): Boolean {
        return nestedScrollingChildHelper.dispatchNestedPreScroll(
            dx, dy, consumed, offsetInWindow
        )
    }

    override fun dispatchNestedFling(
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return nestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return nestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }
}
