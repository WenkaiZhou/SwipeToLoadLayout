package com.kevin.swipetoloadlayout.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.kevin.swipetoloadlayout.OnLoadMoreListener
import com.kevin.swipetoloadlayout.OnRefreshListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipeToLoadLayout.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                swipeToLoadLayout.postDelayed({
                    swipeToLoadLayout.isRefreshing = false
                }, 2000)
            }
        })

        swipeToLoadLayout.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                swipeToLoadLayout.postDelayed({
                    swipeToLoadLayout.isLoadingMore = false
                }, 2000)
            }

        })

        val recyclerView = recycler_view

        val layoutManager = LinearLayoutManager(this)
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
