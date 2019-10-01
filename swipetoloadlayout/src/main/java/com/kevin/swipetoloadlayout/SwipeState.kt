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

import android.util.Log

/**
 * SwipeState
 *
 * @author zwenkai@foxmail.com, Created on 2019-09-27 10:54:31
 *         Major Function：<b>Swipe State</b>
 *         <p/>
 *         Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
internal object SwipeState {

    const val STATE_REFRESHING = -3
    const val STATE_RELEASE_TO_REFRESH = -2
    const val STATE_SWIPING_TO_REFRESH = -1
    const val STATE_DEFAULT = 0
    const val STATE_SWIPING_TO_LOAD_MORE = 1
    const val STATE_RELEASE_TO_LOAD_MORE = 2
    const val STATE_LOADING_MORE = 3

    fun isRefreshing(state: Int): Boolean {
        return state == STATE_REFRESHING
    }

    fun isLoadingMore(state: Int): Boolean {
        return state == STATE_LOADING_MORE
    }

    fun isReleaseToRefresh(state: Int): Boolean {
        return state == STATE_RELEASE_TO_REFRESH
    }

    fun isReleaseToLoadMore(state: Int): Boolean {
        return state == STATE_RELEASE_TO_LOAD_MORE
    }

    fun isSwipingToRefresh(state: Int): Boolean {
        return state == STATE_SWIPING_TO_REFRESH
    }

    fun isSwipingToLoadMore(state: Int): Boolean {
        return state == STATE_SWIPING_TO_LOAD_MORE
    }

    fun isRefreshState(state: Int): Boolean {
        return state < STATE_DEFAULT
    }

    fun isLoadMoreState(state: Int): Boolean {
        return state > STATE_DEFAULT
    }

    fun isStateDefault(state: Int): Boolean {
        return state == STATE_DEFAULT
    }

    fun getState(state: Int): String {
        return when (state) {
            STATE_REFRESHING -> "state_refreshing"
            STATE_RELEASE_TO_REFRESH -> "state_release_to_refresh"
            STATE_SWIPING_TO_REFRESH -> "state_swiping_to_refresh"
            STATE_DEFAULT -> "state_default"
            STATE_SWIPING_TO_LOAD_MORE -> "state_swiping_to_load_more"
            STATE_RELEASE_TO_LOAD_MORE -> "state_release_to_load_more"
            STATE_LOADING_MORE -> "state_loading_more"
            else -> "state_illegal!"
        }
    }

    fun printState(state: Int) {
        Log.i("SwipeState", "state: ${getState(state)}")
    }
}