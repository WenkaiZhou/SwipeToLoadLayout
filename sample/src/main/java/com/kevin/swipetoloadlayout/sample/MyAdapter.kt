package com.kevin.swipetoloadlayout.sample

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import java.util.ArrayList

/**
 * MyAdapter
 *
 * @author zwenkai@foxmail.com, Created on 2019-09-20 12:45:51
 * Major Function：****
 *
 *
 * Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
class MyAdapter(private var mData: MutableList<String>?) :
    RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    fun updateData(data: ArrayList<String>) {
        this.mData = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_rv_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 绑定数据
        holder.mTv.text = mData!![position]

        holder.mTv.setOnClickListener {
            Log.e("test1", "dispatchTouchEvent 点击啦")
            Toast.makeText(holder.itemView.context, "点击了: ${mData!![position]}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return if (mData == null) 0 else mData!!.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var mTv = itemView.findViewById<TextView>(R.id.item_tv)
    }
}