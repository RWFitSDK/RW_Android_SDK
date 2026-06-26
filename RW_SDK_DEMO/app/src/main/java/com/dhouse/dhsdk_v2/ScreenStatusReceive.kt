package com.dhouse.dhsdk_v2

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.example.blesdk.ble.ScanBleService

/**
 * 注册监听手机亮灭屏
 */
class ScreenStatusReceive : BroadcastReceiver() {
    companion object {

        fun registerScreenReceive(context: Context?){
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_SCREEN_ON)
            filter.addAction(Intent.ACTION_SCREEN_OFF)
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            val screenStatusReceive = ScreenStatusReceive()
            context?.registerReceiver(screenStatusReceive,filter)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            Intent.ACTION_SCREEN_ON -> {
                Log.e("TAG", "ScreenStatusReceive " + "手机亮屏")
            }
            Intent.ACTION_SCREEN_OFF -> {

            }
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                //获取蓝牙广播中的蓝牙新状态
                val blueNewState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                //获取蓝牙广播中的蓝牙旧状态
                val blueOldState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, 0)

                when (blueNewState) {
                    //正在打开蓝牙
                    BluetoothAdapter.STATE_TURNING_ON -> {
                        //蓝牙正在打开中
                    }
                    //蓝牙已打开
                    BluetoothAdapter.STATE_ON -> {
//                        蓝牙打开
                        ScanBleService.getService().startScan(true,null)
                    }
                    //正在关闭蓝牙
                    BluetoothAdapter.STATE_TURNING_OFF -> {
//                        蓝牙正在被关闭
                    }
                    //蓝牙已关闭
                    BluetoothAdapter.STATE_OFF -> {
//                        蓝牙关闭
                    }
                }
            }
        }
    }
}