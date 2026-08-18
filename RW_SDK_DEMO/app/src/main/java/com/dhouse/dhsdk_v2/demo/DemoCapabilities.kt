package com.dhouse.dhsdk_v2.demo

import android.content.Context
import com.dhouse.dhsdk_v2.R
import com.example.blesdk.bean.function.SupportMenuBean

fun buildDeviceSettings(context: Context, menu: SupportMenuBean?): List<DemoSettingItem> {
    val result = mutableListOf<DemoSettingItem>()
    if (menu == null) {
        return listOf(otaSetting(context))
    }
    fun add(supported: Boolean, id: String, titleRes: Int, subtitleRes: Int, valueRes: Int = R.string.demo_tap_to_set) {
        if (supported) result += DemoSettingItem(
            id,
            context.getString(titleRes),
            context.getString(subtitleRes),
            context.getString(valueRes)
        )
    }

    add(menu.isAlarm, "alarm", R.string.demo_alarm, R.string.demo_alarm_desc)
    add(menu.isBrightScreenSleepTime, "screen_sleep", R.string.demo_screen_sleep, R.string.demo_time_range_desc)
    add(menu.isBrightScreenTime, "bright_duration", R.string.demo_bright_duration, R.string.demo_bright_duration_desc)
    add(menu.isSupportScreenControl, "screen_control", R.string.demo_screen_control, R.string.demo_screen_control_desc)
    add(menu.isRaiseBrightScreen, "raise_to_wake", R.string.demo_raise_to_wake, R.string.demo_time_range_desc)
    add(menu.isVideoHid, "video", R.string.demo_video_control, R.string.demo_video_control_desc)
    add(menu.isLEDLight, "led", R.string.demo_led_brightness, R.string.demo_led_brightness_desc)
    add(menu.isWearDir, "wear", R.string.demo_wear_position, R.string.demo_wear_position_desc)
    add(menu.isFindDevice, "find", R.string.demo_find_device, R.string.demo_find_device_desc, R.string.demo_execute_now)
    add(menu.isTakePhoto, "take_photo", R.string.demo_camera_control, R.string.demo_camera_control_desc)
    add(menu.isHr, "monitor_hr", R.string.demo_all_day_heart_rate, R.string.demo_monitor_desc)
    add(menu.isBloodOxy, "monitor_bo", R.string.demo_all_day_blood_oxygen, R.string.demo_monitor_desc)
    add(menu.isHrv, "monitor_hrv", R.string.demo_all_day_hrv, R.string.demo_monitor_desc)
    add(menu.isPressure, "monitor_pressure", R.string.demo_all_day_pressure, R.string.demo_monitor_desc)
    add(menu.isBloodPress, "monitor_bp", R.string.demo_all_day_blood_pressure, R.string.demo_monitor_desc)
    add(menu.isBloodSugar, "monitor_sugar", R.string.demo_all_day_blood_sugar, R.string.demo_monitor_desc)
    add(menu.isSupportTemperatureMonitoring, "monitor_temp", R.string.demo_all_day_temperature, R.string.demo_monitor_desc)
    add(menu.isSupportPPGMonitoring, "monitor_ppg", R.string.demo_timed_ppg, R.string.demo_monitor_desc)
    add(menu.isSupportSensorRawPPG, "sensor_raw_ppg", R.string.demo_sensor_raw_ppg, R.string.demo_sensor_raw_ppg_desc)
    add(menu.isSupportHrReminder, "hr_alert", R.string.demo_hr_alert, R.string.demo_hr_alert_desc)
    add(menu.isSupportBoReminder, "bo_alert", R.string.demo_bo_alert, R.string.demo_bo_alert_desc)
    add(menu.isSupportMotoVibrationLevel, "vibration_count", R.string.demo_vibration_parameters, R.string.demo_vibration_parameters_desc)
    add(menu.isSupportAlarmVibrationDuration, "alarm_vibration", R.string.demo_alarm_vibration_count, R.string.demo_alarm_vibration_count_desc)
    add(menu.isSupportVibrationInterval, "vibration_interval", R.string.demo_vibration_interval, R.string.demo_vibration_interval_desc)
    add(menu.isSupportCountReminder, "count_reminder", R.string.demo_count_reminder, R.string.demo_count_reminder_desc)
    add(menu.isSupportFallDetect, "fall_detect", R.string.demo_fall_alert, R.string.demo_fall_alert_desc)
    add(menu.isRememberSwitch, "remember_switch", R.string.demo_tasbeeh_switch, R.string.demo_tasbeeh_switch_desc)
    add(menu.isSupportMuslimTimeDisplayMode, "muslim_time_mode", R.string.demo_tasbeeh_time_display, R.string.demo_tasbeeh_time_display_desc)
    add(menu.isSupportDevicePasswordAuth, "password", R.string.demo_device_password, R.string.demo_device_password_desc)
    add(menu.isPowerOff || menu.isRestart || menu.isRecovery, "power", R.string.demo_device_management, R.string.demo_device_management_desc)
    result += otaSetting(context)
    return result
}

private fun otaSetting(context: Context) = DemoSettingItem(
    "ota",
    context.getString(R.string.demo_firmware_upgrade),
    context.getString(R.string.demo_firmware_upgrade_desc),
    context.getString(R.string.demo_select_firmware)
)
