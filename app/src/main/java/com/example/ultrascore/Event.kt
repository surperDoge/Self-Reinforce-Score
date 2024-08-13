package com.example.ultrascore

import android.icu.util.Calendar
import java.io.Serializable

open class Event (
    val content:String,val plus_score:Int,val minus_score:Int,var series:Int,val state:Boolean){

}
//限定日期
class Event_Date(val content_a:String,val plus_score_a:Int,val minus_score_a:Int,var series_a:Int,val state_a:Boolean,val date:Calendar,)
    :Event(content_a,plus_score_a,minus_score_a,series_a,state_a)
//每日挑战
class Event_Daily(content_b:String,val plus_score_b:Int,val minus_score_b:Int,var series_b:Int,val state_b:Boolean,val time:Int,val last:Int)
    :Event(content_b,plus_score_b,minus_score_b,series_b,state_b)

