package com.dhouse.dhsdk_v2.ui.device

import android.content.Intent
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dhouse.dhsdk_v2.R
import com.dhouse.dhsdk_v2.databinding.FragmentDeviceBinding
import com.dhouse.dhsdk_v2.demo.*
import com.dhouse.dhsdk_v2.XXApplication
import com.dhouse.dhsdk_v2.ui.ScanActivity
import com.dhouse.dhsdk_v2.ui.adapter.DeviceSettingAdapter
import com.example.blesdk.DHBleSdk
import com.example.blesdk.bean.function.*
import com.example.blesdk.callback.data.*
import com.example.blesdk.callback.status.CustomStatusCallback
import com.example.blesdk.utils.Constants

class DeviceFragment : Fragment() {
    private var _binding: FragmentDeviceBinding? = null
    private val binding get() = _binding!!
    private var removeObserver: (() -> Unit)? = null
    private val adapter = DeviceSettingAdapter(::handleSetting)
    private val settingValues = mutableMapOf<String, String>()
    private var pendingSensorRawAction: Int? = null
    private var sensorHistoryCallback: SensorHistoryRawCallback? = null
    private var sensorHistoryLoadingDialog: AlertDialog? = null

    private val sensorRawControlCallback = object : SensorRawControlCallback {
        override fun onResult(data: Int?) {
            if (!isAdded) return
            updateSettingValue("sensor_raw_ppg", getString(R.string.demo_sensor_collection_complete))
        }

        override fun onSuccess() {
            val action = pendingSensorRawAction
            pendingSensorRawAction = null
            if (!isAdded || action == null) return
            val started = action == SENSOR_RAW_START
            updateSettingValue(
                "sensor_raw_ppg",
                getString(if (started) R.string.demo_sensor_collecting else R.string.demo_sensor_stop_sent)
            )
            toast(getString(if (started) R.string.demo_sensor_ppg_started else R.string.demo_sensor_stop_sent))
        }

        override fun onFail(errorCode: Int) {
            pendingSensorRawAction = null
            if (!isAdded) return
            toast(getString(R.string.demo_setting_failed, errorCode))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.settingList.layoutManager = LinearLayoutManager(requireContext())
        binding.settingList.adapter = adapter
        binding.settingList.isNestedScrollingEnabled = false
        binding.searchDevice.setOnClickListener { handleDeviceBindingAction() }
        binding.reconnectDevice.setOnClickListener { DemoStateStore.reconnect() }
        binding.disconnectDevice.setOnClickListener { DemoStateStore.disconnect() }
        binding.refreshDeviceInfo.setOnClickListener { DemoStateStore.refreshDeviceInfo() }
        DHBleSdk.subscribeData(sensorRawControlCallback)
        removeObserver = DemoStateStore.observe(::render)
    }

    override fun onDestroyView() {
        DHBleSdk.dispose(sensorRawControlCallback)
        sensorHistoryCallback?.let(DHBleSdk::dispose)
        sensorHistoryCallback = null
        sensorHistoryLoadingDialog?.dismiss()
        sensorHistoryLoadingDialog = null
        removeObserver?.invoke()
        removeObserver = null
        _binding = null
        super.onDestroyView()
    }

    private fun render(state: DemoUiState) {
        if (_binding == null) return
        val device = state.device
        binding.deviceName.text = device?.bleName?.takeIf { it.isNotBlank() }
            ?: device?.bleDeviceId?.takeIf { it.isNotBlank() }
            ?: getString(R.string.demo_unbound_device)
        binding.deviceAddress.text = device?.bleMac?.takeIf { it.isNotBlank() } ?: "--"
        binding.connectionState.text = state.connectionMessage
        binding.powerValue.text = state.battery?.let { "$it%" } ?: "--"
        binding.modelValue.text = state.deviceModel ?: "--"
        binding.firmwareValue.text = state.firmwareVersion ?: "--"
        binding.reconnectDevice.visibility = if (device != null && !state.connected) View.VISIBLE else View.GONE
        binding.reconnectDevice.isEnabled = !state.connecting
        binding.disconnectDevice.visibility = if (device != null && state.connected) View.VISIBLE else View.GONE
        binding.refreshDeviceInfo.isEnabled = state.ready
        binding.searchDevice.text = getString(if (device == null) R.string.demo_search_device else R.string.demo_unbind_device)
        val settings = buildDeviceSettings(requireContext(), state.supportMenu).map { item ->
            settingValues[item.id]?.let { item.copy(valueText = it) } ?: item
        }
        adapter.submit(settings)
        binding.emptySettings.visibility = if (settings.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun handleDeviceBindingAction() {
        if (DemoStateStore.state.device == null) {
            XXApplication.instance.isEnterScanUIPage = true
            startActivity(Intent(requireContext(), ScanActivity::class.java))
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.demo_unbind_device)
            .setMessage(R.string.demo_unbind_message)
            .setNegativeButton(R.string.demo_cancel, null)
            .setPositiveButton(R.string.demo_unbind_device) { _, _ -> DemoStateStore.forgetDevice() }
            .show()
    }

    private fun handleSetting(item: DemoSettingItem) {
        if (item.id == "ota") {
            showOtaIntegrationInfo()
            return
        }
        if (!DemoStateStore.state.ready) {
            toast(getString(R.string.demo_connect_first))
            return
        }
        when (item.id) {
            "alarm" -> showAlarmActions()
            "screen_sleep" -> editTimeRange(getString(R.string.demo_screen_sleep), 20, 0, 8, 0) { enabled, sh, sm, eh, em ->
                val callback = object : BrightTimeCallback {
                    override fun onResult(data: BrightScreenTimeBean?) = Unit
                    override fun onSuccess() = settingSuccess(this, item.id, if (enabled) timeRangeText(sh, sm, eh, em) else getString(R.string.demo_disabled))
                    override fun onFail(errorCode: Int) = settingFail(this, errorCode)
                }
                DHBleSdk.subscribeData(callback)
                DHBleSdk.setRingBrightScreenSleepTime(BrightScreenTimeBean().apply {
                    isOpen = enabled
                    startHour = sh
                    startMin = sm
                    endHour = eh
                    endMin = em
                })
            }
            "bright_duration" -> showNumberPicker(getString(R.string.demo_bright_duration), 0, 30, 10, "s") { seconds ->
                val callback = object : BrightTimeCallback {
                    override fun onResult(data: BrightScreenTimeBean?) = Unit
                    override fun onSuccess() = settingSuccess(this, item.id, getString(R.string.demo_seconds_value, seconds))
                    override fun onFail(errorCode: Int) = settingFail(this, errorCode)
                }
                DHBleSdk.subscribeData(callback)
                DHBleSdk.setBrightScreenTimeJL(BrightScreenTimeBean().apply { timeSecond = seconds })
            }
            "raise_to_wake" -> editTimeRange(getString(R.string.demo_raise_to_wake), 8, 0, 20, 0) { enabled, sh, sm, eh, em ->
                val callback = object : BrightCallback {
                    override fun onResult(data: BrightScreenBean?) = Unit
                    override fun onSuccess() = settingSuccess(this, item.id, if (enabled) timeRangeText(sh, sm, eh, em) else getString(R.string.demo_disabled))
                    override fun onFail(errorCode: Int) = settingFail(this, errorCode)
                }
                DHBleSdk.subscribeData(callback)
                DHBleSdk.setRaiseBrightScreenJL(BrightScreenBean().apply {
                    isOpen = enabled
                    startHour = sh
                    startMin = sm
                    endHour = eh
                    endMin = em
                })
            }
            "find" -> {
                DHBleSdk.controlFindDeviceJL()
                toast(getString(R.string.demo_find_sent))
            }
            "take_photo" -> choose(getString(R.string.demo_camera_control), arrayOf(getString(R.string.demo_enter_camera_mode), getString(R.string.demo_exit_camera_mode))) { index ->
                val enterMode = index == 0
                val callback = object : TakePhotoCallback {
                    override fun onResult(data: Int?) = Unit
                    override fun onSuccess() = settingSuccess(this, item.id, getString(if (enterMode) R.string.demo_camera_mode_active else R.string.demo_camera_mode_exited))
                    override fun onFail(errorCode: Int) = settingFail(this, errorCode)
                }
                DHBleSdk.subscribeData(callback)
                DHBleSdk.controlTakePhotoJL(if (enterMode) 1 else 0)
            }
            "video" -> {
                val labels = arrayOf(getString(R.string.demo_off), getString(R.string.demo_video), "Book", "Music")
                choose(getString(R.string.demo_video_mode), labels) { index ->
                val callback = object : VideoHidCallback {
                    override fun onResult(data: VideoHidBean?) = Unit
                    override fun onSuccess() = settingSuccess(this, item.id, labels[index])
                    override fun onFail(errorCode: Int) = settingFail(this, errorCode)
                }
                DHBleSdk.subscribeData(callback)
                DHBleSdk.setVideoHidJL(VideoHidBean().apply { hidOpen = index })
                }
            }
            "led" -> choose(getString(R.string.demo_led_brightness), arrayOf(getString(R.string.demo_off), getString(R.string.demo_brightness_level, 1), getString(R.string.demo_brightness_level, 2), getString(R.string.demo_brightness_level, 3))) { index ->
                val callback = object : BrightLedLevelCallback {
                    override fun onResult(data: BrightScreenLedBean?) = Unit
                    override fun onSuccess() = settingSuccess(this, item.id, if (index == 0) getString(R.string.demo_disabled) else getString(R.string.demo_brightness_level, index))
                    override fun onFail(errorCode: Int) = settingFail(this, errorCode)
                }
                DHBleSdk.subscribeData(callback)
                DHBleSdk.setRingLedLevel(BrightScreenLedBean().apply {
                    isOpen = index > 0
                    lcdLevel = index.coerceAtLeast(1)
                })
            }
            "wear" -> choose(getString(R.string.demo_wear_position), arrayOf(getString(R.string.demo_left_hand), getString(R.string.demo_right_hand))) { index ->
                val callback = object : WearHandCallback {
                    override fun onResult(data: FactoryInBean?) = Unit
                    override fun onSuccess() = settingSuccess(this, item.id, getString(if (index == 1) R.string.demo_right_hand else R.string.demo_left_hand))
                    override fun onFail(errorCode: Int) = settingFail(this, errorCode)
                }
                DHBleSdk.subscribeData(callback)
                DHBleSdk.setRingWearHand(index == 1)
            }
            "count_reminder" -> choose(getString(R.string.demo_count_reminder), arrayOf(getString(R.string.demo_off), getString(R.string.demo_minutes_value, 30), getString(R.string.demo_minutes_value, 60), getString(R.string.demo_minutes_value, 90), getString(R.string.demo_minutes_value, 120))) { index ->
                val values = intArrayOf(0, 30, 60, 90, 120)
                val callback = object : CountReminderIntervalCallback {
                    override fun onResult(data: Int?) = Unit
                    override fun onSuccess() = settingSuccess(this, item.id, if (values[index] == 0) getString(R.string.demo_disabled) else getString(R.string.demo_minutes_value, values[index]))
                    override fun onFail(errorCode: Int) = settingFail(this, errorCode)
                }
                DHBleSdk.subscribeData(callback)
                DHBleSdk.setCountReminderInterval(values[index])
            }
            "fall_detect" -> choose(getString(R.string.demo_fall_alert), onOffLabels()) { index ->
                val callback = object : FallDetectCallback {
                    override fun onResult(data: Int?) = Unit
                    override fun onSuccess() = settingSuccess(this, item.id, getString(if (index == 1) R.string.demo_enabled else R.string.demo_disabled))
                    override fun onFail(errorCode: Int) = settingFail(this, errorCode)
                }
                DHBleSdk.subscribeData(callback)
                DHBleSdk.setFallDetect(index == 1)
            }
            "hr_alert" -> editHeartRateAlert(item.id)
            "bo_alert" -> editBloodOxygenAlert(item.id)
            "vibration_count" -> editVibration(item.id)
            "alarm_vibration" -> showNumberPicker(getString(R.string.demo_alarm_vibration_count), 0, 6, 2, getString(R.string.demo_count_value, 1).replace("1", "").trim()) { count ->
                val callback = object : AlarmVibrationDurationCallback {
                    override fun onResult(data: Int?) = Unit
                    override fun onSuccess() = settingSuccess(this, item.id, getString(R.string.demo_count_value, count))
                    override fun onFail(errorCode: Int) = settingFail(this, errorCode)
                }
                DHBleSdk.subscribeData(callback)
                DHBleSdk.setAlarmVibrationDuration(count)
            }
            "vibration_interval" -> showValuePicker(getString(R.string.demo_vibration_interval), (100..1000 step 100).toList(), 500, "ms") { interval ->
                val callback = object : VibrationIntervalCallback {
                    override fun onResult(data: Int?) = Unit
                    override fun onSuccess() = settingSuccess(this, item.id, "${interval}ms")
                    override fun onFail(errorCode: Int) = settingFail(this, errorCode)
                }
                DHBleSdk.subscribeData(callback)
                DHBleSdk.setVibrationInterval(interval)
            }
            "remember_switch" -> choose(getString(R.string.demo_tasbeeh_switch), onOffLabels()) { index ->
                val callback = object : MuslimCountSwitchCallback {
                    override fun onResult(data: Int?) = Unit
                    override fun onSuccess() = settingSuccess(this, item.id, getString(if (index == 1) R.string.demo_enabled else R.string.demo_disabled))
                    override fun onFail(errorCode: Int) = settingFail(this, errorCode)
                }
                DHBleSdk.subscribeData(callback)
                DHBleSdk.deviceRememberSwitch(index)
            }
            "muslim_time_mode" -> choose(
                getString(R.string.demo_tasbeeh_time_display),
                arrayOf(getString(R.string.demo_tasbeeh_time_always), getString(R.string.demo_tasbeeh_time_never), getString(R.string.demo_tasbeeh_time_after_sleep))
            ) { index ->
                val mode = index + 1
                val callback = object : MuslimTimeDisplayModeCallback {
                    override fun onResult(data: Int?) = Unit
                    override fun onSuccess() = settingSuccess(this, item.id, getString(R.string.demo_mode_value, mode))
                    override fun onFail(errorCode: Int) = settingFail(this, errorCode)
                }
                DHBleSdk.subscribeData(callback)
                DHBleSdk.setMuslimTimeDisplayModeJL(mode)
            }
            "password" -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.demo_restore_default_password)
                    .setMessage(R.string.demo_restore_default_password_message)
                    .setNegativeButton(R.string.demo_cancel, null)
                    .setPositiveButton(R.string.demo_modify) { _, _ -> resetPassword() }
                    .show()
            }
            "sensor_raw_ppg" -> showSensorRawPpgActions()
            "power" -> showPowerActions()
            "monitor_hr", "monitor_bo", "monitor_hrv", "monitor_pressure",
            "monitor_bp", "monitor_sugar", "monitor_temp", "monitor_ppg" -> {
                choose(getString(R.string.demo_interval_title, item.title), arrayOf(getString(R.string.demo_off), getString(R.string.demo_every_minutes, 30), getString(R.string.demo_every_minutes, 60))) { index ->
                    setMonitor(item.id, index)
                }
            }
        }
    }

    private fun showSensorRawPpgActions() {
        choose(
            getString(R.string.demo_sensor_raw_ppg),
            arrayOf(
                getString(R.string.demo_start_ppg),
                getString(R.string.demo_stop_ppg),
                getString(R.string.demo_get_ppg_history)
            )
        ) { action ->
            when (action) {
                0 -> controlSensorRawPpg(SENSOR_RAW_START)
                1 -> controlSensorRawPpg(SENSOR_RAW_STOP)
                2 -> getSensorRawPpgHistory()
            }
        }
    }

    private fun controlSensorRawPpg(action: Int) {
        if (pendingSensorRawAction != null) return
        pendingSensorRawAction = action
        if (action == SENSOR_RAW_STOP) {
            updateSettingValue("sensor_raw_ppg", getString(R.string.demo_sensor_stopping))
        }
        // PPG 固定使用 sensorType=2，与微信小程序 Demo 保持一致。
        DHBleSdk.ringControlSensorRaw(action, SENSOR_TYPE_PPG)
    }

    private fun getSensorRawPpgHistory() {
        if (sensorHistoryCallback != null) return
        var records: List<SensorHistoryRawBean> = emptyList()
        val callback = object : SensorHistoryRawCallback {
            override fun onResult(data: List<SensorHistoryRawBean>?) {
                records = data.orEmpty()
            }

            override fun onSuccess() {
                finishSensorHistory(this, records)
            }

            override fun onFail(errorCode: Int) {
                finishSensorHistory(this, null)
                if (isAdded) toast(getString(R.string.demo_setting_failed, errorCode))
            }
        }
        sensorHistoryCallback = callback
        sensorHistoryLoadingDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.demo_ppg_history)
            .setMessage(R.string.demo_reading_history)
            .setCancelable(false)
            .create()
            .also { it.show() }
        DHBleSdk.subscribeData(callback)
        DHBleSdk.ringGetHistorySensorRaw()
    }

