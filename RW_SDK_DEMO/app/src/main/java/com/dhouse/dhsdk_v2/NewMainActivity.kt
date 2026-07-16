package com.dhouse.dhsdk_v2

import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.dhouse.dhsdk_v2.databinding.ActivityNewMainBinding
import com.dhouse.dhsdk_v2.ui.ScanActivity
import com.dhouse.dhsdk_v2.ui.Workout.WorkoutTypeActivity
import com.dhouse.dhsdk_v2.ui.bean.EventBusBean
import com.example.blesdk.rwbaselibrary.HandlerCallback
import com.example.blesdk.DHBleSdk
import com.example.blesdk.bean.function.AlarmRemainderBean
import com.example.blesdk.bean.function.BoReminderBean
import com.example.blesdk.bean.function.BrightScreenBean
import com.example.blesdk.bean.function.BrightScreenLedBean
import com.example.blesdk.bean.function.BrightScreenTimeBean
import com.example.blesdk.bean.function.DrinkReminderBean
import com.example.blesdk.bean.function.FactoryInBean
import com.example.blesdk.bean.function.FirmVersionBean
import com.example.blesdk.bean.function.HrBoActualReminderBean
import com.example.blesdk.bean.function.HrReminderBean
import com.example.blesdk.bean.function.MsgPushBean
import com.example.blesdk.bean.function.PersonBean
import com.example.blesdk.bean.function.PowerBean
import com.example.blesdk.bean.function.SupportMenuBean
import com.example.blesdk.bean.function.VideoHidBean
import com.example.blesdk.bean.sync.BloodOxySyncBean
import com.example.blesdk.bean.sync.BloodPressSyncBean
import com.example.blesdk.bean.sync.BloodSugarSyncBean
import com.example.blesdk.bean.sync.BodyTempSyncBean
import com.example.blesdk.bean.sync.BreatheSyncBean
import com.example.blesdk.bean.sync.HealthDataSyncBean
import com.example.blesdk.bean.sync.HeartRateSyncBean
import com.example.blesdk.bean.sync.HrvSyncBean
import com.example.blesdk.bean.sync.MuslimCountSyncBean
import com.example.blesdk.bean.sync.PressureSyncBean
import com.example.blesdk.bean.sync.SleepSyncBean
import com.example.blesdk.bean.sync.StepSyncBean
import com.example.blesdk.ble.bean.BleDevice
import com.example.blesdk.blering.RingBleError
import com.example.blesdk.blering.RingConnectBleCallback
import com.example.blesdk.callback.HealthDataSyncCallback
import com.example.blesdk.callback.OnFileTransferCallback
import com.example.blesdk.callback.data.*
import com.example.blesdk.callback.data.TimedBloodPressureCallback
import com.example.blesdk.callback.data.MuslimTimeDisplayModeCallback
import com.example.blesdk.callback.data.SensorRawDataCallback
import com.example.blesdk.callback.data.TimedPPGCallback
import com.example.blesdk.bean.function.SensorRawDataBean
import com.example.blesdk.callback.data.SensorHistoryRawCallback
import com.example.blesdk.bean.function.SensorHistoryRawBean
import com.example.blesdk.callback.data.SensorRawControlCallback
import com.example.blesdk.callback.data.AlarmVibrationDurationCallback
import com.example.blesdk.callback.data.VibrationIntervalCallback
import com.example.blesdk.callback.data.TouchEventCallback
import com.example.blesdk.callback.data.FactoryTestCallback
import com.example.blesdk.callback.data.FallDetectCallback
import com.example.blesdk.callback.data.CountReminderIntervalCallback
import com.example.blesdk.callback.status.CommonStatusCallback
import com.example.blesdk.callback.status.FindDeviceControlCallback
import com.example.blesdk.callback.status.HealthDataControlCallback
import com.example.blesdk.utils.CmdConstants
import com.example.blesdk.utils.Constants
import org.greenrobot.eventbus.EventBus

class NewMainActivity : AppCompatActivity(), View.OnClickListener, RingConnectBleCallback, HealthDataSyncCallback {

    private var isConnected: Boolean = false

    private val binding by lazy {
        ActivityNewMainBinding.inflate(layoutInflater)
    }


    private val hrBoActualReminderCallback by lazy {
        object : HrBoActualReminderCallback{
            override fun onResult(data: HrBoActualReminderBean) {
                Log.e("RWSDK", "output: HrBoActualReminderCallback data " + data.type + " value " + data.remindValue)
                if (data.type == 0){ // HR Over Value Alarm

                }
                else if (data.type == 1){// SP02 Over Value Alarm

                }
                else if (data.type == 2){// HR Under Value Alarm

                }
            }

            override fun onFail(errorCode: Int) {
                Log.e("RWSDK", "output: HrBoActualReminderCallback errorCode " + errorCode)
            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: HrBoActualReminderCallback onSuccess")
            }

        }
    }

    private val muslimCountSwitchCallback by lazy {
        object : MuslimCountSwitchCallback{

            override fun onResult(data: Int) {
                Log.e("RWSDK", "output: BoReminderCallback data " + data)
            }

            override fun onFail(errorCode: Int) {
                Log.e("RWSDK", "output: BoReminderCallback errorCode " + errorCode)
            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: BoReminderCallback onSuccess")
            }

        }
    }

    private val muslimCountResetModeCallback by lazy {
        object : MuslimCountResetModeCallback{

            override fun onResult(data: Int) {
                Log.e("RWSDK", "output: MuslimCountResetModeCallback data " + data)
            }

            override fun onFail(errorCode: Int) {
                Log.e("RWSDK", "output: MuslimCountResetModeCallback errorCode " + errorCode)
            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: MuslimCountResetModeCallback onSuccess")
            }

        }
    }

    private val alarmVibrationDurationCallback by lazy {
        object : AlarmVibrationDurationCallback {
            override fun onResult(data: Int?) {
                Log.e("RWSDK", "output: AlarmVibrationDurationCallback data " + data)
            }
            override fun onFail(errorCode: Int) {
                Log.e("RWSDK", "output: AlarmVibrationDurationCallback errorCode " + errorCode)
            }
            override fun onSuccess() {
                Log.e("RWSDK", "output: AlarmVibrationDurationCallback onSuccess")
            }
        }
    }

    private val vibrationIntervalCallback by lazy {
        object : VibrationIntervalCallback {
            override fun onResult(data: Int?) {
                Log.e("RWSDK", "output: VibrationIntervalCallback data " + data + "ms")
            }
            override fun onFail(errorCode: Int) {
                Log.e("RWSDK", "output: VibrationIntervalCallback errorCode " + errorCode)
            }
            override fun onSuccess() {
                Log.e("RWSDK", "output: VibrationIntervalCallback onSuccess")
            }
        }
    }

    private val touchEventCallback by lazy {
        object : TouchEventCallback {
            override fun onResult(data: IntArray?) {
                data?.let {
                    val keyType = it[0]   // 1:触摸按键 2:跌落
                    val touchType = it[1] // 1:单击 2:双击 3:三击 4:长按 5:甩动
                    Log.e("RWSDK", "output: TouchEvent keyType=$keyType touchType=$touchType")
                    if (keyType == 2) {
                        Log.e("RWSDK", "output: Fall Detected!")
                    }
                }
            }
            override fun onFail(errorCode: Int) {}
            override fun onSuccess() {}
        }
    }

    private val boReminderCallback by lazy {
        object : BoReminderCallback{
            override fun onResult(data: BoReminderBean) {
                Log.e("RWSDK", "output: BoReminderCallback data " + data.isOpen + " value " + data.remindValue)
            }

            override fun onFail(errorCode: Int) {
                Log.e("RWSDK", "output: BoReminderCallback errorCode " + errorCode)
            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: BoReminderCallback onSuccess")
            }

        }
    }

