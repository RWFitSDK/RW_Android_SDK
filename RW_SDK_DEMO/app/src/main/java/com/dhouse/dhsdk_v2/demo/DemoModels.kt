package com.dhouse.dhsdk_v2.demo

import androidx.annotation.StringRes
import com.dhouse.dhsdk_v2.R
import com.example.blesdk.bean.function.SupportMenuBean
import com.example.blesdk.blering.HealthDataType

enum class DemoHealthType(
    val id: String,
    @StringRes val titleRes: Int,
    val unit: String,
    /** 单次检测类型(HealthDataType.code), null 表示该类型不支持单次检测 */
    val measurementDataType: Int? = null
) {
    STEP("step", R.string.demo_health_step, "steps"),
    SLEEP("sleep", R.string.demo_health_sleep, "min"),
    HEART_RATE("heart_rate", R.string.demo_health_heart_rate, "bpm", HealthDataType.HEART_RATE.code),
    BLOOD_OXYGEN("blood_oxygen", R.string.demo_health_blood_oxygen, "%", HealthDataType.BLOOD_OXYGEN.code),
    HRV("hrv", R.string.demo_health_hrv, "ms", HealthDataType.HRV.code),
    PRESSURE("pressure", R.string.demo_health_pressure, "", HealthDataType.STRESS.code),
    BLOOD_PRESSURE("blood_pressure", R.string.demo_health_blood_pressure, "mmHg", HealthDataType.BLOOD_PRESSURE.code),
    BLOOD_SUGAR("blood_sugar", R.string.demo_health_blood_sugar, "mmol/L", HealthDataType.BLOOD_SUGAR.code),
    TEMPERATURE("temperature", R.string.demo_health_temperature, "℃", HealthDataType.TEMPERATURE.code),
    MUSLIM_COUNT("muslim_count", R.string.demo_health_muslim_count, "times"),
    WORKOUT("workout", R.string.demo_multi_sport, ""),
    RECORDING("recording", R.string.demo_recording, "");

    fun isSupported(menu: SupportMenuBean?): Boolean {
        menu ?: return false
        return when (this) {
            STEP -> menu.isStep
            SLEEP -> menu.isSleep
            HEART_RATE -> menu.isHr
            BLOOD_OXYGEN -> menu.isBloodOxy
            HRV -> menu.isHrv
            PRESSURE -> menu.isPressure
            BLOOD_PRESSURE -> menu.isBloodPress
            BLOOD_SUGAR -> menu.isBloodSugar
            TEMPERATURE -> menu.isDataTypeTemperature
            MUSLIM_COUNT -> menu.isMuslimCountData
            WORKOUT -> menu.isNewSport
            RECORDING -> menu.isSupportRecording
        }
    }

    companion object {
        fun fromId(id: String?): DemoHealthType? = values().firstOrNull { it.id == id }
    }
}

data class DemoHealthRecord(
    val timestampSeconds: Long,
    val value: String,
    val detail: String
)

data class DemoSettingItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val valueText: String
)
