package com.kevin.swipetoloadlayout

/**
 * SwipeTrigger
 *
 * @author zwenkai@foxmail.com, Created on 2019-09-27 10:30:02
 * Major Function：**上拉或下拉的回调**
 *
 *
 * Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
interface SwipeTrigger {
    fun onPrepare()

    fun onMove(y: Int, isComplete: Boolean, automatic: Boolean)

    fun onRelease()

    fun onComplete()

    fun onReset()
}
