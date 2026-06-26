package com.dhouse.dhsdk_v2.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.dhouse.dhsdk_v2.R
import com.example.blesdk.bean.function.WeatherBean

class WeatherAdapter(data: MutableList<WeatherBean>) : BaseQuickAdapter<WeatherBean, BaseViewHolder>
    (R.layout.item_weather,data) {
    override fun convert(holder: BaseViewHolder, item: WeatherBean) {
        holder.setText(R.id.weatherType,"天气类型:${item.weatherType}")
        holder.setText(R.id.maxTemp,"最大气温:${item.maxTemp}")
        holder.setText(R.id.minTemp,"最低气温:${item.minTemp}")
        holder.setText(R.id.currentTemp,"当前气温:${item.currentTemp}")
    }
}