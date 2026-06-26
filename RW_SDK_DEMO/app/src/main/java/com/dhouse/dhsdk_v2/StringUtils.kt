package com.dhouse.dhsdk_v2

import android.content.Context

object StringUtils {
    fun getWeekArray(context: Context, weeks: IntArray?) : String{
        return if (weeks != null && weeks.isNotEmpty()){
            val buffer = StringBuffer()
            var repeat = 0
            weeks.forEachIndexed{index, i ->
                if (i == 1){
                    buffer.append(getWeekStr(context,index.toFloat()))
                    buffer.append(",")
                    repeat ++
                }
            }
            if (repeat == 7){
                buffer.delete(0,buffer.length)
                buffer.append("每天")
                buffer.toString()
            }else if (buffer.isNotEmpty()){
                buffer.toString().substring(0,buffer.lastIndexOf(","))
            }else{
                "不重复"
            }
        }else{
            "不重复"
        }
    }

    fun getWeekStr(context : Context,week : Float) : String{
        return when(week){
            0f -> "sun"
            1f -> "mon"
            2f -> "tue"
            3f -> "wed"
            4f -> "thu"
            5f -> "fri"
            6f -> "sat"
            else -> "sun"
        }
    }
}