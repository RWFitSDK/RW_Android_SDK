package com.dhouse.dhsdk_v2.ui.set.adapter

import androidx.appcompat.widget.SwitchCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.dhouse.dhsdk_v2.R
import com.dhouse.dhsdk_v2.StringUtils
import com.dhouse.dhsdk_v2.ui.set.bean.AlarmBean

class AlarmAdapter(items : MutableList<AlarmBean>) : BaseQuickAdapter<AlarmBean,
        BaseViewHolder>(R.layout.item_alarm,items) {

    override fun convert(holder: BaseViewHolder, item: AlarmBean) {
        val switch = holder.getView<SwitchCompat>(R.id.switch_status)
        switch.isChecked  = item.isSelected
        holder.setText(R.id.time, String.format("%02d:%02d",item.hour,item.min))
        holder.setText(R.id.tag,item.tag)
        holder.setText(R.id.repeat, StringUtils.getWeekArray(context,item.repeat))
        switch.setOnCheckedChangeListener { _, isChecked ->
            item.isSelected = isChecked
        }
    }
}