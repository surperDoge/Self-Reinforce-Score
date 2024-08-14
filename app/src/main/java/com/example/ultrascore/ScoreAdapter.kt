package com.example.ultrascore

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScoreAdapter (val context: Context, val scoreList:ArrayList<Score>):
        RecyclerView.Adapter<ScoreAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text_history: TextView = view.findViewById(R.id.text_item_history)
        val text_score: TextView = view.findViewById(R.id.text_item_score)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.score_item, parent, false)
        val viewholder=ViewHolder(view)
        return viewholder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val score = scoreList[position]
        holder.text_score.text = score.total.toString()
        Log.e("adapter",score.end.getDate())
        val a = "${score.start.getDate()}-${score.end.getDate()}"
        holder.text_history.text = a
    }

    override fun getItemCount() = scoreList.size

        }