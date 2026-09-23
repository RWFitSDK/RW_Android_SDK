package com.dhouse.dhsdk_v2.demo

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.StringRes
import com.dhouse.dhsdk_v2.R
import com.dhouse.dhsdk_v2.SavedDeviceStore
import com.dhouse.dhsdk_v2.XXApplication
import com.example.blesdk.DHBleSdk
import com.example.blesdk.bean.function.FirmVersionBean
import com.example.blesdk.bean.function.PowerBean
import com.example.blesdk.bean.function.SupportMenuBean
import com.example.blesdk.bean.function.TouchEventBean
import com.example.blesdk.bean.sync.*
import com.example.blesdk.ble.bean.BleDevice
import com.example.blesdk.blering.RingBleError
import com.example.blesdk.blering.RingConnectBleCallback
import com.example.blesdk.callback.HealthDataSyncCallback
import com.example.blesdk.callback.data.HealthMeasurementCallback
import com.example.blesdk.blering.OnDevicePushListener
import com.example.blesdk.blering.DevicePushType
import com.example.blesdk.blering.PushData
import com.example.blesdk.bean.sync.MuslimCountItemBean
import com.example.blesdk.callback.data.HealthMeasurementResult
import com.example.blesdk.callback.data.HealthRealtimeValue
import com.example.blesdk.blering.HealthDataType
import com.example.blesdk.callback.data.FirmwareCallback
import com.example.blesdk.callback.data.PowerCallback
import com.example.blesdk.callback.data.TakePhotoCallback
import com.example.blesdk.callback.status.MusicPushSettingCallback
import com.example.blesdk.utils.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArraySet

data class DemoUiState(
    val device: BleDevice? = null,
    val supportMenu: SupportMenuBean? = null,
    val connected: Boolean = false,
    val ready: Boolean = false,
    val connecting: Boolean = false,
    val connectionMessage: String = "",
    val battery: Int? = null,
    val firmwareVersion: String? = null,
    val deviceModel: String? = null,
    val syncing: Boolean = false,
    val syncProgress: Int = 0,
    val activeMeasurement: DemoHealthType? = null,
    val summaryValues: Map<String, DemoHealthRecord> = emptyMap(),
    val realtimeValues: Map<String, DemoHealthRecord> = emptyMap(),
    val records: Map<String, List<DemoHealthRecord>> = emptyMap()
)

/** Demo-only in-memory state. Health records are intentionally never persisted. */
object DemoStateStore : RingConnectBleCallback, HealthDataSyncCallback {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val listeners = CopyOnWriteArraySet<(DemoUiState) -> Unit>()
    private var appContext: Context? = null
    private var callbacksSubscribed = false
    private val stepDays = linkedMapOf<String, StepDayData>()

    @Volatile
    var state: DemoUiState = DemoUiState()
        private set

    /** 设备主动推送(常驻): 赞念计数 + 电量推送 + 触摸事件, 一个监听器按 type 分流; BLE线程回调, 转主线程更新。
     *  电量实体与 getPowerJL 查询共用 PowerBean(同一纯解析函数)。 */
    private val devicePushListener = object : OnDevicePushListener {
        override fun onPush(data: PushData) {
            when (data.type) {
                DevicePushType.MUSLIM_COUNT -> {
                    val item = data.value as? MuslimCountItemBean ?: return
                    mainHandler.post {
                        updateRealtimeValue(DemoHealthType.MUSLIM_COUNT,
                            DemoHealthRecord(normalizeSeconds(item.timeMills), text(R.string.demo_count_value, item.count), text(R.string.demo_realtime_data)))
                    }
                }
                DevicePushType.POWER -> {
                    val powerBean = data.value as? PowerBean ?: return
                    mainHandler.post { update { it.copy(battery = powerBean.power) } }
                }
                DevicePushType.TOUCH_EVENT -> {
                    val event = data.value as? TouchEventBean ?: return
                    Log.e("RWSDK", "Touch push keyType=${event.keyType} touchType=${event.touchType}")
                }
                else -> Unit
            }
        }
    }

