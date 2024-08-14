package com.example.ultrascore

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ultrascore.databinding.ActivityScoreBinding

class activity_score : AppCompatActivity() {

    private lateinit var prefs : SharedPreferences
    private lateinit var editor: Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val binding= ActivityScoreBinding.inflate(layoutInflater)
        editor=getSharedPreferences("score_list",Context.MODE_PRIVATE).edit()
        prefs=getSharedPreferences("score_list",Context.MODE_PRIVATE)

        val scoreList=ArrayList<Score>()

        var score_num=prefs.getInt("score_num",-1)
        val calendarInstance=Calendar.getInstance()
        var score=prefs.getInt("score_now",0)

        binding.textScore.text=score.toString()

        setContentView(binding.root)

        Log.e("score1","$score_num")

        binding.scoreRecycler.layoutManager=LinearLayoutManager(this)
        val adapter=ScoreAdapter(this,scoreList)
        binding.scoreRecycler.adapter=adapter

        /*
        第一次打开应用的时候，会初始化score_num为1，此后都是这样，最大的一个score是还在用的，剩下的都是历史
        点击按钮的时候，先把endyear，score（最终）存进去，再把score_num加一，储存新的 start
         */

        binding.buttonClearScore.setOnClickListener{
            binding.textScore.text="0"

            editor.apply {
                putInt("score_now",0)
                putInt("score_${score_num}",score)
                putInt("end_year_${score_num}",calendarInstance.get(Calendar.YEAR))
                putInt("end_month_${score_num}",calendarInstance.get(Calendar.MONTH))
                putInt("end_day_${score_num}",calendarInstance.get(Calendar.DAY_OF_MONTH))

                apply()
            }
            val newHistoryStart=Calendar.getInstance().apply{
                set(Calendar.YEAR,prefs.getInt("start_year_${score_num}",1))
                set(Calendar.MONTH,prefs.getInt("start_month_${score_num}",1))
                set(Calendar.DAY_OF_MONTH,prefs.getInt("start_day_${score_num}",1))
            }

            score_num+=1
            //这里稍微有点冗余，第一次之后所有score的结束就等于下一次的开始日期
            editor.apply { putInt("score_num",score_num)
                putInt("start_year_${score_num}",calendarInstance.get(Calendar.YEAR))
                putInt("start_month_${score_num}",calendarInstance.get(Calendar.MONTH))
                putInt("start_day_${score_num}",calendarInstance.get(Calendar.DAY_OF_MONTH))
                apply() }
            val newHistoryEnd=Calendar.getInstance()

            //TODO：探究a=b，改变b后a变不变
            val newHistoryScore=Score(score,newHistoryStart,newHistoryEnd)
            Log.e("button","$score")
            scoreList.add(newHistoryScore)
            //TODO:这个adapter可以优化
            adapter.notifyDataSetChanged()
            Log.e("score2","$score_num")
            score=0
        }

        //加载历史分数到recycler里
        if(score_num>1) {
            for (i in 1 until score_num ) {
                val total = prefs.getInt("score_$i", -114514)
                val start = with(calendarInstance) {
                    set(Calendar.YEAR, prefs.getInt("start_year_$i", 1))
                    set(Calendar.MONTH, prefs.getInt("start_month_$i", 1))
                    set(Calendar.DAY_OF_MONTH, prefs.getInt("start_day_$i", 1))
                    calendarInstance
                }
                val end = with(calendarInstance) {
                    set(Calendar.YEAR, prefs.getInt("end_year_$i", 1))
                    set(Calendar.MONTH, prefs.getInt("end_month_$i", 1))
                    set(Calendar.DAY_OF_MONTH, prefs.getInt("end_day_$i", 1))
                    calendarInstance
                }
                val scoreLoad = Score(total, start, end)
                scoreList.add(scoreLoad)
                adapter.notifyItemInserted(0)
            }
        }

    }
}