package com.example.ultrascore

import android.content.Context
import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


/*
点击事件来源：
https://www.practicalcoding.net/post/recyclerview-item-click-kotlin
 */
class EventAdapter (val context:Context,val eventList:ArrayList<Event>,val listener: ItemClickListener):
RecyclerView.Adapter<EventAdapter.ViewHolder>(){
    inner class ViewHolder(view: View):RecyclerView.ViewHolder(view),View.OnClickListener{
        val content:TextView=view.findViewById(R.id.event_content)
        val date:TextView=view.findViewById(R.id.event_date)
        val score:TextView=view.findViewById(R.id.event_score)
        val button:Button=view.findViewById(R.id.button_delete)

        init{
            button.setOnClickListener { val position=adapterPosition
                if(position!= RecyclerView.NO_POSITION){
                    listener.onDeleteClick(position)
                }
            }
            view.setOnClickListener {val position=adapterPosition
                if(position!= RecyclerView.NO_POSITION){
                    listener.onItemClick(position)
                }
            }
        }

        override fun onClick(p0: View?) {
            val position=adapterPosition
            if(position!= RecyclerView.NO_POSITION){
                listener.onDeleteClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(context)
            .inflate(R.layout.event_item,parent,false)
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
            val today=Calendar.getInstance()
            val b=event.last-today.until(event.create)
            val a="每日应完成${event.time}次,还要持续${b}天"
            holder.date.text=a
        }else{
            event=event as Event_Date
            val a="截止日期%${event.date.getDate()}"
            holder.date.text=a
        }
    }

    override fun getItemCount()=eventList.size
}