    /** 戒指在拍照模式下主动上报的拍照事件；Demo只记录，不主动触发拍照。 */
    private val takePhotoCallback = object : TakePhotoCallback {
        override fun onResult(data: Int?) {
            Log.e("RWSDK", "Ring take photo command received: $data")
        }

        override fun onSuccess() {
            Log.e("RWSDK", "Take photo mode command sent successfully")
        }

        override fun onFail(errorCode: Int) {
            Log.e("RWSDK", "Take photo mode command failed: $errorCode")
        }
    }

    /** 音乐模式下设备主动上报的控制事件；当前仅支持上一曲和下一曲。 */
    private val musicPushSettingCallback = object : MusicPushSettingCallback {
        override fun onResult(data: Int?) {
            when (data) {
                3 -> Log.e("RWSDK", "MusicPushSettingCallback: previous track, command=3")
                4 -> Log.e("RWSDK", "MusicPushSettingCallback: next track, command=4")
            }
        }

        override fun onSuccess() = Unit

        override fun onFail(errorCode: Int) {
            Log.e("RWSDK", "MusicPushSettingCallback failed, errorCode=$errorCode")
        }
    }

    fun attach(context: Context) {
        appContext = context.applicationContext
        DHBleSdk.setConnectBleCallback(this)
        if (!callbacksSubscribed) {
            callbacksSubscribed = true
            DHBleSdk.subscribeData(takePhotoCallback)
            DHBleSdk.subscribeData(musicPushSettingCallback)
            DHBleSdk.addOnDevicePushListener(devicePushListener)
        }
        refreshFromApplication()
    }

    fun observe(listener: (DemoUiState) -> Unit): () -> Unit {
        listeners += listener
        mainHandler.post { listener(state) }
        return { listeners -= listener }
    }

    fun refreshFromApplication() {
        val context = appContext ?: return
        val device = XXApplication.instance.currentConnectDevcie ?: SavedDeviceStore.load(context)
        val menu = XXApplication.instance.currentSupportMenuBean ?: state.supportMenu
        val connected = DHBleSdk.isBleConnected()
        update {
            it.copy(
                device = device,
                supportMenu = menu,
                connected = connected,
                ready = connected && menu != null,
                connecting = false,
                connectionMessage = when {
                    connected && menu != null -> text(R.string.demo_connected)
                    connected -> text(R.string.demo_initializing)
                    device != null -> text(R.string.demo_saved_not_connected)
                    else -> text(R.string.demo_unbound_device)
                }
            )
        }
        if (connected && menu != null) refreshDeviceInfo()
    }

    fun reconnect() {
        val context = appContext ?: return
        val device = state.device ?: SavedDeviceStore.load(context) ?: return
        update { it.copy(device = device, connecting = true, connectionMessage = text(R.string.demo_connecting)) }
        DHBleSdk.connectDeviceWithModel(device)
    }

    fun disconnect() {
        update {
            it.copy(
                connected = false,
                ready = false,
                connecting = false,
                connectionMessage = text(R.string.demo_disconnected),
                syncing = false,
                syncProgress = 0,
                activeMeasurement = null
            )
        }
        DHBleSdk.disconnect()
    }

    fun forgetDevice() {
        appContext?.let { SavedDeviceStore.clear(it) }
        XXApplication.instance.currentConnectDevcie = null
        XXApplication.instance.currentSupportMenuBean = null
        DHBleSdk.disconnect()
        update {
            DemoUiState(records = emptyMap())
        }
    }

    fun syncAll() {
        if (!state.ready || state.syncing) return
        stepDays.clear()
        update { it.copy(syncing = true, syncProgress = 0) }
        DHBleSdk.syncAllHealthData(this)
    }

