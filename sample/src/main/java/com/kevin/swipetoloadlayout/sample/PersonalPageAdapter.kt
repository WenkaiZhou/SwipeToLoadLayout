package com.kevin.swipetoloadlayout.sample

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

/**
 * PersonalPageAdapter
 *
 * @author zwenkai@foxmail.com, Created on 2019-09-30 12:45:08
 *         Major Function：<b></b>
 *         <p/>
 *         Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
class PersonalPageAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        if (position == 0) {
            return PersonalNotesFragment()
        }
        return PersonalNotesFragment()
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int) = if (position == 0) "笔记" else "提问"

}