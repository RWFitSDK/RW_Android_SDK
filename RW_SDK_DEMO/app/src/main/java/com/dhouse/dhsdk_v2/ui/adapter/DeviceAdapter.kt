package com.dhouse.dhsdk_v2.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.dhouse.dhsdk_v2.R
import com.example.blesdk.ble.bean.BleDevice

class DeviceAdapter(data : MutableList<BleDevice>) :
    BaseQuickAdapter<BleDevice,BaseViewHolder>(R.layout.item_device,data) {

    override fun convert(holder: BaseViewHolder, item: BleDevice) {
        holder.setText(R.id.nameTv,item.bleName)
        holder.setText(R.id.macTv,item.bleMac)
    }

}