    private fun finishSensorHistory(callback: SensorHistoryRawCallback, records: List<SensorHistoryRawBean>?) {
        DHBleSdk.dispose(callback)
        if (sensorHistoryCallback === callback) sensorHistoryCallback = null
        sensorHistoryLoadingDialog?.dismiss()
        sensorHistoryLoadingDialog = null
        if (!isAdded || records == null) return

        val ppgRecords = records.filter { it.type == SENSOR_HISTORY_TYPE_PPG }
        val sampleCount = ppgRecords.sumOf { it.ppgDataList?.size ?: 0 }
        updateSettingValue(
            "sensor_raw_ppg",
            getString(R.string.demo_ppg_history_summary, ppgRecords.size, sampleCount)
        )
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.demo_ppg_history)
            .setMessage(
                if (ppgRecords.isEmpty()) {
                    getString(R.string.demo_ppg_history_empty)
                } else {
                    getString(R.string.demo_ppg_history_result, ppgRecords.size, sampleCount)
                }
            )
            .setPositiveButton(R.string.demo_confirm, null)
            .show()
    }

    private fun updateSettingValue(settingId: String, value: String) {
        settingValues[settingId] = value
        if (_binding != null) render(DemoStateStore.state)
    }

    private fun showOtaIntegrationInfo() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.demo_ota_demo_title)
            .setMessage(R.string.demo_ota_demo_message)
            .setPositiveButton(R.string.demo_confirm, null)
            .show()

        // Select a valid firmware path before calling:
        // DHBleSdk.ringOtaWithFileData(filePath, callback)
    }

    private fun showAlarmActions() {
        choose(getString(R.string.demo_alarm_management), arrayOf(getString(R.string.demo_set_alarm), getString(R.string.demo_get_all_alarms), getString(R.string.demo_delete_all_alarms))) { index ->
            when (index) {
                0 -> editAlarm()
                1 -> getAllAlarms()
                2 -> confirmDeleteAllAlarms()
            }
        }
    }

    private fun editAlarm() {
        TimePickerDialog(requireContext(), { _, hour, minute ->
            val repeatLabels = arrayOf(getString(R.string.demo_once), getString(R.string.demo_every_day), getString(R.string.demo_weekdays), getString(R.string.demo_weekends))
            choose(getString(R.string.demo_repeat_schedule), repeatLabels) { repeatIndex ->
                val repeat = IntArray(7)
                when (repeatIndex) {
                    1 -> repeat.fill(1)
                    2 -> (1..5).forEach { repeat[it] = 1 }
                    3 -> { repeat[0] = 1; repeat[6] = 1 }
                }
                val callback = object : AlarmCallback {
                    override fun onResult(data: List<AlarmRemainderBean?>?) = Unit
                    override fun onSuccess() = settingSuccess(
                        this,
                        "alarm",
                        String.format("%02d:%02d · %s", hour, minute, repeatLabels[repeatIndex])
                    )
                    override fun onFail(errorCode: Int) = settingFail(this, errorCode)
                }
                val alarm = AlarmRemainderBean().apply {
                    alarmTag = ""
                    repeatModel = repeat
                    startHour = hour
                    startMin = minute
                    isOpen = true
                    alarmId = 0
                }
                DHBleSdk.subscribeData(callback)
                DHBleSdk.setAlarmRemindJL(listOf(alarm))
            }
        }, 7, 0, true).show()
    }

    private fun getAllAlarms() {
        val callback = object : AlarmCallback {
            override fun onResult(data: List<AlarmRemainderBean?>?) {
                DHBleSdk.dispose(this)
                if (!isAdded) return
                val alarms = data.orEmpty().filterNotNull()
                val message = if (alarms.isEmpty()) {
                    getString(R.string.demo_no_alarms)
                } else {
                    alarms.mapIndexed { index, alarm ->
                        val time = String.format("%02d:%02d", alarm.startHour, alarm.startMin)
                        getString(R.string.demo_alarm_item, index + 1, time, getString(if (alarm.isOpen) R.string.demo_on else R.string.demo_off), alarmRepeatText(alarm.repeatModel))
                    }.joinToString("\n")
                }
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.demo_all_alarms, alarms.size))
                    .setMessage(message)
                    .setPositiveButton(R.string.demo_confirm, null)
                    .show()
            }

            override fun onSuccess() = Unit
            override fun onFail(errorCode: Int) = settingFail(this, errorCode)
        }
        DHBleSdk.subscribeData(callback)
        DHBleSdk.getAlarmRemindJL()
    }

    private fun confirmDeleteAllAlarms() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.demo_delete_all_alarms)
            .setMessage(R.string.demo_delete_all_alarms_message)
            .setNegativeButton(R.string.demo_cancel, null)
            .setPositiveButton(R.string.demo_delete) { _, _ -> deleteAllAlarms() }
            .show()
    }

    private fun deleteAllAlarms() {
        val callback = object : AlarmCallback {
            override fun onResult(data: List<AlarmRemainderBean?>?) = Unit
            override fun onSuccess() = settingSuccess(this, "alarm", getString(R.string.demo_no_alarms))
            override fun onFail(errorCode: Int) = settingFail(this, errorCode)
        }
        DHBleSdk.subscribeData(callback)
        DHBleSdk.deleteAllAlarmRemindJL()
    }

    private fun alarmRepeatText(repeatModel: IntArray?): String {
        val repeat = repeatModel ?: return getString(R.string.demo_once)
        val weekdayNames = resources.getStringArray(R.array.demo_weekday_names)
        val enabledDays = mutableListOf<String>()
        repeat.forEachIndexed { index, enabled ->
            if (enabled == 1) weekdayNames.getOrNull(index)?.let(enabledDays::add)
        }
        return when (enabledDays.size) {
            0 -> getString(R.string.demo_once)
            7 -> getString(R.string.demo_every_day)
            else -> enabledDays.joinToString(", ")
        }
    }

    private fun editTimeRange(
        title: String,
        defaultStartHour: Int,
        defaultStartMinute: Int,
        defaultEndHour: Int,
        defaultEndMinute: Int,
        action: (Boolean, Int, Int, Int, Int) -> Unit
    ) {
        choose(getString(R.string.demo_switch_title, title), onOffLabels()) { enabledIndex ->
            if (enabledIndex == 0) {
                action(false, defaultStartHour, defaultStartMinute, defaultEndHour, defaultEndMinute)
                return@choose
            }
            TimePickerDialog(requireContext(), { _, startHour, startMinute ->
                TimePickerDialog(requireContext(), { _, endHour, endMinute ->
                    action(true, startHour, startMinute, endHour, endMinute)
                }, defaultEndHour, defaultEndMinute, true).apply { setTitle(getString(R.string.demo_select_end_time)) }.show()
            }, defaultStartHour, defaultStartMinute, true).apply { setTitle(getString(R.string.demo_select_start_time)) }.show()
        }
    }

    private fun editHeartRateAlert(settingId: String) {
        choose(getString(R.string.demo_hr_alert), onOffLabels()) { enabled ->
            if (enabled == 0) {
                sendHeartRateAlert(settingId, 0, 160, 0xff)
                return@choose
            }
            showNumberPicker(getString(R.string.demo_heart_rate_upper), 100, 220, 160, "bpm") { upper ->
                val lowerValues = listOf(0xff) + (40..100 step 5).toList()
                showValuePicker(getString(R.string.demo_heart_rate_lower), lowerValues, 0xff, "bpm", getString(R.string.demo_not_set)) { lower ->
                    sendHeartRateAlert(settingId, 1, upper, lower)
                }
            }
        }
    }

    private fun sendHeartRateAlert(settingId: String, enabled: Int, upper: Int, lower: Int) {
        val callback = object : HrReminderCallback {
            override fun onResult(data: HrReminderBean?) = Unit
            override fun onSuccess() = settingSuccess(
                this,
                settingId,
                if (enabled == 0) getString(R.string.demo_disabled) else "${lower.takeIf { it != 0xff } ?: "--"}–$upper bpm"
            )
            override fun onFail(errorCode: Int) = settingFail(this, errorCode)
        }
        DHBleSdk.subscribeData(callback)
        DHBleSdk.deviceSetHrAlertCmd(enabled, upper, lower)
    }

    private fun editBloodOxygenAlert(settingId: String) {
        choose(getString(R.string.demo_bo_alert), onOffLabels()) { enabled ->
            if (enabled == 0) {
                sendBloodOxygenAlert(settingId, 0, 94)
            } else {
                showNumberPicker(getString(R.string.demo_blood_oxygen_lower), 70, 100, 94, "%") { value ->
                    sendBloodOxygenAlert(settingId, 1, value)
                }
            }
        }
    }

    private fun sendBloodOxygenAlert(settingId: String, enabled: Int, value: Int) {
        val callback = object : BoReminderCallback {
            override fun onResult(data: BoReminderBean?) = Unit
            override fun onSuccess() = settingSuccess(this, settingId, if (enabled == 0) getString(R.string.demo_disabled) else getString(R.string.demo_below_percent, value))
            override fun onFail(errorCode: Int) = settingFail(this, errorCode)
        }
        DHBleSdk.subscribeData(callback)
        DHBleSdk.deviceSetBoAlertCmd(enabled, value)
    }

    private fun editVibration(settingId: String) {
        choose(getString(R.string.demo_vibration_strength), arrayOf(getString(R.string.demo_off), getString(R.string.demo_low), getString(R.string.demo_medium), getString(R.string.demo_high))) { level ->
            showNumberPicker(getString(R.string.demo_vibration_count), 0, 6, 2, getString(R.string.demo_count_value, 1).replace("1", "").trim()) { count ->
                val callback = object : VibrationCountCallback {
                    override fun onResult(data: BrightVibrationBean?) = Unit
                    override fun onSuccess() = settingSuccess(this, settingId, getString(R.string.demo_level_count_value, level, count))
                    override fun onFail(errorCode: Int) = settingFail(this, errorCode)
                }
                DHBleSdk.subscribeData(callback)
                DHBleSdk.setVibrationCount(level, count)
            }
        }
    }

    private fun setMonitor(id: String, selected: Int) {
        val interval = if (selected == 1) 30 else 60
        val valueText = if (selected == 0) getString(R.string.demo_disabled) else getString(R.string.demo_every_minutes, interval)
        val bean = DrinkReminderBean().apply {
            isOpen = selected > 0
            remindDuration = interval
            startHour = 0
            startMin = 0
            endHour = 23
            endMin = 59
        }
        when (id) {
            "monitor_hr" -> {
                val callback = object : TimedHeartRateCallback {
                    override fun onResult(data: DrinkReminderBean?) = Unit
                    override fun onSuccess() { settingSuccess(this, id, valueText) }
                    override fun onFail(errorCode: Int) { settingFail(this, errorCode) }
                }
                DHBleSdk.subscribeData(callback); DHBleSdk.setTimedHeartRateJL(bean)
            }
            "monitor_bo" -> {
                val callback = object : TimedBloodOxygenCallback {
                    override fun onResult(data: DrinkReminderBean?) = Unit
                    override fun onSuccess() { settingSuccess(this, id, valueText) }
                    override fun onFail(errorCode: Int) { settingFail(this, errorCode) }
                }
                DHBleSdk.subscribeData(callback); DHBleSdk.setTimedBloodOxygenJL(bean)
            }
            "monitor_hrv" -> {
                val callback = object : TimedHrvCallback {
                    override fun onResult(data: DrinkReminderBean?) = Unit
                    override fun onSuccess() { settingSuccess(this, id, valueText) }
                    override fun onFail(errorCode: Int) { settingFail(this, errorCode) }
                }
                DHBleSdk.subscribeData(callback); DHBleSdk.setTimedHRVJL(bean)
            }
            "monitor_pressure" -> {
                val callback = object : TimedStressCallback {
                    override fun onResult(data: DrinkReminderBean?) = Unit
                    override fun onSuccess() { settingSuccess(this, id, valueText) }
                    override fun onFail(errorCode: Int) { settingFail(this, errorCode) }
                }
                DHBleSdk.subscribeData(callback); DHBleSdk.setTimedStressJL(bean)
            }
            "monitor_bp" -> {
                val callback = object : TimedBloodPressureCallback {
                    override fun onResult(data: DrinkReminderBean?) = Unit
                    override fun onSuccess() { settingSuccess(this, id, valueText) }
                    override fun onFail(errorCode: Int) { settingFail(this, errorCode) }
                }
                DHBleSdk.subscribeData(callback); DHBleSdk.setTimedBloodPressureJL(bean)
            }
            "monitor_sugar" -> {
                val callback = object : TimedBloodSugarCallback {
                    override fun onResult(data: DrinkReminderBean?) = Unit
                    override fun onSuccess() { settingSuccess(this, id, valueText) }
                    override fun onFail(errorCode: Int) { settingFail(this, errorCode) }
                }
                DHBleSdk.subscribeData(callback); DHBleSdk.setTimedBloodSugarJL(bean)
            }
            "monitor_temp" -> {
                val callback = object : TimedBodyTemperatureCallback {
                    override fun onResult(data: DrinkReminderBean?) = Unit
                    override fun onSuccess() { settingSuccess(this, id, valueText) }
                    override fun onFail(errorCode: Int) { settingFail(this, errorCode) }
                }
                DHBleSdk.subscribeData(callback); DHBleSdk.setTimedBodyTemperature(bean)
            }
            "monitor_ppg" -> {
                val callback = object : TimedPPGCallback {
                    override fun onResult(data: DrinkReminderBean?) = Unit
                    override fun onSuccess() { settingSuccess(this, id, valueText) }
                    override fun onFail(errorCode: Int) { settingFail(this, errorCode) }
                }
                DHBleSdk.subscribeData(callback); DHBleSdk.setTimedPPGJL(bean)
            }
        }
    }

    private fun resetPassword() {
        DHBleSdk.modifyDevicePwd("0000", object : CustomStatusCallback {
            override fun onSuccess() { toast(getString(R.string.demo_password_restored)) }
            override fun onFail(errorCode: Int) { toast(getString(R.string.demo_modify_failed, errorCode)) }
        })
    }

    private fun showPowerActions() {
        val menu = DemoStateStore.state.supportMenu ?: return
        val labels = mutableListOf<String>()
        val values = mutableListOf<Int>()
        if (menu.isPowerOff) { labels += getString(R.string.demo_power_off); values += Constants.CONTROL_DEVICE_POWER_OFF }
        if (menu.isRestart) { labels += getString(R.string.demo_restart); values += Constants.CONTROL_DEVICE_RESTART }
        if (menu.isRecovery) { labels += getString(R.string.demo_factory_reset); values += Constants.CONTROL_DEVICE_RECOVERY }
        choose(getString(R.string.demo_device_management), labels.toTypedArray()) { index ->
            AlertDialog.Builder(requireContext())
                .setTitle(labels[index])
                .setMessage(R.string.demo_confirm_operation)
                .setNegativeButton(R.string.demo_cancel, null)
                .setPositiveButton(R.string.demo_confirm) { _, _ -> DHBleSdk.setPowerOffJL(values[index]) }
                .show()
        }
    }

    private fun choose(title: String, labels: Array<String>, action: (Int) -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setItems(labels) { _, which -> action(which) }
            .show()
    }

    private fun showNumberPicker(
        title: String,
        min: Int,
        max: Int,
        initial: Int,
        suffix: String,
        action: (Int) -> Unit
    ) {
        showValuePicker(title, (min..max).toList(), initial, suffix, null, action)
    }

    private fun showValuePicker(
        title: String,
        values: List<Int>,
        initial: Int,
        suffix: String,
        specialFirstLabel: String? = null,
        action: (Int) -> Unit
    ) {
        if (values.isEmpty()) return
        val picker = NumberPicker(requireContext()).apply {
            minValue = 0
            maxValue = values.lastIndex
            displayedValues = values.mapIndexed { index, value ->
                if (index == 0 && specialFirstLabel != null) specialFirstLabel else "$value $suffix"
            }.toTypedArray()
            this.value = values.indexOf(initial).takeIf { it >= 0 } ?: 0
            wrapSelectorWheel = false
        }
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(picker)
            .setNegativeButton(R.string.demo_cancel, null)
            .setPositiveButton(R.string.demo_confirm) { _, _ -> action(values[picker.value]) }
            .show()
    }

    private fun timeRangeText(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): String {
        return String.format("%02d:%02d～%02d:%02d", startHour, startMinute, endHour, endMinute)
    }

    private fun settingSuccess(callback: Any, settingId: String? = null, valueText: String? = null) {
        toast(getString(R.string.demo_setting_success))
        if (callback is com.example.blesdk.blering.BaseDataCallback<*>) DHBleSdk.dispose(callback)
        if (settingId != null && valueText != null) {
            settingValues[settingId] = valueText
            render(DemoStateStore.state)
        }
    }

    private fun settingFail(callback: Any, errorCode: Int) {
        toast(getString(R.string.demo_setting_failed, errorCode))
        if (callback is com.example.blesdk.blering.BaseDataCallback<*>) DHBleSdk.dispose(callback)
    }

    private fun toast(message: String) {
        if (isAdded) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun onOffLabels() = arrayOf(getString(R.string.demo_off), getString(R.string.demo_on))

    companion object {
        private const val SENSOR_RAW_START = 1
        private const val SENSOR_RAW_STOP = 2
        private const val SENSOR_TYPE_PPG = 2
        private const val SENSOR_HISTORY_TYPE_PPG = 1
    }
}