    fun startMeasurement(type: DemoHealthType) {
        val dataType = type.measurementDataType ?: return
        if (!state.ready) return
        update { it.copy(activeMeasurement = type) }
        DHBleSdk.controlOpen(1, dataType, object : HealthMeasurementCallback {
            override fun onStarted() = Unit
            override fun onData(data: HealthRealtimeValue) {
                val type = DemoHealthType.values().firstOrNull { it.measurementDataType == data.dataType } ?: return
                val valueText = when (data.dataType) {
                    HealthDataType.BLOOD_PRESSURE.code -> "${data.value.toInt()}/${data.extraValue.toInt()} mmHg"
                    HealthDataType.BLOOD_SUGAR.code, HealthDataType.TEMPERATURE.code -> "%.1f".format(data.value) + " ${type.unit}"
                    else -> "${data.value.toInt()} ${type.unit}".trim()
                }
                updateRealtimeValue(type, DemoHealthRecord(normalizeSeconds(data.timestamp), valueText, text(R.string.demo_realtime_measurement)))
            }
            override fun onFinished(result: HealthMeasurementResult) {
                update { it.copy(activeMeasurement = null) }
            }
        })
    }

    fun stopMeasurement(type: DemoHealthType) {
        val dataType = type.measurementDataType ?: return
        update { it.copy(activeMeasurement = null) }
        DHBleSdk.controlOpen(0, dataType, object : HealthMeasurementCallback {
            override fun onStarted() = Unit
            override fun onData(data: HealthRealtimeValue) = Unit
            override fun onFinished(result: HealthMeasurementResult) = Unit
        })
    }

    fun refreshDeviceInfo() {
        if (!state.ready) return
        val powerCallback = object : PowerCallback {
            override fun onResult(data: PowerBean?) {
                data?.let { value -> update { it.copy(battery = value.power) } }
                DHBleSdk.dispose(this)
            }
            override fun onSuccess() = Unit
            override fun onFail(errorCode: Int) { DHBleSdk.dispose(this) }
        }
        val firmwareCallback = object : FirmwareCallback {
            override fun onResult(data: FirmVersionBean?) {
                data?.let { value ->
                    update { it.copy(firmwareVersion = value.deviceNo, deviceModel = value.deviceClazz) }
                }
                DHBleSdk.dispose(this)
            }
            override fun onSuccess() = Unit
            override fun onFail(errorCode: Int) { DHBleSdk.dispose(this) }
        }
        DHBleSdk.subscribeData(powerCallback)
        DHBleSdk.getPowerJL()
        DHBleSdk.subscribeData(firmwareCallback)
        DHBleSdk.getFirmwareVersionJL()
    }

    override fun onRingConnecting(device: BleDevice?) {
        update { it.copy(device = device ?: it.device, connecting = true, connectionMessage = text(R.string.demo_connecting)) }
    }

    override fun onRingConnected(device: BleDevice?) {
        val connectedDevice = device ?: state.device
        connectedDevice?.let {
            XXApplication.instance.currentConnectDevcie = it
            appContext?.let { context -> SavedDeviceStore.save(context, it) }
        }
        update {
            it.copy(device = connectedDevice, connected = true, ready = false, connecting = true, connectionMessage = text(R.string.demo_initializing))
        }
    }

    override fun onRingConnectFailed(device: BleDevice?, reason: RingBleError) {
        update {
            it.copy(
                connected = false,
                ready = false,
                connecting = false,
                syncing = false,
                syncProgress = 0,
                activeMeasurement = null,
                connectionMessage = if (reason == RingBleError.PASSWORD_AUTH_FAILED) {
                    text(R.string.demo_password_auth_failed)
                } else {
                    text(R.string.demo_connection_disconnected_reason, reason.toString())
                }
            )
        }
    }

    override fun onRingDidFunctionMenu(device: BleDevice?, supportMenuBean: SupportMenuBean) {
        val connectedDevice = device ?: state.device
        XXApplication.instance.currentSupportMenuBean = supportMenuBean
        connectedDevice?.let {
            XXApplication.instance.currentConnectDevcie = it
            appContext?.let { context -> SavedDeviceStore.save(context, it) }
        }
        update {
            it.copy(
                device = connectedDevice,
                supportMenu = supportMenuBean,
                connected = true,
                ready = true,
                connecting = false,
                connectionMessage = text(R.string.demo_connected)
            )
        }
        refreshDeviceInfo()
    }