    private val hrReminderCallback by lazy {
        object : HrReminderCallback{
            override fun onResult(data: HrReminderBean) {
                Log.e("RWSDK", "output: HrReminderCallback data " + data.isOpen + " value " + data.remindValue)
            }

            override fun onFail(errorCode: Int) {
                Log.e("RWSDK", "output: HrReminderCallback errorCode " + errorCode)
            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: HrReminderCallback onSuccess")
            }

        }
    }
    private val raiseBrightTimeCallback by lazy {
        object : BrightCallback{
            override fun onResult(data: BrightScreenBean) {
                Log.e("RWSDK", "output: BrightCallback data " + data.isOpen)
            }

            override fun onFail(errorCode: Int) {
                Log.e("RWSDK", "output: BrightCallback errorCode " + errorCode)
            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: BrightCallback onSuccess")
            }

        }
    }

    private val brightTimeCallback by lazy {
        object : BrightTimeCallback{
            override fun onResult(data: BrightScreenTimeBean) {
                Log.e("RWSDK", "output: BrightTimeCallback data " + data.timeSecond)
            }

            override fun onFail(errorCode: Int) {
                Log.e("RWSDK", "output: BrightTimeCallback errorCode " + errorCode)
            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: BrightTimeCallback onSuccess")
            }

        }
    }

    private val alarmCallback by lazy {
        object : AlarmCallback{
            override fun onResult(data: List<AlarmRemainderBean?>?) {
                Log.e("RWSDK", "output: AlarmCallback data " + data?.size)
            }

            override fun onFail(errorCode: Int) {
                Log.e("RWSDK", "output: AlarmCallback errorCode " + errorCode)
            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: AlarmCallback onSuccess")
            }

        }
    }

    private val findDeviceControlCallback by lazy {
        object : FindDeviceControlCallback {
            override fun onSuccess() {
                Log.e("RWSDK", "output: FindDeviceControlCallback onSuccess")
            }

            override fun onFail(errorCode: Int) {

            }

            override fun onResult(data: Int?) {

            }
        }
    }

    private val hrMonitorCallback by lazy {
        object : TimedHeartRateCallback {
            override fun onResult(p0: DrinkReminderBean?) {
                Log.e("RWSDK", "output: TimedHeartRateCallback Get " + p0?.remindDuration + " isOpen " + p0?.isOpen)
                Toast.makeText(applicationContext,"TimedHeartRateCallback Get " + p0?.remindDuration + " isOpen " + p0?.isOpen,Toast.LENGTH_LONG).show()
            }

            override fun onFail(p0: Int) {

            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: TimedHeartRateCallback Setting OK ")
            }
        }
    }

    private val timedBloodOxygenCallback by lazy {
        object : TimedBloodOxygenCallback {
            override fun onResult(p0: DrinkReminderBean?) {
                Log.e("RWSDK", "output: TimedBloodOxygenCallback Get " + p0?.remindDuration + " isOpen " + p0?.isOpen)
                Toast.makeText(applicationContext,"TimedBloodOxygenCallback Get " + p0?.remindDuration + " isOpen " + p0?.isOpen,Toast.LENGTH_LONG).show()

            }

            override fun onFail(p0: Int) {

            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: TimedBloodOxygenCallback Setting OK ")
            }
        }
    }

    private val hrvDataCallback by lazy {
        object : TimedHrvCallback {
            override fun onResult(data: DrinkReminderBean?) {
                Log.e("RWSDK", "output: TimedHrvCallback Get " + data?.remindDuration + " isOpen " + data?.isOpen)
            }

            override fun onFail(errorCode: Int) {

            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: TimedHrvCallback Setting OK ")
            }
        }
    }

    private val ppgDataCallback by lazy {
        object : TimedPPGCallback {
            override fun onResult(data: DrinkReminderBean?) {
                Log.e("RWSDK", "output: TimedPPGCallback Get " + data?.remindDuration + " isOpen " + data?.isOpen)
            }

            override fun onFail(errorCode: Int) {

            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: TimedPPGCallback Setting OK ")
            }
        }
    }

    private val stressDataCallback by lazy {
        object : TimedStressCallback {
            override fun onResult(data: DrinkReminderBean?) {
                Log.e("RWSDK", "output: TimedStressCallback Get " + data?.remindDuration + " isOpen " + data?.isOpen)
            }

            override fun onFail(errorCode: Int) {

            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: TimedStressCallback Setting OK ")
            }
        }
    }

    private val bloodSugarDataCallback by lazy {
        object : TimedBloodSugarCallback {
            override fun onResult(data: DrinkReminderBean?) {
                Log.e("RWSDK", "output: TimedBloodSugarCallback Get " + data?.remindDuration + " isOpen " + data?.isOpen)
            }

            override fun onFail(errorCode: Int) {

            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: TimedBloodSugarCallback Setting OK ")
            }
        }
    }

    private val timedBloodPressureCallback by lazy {
        object : TimedBloodPressureCallback {
            override fun onResult(p0: DrinkReminderBean?) {
                Log.e("RWSDK", "output: TimedBloodPressureCallback Get " + p0?.remindDuration + " isOpen " + p0?.isOpen)
                Toast.makeText(applicationContext,"TimedBloodPressureCallback Get " + p0?.remindDuration + " isOpen " + p0?.isOpen,Toast.LENGTH_LONG).show()
            }

            override fun onFail(p0: Int) {

            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: TimedBloodPressureCallback Setting OK ")
            }
        }
    }

    private val timedBodyTemperatureCallback by lazy {
        object : TimedBodyTemperatureCallback {
            override fun onResult(p0: DrinkReminderBean?) {
                Log.e("RWSDK", "output: TimedBodyTemperatureCallback Get " + p0?.remindDuration + " isOpen " + p0?.isOpen)
            }

            override fun onFail(p0: Int) {

            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: TimedBodyTemperatureCallback Setting OK ")
            }
        }
    }

    private val muslimTimeDisplayModeCallback by lazy {
        object : MuslimTimeDisplayModeCallback {
            override fun onResult(data: Int?) {
                Log.e("RWSDK", "output: MuslimTimeDisplayModeCallback Get mode=" + data)
                Toast.makeText(applicationContext,"MuslimTimeDisplayMode Get mode=" + data,Toast.LENGTH_LONG).show()
            }

            override fun onFail(errorCode: Int) {
                Log.e("RWSDK", "output: MuslimTimeDisplayModeCallback onFail " + errorCode)
            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: MuslimTimeDisplayModeCallback Setting OK ")
            }
        }
    }

    private val sensorRawDataCallback by lazy {
        object : SensorRawDataCallback {
            override fun onResult(data: SensorRawDataBean?) {
                data?.let {
                    when (it.type) {
                        1 -> { // PPG
                            Log.e("RWSDK", "output: SensorRaw PPG count=${it.ppgDataList.size} data=${it.ppgDataList}")
                        }
                        2 -> { // ACC
                            Log.e("RWSDK", "output: SensorRaw ACC count=${it.accDataList.size} data=${it.accDataList}")
                        }
                        3 -> { // PPG Red
                            Log.e("RWSDK", "output: SensorRaw PPG Red count=${it.ppgRedDataList.size} data=${it.ppgRedDataList}")
                        }
                        4 -> { // IR
                            Log.e("RWSDK", "output: SensorRaw IR count=${it.irDataList.size} data=${it.irDataList}")
                        }
                        5 -> { // 睡眠实时数据
                            Log.e("RWSDK", "output: SensorRaw Sleep count=${it.sleepDataList.size}")
                            for (item in it.sleepDataList) {
                                Log.e("RWSDK", "  timestamp=${item[0]} mode=${item[1]}")
                            }
                        }
                        else -> {

                        }
                    }
                }
            }

            override fun onFail(errorCode: Int) {
                Log.e("RWSDK", "output: SensorRawDataCallback onFail " + errorCode)
            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: SensorRawDataCallback onSuccess ")
            }
        }
    }

