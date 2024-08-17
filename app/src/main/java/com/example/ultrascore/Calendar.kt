package com.example.ultrascore

import android.icu.util.Calendar
import android.util.Log

/*
本函数仅用于event_daily的计算，不能用用于真正的日期计算
会报错
a.until(b)是用a的时间减去b的时间，a更晚才输出正数
 */
fun Calendar.until(b:Calendar):Int{
    return if(this.get(Calendar.YEAR)==b.get(Calendar.YEAR)){
        this.get(Calendar.DAY_OF_YEAR)-b.get(Calendar.DAY_OF_YEAR)
    }else if(this.get(Calendar.YEAR)>b.get(Calendar.YEAR)){
        if(b.get(Calendar.YEAR)%4 !=0){this.get(Calendar.DAY_OF_YEAR) +365 -b.get(Calendar.DAY_OF_YEAR)+1}
        else{this.get(Calendar.DAY_OF_YEAR) +366 -b.get(Calendar.DAY_OF_YEAR)+1}
    }else{
        if(this.get(Calendar.YEAR)%4 !=0){-b.get(Calendar.DAY_OF_YEAR) -365 +this.get(Calendar.DAY_OF_YEAR)-1}
        else{-b.get(Calendar.DAY_OF_YEAR) -366 +this.get(Calendar.DAY_OF_YEAR)-1}}
}

/*
用于获取标准日期格式
 */
fun Calendar.getDate():String{
    return "${this.get(Calendar.YEAR)}/${this.get(Calendar.MONTH)+1}/${this.get(Calendar.DAY_OF_MONTH)}"
}