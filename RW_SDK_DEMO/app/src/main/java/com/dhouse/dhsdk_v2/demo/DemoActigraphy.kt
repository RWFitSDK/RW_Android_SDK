package com.dhouse.dhsdk_v2.demo

import com.example.blesdk.bean.sync.ActigraphySyncBean

/** 每个15分钟组独立成行，仅显示SDK返回的有效项，不合并或累加体动值。 */
fun actigraphyDetailRecords(groups: List<ActigraphySyncBean>, countText: (Int) -> String): List<DemoHealthRecord> =
    groups.sortedByDescending { it.timestamp }.map { group ->
        DemoHealthRecord(group.timestamp, countText(group.validCount),
            "activityCounts: ${group.activityCounts.joinToString(", ", "[", "]")}")
    }
