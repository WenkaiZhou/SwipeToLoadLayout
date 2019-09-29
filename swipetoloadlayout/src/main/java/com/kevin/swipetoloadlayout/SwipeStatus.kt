package com.kevin.swipetoloadlayout

import android.util.Log

/**
 * SwipeStatus
 *
 * @author zwenkai@foxmail.com, Created on 2019-09-27 10:54:31
 *         Major Function：<b></b>
 *         <p/>
 *         Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
internal object SwipeStatus {

    const val STATUS_REFRESH_RETURNING = -4
    const val STATUS_REFRESHING = -3
    const val STATUS_RELEASE_TO_REFRESH = -2
    const val STATUS_SWIPING_TO_REFRESH = -1
    const val STATUS_DEFAULT = 0
    const val STATUS_SWIPING_TO_LOAD_MORE = 1
    const val STATUS_RELEASE_TO_LOAD_MORE = 2
    const val STATUS_LOADING_MORE = 3
    const val STATUS_LOAD_MORE_RETURNING = 4

    fun isRefreshing(status: Int): Boolean {
        return status == STATUS_REFRESHING
    }

    fun isLoadingMore(status: Int): Boolean {
        return status == STATUS_LOADING_MORE
    }

    fun isReleaseToRefresh(status: Int): Boolean {
        return status == STATUS_RELEASE_TO_REFRESH
    }

    fun isReleaseToLoadMore(status: Int): Boolean {
        return status == STATUS_RELEASE_TO_LOAD_MORE
    }

    fun isSwipingToRefresh(status: Int): Boolean {
        return status == STATUS_SWIPING_TO_REFRESH
    }

    fun isSwipingToLoadMore(status: Int): Boolean {
        return status == STATUS_SWIPING_TO_LOAD_MORE
    }

    fun isRefreshStatus(status: Int): Boolean {
        return status < STATUS_DEFAULT
    }

    fun isLoadMoreStatus(status: Int): Boolean {
        return status > STATUS_DEFAULT
    }

    fun isStatusDefault(status: Int): Boolean {
        return status == STATUS_DEFAULT
    }

    fun getStatus(status: Int): String {
        return when (status) {
            STATUS_REFRESH_RETURNING -> "status_refresh_returning"
            STATUS_REFRESHING -> "status_refreshing"
            STATUS_RELEASE_TO_REFRESH -> "status_release_to_refresh"
            STATUS_SWIPING_TO_REFRESH -> "status_swiping_to_refresh"
            STATUS_DEFAULT -> "status_default"
            STATUS_SWIPING_TO_LOAD_MORE -> "status_swiping_to_load_more"
            STATUS_RELEASE_TO_LOAD_MORE -> "status_release_to_load_more"
            STATUS_LOADING_MORE -> "status_loading_more"
            STATUS_LOAD_MORE_RETURNING -> "status_load_more_returning"
            else -> "status_illegal!"
        }
    }

    fun printStatus(status: Int) {
        Log.i("SwipeStatus", "printStatus:" + getStatus(status))
    }
}