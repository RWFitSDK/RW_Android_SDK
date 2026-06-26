package com.dhouse.dhsdk_v2.ui.set.adapter

import androidx.appcompat.widget.SwitchCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.dhouse.dhsdk_v2.R
import com.dhouse.dhsdk_v2.ui.set.bean.SwitchBean

class SwitchAdapter(items : MutableList<SwitchBean>) : BaseQuickAdapter<SwitchBean,
        BaseViewHolder>(R.layout.item_switch,items) {

    override fun convert(holder: BaseViewHolder, item: SwitchBean) {
        holder.setText(R.id.name,item.name)
        val switch = holder.getView<SwitchCompat>(R.id.switch_button)
        switch.isChecked  = item.isOpen
        switch.setOnCheckedChangeListener { _, isChecked ->
            item.isOpen = isChecked
        }
    }
}