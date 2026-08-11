package com.dhouse.dhsdk_v2.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.dhouse.dhsdk_v2.R
import com.dhouse.dhsdk_v2.databinding.FragmentHomeBinding
import com.dhouse.dhsdk_v2.XXApplication
import com.dhouse.dhsdk_v2.demo.DemoHealthType
import com.dhouse.dhsdk_v2.demo.DemoStateStore
import com.dhouse.dhsdk_v2.demo.DemoUiState
import com.dhouse.dhsdk_v2.ui.ScanActivity
import com.dhouse.dhsdk_v2.ui.Workout.WorkoutTypeActivity
import com.dhouse.dhsdk_v2.ui.adapter.HealthCellAdapter
import com.dhouse.dhsdk_v2.ui.adapter.HealthCellModel
import com.dhouse.dhsdk_v2.ui.health.HealthDetailActivity

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var removeObserver: (() -> Unit)? = null
    private var syncDialog: AlertDialog? = null
    private val adapter = HealthCellAdapter { type ->
        if (type == DemoHealthType.WORKOUT) {
            if (DemoStateStore.state.ready) {
                startActivity(Intent(requireContext(), WorkoutTypeActivity::class.java))
            } else {
                Toast.makeText(requireContext(), R.string.demo_connect_first, Toast.LENGTH_SHORT).show()
            }
        } else {
            startActivity(HealthDetailActivity.intent(requireContext(), type))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.healthList.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.healthList.adapter = adapter
        binding.healthList.setHasFixedSize(false)

        binding.deviceAction.setOnClickListener {
            val state = DemoStateStore.state
            if (state.device == null) {
                XXApplication.instance.isEnterScanUIPage = true
                startActivity(Intent(requireContext(), ScanActivity::class.java))
            } else if (!state.connected) {
                DemoStateStore.reconnect()
            }
        }
        binding.healthRefresh.setOnRefreshListener {
            if (!DemoStateStore.state.ready) {
                binding.healthRefresh.isRefreshing = false
                Toast.makeText(requireContext(), R.string.demo_connect_first, Toast.LENGTH_SHORT).show()
            } else {
                DemoStateStore.syncAll()
            }
        }
        removeObserver = DemoStateStore.observe(::render)
    }

    override fun onDestroyView() {
        hideSyncDialog()
        removeObserver?.invoke()
        removeObserver = null
        _binding = null
        super.onDestroyView()
    }

    private fun render(state: DemoUiState) {
        if (_binding == null) return
        val name = state.device?.bleName?.takeIf { it.isNotBlank() }
            ?: state.device?.bleDeviceId?.takeIf { it.isNotBlank() }
            ?: getString(R.string.demo_unbound_device)
        binding.deviceName.text = name
        binding.deviceStatus.text = state.connectionMessage
        binding.deviceAction.text = when {
            state.device == null -> getString(R.string.demo_add_device)
            !state.connected -> getString(R.string.demo_reconnect)
            else -> getString(R.string.demo_connected)
        }
        binding.deviceAction.isEnabled = state.device == null || !state.connected
        binding.healthRefresh.isEnabled = !state.syncing
        binding.healthRefresh.isRefreshing = state.syncing
        if (state.syncing) showSyncDialog(state.syncProgress) else hideSyncDialog()
        binding.syncProgress.visibility = if (state.syncing) View.VISIBLE else View.GONE
        binding.syncProgress.progress = state.syncProgress

        val cells = DemoHealthType.values()
            .filter { it.isSupported(state.supportMenu) }
            .map { type ->
                val latest = state.summaryValues[type.id] ?: listOfNotNull(
                        state.realtimeValues[type.id],
                        state.records[type.id]?.firstOrNull()
                    ).maxByOrNull { it.timestampSeconds }
                HealthCellModel(type, latest)
            }
        adapter.submit(cells)
        binding.emptyHealth.visibility = if (cells.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showSyncDialog(progress: Int) {
        val dialog = syncDialog ?: AlertDialog.Builder(requireContext())
            .setTitle(R.string.demo_sync_health)
            .setView(R.layout.dialog_sync_health)
            .setCancelable(false)
            .create()
            .also {
                syncDialog = it
                it.show()
            }
        if (!dialog.isShowing) dialog.show()
        dialog.findViewById<TextView>(R.id.sync_message)?.text = getString(R.string.demo_syncing_progress, progress)
    }

    private fun hideSyncDialog() {
        syncDialog?.dismiss()
        syncDialog = null
    }
}
