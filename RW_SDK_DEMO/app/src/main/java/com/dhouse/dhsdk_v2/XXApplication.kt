package com.dhouse.dhsdk_v2

import android.annotation.SuppressLint
import android.app.Application
import com.example.blesdk.bean.function.SupportMenuBean
import com.example.blesdk.ble.bean.BleDevice

class XXApplication : Application() {
    companion object {
        private const val TAG = "BaseApplication"

        @SuppressLint("StaticFieldLeak")
        lateinit var instance: XXApplication
            private set
    }

    var isEnterOta = false
    var isEnterScanUIPage = false
    var currentConnectDevcie: BleDevice? = null
    var currentSupportMenuBean: SupportMenuBean? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}