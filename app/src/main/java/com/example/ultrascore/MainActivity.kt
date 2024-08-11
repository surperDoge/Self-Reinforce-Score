package com.example.ultrascore

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ultrascore.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),ItemClickListener {

    private val eventList=ArrayList<Event>()
    private val adapter=EventAdapter(this,eventList,this)//这里不懂
    private var event_uncomplete= mutableSetOf<String>()

    //这种写法good
    private lateinit var editor:Editor
    private lateinit var prefs:SharedPreferences

    //创建事件时接收信息，添加到recycler的逻辑
    private val requestDataLauncher =registerForActivityResult(ActivityResultContracts
        .StartActivityForResult()){result->
        if(result.resultCode== RESULT_OK){

            //读取从create_event传来的数据
            val content=result.data?.getStringExtra("content")
            val plus=result.data?.getIntExtra("plusa",0)
            val minus=result.data?.getIntExtra("minus",0)
            val state=result.data?.getBooleanExtra("state",true)as Boolean//true为每天完成，false为限定日期 //这里写个as重载就下面就不用加!!了
            val time=result.data?.getIntExtra("time",-1996)as Int
            //把create_event的年，月，日分别取出来，存储也是分别存储，所以date需要重写
            val year=result.data?.getIntExtra("year",1) as Int
            val month=result.data?.getIntExtra("month",1) as Int
            val day=result.data?.getIntExtra("day",1)as Int

            val date=Calendar.getInstance()
            date.apply{set(Calendar.YEAR,year)
                set(Calendar.DAY_OF_MONTH,day)
                set(Calendar.MONTH,month)}

            //合理设计事件编号
            var event_num=prefs.getInt("event_num",0)
            event_num=event_num+1

            //根据事件类型创建显示的item并存储
            if(state){
                val event=Event_Daily(content!!,plus!!,minus!!,event_num,state,time)
                //显示新增的事件
                eventList.add(event)
                adapter.notifyItemInserted(adapter.itemCount-1)

                event_uncomplete.add("${event_num}")
                storeEvent(event,event_num,true)//完成信息存储
                editor.putInt("time_${event_num}",time).apply()
            }else{
                val event=Event_Date(content!!,plus!!,minus!!,event_num,state,date)
                eventList.add(event)
                adapter.notifyItemInserted(adapter.itemCount-1)

                event_uncomplete.add("${event_num}")
                storeEvent(event,event_num,false)//完成信息存储
                editor.apply{putInt("date_year_${event_num}",year)
                            putInt("date_month_${event_num}",month)
                            putInt("date_day_${event_num}",day)
                            apply()}
            }
        }

    }

    /*
    逻辑：
    event_num记录从古至今所有被存储的事件编号数目，主要用于给新事件加编号
    event_umcomplete记录所有未完成事件（会显示到主屏幕）的编号
     */
    private fun storeEvent(event:Event,event_num:Int,state:Boolean){

        editor.apply{
            putString("content_${event_num}",event.content)
            putInt("plus_${event_num}",event.plus_score)
            putInt("minus_${event_num}",event.minus_score)
            putInt("series_${event_num}",event_num)
            putStringSet("event_uncomplete",event_uncomplete)
            putInt("event_num",event_num)
            putBoolean("state_${event_num}",state)
            apply()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbar)
        setContentView(binding.root)//这个很重要
        //正式定义sharedprefs
        prefs=getSharedPreferences("eventlist",Context.MODE_PRIVATE)
        editor =getSharedPreferences("eventlist",Context.MODE_PRIVATE).edit()

        event_uncomplete=prefs.getStringSet("event_uncomplete",event_uncomplete)!!//加载未完成事件列表
        //加载事件到recyler里
        for (i in event_uncomplete.map{it.toInt()}){
            if(prefs.getBoolean("state_$i",true)) {
                val event = Event_Daily(
                    prefs.getString("content_${i}", "you stupid, bug!!")!!,
                    prefs.getInt("plus_${i}", 0),
                    prefs.getInt("minus_${i}", 0),
                    prefs.getInt("series_$i", -114514),//这里写这个数相当于报错
                    true,prefs.getInt("time_$i",-100)
                )//这里也有默认每天完成
            eventList.add(event)
            adapter.notifyItemInserted(adapter.itemCount-1)}else{
                val date=Calendar.getInstance()
                date.apply{set(Calendar.YEAR,prefs.getInt("date_year_$i",1))
                    set(Calendar.YEAR,prefs.getInt("date_month_$i",1))
                    set(Calendar.YEAR,prefs.getInt("date_day_$i",1))}
                val event = Event_Date(
                    prefs.getString("content_${i}", "you stupid, bug for content in oncreate!!")!!,
                    prefs.getInt("plus_${i}", 0),
                    prefs.getInt("minus_${i}", 0),
                    prefs.getInt("series_$i", -114514),//这里写这个数相当于报错
                    false,
                    date)//这里也有默认每天完成
                eventList.add(event)
                adapter.notifyItemInserted(adapter.itemCount-1)
            }
        }
        binding.recyclerView.layoutManager=LinearLayoutManager(this)
        binding.recyclerView.adapter=adapter
        val today=Calendar.getInstance()

        //记录一下这次打开软件的时间，方便判断event_daily什么时候跳弹窗
        editor.apply{putInt("last_open_year",today.get(Calendar.YEAR))
                    putInt("last_open_day",today.get(Calendar.DAY_OF_YEAR))
                    apply()}


        for(i in eventList){
            judgeFinished(i,today)
        }
    }
    //莫名其妙的bug：点击那个绿色的run不叫destroy

    //强制弹出确认事件是否完成的弹窗
    private fun judgeFinished(event: Event,today:Calendar){
        val daily= today.get(Calendar.YEAR)>prefs.getInt("last_open_year",today.get(Calendar.YEAR))||
                today.get(Calendar.DAY_OF_YEAR)>prefs.getInt("last_open_day",today.get(Calendar.DAY_OF_YEAR))
        if(event is Event_Date && event.date.before(today))//&&与 ||或 !非
        {AlertDialog}

        else if(event is Event_Daily&& daily){}
    }

    //这两个是右上角菜单的重写
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.menu_create -> {val intent=Intent(this,create_event::class.java)
                requestDataLauncher.launch(intent)}
        }
        return true
    }

    //删除事件的逻辑
    override fun onItemClick(positon: Int) {
        val event=eventList[positon]
        eventList.remove(event)
        adapter.notifyItemRemoved(positon)
        Log.e("onItemClick","clicked series:${event.series}")
        event_uncomplete.remove("${event.series}")
        Log.e("onItemClick","$event_uncomplete")

        editor.remove("event_uncomplete")
        editor.commit()
        editor.putStringSet("event_uncomplete",event_uncomplete)
        editor.apply()
        //这里eventlist的文件没改，但是log输出是对的？？？？？？？？？？？？？？？？？？？？
        //明天去研究下sharedprefs的写入逻辑吧！
        //一天以后：确实是sharedprefs有bug，从这里读取的数据更新会麻烦一点，难以保持数据的一致性
    }
}