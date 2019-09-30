package com.kevin.swipetoloadlayout.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.kevin.swipetoloadlayout.OnRefreshListener
import kotlinx.android.synthetic.main.fragment_personal_notes.*

/**
 * PersonalNotesFragment
 *
 * @author zwenkai@foxmail.com, Created on 2019-09-30 12:47:42
 *         Major Function：<b></b>
 *         <p/>
 *         Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
class PersonalNotesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_personal_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeToLoadLayout.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                swipeToLoadLayout.postDelayed({
                    swipeToLoadLayout.isRefreshing = false
                }, 2000)
            }
        })

        val recyclerView = recycler_view

        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        val adapter = MyAdapter(getData())
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = DefaultItemAnimator()
    }

    private fun getData(): MutableList<String> {
        val data = mutableListOf<String>()
        val temp = " item"
        for (i in 0..15) {
            data.add(i.toString() + temp)
        }
        return data
    }

}