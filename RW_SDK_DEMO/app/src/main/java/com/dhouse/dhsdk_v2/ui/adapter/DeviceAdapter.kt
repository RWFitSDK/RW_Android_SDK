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
        val chargingStatus = when (item.batteryStatus) {
            0 -> context.getString(R.string.demo_battery_not_charging)
            1 -> context.getString(R.string.demo_battery_charging)
            2 -> context.getString(R.string.demo_battery_full)
            else -> null
        }
        val rssiText = "RSSI: ${item.bleRssi} dBm"
        holder.setText(R.id.rssiTv, chargingStatus?.let { "$rssiText · $it" } ?: rssiText)
    }

}
