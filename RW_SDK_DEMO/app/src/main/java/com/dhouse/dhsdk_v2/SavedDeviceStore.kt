package com.dhouse.dhsdk_v2

import android.content.Context
import com.example.blesdk.ble.bean.BleDevice

object SavedDeviceStore {
    private const val PREF_NAME = "rw_ble_demo_saved_device"
    private const val KEY_NAME = "name"
    private const val KEY_MAC = "mac"
    private const val KEY_DEVICE_ID = "device_id"
    private const val KEY_RSSI = "rssi"
    private const val KEY_PID_TYPE = "pid_type"
    private const val KEY_RAW_DATA_FLAG = "raw_data_flag"

    fun save(context: Context, device: BleDevice?) {
        device ?: return
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_NAME, device.bleName.orEmpty())
            .putString(KEY_MAC, device.bleMac.orEmpty())
            .putString(KEY_DEVICE_ID, device.bleDeviceId.orEmpty())
            .putInt(KEY_RSSI, device.bleRssi)
            .putInt(KEY_PID_TYPE, device.pidType)
            .putString(KEY_RAW_DATA_FLAG, device.rawDataFlag.orEmpty())
            .apply()
    }

    fun load(context: Context): BleDevice? {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val mac = pref.getString(KEY_MAC, null)?.takeIf { it.isNotBlank() } ?: return null
        return BleDevice().apply {
            bleName = pref.getString(KEY_NAME, "").orEmpty()
            bleMac = mac
            bleDeviceId = pref.getString(KEY_DEVICE_ID, "").orEmpty()
            bleRssi = pref.getInt(KEY_RSSI, 0)
            pidType = pref.getInt(KEY_PID_TYPE, 0)
            rawDataFlag = pref.getString(KEY_RAW_DATA_FLAG, "").orEmpty()
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
