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
        val deviceFormRes = when (item.deviceForm) {
            0 -> R.string.demo_device_form_screenless_ring
            1 -> R.string.demo_device_form_screen_ring
            2 -> R.string.demo_device_form_screenless_bracelet
            3 -> R.string.demo_device_form_screen_bracelet
            4 -> R.string.demo_device_form_screenless_wristband
            5 -> R.string.demo_device_form_screen_wristband
            6 -> R.string.demo_device_form_watch
            else -> null
        }
        holder.setText(R.id.deviceFormTv, deviceFormRes?.let { context.getString(it) } ?: "")
        holder.setGone(R.id.deviceFormTv, deviceFormRes == null)
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
