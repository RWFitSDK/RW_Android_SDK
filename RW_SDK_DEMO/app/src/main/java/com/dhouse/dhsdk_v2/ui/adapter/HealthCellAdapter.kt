package com.dhouse.dhsdk_v2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dhouse.dhsdk_v2.R
import com.dhouse.dhsdk_v2.databinding.ItemHealthCellBinding
import com.dhouse.dhsdk_v2.demo.DemoHealthRecord
import com.dhouse.dhsdk_v2.demo.DemoHealthType
import java.text.SimpleDateFormat
import java.util.*

data class HealthCellModel(val type: DemoHealthType, val latest: DemoHealthRecord?)

class HealthCellAdapter(private val onClick: (DemoHealthType) -> Unit) :
    RecyclerView.Adapter<HealthCellAdapter.Holder>() {
    private val items = mutableListOf<HealthCellModel>()
    private val formatter = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    fun submit(newItems: List<HealthCellModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemHealthCellBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(items[position])

    inner class Holder(private val binding: ItemHealthCellBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HealthCellModel) {
            val title = binding.root.context.getString(item.type.titleRes)
            binding.healthTitle.text = title
            if (item.type == DemoHealthType.WORKOUT) {
                binding.healthValue.text = binding.root.context.getString(R.string.demo_enter_multi_sport)
                binding.healthTime.text = ""
            } else {
                binding.healthValue.text = item.latest?.value ?: binding.root.context.getString(R.string.demo_no_data)
                binding.healthTime.text = item.latest?.let {
                    formatter.format(Date(it.timestampSeconds * 1000L))
                } ?: binding.root.context.getString(R.string.demo_view_details)
            }
            binding.root.setOnClickListener { onClick(item.type) }
        }
    }
}
