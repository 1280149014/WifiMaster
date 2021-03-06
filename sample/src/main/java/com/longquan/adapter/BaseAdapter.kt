package com.longquan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.longquan.R
import com.longquan.bean.SsidBean

import kotlinx.android.synthetic.main.recycleview_item.view.*

/**
 * author : charile yuan
 * date   : 21-2-18
 * desc   :
 */
class BaseAdapter(private val list: ArrayList<SsidBean>) : Adapter<RecyclerView.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycleview_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val ssidBean = list[position]
        (holder.itemView).wifi_name.text = ssidBean.ssid
    }

}