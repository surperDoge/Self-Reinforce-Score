package com.example.ultrascore

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.icu.util.Calendar
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ultrascore.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),ItemClickListener {

    private val eventList=ArrayList<Event>()
    private val adapter=EventAdapter(this,eventList,this)//这里不懂
    private var event_uncomplete= mutableSetOf<String>()


    private lateinit var prefs_score:SharedPreferences
    private lateinit var editor_score:Editor


    //这种写法good
    /*
     简述event_list的sharedprefs逻辑
     储存了内容，事件，次数等等系列事件数据
     以及未完成事件，事件总编号等
     每个事件都有一个独一无二的编号
     */
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
            val last=result.data?.getIntExtra("last",-100) as Int

            val date=Calendar.getInstance()

            //合理设计事件编号
            var event_num=prefs.getInt("event_num",0)
            event_num=event_num+1

            //根据事件类型创建显示的item并存储
            if(state){
                val create=Calendar.getInstance()
                create.apply {
                    set(Calendar.YEAR ,year )
                    set(Calendar.MONTH ,month)
                    set(Calendar.DAY_OF_MONTH ,day)}
                val event=Event_Daily(content!!,plus!!,minus!!,event_num,state,time,last,create)
                //显示新增的事件
                eventList.add(event)
                adapter.notifyItemInserted(adapter.itemCount-1)

                event_uncomplete.add("${event_num}")
                storeEvent(event,event_num,true)//完成信息存储
                editor.apply{
                    putInt("time_${event_num}",time)
                    putInt("last_${event_num}",last)
                    //顺便记录事件的创建日期
                    putInt("create_year_${event_num}",date.get(Calendar.YEAR))
                    putInt("create_month_${event_num}",date.get(Calendar.MONTH))
                    putInt("create_day_${event_num}",date.get(Calendar.DAY_OF_MONTH))
                    apply() }
            }else{
                date.apply{set(Calendar.YEAR,year)
                    set(Calendar.DAY_OF_MONTH,day)
                    set(Calendar.MONTH,month)}
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
                val create=Calendar.getInstance()
                create.apply {
                    set(Calendar.YEAR ,prefs.getInt("create_year_$i",0))
                    set(Calendar.MONTH ,prefs.getInt("create_month_$i",0))
                    set(Calendar.DAY_OF_MONTH ,prefs.getInt("create_day_$i",0))}
                val event = Event_Daily(
                    prefs.getString("content_${i}", "you stupid, bug!!")!!,
                    prefs.getInt("plus_${i}", 0),
                    prefs.getInt("minus_${i}", 0),
                    prefs.getInt("series_$i", -114514),//这里写这个数相当于报错
                    true,
                    prefs.getInt("time_$i",-100),
                    prefs.getInt("last_$i",-100),
                    create
                )//这里也有默认每天完成
            eventList.add(event)
            adapter.notifyItemInserted(adapter.itemCount-1)}
            else{
                val date=Calendar.getInstance()
                date.apply{set(Calendar.YEAR,prefs.getInt("date_year_$i",1))
                    set(Calendar.MONTH,prefs.getInt("date_month_$i",1))
                    set(Calendar.DAY_OF_MONTH,prefs.getInt("date_day_$i",1))}
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

        //分数初始化
        prefs_score=getSharedPreferences("score_list",Context.MODE_PRIVATE)
        editor_score=getSharedPreferences("score_list",Context.MODE_PRIVATE).edit()
        if (prefs_score.getInt("score_num",0)==0){
            editor_score.apply{
                putInt("score_num",1)
                putInt("start_year_1",today.get(Calendar.YEAR))
                putInt("start_month_1",today.get(Calendar.MONTH))
                putInt("start_day_1",today.get(Calendar.DAY_OF_MONTH))
                apply()
            }

        }
        //弹窗强制确认是否完成事件
        for(i in eventList){
            judgeFinished(i,today)
        }
    }
    //莫名其妙的bug：点击那个绿色的run不叫destroy

    //强制弹出确认事件是否完成的弹窗
    private fun judgeFinished(event: Event,today:Calendar){

        var score=prefs_score.getInt("score_now",0)

        //TODO:事件完成强制判断优化
        //但以后精确到time就要改了
        //这个写法是有问题的
        if(event is Event_Date && event.date.until(today)<0)//&&与 ||或 !非
        {val bulider=AlertDialog.Builder(this)
            bulider.apply {
            setTitle("你完成了吗？")
            setMessage("您的事件\n“${event.content}”应该在${event.date.get(Calendar.YEAR)}" +
                    "/${event.date.get(Calendar.MONTH) + 1}" +
                    "/${event.date.get(Calendar.DAY_OF_MONTH)}就到期了哟")
                setCancelable(false)
            setPositiveButton("完!成!"){_,_ ->
                score+=event.plus_score
                deleteEvent(event)
                adapter.notifyDataSetChanged()//TODO:adapter优化
                editor_score.apply { putInt("score_now",score)
                apply()}
                }
            setNegativeButton("没有！"){_,_->
                score-=event.minus_score
                deleteEvent(event)
                adapter.notifyDataSetChanged()
                editor_score.apply { putInt("score_now",score)
                    apply()}
            }
            show()
            }
        }

        /*
        这里写一下 event_daily的逻辑，就是判断今天到创建的日期已经几号了，有没有超过last
        然后通过和储存的“已判断次数”(already)进行对比，最后确认应该弹出几次这个弹窗，配上时间

         */

        else if (event is Event_Daily )
        {   var already=prefs.getInt("already_${event.series}",0)

            while( already<Calendar.getInstance().until(event.create)){

                val b=event.create//防止影响循环判断
                b.add(Calendar.DAY_OF_YEAR,1)

                val builder=AlertDialog.Builder(this)
                val array= arrayOfNulls<CharSequence>(event.time+1)
                for(i in 0 until event.time+1){
                    array[i]=i.toString()
                }//你知道吗,string继承charsequence
                /*
                https://stackoverflow.com/questions/7861279/how-to-set-single-choice-items-inside-alertdialog
                Seems that Buttons, Message and Multiple choice items are mutually exclusive.
                Try to comment out setMessage(), setPositiveButton() and setNegativeButton().
                 Didn't check it myself.
                https://developer.android.com/develop/ui/views/components/dialogs?hl=zh-cn#DialogFragment
                "Because the list appears in the dialog's content area,
                the dialog cannot show both a message and a list
                and you should set a title for the dialog with setTitle()."
                 */
                var choose=0
                builder.apply {
                    setCancelable(false)
                    setTitle("在${b.get(Calendar.DAY_OF_MONTH)}号,你完成了几次事件${event.content}?")
                    //setMessage("您的事件要求")
                    setSingleChoiceItems(array ,0){_,which->
                        choose=which
                    }
                    setPositiveButton("确认"){_,_->
                        score+=event.plus_score*choose
                        score-=event.minus_score*(event.time-choose)
                        editor_score.apply { putInt("score_now",score)
                            apply()}
                    }
                    show()
                }
                already += 1
            }
            editor.apply{

                putInt("already_${event.series}",already)
                apply()
            }
        }
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
            R.id.menu_score->{

                startActivity(Intent(this,activity_score::class.java))
            }
        }
        return true
    }

    private fun deleteEvent(event: Event){
        eventList.remove(event)
        event_uncomplete.remove("${event.series}")

        editor.remove("event_uncomplete")
        editor.commit()
        editor.putStringSet("event_uncomplete",event_uncomplete)
        editor.apply()
        //这里eventlist的文件没改，但是log输出是对的？？？？？？？？？？？？？？？？？？？？
        //明天去研究下sharedprefs的写入逻辑吧！
        //一天以后：确实是sharedprefs有bug，从这里读取的数据更新会麻烦一点，难以保持数据的一致性
    }

    //删除事件的逻辑
    override fun onDeleteClick(positon: Int) {
        val event=eventList[positon]
        val builderConfirm=AlertDialog.Builder(this)
        builderConfirm.apply {
            setTitle("删除事件")
            setMessage("确认要删除${event.content}吗?")
            setPositiveButton("确认"){_,_->
                deleteEvent(event)
                adapter.notifyItemRemoved(positon)
            }
            setNegativeButton("取消"){_,_ ->}
            show()
            }


    }

    override fun onItemClick(position: Int) {
        var score=prefs_score.getInt("score_now",0)
        val event=eventList[position]

        if(event is Event_Date)
        {val bulider=AlertDialog.Builder(this)
            bulider.apply {
                setTitle("你完成了吗？")
                setMessage("关于您的事件\n“${event.content}“")
                setCancelable(false)
                setPositiveButton("完!成!"){_,_ ->
                    score+=event.plus_score
                    deleteEvent(event)
                    adapter.notifyDataSetChanged()//这里以后换个方法
                    editor_score.apply { putInt("score_now",score)
                        apply()}
                }
                setNegativeButton("没有"){_,_-> }
                show()
            }
        }else{Toast.makeText(this,"阿巴阿巴这个待开发哦",Toast.LENGTH_SHORT).show()}
    }

}