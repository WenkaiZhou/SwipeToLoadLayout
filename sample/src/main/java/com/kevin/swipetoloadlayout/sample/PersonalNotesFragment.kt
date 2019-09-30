package com.kevin.swipetoloadlayout.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.kevin.delegationadapter.extras.load.LoadDelegationAdapter
import com.kevin.swipetoloadlayout.OnRefreshListener
import kotlinx.android.synthetic.main.fragment_personal_notes.*
import java.util.*

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

    private lateinit var delegationAdapter: LoadDelegationAdapter

    private var lastId = 0

    // 加载的次数，用于模拟场景
    private var count: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_personal_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化刷新控件
        initRefreshView()
        initRecyclerView()
        initLoadMore()

        // 第一次进入，模拟用户下拉刷新
        swipeToLoadLayout.isRefreshing = true
    }

    private fun initRefreshView() {
        // 尽管该控件也可以做加载更多的监听，但是不建议使用它用来加载更多
        swipeToLoadLayout.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                swipeToLoadLayout.postDelayed({
                    swipeToLoadLayout.isRefreshing = false
                    delegationAdapter.reset()
                    lastId = 0

                    // 更新内容
                    count = 0
                    val stringList = getData()
                    delegationAdapter.setDataItems(stringList)
                }, 2000)
            }
        })
    }

    private fun initRecyclerView() {
        // 设置LayoutManager
        val recyclerView = recycler_view
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        // 设置Adapter
        delegationAdapter = LoadDelegationAdapter()
        // 添加委托Adapter
        delegationAdapter
            .setLoadDelegate(LoadAdapterDelegate())
            .addDelegate(TextAdapterDelegate())
        recyclerView.adapter = delegationAdapter
    }

    private fun initLoadMore() {
        val recyclerView = recycler_view
        delegationAdapter.setOnLoadListener(object : LoadDelegationAdapter.OnLoadListener {
            override fun onLoadMore() {
                recyclerView.postDelayed({
                    delegationAdapter.setLoading(false)
                    if (count == 2) {
                        // 模拟加载失败
                        delegationAdapter.setLoadFailed()
                    } else if (count == 5) {
                        // 模拟加载完成，没有更多数据
                        val stringList = getData()
                        delegationAdapter.addDataItems(stringList)
                        delegationAdapter.setLoadCompleted()
                    } else {
                        // 模拟加载数据
                        val stringList = getData()
                        delegationAdapter.addDataItems(stringList)
                    }
                    count++
                }, 2000)
            }
        })
    }

    /**
     * 假装获取条目数据
     *
     * @return
     */
    private fun getData(): MutableList<String> {
        val stringList = mutableListOf<String>()
        val num = Random().nextInt(10) + 5
        for (i in 0 until num) {
            stringList.add(" 条目：" + lastId++)
        }
        return stringList
    }

}