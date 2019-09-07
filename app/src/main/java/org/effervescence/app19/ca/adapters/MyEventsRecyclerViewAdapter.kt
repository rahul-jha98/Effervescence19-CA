package org.effervescence.app19.ca.adapters

import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import kotlinx.android.synthetic.main.fragment_events_list_item.view.*
import org.effervescence.app19.ca.models.EventDetails
import androidx.core.content.ContextCompat.startActivity
import android.R
import android.util.Log
import android.widget.Toast
import org.effervescence.app19.ca.activities.HomeActivity


class MyEventsRecyclerViewAdapter(@get:JvmName("getEventsList_") private var mEventsList: ArrayList<EventDetails>)
    : RecyclerView.Adapter<MyEventsRecyclerViewAdapter.MyViewHolder>() {

    private var mListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClicked(position: Int)
    }

    fun setOnClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(org.effervescence.app19.ca.R.layout.fragment_events_list_item, parent, false)
        return MyViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentEvent = mEventsList[position]

        holder.titleTV.text = currentEvent.task
        holder.descriptionTV.text = currentEvent.note
        holder.pointsTV.text = currentEvent.points.toString()

        holder.pointTextTV.text = if (currentEvent.points > 1) "pts" else "point"

    }

    override fun getItemCount(): Int = mEventsList.size

    inner class MyViewHolder(mView: View, mListener: OnItemClickListener?) : RecyclerView.ViewHolder(mView) {

        var titleTV = mView.event_title
        var descriptionTV = mView.event_description
        var pointsTV = mView.events_points
        var pointTextTV = mView.point_text_TV
        var uploadButton = mView.upload_button

        init {
            uploadButton.setOnClickListener {
                if (mListener != null) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClicked(position)
                    }
                }
            }
            descriptionTV.setOnClickListener{
                if (mListener != null) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClicked(position)
                    }
                }
            }

        }
    }

    fun swapList(list: ArrayList<EventDetails>){
        mEventsList = list
    }
}