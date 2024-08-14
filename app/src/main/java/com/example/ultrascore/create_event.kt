package com.example.ultrascore

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ultrascore.databinding.ActivityCreateEventBinding

class create_event : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var event_switch_checked = false
        val binding = ActivityCreateEventBinding.inflate(layoutInflater)

        setContentView(binding.root)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val datepicker =binding.datepicker
        val date_picked =Calendar.getInstance()
        datepicker.init(date_picked.get(Calendar.YEAR),date_picked.get(Calendar.MONTH),
            date_picked.get(Calendar.DAY_OF_MONTH))
        {view,year,month,day->
            date_picked.apply{set(Calendar.YEAR,year)
                set(Calendar.DAY_OF_MONTH,day)
                set(Calendar.MONTH,month)
            }
        }
        //创建事件时设置日期和设置次数两种模式的切换
        binding.eventSwitch.setOnCheckedChangeListener{ _ , isChecked ->
            if(isChecked){binding.eventLayoutDateset.visibility= View.INVISIBLE
            binding.eventLayoutTimeset.visibility=View.VISIBLE
            event_switch_checked=true   }else{
                binding.eventLayoutDateset.visibility= View.VISIBLE
                binding.eventLayoutTimeset.visibility=View.INVISIBLE
                event_switch_checked=false
            }

        }
        binding.createEventButton.setOnClickListener{
            val intent=Intent()
            val content=binding.editContent.text.toString()
            val plus=if(binding.editPlus.text.isEmpty()){0} else binding.editPlus.text.toString().toInt()//空着默认为0，下同
            val minus=if(binding.editMinus.text.isEmpty()){0} else binding.editMinus.text.toString().toInt()
            val time=if(binding.editTime.text.isEmpty()){-123456789 }else
                binding.editTime.text.toString().toInt()
            val last=if(binding.editLastTime.text.isEmpty()){31}else
                binding.editLastTime.text.toString().toInt()

            intent.apply {
                putExtra("content",content)
                putExtra("plusa",plus)
                putExtra("minus",minus)
                putExtra("state",event_switch_checked)
                putExtra("time",time)
                putExtra("year",date_picked.get(Calendar.YEAR))
                putExtra("month",date_picked.get(Calendar.MONTH))
                putExtra("day",date_picked.get(Calendar.DAY_OF_MONTH))
                putExtra("last",last)
            }

            //规范性判断
            if(content.isEmpty()){ Toast.makeText(this,"事件内容不能为空",Toast.LENGTH_SHORT).show() }
            else if(plus.toString().toInt()>100){Toast.makeText(this,"加分不支持大于100",Toast.LENGTH_SHORT).show() }
            else if(minus.toString().toInt()>100){Toast.makeText(this,"减分不支持大于100",Toast.LENGTH_SHORT).show() }
            else{
                setResult(RESULT_OK,intent)
                finish()}

        }

    }
}