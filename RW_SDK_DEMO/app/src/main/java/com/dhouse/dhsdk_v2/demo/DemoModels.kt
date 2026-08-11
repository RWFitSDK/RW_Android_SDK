package com.dhouse.dhsdk_v2.demo

import androidx.annotation.StringRes
import com.dhouse.dhsdk_v2.R
import com.example.blesdk.bean.function.SupportMenuBean
import com.example.blesdk.utils.CmdConstants

enum class DemoHealthType(
    val id: String,
    @StringRes val titleRes: Int,
    val unit: String,
    val measurementKey: Byte? = null
) {
    STEP("step", R.string.demo_health_step, "steps"),
    SLEEP("sleep", R.string.demo_health_sleep, "min"),
    HEART_RATE("heart_rate", R.string.demo_health_heart_rate, "bpm", CmdConstants.JL_HR_DATA_TRANSFER_KEY),
    BLOOD_OXYGEN("blood_oxygen", R.string.demo_health_blood_oxygen, "%", CmdConstants.JL_BO_DATA_TRANSFER_KEY),
    HRV("hrv", R.string.demo_health_hrv, "ms", CmdConstants.JL_HRV_DATA_TRANSFER_KEY),
    PRESSURE("pressure", R.string.demo_health_pressure, "", CmdConstants.JL_PRESSURE_DATA_TRANSFER_KEY),
    BLOOD_PRESSURE("blood_pressure", R.string.demo_health_blood_pressure, "mmHg", CmdConstants.JL_BP_DATA_TRANSFER_KEY),
    BLOOD_SUGAR("blood_sugar", R.string.demo_health_blood_sugar, "mmol/L", CmdConstants.JL_BLOODSUGAR_DATA_TRANSFER_KEY),
    TEMPERATURE("temperature", R.string.demo_health_temperature, "℃", CmdConstants.JL_TEMP_DATA_TRANSFER_KEY),
    MUSLIM_COUNT("muslim_count", R.string.demo_health_muslim_count, "times"),
    WORKOUT("workout", R.string.demo_multi_sport, "");

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
