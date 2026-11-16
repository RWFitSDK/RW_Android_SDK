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
    add(menu.isSupportCustomNumberDisplay, "custom_number_display", R.string.demo_custom_number_display, R.string.demo_custom_number_desc)
    add(menu.isSupportCustomTextDisplay, "custom_text_display", R.string.demo_custom_text_display, R.string.demo_custom_text_desc)
    add(menu.isRaiseBrightScreen, "raise_to_wake", R.string.demo_raise_to_wake, R.string.demo_time_range_desc)
    add(menu.isVideoHid, "video", R.string.demo_video_control, R.string.demo_video_control_desc)
    add(menu.isLEDLight, "led", R.string.demo_led_brightness, R.string.demo_led_brightness_desc)
    add(menu.isLEDLight, "time_format", R.string.demo_time_format, R.string.demo_time_format_desc)
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
    add(menu.isSupportCompositeMeasurement, "composite_measurement", R.string.demo_composite_measurement, R.string.demo_composite_measurement_desc)
    add(menu.isSupportPpgSampleRate, "ppg_sample_rate", R.string.demo_ppg_sample_rate, R.string.demo_ppg_sample_rate_desc)
    add(menu.isSupportCustomerModel, "customer_model", R.string.demo_customer_model, R.string.demo_customer_model_desc)
    add(menu.isSupportDeviceBodyColor, "device_body_color", R.string.demo_device_body_color, R.string.demo_device_body_color_desc)
    add(menu.isSupportCustomerModel, "bt_name", R.string.demo_device_name, R.string.demo_device_name_desc)
    add(menu.isSupportSensorRawPPG, "sensor_raw_ppg", R.string.demo_sensor_raw_ppg, R.string.demo_sensor_raw_ppg_desc)
    add(menu.isSupportHrReminder, "hr_alert", R.string.demo_hr_alert, R.string.demo_hr_alert_desc)
    add(menu.isSupportBoReminder, "bo_alert", R.string.demo_bo_alert, R.string.demo_bo_alert_desc)
    add(menu.isSupportVibrationSwitch, "vibration_switch", R.string.demo_vibration_switch, R.string.demo_vibration_switch_desc)
    add(menu.isSupportDeviceSize, "device_size", R.string.demo_device_size, R.string.demo_device_size_desc)
    add(menu.isSupportMotoVibrationLevel, "vibration_count", R.string.demo_vibration_parameters, R.string.demo_vibration_parameters_desc)
    add(menu.isSupportVibrationControl, "vibration_control", R.string.demo_vibration_control, R.string.demo_vibration_control_desc)
    add(menu.isSupportAlarmVibrationDuration, "alarm_vibration", R.string.demo_alarm_vibration_count, R.string.demo_alarm_vibration_count_desc)
    add(menu.isSupportVibrationInterval, "vibration_interval", R.string.demo_vibration_interval, R.string.demo_vibration_interval_desc)
    add(menu.isSupportCountReminder, "count_reminder", R.string.demo_count_reminder, R.string.demo_count_reminder_desc)
    add(menu.isSupportFallDetect, "fall_detect", R.string.demo_fall_alert, R.string.demo_fall_alert_desc)
    add(menu.isSupportSedentary, "sedentary", R.string.demo_sedentary, R.string.demo_sedentary_desc)
    add(menu.isDrink, "drink_reminder", R.string.demo_drink_reminder, R.string.demo_drink_reminder_desc)
    add(menu.isSupportDeviceChallenge, "device_challenge", R.string.demo_device_challenge, R.string.demo_device_challenge_desc, R.string.demo_execute_now)
    add(menu.isRememberSwitch, "remember_switch", R.string.demo_tasbeeh_switch, R.string.demo_tasbeeh_switch_desc)
    add(menu.isSupportCountControl, "count_control", R.string.demo_count_control, R.string.demo_count_control_desc, R.string.demo_count_control_unknown)
    add(menu.isSupportMuslimTimeDisplayMode, "muslim_time_mode", R.string.demo_tasbeeh_time_display, R.string.demo_tasbeeh_time_display_desc)
    add(menu.isSupportDevicePasswordAuth, "password", R.string.demo_device_password, R.string.demo_device_password_desc)
    add(menu.isPowerOff || menu.isRestart || menu.isRecovery, "power", R.string.demo_device_management, R.string.demo_device_management_desc)
    result += otaSetting(context)
    return result
}


/** 电量查询及主动推送中的充电状态，不使用扫描结果中的充电状态。 */
fun batteryChargingStatusRes(status: Int?): Int? = when (status) {
    0 -> R.string.demo_battery_not_charging
    1 -> R.string.demo_battery_charging
    else -> null
}

private fun otaSetting(context: Context) = DemoSettingItem(
    "ota",
    context.getString(R.string.demo_firmware_upgrade),
    context.getString(R.string.demo_firmware_upgrade_desc),
    context.getString(R.string.demo_select_firmware)
)

/** 扫描列表与设备信息共用产品形态名称；未知值不显示。 */
fun deviceFormRes(form: Int): Int? = when (form) {
    0 -> R.string.demo_device_form_screenless_ring
    1 -> R.string.demo_device_form_screen_ring
    2 -> R.string.demo_device_form_screenless_bracelet
    3 -> R.string.demo_device_form_screen_bracelet
    4 -> R.string.demo_device_form_screenless_wristband
    5 -> R.string.demo_device_form_screen_wristband
    6 -> R.string.demo_device_form_watch
    else -> null
}

/** 未支持客户型号时，不展示 SDK 为兼容旧设备返回的原型号。 */
fun customerModelForDisplay(model: String?, supported: Boolean): String? =
    model?.takeIf { supported && it.isNotBlank() }

/** 扫描列表、设备信息与设置页共用颜色名称；未知值不显示。 */
fun deviceSizeRes(size: Int): Int? = when (size) {
    0 -> R.string.demo_device_size_unset
    in 1..255 -> R.string.demo_device_size_value
    else -> null
}

fun deviceBodyColorRes(color: Int): Int? = when (color) {
    0 -> R.string.demo_body_color_unset
    1 -> R.string.demo_body_color_black
    2 -> R.string.demo_body_color_pink
    3 -> R.string.demo_body_color_red
    4 -> R.string.demo_body_color_silver
    5 -> R.string.demo_body_color_gold
    else -> null
}
