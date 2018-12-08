package com.komatsu.ufoterminal

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.record_item.view.*

class UFORecordRecyclerAdapter(
        private val context: Context?,
        private val data: List<UFORecordFile>,
        private val listener: OnRecyclerListener
) : RecyclerView.Adapter<UFORecordRecyclerAdapter.ViewHolder>() {


    interface OnRecyclerListener {
        fun onRecordSelected(fileName: String)
        fun onRecordRenameStart(fileName: String)
        fun onRecordDeleteStart(fileName: String)
    }

    class ViewHolder(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val titleView: TextView = itemView.recordTitle
        val timeView: TextView = itemView.recordTime
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): UFORecordRecyclerAdapter.ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        return UFORecordRecyclerAdapter.ViewHolder(inflater.inflate(R.layout.record_item, p0, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(vh: UFORecordRecyclerAdapter.ViewHolder, i: Int) {
        if (data.size <= i) return

        vh.titleView.text = data[i].title
        vh.timeView.text = data[i].created

        vh.itemView.setOnClickListener { listener.onRecordSelected(data[i].title) }
        vh.itemView.recordRename.setOnClickListener { listener.onRecordRenameStart(data[i].title) }
        vh.itemView.recordDelete.setOnClickListener { listener.onRecordDeleteStart(data[i].title) }
    }
}