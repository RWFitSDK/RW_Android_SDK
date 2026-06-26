package com.dhouse.dhsdk_v2.ui.Workout

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.dhouse.dhsdk_v2.R

class WorkoutTypeAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_workout_type) {

    override fun convert(holder: BaseViewHolder, item: String) {
        holder.setText(R.id.tvTitle, item)
    }
}