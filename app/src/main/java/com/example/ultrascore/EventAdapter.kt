package com.example.ultrascore

import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

class EventAdapter (val context:Context,val eventList:ArrayList<Event>,val listener: ItemClickListener):
RecyclerView.Adapter<EventAdapter.ViewHolder>(){
    inner class ViewHolder(view: View):RecyclerView.ViewHolder(view),View.OnClickListener{
        val content:TextView=view.findViewById(R.id.event_content)
        val date:TextView=view.findViewById(R.id.event_date)
        val score:TextView=view.findViewById(R.id.event_score)
        val button:Button=view.findViewById(R.id.button_delete)

        init{
            button.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position=adapterPosition
            if(position!= RecyclerView.NO_POSITION){
                listener.onItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.event_item,parent,false)
        val viewholder=ViewHolder(view)
        return viewholder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var event=eventList[position]
        val what_score="+${event.plus_score},-${event.minus_score}"
        holder.content.text=event.content
        //holder.date.text=event.date
        holder.score.text=what_score
        if(event.state){
            event=event as Event_Daily
            val a="每日应完成${event.time}次"
            holder.date.text=a
        }else{
            event=event as Event_Date
            val a="截止日期${event.date.get(Calendar.YEAR)}/${event.date.get(Calendar.MONTH)+1}/${event.date.get(Calendar.DAY_OF_MONTH)}"
            holder.date.text=a
        }
    }

    override fun getItemCount()=eventList.size
}