    override fun onSyncProgress(currentPro: Int) {
        if (!state.connected || !state.ready) return
        update { it.copy(syncing = true, syncProgress = currentPro.coerceIn(0, 100)) }
    }

    override fun onSyncFinish() {
        update { it.copy(syncing = false, syncProgress = 100) }
    }

    override fun onSyncError(errorCode: Int) {
        update { it.copy(syncing = false, connectionMessage = text(R.string.demo_sync_failed, errorCode)) }
    }

    override fun onSyncStep(data: MutableList<StepSyncBean>?) {
        data.orEmpty().forEach { bean ->
            val dayTimestamp = normalizeSeconds(
                bean.time.takeIf { it > 0 }
                    ?: bean.items?.firstOrNull()?.timestamp
                    ?: 0L
            )
            val dateKey = bean.date?.takeIf { it.isNotBlank() }
                ?: DAY_FORMAT.format(Date(dayTimestamp * 1000L))
            val summary = DemoHealthRecord(
                dayTimestamp,
                text(R.string.demo_steps_value, bean.totalSteps),
                "$dateKey · ${bean.totalCalorie} cal · ${bean.totalDistance} m"
            )
            val items = bean.items.orEmpty().map { item ->
                DemoHealthRecord(
                    normalizeSeconds(if (item.timestamp > 0) item.timestamp else bean.time),
                    text(R.string.demo_steps_value, item.steps),
                    text(R.string.demo_step_detail, dateKey, item.index, item.calorie.toString(), item.distance.toString())
                )
            }
            // 全量同步先返回0x051A今天数据、再返回0x0502历史数据；同一天优先保留前者。
            if (!stepDays.containsKey(dateKey)) {
                stepDays[dateKey] = StepDayData(dayTimestamp, summary, items)
            }
        }
        publishStepDays()
    }

    override fun onSyncSleep(data: MutableList<SleepSyncBean>?) {
        val records = mutableListOf<DemoHealthRecord>()
        data.orEmpty().forEach { bean ->
            var itemTime = normalizeSeconds(bean.asleepTime.takeIf { it > 0 } ?: bean.time)
            if (bean.items.isNullOrEmpty()) {
                records += DemoHealthRecord(normalizeSeconds(bean.time), text(R.string.demo_minutes_record, bean.totalSleepTime), text(R.string.demo_sleep_record))
            } else {
                bean.items.forEach { item ->
                    records += DemoHealthRecord(
                        itemTime,
                        sleepTypeName(item.sleepType),
                        text(R.string.demo_minutes_record, item.len) + if (item.isTemporary == 1) text(R.string.demo_temporary_data) else ""
                    )
                    itemTime += item.len * 60L
                }
            }
        }
        replaceRecords(DemoHealthType.SLEEP, records)
    }

    override fun onSyncHr(data: MutableList<HeartRateSyncBean>?) {
        replaceRecords(DemoHealthType.HEART_RATE, data.orEmpty().flatMap { it.items.orEmpty() }.map {
            DemoHealthRecord(normalizeSeconds(it.timeMills), "${it.hr} bpm", text(R.string.demo_health_heart_rate))
        })
    }

    override fun onSyncBp(data: MutableList<BloodPressSyncBean>?) {
        replaceRecords(DemoHealthType.BLOOD_PRESSURE, data.orEmpty().flatMap { it.items.orEmpty() }.map {
            DemoHealthRecord(normalizeSeconds(it.timeMills), "${it.sp}/${it.dp} mmHg", text(R.string.demo_systolic_diastolic))
        })
    }

    override fun onSyncBo(data: MutableList<BloodOxySyncBean>?) {
        replaceRecords(DemoHealthType.BLOOD_OXYGEN, data.orEmpty().flatMap { it.items.orEmpty() }.map {
            DemoHealthRecord(normalizeSeconds(it.timeMills), "${it.bloodOxy}%", text(R.string.demo_health_blood_oxygen))
        })
    }

