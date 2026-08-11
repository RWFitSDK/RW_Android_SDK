package com.dhouse.dhsdk_v2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dhouse.dhsdk_v2.databinding.ItemHealthDetailBinding
import com.dhouse.dhsdk_v2.demo.DemoHealthRecord
import java.text.SimpleDateFormat
import java.util.*

class HealthDetailAdapter : RecyclerView.Adapter<HealthDetailAdapter.Holder>() {
    private val items = mutableListOf<DemoHealthRecord>()
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun submit(newItems: List<DemoHealthRecord>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemHealthDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(items[position])

    inner class Holder(private val binding: ItemHealthDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DemoHealthRecord) {
            binding.detailValue.text = item.value
            binding.detailDescription.text = item.detail
            binding.detailTime.text = formatter.format(Date(item.timestampSeconds * 1000L))
        }
    }
}