    private val sensorRawControlCallback by lazy {
        object : SensorRawControlCallback {
            override fun onResult(data: Int?) {
                Log.e("RWSDK", "output: SensorRawControl device stopped, reason=" + data)
            }

            override fun onFail(errorCode: Int) {
                Log.e("RWSDK", "output: SensorRawControlCallback onFail " + errorCode)
            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: SensorRawControlCallback onSuccess")
            }
        }
    }

    private val sensorHistoryRawCallback by lazy {
        object : SensorHistoryRawCallback {
            override fun onResult(data: List<SensorHistoryRawBean>?) {
                data?.let {
                    Log.e("RWSDK", "output: SensorHistoryRaw count=${it.size}")
                    for (bean in it) {
                        Log.e("RWSDK", "  " + bean.toString())
                    }
                }
            }

            override fun onFail(errorCode: Int) {
                Log.e("RWSDK", "output: SensorHistoryRawCallback onFail " + errorCode)
            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: SensorHistoryRawCallback sync finished")
            }
        }
    }

    private val factoryTestCallback by lazy {
        object : FactoryTestCallback {
            override fun onResult(data: LongArray?) {
                data?.let {
                    val testMode = it[0]
                    val result = it[1]
                    if (result == 0L) {
                        Log.e("RWSDK", "FactoryTest calibrating... testMode=0x${testMode.toString(16)}")
                    } else {
                        Log.e("RWSDK", "FactoryTest done! testMode=0x${testMode.toString(16)} result=$result")
                    }
                }
            }
            override fun onFail(errorCode: Int) {}
            override fun onSuccess() {}
        }
    }

    private val fallDetectCallback by lazy {
        object : FallDetectCallback {
            override fun onResult(data: Int?) {
                Log.e("RWSDK", "FallDetect state: $data (0=off, 1=on)")
            }
            override fun onFail(errorCode: Int) {}
            override fun onSuccess() {}
        }
    }

    private val countReminderCallback by lazy {
        object : CountReminderIntervalCallback {
            override fun onResult(data: Int?) {
                Log.e("RWSDK", "CountReminderInterval: $data min (0=off)")
            }
            override fun onFail(errorCode: Int) {}
            override fun onSuccess() {}
        }
    }

    private val videoHidCallback by lazy {
        object : VideoHidCallback {
            override fun onResult(data: VideoHidBean?) {
                Log.e("RWSDK", "output: VideoHidCallback Get " + data)
            }

            override fun onFail(errorCode: Int) {

            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: VideoHidCallback Setting OK ")
            }
        }
    }

    private val brightLedLevelCallback by lazy {
        object : BrightLedLevelCallback {
            override fun onResult(data: BrightScreenLedBean?) {
                Log.e("RWSDK", "output: BrightLedLevelCallback Get " + data)
            }

            override fun onFail(errorCode: Int) {

            }

            override fun onSuccess() {
                Log.e("RWSDK", "output: BrightLedLevelCallback Setting OK ")
            }
        }
    }

    private val ringWearHandCallback by lazy {
        object : WearHandCallback {
            override fun onSuccess() {
                Log.e("RWSDK", "output: WearHandCallback Setting OK ")
                DHBleSdk.dispose(this)
            }
            override fun onResult(data: FactoryInBean?) {
                Log.e("RWSDK", "output: WearHandCallback Get " + data)
                DHBleSdk.dispose(this)
            }
            override fun onFail(errorCode: Int) {
                DHBleSdk.dispose(this)
            }
        }
    }

    private val healthDataBroCallback by lazy {
        object : HealthDataBroCallback{
            override fun onResult(data: HealthDataSyncBean?) {
                data?.let {
                    when (it.dataType) {
                        Constants.RingHealthType.HR -> { //Heart Rate
                            Log.e("RWSDK", "Output: hr Value " + it.hrPartData.last().hr)
                        }
                        Constants.RingHealthType.HRV -> {//HRV
                            Log.e("RWSDK", "Output: HRV Value " + it.hrPartData.last().hr)
                        }
                        Constants.RingHealthType.BLOOD_OXY -> {//Blood Oxygen(血氧)
                            Log.e("RWSDK", "Output: Blood Oxygen Value " + it.boPartData.last().bo)
                        }
                        Constants.RingHealthType.PRESSURE -> {//压力 Stress
                            Log.e("RWSDK", "Output: Stress Value " + it.pressurePartData.last().pressure)
                        }
                        Constants.RingHealthType.BLOOD_SUGAR -> {//血糖 BloodSugar
                            Log.e("RWSDK", "Output: BloodSugar Value " + it.tempPartData.last().temp)
                        }
                        Constants.RingHealthType.MUSLIM_COUNT -> { //Msulim Count 赞念
                            Log.e("RWSDK", "赞念 Value " + it.muslimCountPartData.count)
                        }
                        Constants.RingHealthType.BLOOD_BP -> { //血压 Blood Pressure
                            Log.e("RWSDK", "Blood Pressure Value " + it.bpPartData.last().dp + " " + it.bpPartData.last().sp)
                        }
                        else -> {

                        }
                    }
                }
            }
            override fun onFail(errorCode: Int) {

            }

            override fun onSuccess() {

            }

        }
    }

    private val testHrCallback by lazy {
        object : HealthDataControlCallback {
            override fun onSuccess() {
                Log.e("RWSDK", "Output: HealthDataControlCallback Control onSuccess")
            }

            override fun onResult(data: Int?) {
                Log.e("RWSDK", "Output: HealthDataControlCallback onResult " + data)
                data?.let {
                    if (data >= 10){
                        Log.e("RWSDK", "Output: Measurement completed (测量完成)")
                    }
                }
            }

            override fun onFail(errorCode: Int) {

            }
        }
    }

    /**
     * Camera control monitoring 拍照控制监听
     */
    private val takePhotoCallback by lazy {
        object : TakePhotoCallback {
            override fun onSuccess() {
                Log.e("RWSDK", "Output: TakePhotoCallback onSuccess")
            }

            override fun onFail(errorCode: Int) {

            }

            override fun onResult(data: Int?) {
                Log.e("RWSDK", "Output: TakePhotoCallback onResult " + data)
                data.let {
                    when (it){
                        2 -> {
                            //TODO Start the phone's custom camera to take photos 启动手机自定义相机拍照
                        }
                    }
                }
            }
        }
    }

