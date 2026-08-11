package com.dhouse.dhsdk_v2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dhouse.dhsdk_v2.databinding.ItemDeviceSettingBinding
import com.dhouse.dhsdk_v2.demo.DemoSettingItem

class DeviceSettingAdapter(private val onClick: (DemoSettingItem) -> Unit) :
    RecyclerView.Adapter<DeviceSettingAdapter.Holder>() {
    private val items = mutableListOf<DemoSettingItem>()

    fun submit(newItems: List<DemoSettingItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemDeviceSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(items[position])

    inner class Holder(private val binding: ItemDeviceSettingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DemoSettingItem) {
            binding.settingIcon.text = item.title.take(1)
            binding.settingTitle.text = item.title
            binding.settingSubtitle.text = item.subtitle
            binding.settingValue.text = item.valueText
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
