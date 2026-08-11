package com.dhouse.dhsdk_v2.ui.health

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dhouse.dhsdk_v2.R
import com.dhouse.dhsdk_v2.databinding.ActivityHealthDetailBinding
import com.dhouse.dhsdk_v2.demo.DemoHealthType
import com.dhouse.dhsdk_v2.demo.DemoStateStore
import com.dhouse.dhsdk_v2.demo.DemoUiState
import com.dhouse.dhsdk_v2.ui.adapter.HealthDetailAdapter

class HealthDetailActivity : AppCompatActivity() {
    private val binding by lazy { ActivityHealthDetailBinding.inflate(layoutInflater) }
    private val adapter = HealthDetailAdapter()
    private var removeObserver: (() -> Unit)? = null
    private lateinit var type: DemoHealthType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = DemoHealthType.fromId(intent.getStringExtra(EXTRA_TYPE)) ?: run {
            finish()
            return
        }
        setContentView(binding.root)
        binding.toolbar.title = getString(type.titleRes)
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.detailList.layoutManager = LinearLayoutManager(this)
        binding.detailList.adapter = adapter

        binding.measurementAction.setOnClickListener {
            val state = DemoStateStore.state
            if (!state.ready) {
                Toast.makeText(this, R.string.demo_connect_first, Toast.LENGTH_SHORT).show()
            } else if (state.activeMeasurement == type) {
                DemoStateStore.stopMeasurement(type)
            } else {
                state.activeMeasurement?.let { DemoStateStore.stopMeasurement(it) }
                DemoStateStore.startMeasurement(type)
            }
        }
        removeObserver = DemoStateStore.observe(::render)
    }

    override fun onDestroy() {
        if (isFinishing && DemoStateStore.state.activeMeasurement == type) {
            DemoStateStore.stopMeasurement(type)
        }
        removeObserver?.invoke()
        removeObserver = null
        super.onDestroy()
    }

    private fun render(state: DemoUiState) {
        val records = state.records[type.id].orEmpty()
        adapter.submit(records)
        val latest = state.summaryValues[type.id] ?: listOfNotNull(
                state.realtimeValues[type.id],
                records.firstOrNull()
            ).maxByOrNull { it.timestampSeconds }
        binding.latestValue.text = latest?.value ?: getString(R.string.demo_no_data)
        binding.latestDescription.text = latest?.detail ?: getString(R.string.demo_sync_or_measure)
        binding.emptyDetail.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
        val supportsMeasurement = type.measurementKey != null
        binding.measurementSection.visibility = if (supportsMeasurement) View.VISIBLE else View.GONE
        if (supportsMeasurement) {
            val measuring = state.activeMeasurement == type
            binding.measurementStatus.text = getString(if (measuring) R.string.demo_measuring else R.string.demo_not_started)
            binding.measurementAction.text = getString(if (measuring) R.string.demo_stop_measurement else R.string.demo_start_measurement)
            binding.measurementAction.isEnabled = state.ready
        }
    }

    companion object {
        private const val EXTRA_TYPE = "health_type"
        fun intent(context: Context, type: DemoHealthType): Intent {
            return Intent(context, HealthDetailActivity::class.java).putExtra(EXTRA_TYPE, type.id)
        }
    }
}