    /**
     * 文件传输进度监听
     */
    private val dialTransferCallback by lazy {
        object : OnFileTransferCallback {
            override fun onProgress(pro: Float) {
                //onInstallProgress(pro * 100)
            }

            override fun onFinish() {
                //   onInstallSuccess()
            }

            override fun onFail(code: Int) {
                // onInstallFail(code)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        DHBleSdk.initSDK(this)

        //    EventBus.getDefault().register(this)
        val toolbar:Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        binding.disconnect.setOnClickListener(this)
        binding.reconnect.setOnClickListener(this)
        binding.searchDevice.setOnClickListener(this)
        //Setting
        binding.btSettingTime.setOnClickListener(this)
        binding.btGetPower.setOnClickListener(this)
        binding.btGetFirmwareinfo.setOnClickListener(this)
        binding.btFindDevice.setOnClickListener(this)
        binding.btGetHrmonitor.setOnClickListener(this)
        binding.btSetHrmonitor.setOnClickListener(this)
        binding.btGetBomonitor.setOnClickListener(this)
        binding.btSetBomonitor.setOnClickListener(this)
        binding.btGetBpmonitor.setOnClickListener(this)
        binding.btSetBpmonitor.setOnClickListener(this)
        binding.btGetTempMonitor.setOnClickListener(this)
        binding.btSetTempMonitor.setOnClickListener(this)
        binding.btGetMuslimTimeMode.setOnClickListener(this)
        binding.btSetMuslimTimeMode.setOnClickListener(this)
        binding.btGetHrvmonitor.setOnClickListener(this)
        binding.btSetHrvmonitor.setOnClickListener(this)
        binding.btGetPpgmonitor.setOnClickListener(this)
        binding.btSetPpgmonitor.setOnClickListener(this)
        binding.btGetVideoswitch.setOnClickListener(this)
        binding.btSetVideoswitch.setOnClickListener(this)
        binding.btGetLedlevel.setOnClickListener(this)
        binding.btSetLedlevel.setOnClickListener(this)
        binding.btGetWearposition.setOnClickListener(this)
        binding.btSetWearposition.setOnClickListener(this)
        binding.btGetStressmonitor.setOnClickListener(this)
        binding.btSetStressmonitor.setOnClickListener(this)
        binding.btGetBsmonitor.setOnClickListener(this)
        binding.btSetBsmonitor.setOnClickListener(this)
        binding.btGetVibration.setOnClickListener(this)
        binding.btSetVibration.setOnClickListener(this)
        binding.btSetAlarm.setOnClickListener(this)
        binding.btGetAlarm.setOnClickListener(this)
        binding.btDelAlarm.setOnClickListener(this)
        binding.btGetScreenSleepmode.setOnClickListener(this)
        binding.btSetScreenSleepmode.setOnClickListener(this)
        binding.btGetScreenOntime.setOnClickListener(this)
        binding.btSetScreenOntime.setOnClickListener(this)
        binding.btGetRaiseScreen.setOnClickListener(this)
        binding.btSetRaiseScreen.setOnClickListener(this)
        binding.btGetHrAlarm.setOnClickListener(this)
        binding.btSetHrAlarm.setOnClickListener(this)
        binding.btSetBoAlarm.setOnClickListener(this)
        binding.btGetBoAlarm.setOnClickListener(this)
        binding.btGetMuslimcountSwitch.setOnClickListener(this)
        binding.btSetMuslimcountSwitch.setOnClickListener(this)
        binding.btGetAlarmVibration.setOnClickListener(this)
        binding.btSetAlarmVibration.setOnClickListener(this)
        binding.btGetVibrationInterval.setOnClickListener(this)
        binding.btSetVibrationInterval.setOnClickListener(this)
        binding.btSetTimeformat.setOnClickListener(this)


        binding.btControlOpenHr.setOnClickListener(this)
        binding.btControlCloseHr.setOnClickListener(this)
        binding.btControlOpenBo.setOnClickListener(this)
        binding.btControlCloseBo.setOnClickListener(this)
        binding.btControlOpenHrv.setOnClickListener(this)
        binding.btControlCloseHrv.setOnClickListener(this)
        binding.btOpenControlTakephoto.setOnClickListener(this)
        binding.btCloseControlTakephoto.setOnClickListener(this)
        binding.btControlFinddevice.setOnClickListener(this)
        binding.btControlShutdown.setOnClickListener(this)
        binding.btControlOpenStress.setOnClickListener(this)
        binding.btControlCloseStress.setOnClickListener(this)
        binding.btControlOpenBs.setOnClickListener(this)
        binding.btControlCloseBs.setOnClickListener(this)
        binding.btControlOpenBp.setOnClickListener(this)
        binding.btControlCloseBp.setOnClickListener(this)
        binding.btSensorStart.setOnClickListener(this)
        binding.btSensorStop.setOnClickListener(this)
        binding.btSensorHistory.setOnClickListener(this)
        binding.btFactoryTestHr.setOnClickListener(this)
        binding.btFallDetectGet.setOnClickListener(this)
        binding.btFallDetectSet.setOnClickListener(this)
        binding.btGetCountReminder.setOnClickListener(this)
        binding.btSetCountReminder.setOnClickListener(this)

        binding.btDataGethealth.setOnClickListener(this)
        binding.btDataGethealthOnlyOne.setOnClickListener(this)

        binding.btSportWorkout.setOnClickListener(this)

        binding.btUpdateOta.setOnClickListener(this)


        DHBleSdk.setConnectBleCallback(this)
        DHBleSdk.subscribeData(touchEventCallback) //监听设备触摸事件
        loadSavedDevice()
        updateDevicePanel()
    }


    override fun onResume() {
        super.onResume()
        XXApplication.instance.isEnterOta = false

        Log.e("RWSDK", "onResume")

        DHBleSdk.setConnectBleCallback(this)

        loadSavedDevice()
        updateDevicePanel()
    }

    override fun onDestroy() {
        super.onDestroy()

    }


    override fun onClick(v: View?) {
        when(v?.id){
            R.id.disconnect -> {
              //  binding.progressBar.visibility = View.VISIBLE

                DHBleSdk.disconnect()
                binding.progressBar.visibility = View.GONE
                isConnected = false
                updateDevicePanel()

            }
            R.id.reconnect -> {
                reconnectSavedDevice()
            }
            R.id.search_device -> {
                clearSavedDeviceAndSearch()

            }
            R.id.clear -> {

            }
            R.id.bt_setting_time -> {
                Log.e("RWSDK", DHBleSdk.getSDKVersion())
            }
            R.id.bt_get_power -> {
                DHBleSdk.subscribeData(object : PowerCallback {
                    override fun onSuccess() {

                    }

                    override fun onFail(errorCode: Int) {
                        Log.e("RWSDK", "output: ERROR CODE " + errorCode)
                    }

                    override fun onResult(data: PowerBean?) {
                        data?.let {
                            Log.e("RWSDK", "output: Battery " + data.power)
                        }
                    }
                })
                DHBleSdk.getPowerJL()
            }
            R.id.bt_get_firmwareinfo -> {
                DHBleSdk.subscribeData(object : FirmwareCallback {
                    override fun onSuccess() {

                    }

                    override fun onFail(errorCode: Int) {
                        Log.e("RWSDK", "output: ERROR CODE " + errorCode)
                    }

                    override fun onResult(data: FirmVersionBean?) {
                        data?.let {
                            Log.e("RWSDK", "output: Firmware Info " + data)
                        }
                    }
                })
                DHBleSdk.getFirmwareVersionJL()
            }

            R.id.bt_find_device -> {
                DHBleSdk.subscribeStatus(object : CommonStatusCallback{
                    override fun onSuccess(id: Int) {
                        Log.e("RWSDK", "user info set ok")
                    }

                    override fun onFail(id: Int, errorCode: Int) {
                        Log.e("RWSDK", "user info set failed")
                    }
                })
                val persionBean = PersonBean()
                persionBean.measureUnit = 0
                persionBean.gender = 1
                persionBean.height = 170.5f
                persionBean.weight = 80f
                persionBean.age = 20
                DHBleSdk.setUserInfo(persionBean)
            }
            R.id.bt_get_hrmonitor -> {
                DHBleSdk.subscribeData(hrMonitorCallback)
                DHBleSdk.getTimedHeartRateJL()
            }
            R.id.bt_set_hrmonitor -> {
                DHBleSdk.subscribeData(hrMonitorCallback)
                val hrMonitorBean = DrinkReminderBean()
                hrMonitorBean.isOpen = true  //Heart rate monitoring switch
                hrMonitorBean.remindDuration = 60 //Heart rate monitoring interval unit is minutes, only 30 minutes and 60 minutes
                hrMonitorBean.startHour = 0 //fixed
                hrMonitorBean.startMin = 0 //fixed
                hrMonitorBean.endHour = 23 //fixed
                hrMonitorBean.endMin = 59  //fixed
                DHBleSdk.setTimedHeartRateJL(hrMonitorBean)
            }
            R.id.bt_get_bomonitor -> {
                Log.e("RWSDK", "output: DHBleSdk.getTimedBloodOxygenJL")
                DHBleSdk.subscribeData(timedBloodOxygenCallback)
                DHBleSdk.getTimedBloodOxygenJL()
            }
            R.id.bt_set_bomonitor -> {
                DHBleSdk.subscribeData(timedBloodOxygenCallback)
                val healthMonitorBean = DrinkReminderBean()
                healthMonitorBean.isOpen = true  //BloodOxygen monitoring switch
                healthMonitorBean.remindDuration = 60 //BloodOxygen monitoring interval unit is minutes, only 30 minutes and 60 minutes
                healthMonitorBean.startHour = 0 //fixed
                healthMonitorBean.startMin = 0 //fixed
                healthMonitorBean.endHour = 23 //fixed
                healthMonitorBean.endMin = 59  //fixed
                DHBleSdk.setTimedBloodOxygenJL(healthMonitorBean)
            }
            R.id.bt_get_hrvmonitor -> {
                val support = XXApplication.instance.currentSupportMenuBean

                DHBleSdk.subscribeData(hrvDataCallback)
                DHBleSdk.getTimedHRVJL()
            }
            R.id.bt_set_hrvmonitor -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isHrv == true) {
                    DHBleSdk.subscribeData(hrvDataCallback)
                    val healthMonitorBean = DrinkReminderBean()
                    healthMonitorBean.isOpen = false  //HRV monitoring switch
                    healthMonitorBean.remindDuration = 60 //HRV monitoring interval unit is minutes, only 30 minutes and 60 minutes
                    healthMonitorBean.startHour = 0 //fixed
                    healthMonitorBean.startMin = 0 //fixed
                    healthMonitorBean.endHour = 23 //fixed
                    healthMonitorBean.endMin = 59  //fixed
                    DHBleSdk.setTimedHRVJL(healthMonitorBean)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_get_ppgmonitor -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isSupportPPGMonitoring == true) {
                    DHBleSdk.subscribeData(ppgDataCallback)
                    DHBleSdk.getTimedPPGJL()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_ppgmonitor -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isSupportPPGMonitoring == true) {
                    DHBleSdk.subscribeData(ppgDataCallback)
                    val healthMonitorBean = DrinkReminderBean()
                    healthMonitorBean.isOpen = true
                    healthMonitorBean.remindDuration = 60
                    healthMonitorBean.startHour = 0
                    healthMonitorBean.startMin = 0
                    healthMonitorBean.endHour = 23
                    healthMonitorBean.endMin = 59
                    DHBleSdk.setTimedPPGJL(healthMonitorBean)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_get_stressmonitor -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isPressure == true) {
                    DHBleSdk.subscribeData(stressDataCallback)
                    DHBleSdk.getTimedStressJL()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_stressmonitor -> {

                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isPressure == true) {
                    DHBleSdk.subscribeData(stressDataCallback)
                    val healthMonitorBean = DrinkReminderBean()
                    healthMonitorBean.isOpen = true  //HRV monitoring switch
                    healthMonitorBean.remindDuration = 60 //HRV monitoring interval unit is minutes, only 30 minutes and 60 minutes
                    healthMonitorBean.startHour = 0 //fixed
                    healthMonitorBean.startMin = 0 //fixed
                    healthMonitorBean.endHour = 23 //fixed
                    healthMonitorBean.endMin = 59  //fixed
                    DHBleSdk.setTimedStressJL(healthMonitorBean)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }

            }
            R.id.bt_get_bsmonitor -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isBloodSugar == true) {
                    DHBleSdk.subscribeData(bloodSugarDataCallback)
                    DHBleSdk.getTimedBloodSugarJL()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_bsmonitor -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isBloodSugar == true) {
                    DHBleSdk.subscribeData(bloodSugarDataCallback)
                    val healthMonitorBean = DrinkReminderBean()
                    healthMonitorBean.isOpen = true  //HRV monitoring switch
                    healthMonitorBean.remindDuration = 60 //HRV monitoring interval unit is minutes, only 30 minutes and 60 minutes
                    healthMonitorBean.startHour = 0 //fixed
                    healthMonitorBean.startMin = 0 //fixed
                    healthMonitorBean.endHour = 23 //fixed
                    healthMonitorBean.endMin = 59  //fixed
                    DHBleSdk.setTimedBloodSugarJL(healthMonitorBean)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }

            }
            R.id.bt_get_videoswitch -> {
                DHBleSdk.subscribeData(videoHidCallback)
                DHBleSdk.getVideoHidJL()
            }
            R.id.bt_set_videoswitch -> {
                DHBleSdk.subscribeData(videoHidCallback)
                val videoHidBean = VideoHidBean()
                videoHidBean.hidOpen = 0  //Whether to open short video control
                DHBleSdk.setVideoHidJL(videoHidBean)
            }
            R.id.bt_get_ledlevel -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isLEDLight == true) {
                    DHBleSdk.subscribeData(brightLedLevelCallback)
                    DHBleSdk.getRingLedLevel()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_ledlevel -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isLEDLight == true) {
                    DHBleSdk.subscribeData(brightLedLevelCallback)
                    val tBrightScreenLedBean = BrightScreenLedBean()
                    tBrightScreenLedBean.isOpen = true //false为off,ture为(1-3Level)
                    tBrightScreenLedBean.lcdLevel = 3 //1-3Level: 1 low 2 mid 3 high
                    DHBleSdk.setRingLedLevel(tBrightScreenLedBean)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_get_wearposition -> {

                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isWearDir == true) {
                    DHBleSdk.subscribeData(ringWearHandCallback)
                    DHBleSdk.getRingWearDir()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }

            }
            R.id.bt_set_wearposition -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isWearDir == true) {
                    DHBleSdk.subscribeData(ringWearHandCallback)
                    DHBleSdk.setRingWearHand(false) //False is left hand, true is right hand
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_get_vibration -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isSupportMotoVibrationLevel == true) {
                    DHBleSdk.setVibrationCount(1, 1)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }

            }
            R.id.bt_set_vibration -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isSupportMotoVibrationLevel == true) {
                    DHBleSdk.getVibrationCount()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }

            }
            R.id.bt_get_alarm -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isAlarm == true) {
                    DHBleSdk.subscribeData(alarmCallback)
                    DHBleSdk.getAlarmRemindJL()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_alarm -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isAlarm == true) {
                    val params = mutableListOf<AlarmRemainderBean>()
                    val alarmRemainderBean = AlarmRemainderBean()
                    alarmRemainderBean.alarmTag = ""
                    alarmRemainderBean.repeatModel = IntArray(7) //单次；周日至周六,要重复的对应置1
                    alarmRemainderBean.startHour = 7
                    alarmRemainderBean.startMin = 0
                    alarmRemainderBean.isOpen = true
                    alarmRemainderBean.alarmId = 0
                    params += alarmRemainderBean

                    val alarmRemainderBean2 = AlarmRemainderBean()
                    alarmRemainderBean2.alarmTag = ""
                    alarmRemainderBean2.repeatModel = IntArray(7) //单次；周日至周六,要重复的对应置1
                    alarmRemainderBean2.startHour = 8
                    alarmRemainderBean2.startMin = 0
                    alarmRemainderBean2.isOpen = false
                    alarmRemainderBean2.alarmId = 0
                    params += alarmRemainderBean2

                    DHBleSdk.subscribeData(alarmCallback)
                    DHBleSdk.setAlarmRemindJL(params)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_del_alarm -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isAlarm == true) {
                    DHBleSdk.subscribeData(alarmCallback)
                    DHBleSdk.deleteAllAlarmRemindJL()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_get_screen_sleepmode -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isBrightScreenSleepTime == true) {
                    DHBleSdk.subscribeData(brightTimeCallback)
                    DHBleSdk.getRingBrightScreenSleepTime()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_screen_sleepmode -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isBrightScreenSleepTime == true) {
                    val briScreenTime = BrightScreenTimeBean()
                    briScreenTime.isOpen = true
                    briScreenTime.startHour = 20 //晚上8点至早上8点睡眠
                    briScreenTime.startMin = 0
                    briScreenTime.endHour = 8
                    briScreenTime.endMin = 0
                    DHBleSdk.subscribeData(brightTimeCallback)
                    DHBleSdk.setRingBrightScreenSleepTime(briScreenTime)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_get_screen_ontime -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isBrightScreenTime == true) {
                    DHBleSdk.subscribeData(brightTimeCallback)
                    DHBleSdk.getBrightScreenTimeJL()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_screen_ontime -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isBrightScreenTime == true) {
                    val briScreenTime = BrightScreenTimeBean()
                    briScreenTime.timeSecond = 10 //亮屏10s
                    DHBleSdk.subscribeData(brightTimeCallback)
                    DHBleSdk.setBrightScreenTimeJL(briScreenTime)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_get_raise_screen -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isRaiseBrightScreen == true) {
                    DHBleSdk.subscribeData(raiseBrightTimeCallback)
                    DHBleSdk.getRaiseBrightScreenJL()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_raise_screen -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isRaiseBrightScreen == true) {
                    val rasieScreenTime = BrightScreenBean()
                    rasieScreenTime.isOpen = true
                    rasieScreenTime.startHour = 8 //早上8点至晚上8点 抬腕亮屏
                    rasieScreenTime.startMin = 0
                    rasieScreenTime.endHour = 20
                    rasieScreenTime.endMin = 0
                    DHBleSdk.subscribeData(raiseBrightTimeCallback)
                    DHBleSdk.setRaiseBrightScreenJL(rasieScreenTime)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_get_hr_alarm -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isSupportHrReminder == true) {
                    DHBleSdk.subscribeData(hrBoActualReminderCallback) //Alert Message
                    DHBleSdk.subscribeData(hrReminderCallback)
                    DHBleSdk.deviceGetHrAlertCmd()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_hr_alarm -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isSupportHrReminder == true) {
                    DHBleSdk.subscribeData(hrBoActualReminderCallback) //Alert Message
                    DHBleSdk.subscribeData(hrReminderCallback)
                    DHBleSdk.deviceSetHrAlertCmd(1, 140, 0xff)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_get_bo_alarm -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isSupportBoReminder == true) {
                    DHBleSdk.subscribeData(hrBoActualReminderCallback) //Alert Message
                    DHBleSdk.subscribeData(boReminderCallback)
                    DHBleSdk.deviceGetBoAlertCmd()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_bo_alarm -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isSupportBoReminder == true) {
                    DHBleSdk.subscribeData(hrBoActualReminderCallback) //Alert  Message

                    DHBleSdk.subscribeData(boReminderCallback)
                    DHBleSdk.deviceSetBoAlertCmd(1, 90)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_get_muslimcount_switch -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isRememberSwitch == true) {
                    DHBleSdk.subscribeData(muslimCountSwitchCallback)
                    DHBleSdk.deviceRememberSwitchGet()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_muslimcount_switch -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isRememberSwitch == true) {

                    DHBleSdk.subscribeData(muslimCountSwitchCallback)
                    DHBleSdk.deviceRememberSwitch(1)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_get_muslim_count_reset_mode -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isSupportMuslimTimeDisplayMode == true) {
                    DHBleSdk.subscribeData(muslimCountResetModeCallback)
                    DHBleSdk.getMuslimCountResetModeJL()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_muslim_count_reset_mode -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isSupportMuslimTimeDisplayMode == true) {
                    DHBleSdk.subscribeData(muslimCountResetModeCallback)
                    DHBleSdk.setMuslimCountResetModeJL(1)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_get_alarm_vibration -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isSupportAlarmVibrationDuration == true) {
                    DHBleSdk.subscribeData(alarmVibrationDurationCallback)
                    DHBleSdk.getAlarmVibrationDuration()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_alarm_vibration -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isSupportAlarmVibrationDuration == true) {
                    DHBleSdk.subscribeData(alarmVibrationDurationCallback)
                    DHBleSdk.setAlarmVibrationDuration(2) // 默认2次
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_get_vibration_interval -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isSupportVibrationInterval == true) {
                    DHBleSdk.subscribeData(vibrationIntervalCallback)
                    DHBleSdk.getVibrationInterval()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_vibration_interval -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isSupportVibrationInterval == true) {
                    DHBleSdk.subscribeData(vibrationIntervalCallback)
                    DHBleSdk.setVibrationInterval(500) // 默认500ms
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_get_message_push -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isPushMsgEnableSwitch == true) {

                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_message_push -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isPushMsgEnableSwitch == true) {
                    val messageBean = MsgPushBean()
                    messageBean.appId = "com.ten.wenxin"
                    messageBean.title = "1111"
                    messageBean.content = "8888"
                    DHBleSdk.setPushMsgJL(messageBean)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_timeformat -> {
                DHBleSdk.subscribeStatus(object : CommonStatusCallback{
                    override fun onSuccess(id: Int) {
                        Log.e("RWSDK", "time format set ok")
                    }

                    override fun onFail(id: Int, errorCode: Int) {
                        Log.e("RWSDK", "time format set failed")
                    }
                })
                DHBleSdk.ringSetTimeformat(0)
            }


            R.id.bt_control_open_hr -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isHr == true) {
                    DHBleSdk.subscribeData(healthDataBroCallback) //Monitor real-time health data return (监听实时健康数据返回)
                    DHBleSdk.subscribeData(testHrCallback) //Monitor control command results (监听控制指令结果)
                    DHBleSdk.controlHealthDataJL(CmdConstants.JL_HR_DATA_TRANSFER_KEY, 1)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_control_close_hr -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isHr == true) {
                    DHBleSdk.subscribeData(testHrCallback)
                    DHBleSdk.controlHealthDataJL(CmdConstants.JL_HR_DATA_TRANSFER_KEY, 0)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_control_open_bo -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isBloodOxy == true) {
                    DHBleSdk.subscribeData(healthDataBroCallback) //Monitor real-time health data return (监听实时健康数据返回)
                    DHBleSdk.subscribeData(testHrCallback) //Monitor control command results (监听控制指令结果)
                    DHBleSdk.controlHealthDataJL(CmdConstants.JL_BO_DATA_TRANSFER_KEY, 1)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_control_close_bo -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isBloodOxy == true) {
                    DHBleSdk.subscribeData(testHrCallback)
                    DHBleSdk.controlHealthDataJL(CmdConstants.JL_BO_DATA_TRANSFER_KEY, 0)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_control_open_hrv -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isHrv == true) {
                    DHBleSdk.subscribeData(healthDataBroCallback) //Monitor real-time health data return (监听实时健康数据返回)
                    DHBleSdk.subscribeData(testHrCallback) //Monitor control command results (监听控制指令结果)
                    DHBleSdk.controlHealthDataJL(CmdConstants.JL_HRV_DATA_TRANSFER_KEY, 1)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_control_close_hrv -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isHrv == true) {
                    DHBleSdk.subscribeData(testHrCallback)
                    DHBleSdk.controlHealthDataJL(CmdConstants.JL_HRV_DATA_TRANSFER_KEY, 0)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_control_open_stress -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isPressure == true) {
                    DHBleSdk.subscribeData(healthDataBroCallback) //Monitor real-time health data return (监听实时健康数据返回)
                    DHBleSdk.subscribeData(testHrCallback) //Monitor control command results (监听控制指令结果)
                    DHBleSdk.controlHealthDataJL(CmdConstants.JL_PRESSURE_DATA_TRANSFER_KEY, 1)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_control_close_stress -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isPressure == true) {
                    DHBleSdk.subscribeData(testHrCallback)
                    DHBleSdk.controlHealthDataJL(CmdConstants.JL_PRESSURE_DATA_TRANSFER_KEY, 0)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_control_open_bs -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isBloodSugar == true) {
                    DHBleSdk.subscribeData(healthDataBroCallback) //Monitor real-time health data return (监听实时健康数据返回)
                    DHBleSdk.subscribeData(testHrCallback) //Monitor control command results (监听控制指令结果)
                    DHBleSdk.controlHealthDataJL(CmdConstants.JL_BLOODSUGAR_DATA_TRANSFER_KEY, 1)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_control_close_bs -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isBloodSugar == true) {
                    DHBleSdk.subscribeData(testHrCallback)
                    DHBleSdk.controlHealthDataJL(CmdConstants.JL_BLOODSUGAR_DATA_TRANSFER_KEY, 0)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_get_bpmonitor -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isBloodPress == true) {
                    DHBleSdk.subscribeData(timedBloodPressureCallback)
                    DHBleSdk.getTimedBloodPressureJL()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_bpmonitor -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isBloodPress == true) {
                    DHBleSdk.subscribeData(timedBloodPressureCallback)
                    val healthMonitorBean = DrinkReminderBean()
                    healthMonitorBean.isOpen = true
                    healthMonitorBean.remindDuration = 60
                    healthMonitorBean.startHour = 0
                    healthMonitorBean.startMin = 0
                    healthMonitorBean.endHour = 23
                    healthMonitorBean.endMin = 59
                    DHBleSdk.setTimedBloodPressureJL(healthMonitorBean)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_get_temp_monitor -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isSupportTemperatureMonitoring == true) {
                    DHBleSdk.subscribeData(timedBodyTemperatureCallback)
                    DHBleSdk.getTimedBodyTemperature()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_temp_monitor -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isSupportTemperatureMonitoring == true) {
                    DHBleSdk.subscribeData(timedBodyTemperatureCallback)
                    val healthMonitorBean = DrinkReminderBean()
                    healthMonitorBean.isOpen = true
                    healthMonitorBean.remindDuration = 60
                    healthMonitorBean.startHour = 0
                    healthMonitorBean.startMin = 0
                    healthMonitorBean.endHour = 23
                    healthMonitorBean.endMin = 59
                    DHBleSdk.setTimedBodyTemperature(healthMonitorBean)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_get_muslim_time_mode -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isSupportMuslimTimeDisplayMode == true) {
                    DHBleSdk.subscribeData(muslimTimeDisplayModeCallback)
                    DHBleSdk.getMuslimTimeDisplayModeJL()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_set_muslim_time_mode -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isSupportMuslimTimeDisplayMode == true) {
                    DHBleSdk.subscribeData(muslimTimeDisplayModeCallback)
                    DHBleSdk.setMuslimTimeDisplayModeJL(2) // 默认3: 戒指休眠10分钟后再次唤醒时显示时间
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_control_open_bp -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isBloodPress == true) {
                    DHBleSdk.subscribeData(healthDataBroCallback)
                    DHBleSdk.subscribeData(testHrCallback)
                    DHBleSdk.controlHealthDataJL(CmdConstants.JL_BP_DATA_TRANSFER_KEY, 1)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_control_close_bp -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isBloodPress == true) {
                    DHBleSdk.subscribeData(testHrCallback)
                    DHBleSdk.controlHealthDataJL(CmdConstants.JL_BP_DATA_TRANSFER_KEY, 0)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_sensor_start -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isSupportSensorRawPPG == true) {
                    DHBleSdk.subscribeData(sensorRawDataCallback)
                    DHBleSdk.subscribeData(sensorRawControlCallback)
                    DHBleSdk.ringControlSensorRaw(1, 1) // 开启PPG+ACC (示例, sensorType按位组合)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_sensor_stop -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isSupportSensorRawPPG == true) {
                    DHBleSdk.subscribeData(sensorRawControlCallback)
                    DHBleSdk.ringControlSensorRaw(2, 1) // 关闭PPG+ACC
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_sensor_history -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isSupportSensorRawPPG == true) {
                    DHBleSdk.subscribeData(sensorHistoryRawCallback)
                    DHBleSdk.ringGetHistorySensorRaw()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_factory_test_hr -> {
                DHBleSdk.subscribeData(factoryTestCallback)
                DHBleSdk.startFactoryTest(0x15) // 心率校正
            }
            R.id.bt_fall_detect_get -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isSupportFallDetect == true) {
                    DHBleSdk.subscribeData(fallDetectCallback)
                    DHBleSdk.getFallDetect()
                } else {
                    Log.e("RWSDK", "Not Support Fall Detect")
                }
            }
            R.id.bt_fall_detect_set -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isSupportFallDetect == true) {
                    DHBleSdk.subscribeData(fallDetectCallback)
                    DHBleSdk.setFallDetect(true)
                } else {
                    Log.e("RWSDK", "Not Support Fall Detect")
                }
            }
            R.id.bt_get_count_reminder -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isSupportCountReminder == true) {
                    DHBleSdk.subscribeData(countReminderCallback)
                    DHBleSdk.getCountReminderInterval()
                } else {
                    Log.e("RWSDK", "Not Support Count Reminder")
                }
            }
            R.id.bt_set_count_reminder -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isSupportCountReminder == true) {
                    DHBleSdk.subscribeData(countReminderCallback)
                    DHBleSdk.setCountReminderInterval(60)
                } else {
                    Log.e("RWSDK", "Not Support Count Reminder")
                }
            }
            R.id.bt_open_control_takephoto -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isTakePhoto == true) {
                    DHBleSdk.subscribeData(takePhotoCallback)
                    DHBleSdk.controlTakePhotoJL(1) //Open Photo打开拍照
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_close_control_takephoto -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isTakePhoto == true) {
                    DHBleSdk.dispose(takePhotoCallback)
                    DHBleSdk.controlTakePhotoJL(0) //Close photo taking关闭拍照
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_control_finddevice -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isFindDevice == true) {
                    DHBleSdk.subscribeData(findDeviceControlCallback)
                    DHBleSdk.controlFindDeviceJL()
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_control_shutdown -> {
                val support = XXApplication.instance.currentSupportMenuBean
                if (support?.isPowerOff == true) {
                    //可订阅subscribe DeviceControlCallback回调
                    DHBleSdk.setPowerOffJL(Constants.CONTROL_DEVICE_POWER_OFF) //Shutdown (关机)
                    //DHBleSdk.setPowerOffJL(Constants.CONTROL_DEVICE_RECOVERY) //Factory Reset(恢复出厂)
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bt_data_gethealth -> {
                //Implement HealthDataSyncCallback to get health data
                DHBleSdk.syncAllHealthData(this)
            }
            R.id.bt_data_gethealth_onlyOne -> {
                //Implement HealthDataSyncCallback to get health data
                DHBleSdk.syncHealthDataByType(Constants.RingHealthType.TODAY_STEP, this)
            }
            R.id.bt_sport_workout -> {
                val support = XXApplication.instance.currentSupportMenuBean

                if (support?.isNewSport == true) {
                    startActivity(Intent(this@NewMainActivity, WorkoutTypeActivity::class.java))
                } else {
                    Toast.makeText(this, "Not Support!", Toast.LENGTH_SHORT).show()
                }

            }
            R.id.bt_update_ota -> {
                XXApplication.instance.isEnterOta = true
                val otaPath = "" //bin文件,厂家提供
                DHBleSdk.ringOtaWithFileData(otaPath, dialTransferCallback)
            }
        }
    }

    private var device: BleDevice?= null

    private fun loadSavedDevice() {
        val currentDevice = XXApplication.instance.currentConnectDevcie
        device = currentDevice ?: SavedDeviceStore.load(this)
        if (currentDevice == null) {
            XXApplication.instance.currentConnectDevcie = device
        }
        isConnected = DHBleSdk.isBleConnected()
    }

    private fun updateDevicePanel() {
        val savedDevice = device
        val deviceId = savedDevice?.bleDeviceId?.takeIf { it.isNotBlank() } ?: "-"
        val name = savedDevice?.bleName?.takeIf { it.isNotBlank() } ?: deviceId
        val mac = savedDevice?.bleMac?.takeIf { it.isNotBlank() } ?: "-"
        binding.bleName.text = "Name: $name"
        binding.bleMac.text = "MAC: $mac"
        binding.bleDeviceId.text = "DeviceID: $deviceId"
        binding.bleStatus.text = when {
            isConnected -> "Connected"
            savedDevice != null -> "Saved device, disconnected"
            else -> "No saved device"
        }
        binding.reconnect.isEnabled = savedDevice != null && !isConnected
        binding.disconnect.isEnabled = isConnected
        binding.searchDevice.text = if (savedDevice == null) "Search Device" else "Search Again"
    }

    private fun reconnectSavedDevice() {
        val savedDevice = device ?: SavedDeviceStore.load(this)
        if (savedDevice?.bleMac.isNullOrBlank()) {
            Toast.makeText(this, "No saved device, please search first", Toast.LENGTH_SHORT).show()
            updateDevicePanel()
            return
        }
        device = savedDevice
        XXApplication.instance.currentConnectDevcie = savedDevice
        binding.progressBar.visibility = View.VISIBLE
        updateDevicePanel()
        binding.bleStatus.text = "Connecting"
        DHBleSdk.connectDeviceWithModel(savedDevice!!)
    }

    private fun clearSavedDeviceAndSearch() {
        DHBleSdk.disconnect()
        SavedDeviceStore.clear(this)
        XXApplication.instance.currentConnectDevcie = null
        XXApplication.instance.currentSupportMenuBean = null
        device = null
        isConnected = false
        updateDevicePanel()
        XXApplication.instance.isEnterScanUIPage = true
        startActivity(Intent(this@NewMainActivity, ScanActivity::class.java))
    }

    override fun onBackPressed() {
//        super.onBackPressed()

        val home = Intent(Intent.ACTION_MAIN)
        home.addCategory(Intent.CATEGORY_HOME)
        startActivity(home)
    }


    override fun onSyncProgress(p0: Int) {

    }

    override fun onSyncFinish() {
        Log.e("RWSDK", "output: onSyncFinish ")
    }

    override fun onSyncError(p0: Int) {

    }

    override fun onSyncStep(p0: MutableList<StepSyncBean>?) {
        Log.e("RWSDK", "onSyncStep " + p0?.size)
    }

    override fun onSyncSleep(p0: MutableList<SleepSyncBean>?) {
        Log.e("RWSDK", "onSyncSleep " + p0?.size)
        p0?.forEachIndexed { index, bean ->
            Log.e("RWSDK", "---- Sleep Segment $index ----")
            Log.e("RWSDK", "time=${bean.time}, asleepTime=${bean.asleepTime}, awakeTime=${bean.awakeTime}")
            Log.e("RWSDK", "totalSleepTime=${bean.totalSleepTime} min, itemCount=${bean.itemCount}")

            bean.items?.forEachIndexed { idx, item ->
                Log.e("RWSDK", "  item $idx -> len=${item.len} min, type=${item.sleepType}")
            }
        }
    }

    override fun onSyncHr(p0: MutableList<HeartRateSyncBean>?) {
        Log.e("RWSDK", "output: onSyncHr " + p0?.size)
    }

    override fun onSyncBp(p0: MutableList<BloodPressSyncBean>?) {
        Log.e("RWSDK", "output: onSyncBp " + p0?.size)
    }

    override fun onSyncBo(p0: MutableList<BloodOxySyncBean>?) {
        Log.e("RWSDK", "output: onSyncBo " + p0?.size)
    }

    override fun onSyncTemp(p0: MutableList<BodyTempSyncBean>?) {

    }

    override fun onSyncPressure(p0: MutableList<PressureSyncBean>?) {

    }

    override fun onSyncBloodSugar(p0: MutableList<BloodSugarSyncBean>?) {

    }

    override fun onSyncBreath(p0: MutableList<BreatheSyncBean>?) {

    }

    override fun onSyncHrv(p0: MutableList<HrvSyncBean>?) {
        Log.e("RWSDK", "output: onSyncHrv " + p0?.size)
    }

    override fun onSyncMuslimCount(p0: MutableList<MuslimCountSyncBean>?) {

    }

    override fun onRingConnecting(device: BleDevice?) {
        this.device = device ?: this.device
        XXApplication.instance.currentConnectDevcie = this.device
        runOnUiThread {
            binding.progressBar.visibility = View.VISIBLE
            updateDevicePanel()
            binding.bleStatus.text = "Connecting"
        }

    }

    override fun onRingConnected(device: BleDevice?) {
        this.device = device ?: this.device
        this.device?.let {
            XXApplication.instance.currentConnectDevcie = it
            SavedDeviceStore.save(this, it)
        }
        isConnected = true
        runOnUiThread {
            binding.progressBar.visibility = View.GONE
            updateDevicePanel()
        }

    }

    override fun onRingConnectFailed(device: BleDevice?, reason: RingBleError) {
        Log.e("RWSDK", "main onRingConnectFailed " + reason)
        isConnected = false

        EventBus.getDefault().post(EventBusBean.LogEvent("", 1))

        runOnUiThread {
            binding.bleStatus.apply {
                text = "Disconnected"

                postDelayed(Runnable {
                    binding.progressBar.visibility = View.GONE
                    updateDevicePanel()

                }, 1000)
            }
        }

    }

    override fun onRingDidFunctionMenu(device: BleDevice?, supportMenuBean: SupportMenuBean) {
        Log.e("RWSDK", "onRingDidFunctionMenu " + supportMenuBean + " isSupportSensorRawACC " + supportMenuBean.isSupportSensorRawACC)

        EventBus.getDefault().post(EventBusBean.LogEvent("", 0))
        device?.let {
            SavedDeviceStore.save(this, it)
            runOnUiThread {
                updateDevicePanel()
            }
        }

    }
}