    override fun onSyncTemp(data: MutableList<BodyTempSyncBean>?) {
        replaceRecords(DemoHealthType.TEMPERATURE, data.orEmpty().flatMap { it.items.orEmpty() }.map {
            DemoHealthRecord(normalizeSeconds(it.timeMills), "%.1f ℃".format(it.temp / 10f), text(R.string.demo_health_temperature))
        })
    }

    override fun onSyncPressure(data: MutableList<PressureSyncBean>?) {
        replaceRecords(DemoHealthType.PRESSURE, data.orEmpty().flatMap { it.items.orEmpty() }.map {
            DemoHealthRecord(normalizeSeconds(it.timeMills), it.pressure.toString(), text(R.string.demo_health_pressure))
        })
    }

    override fun onSyncBloodSugar(data: MutableList<BloodSugarSyncBean>?) {
        replaceRecords(DemoHealthType.BLOOD_SUGAR, data.orEmpty().flatMap { it.items.orEmpty() }.map {
            DemoHealthRecord(normalizeSeconds(it.timeMills), "${it.sugar} mmol/L", text(R.string.demo_health_blood_sugar))
        })
    }

    override fun onSyncBreath(data: MutableList<BreatheSyncBean>?) = Unit

    override fun onSyncHrv(data: MutableList<HrvSyncBean>?) {
        replaceRecords(DemoHealthType.HRV, data.orEmpty().flatMap { it.items.orEmpty() }.map {
            DemoHealthRecord(normalizeSeconds(it.timeMills), "${it.hrv} ms", "HRV")
        })
    }

    override fun onSyncMuslimCount(data: MutableList<MuslimCountSyncBean>?) {
        replaceRecords(DemoHealthType.MUSLIM_COUNT, data.orEmpty().flatMap { it.items.orEmpty() }.map {
            DemoHealthRecord(normalizeSeconds(it.timeMills), text(R.string.demo_count_value, it.count), text(R.string.demo_health_muslim_count))
        })
    }

    private fun updateRealtimeValue(type: DemoHealthType, record: DemoHealthRecord) {
        update { current ->
            val map = current.realtimeValues.toMutableMap()
            map[type.id] = record
            current.copy(realtimeValues = map)
        }
    }

    private fun replaceRecords(type: DemoHealthType, records: List<DemoHealthRecord>) {
        update { current ->
            val map = current.records.toMutableMap()
            map[type.id] = records.sortedByDescending { it.timestampSeconds }
            current.copy(records = map)
        }
    }

    private fun publishStepDays() {
        val days = stepDays.values.sortedByDescending { it.dayTimestamp }
        val details = days.flatMap { day -> day.items.sortedByDescending { it.timestampSeconds } }
        update { current ->
            val summaries = current.summaryValues.toMutableMap()
            days.firstOrNull()?.let { summaries[DemoHealthType.STEP.id] = it.summary }
                ?: summaries.remove(DemoHealthType.STEP.id)
            val records = current.records.toMutableMap()
            records[DemoHealthType.STEP.id] = details
            current.copy(summaryValues = summaries, records = records)
        }
    }

    private fun update(transform: (DemoUiState) -> DemoUiState) {
        state = transform(state)
        publish()
    }

    private fun publish() {
        val snapshot = state
        mainHandler.post { listeners.forEach { it(snapshot) } }
    }

    private fun normalizeSeconds(value: Long): Long = when {
        value <= 0 -> System.currentTimeMillis() / 1000L
        value > 10_000_000_000L -> value / 1000L
        else -> value
    }

    private fun sleepTypeName(type: Int): String = when (type) {
        1 -> text(R.string.demo_deep_sleep)
        2 -> text(R.string.demo_light_sleep)
        3 -> text(R.string.demo_rem_sleep)
        4 -> text(R.string.demo_awake)
        else -> text(R.string.demo_sleep_state, type)
    }

    private fun text(@StringRes resId: Int, vararg args: Any): String {
        val context = appContext ?: XXApplication.instance.applicationContext
        return if (args.isEmpty()) context.getString(resId) else context.getString(resId, *args)
    }

    private data class StepDayData(
        val dayTimestamp: Long,
        val summary: DemoHealthRecord,
        val items: List<DemoHealthRecord>
    )

    private val DAY_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
}
