package com.dhouse.dhsdk_v2.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dhouse.dhsdk_v2.BleHelper
import com.dhouse.dhsdk_v2.R
import com.dhouse.dhsdk_v2.ScreenStatusReceive
import com.dhouse.dhsdk_v2.XXApplication
import com.dhouse.dhsdk_v2.databinding.ActivityScanBinding
import com.dhouse.dhsdk_v2.ui.adapter.DeviceAdapter
import com.example.blesdk.DHBleSdk
import com.example.blesdk.bean.function.SupportMenuBean
import com.example.blesdk.ble.ScanBleService
import com.example.blesdk.ble.bean.BleDevice
import com.example.blesdk.blering.RingBleError
import com.example.blesdk.blering.RingConnectBleCallback
import com.example.blesdk.callback.ScanDeviceCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions

class ScanActivity : AppCompatActivity(), ScanDeviceCallback, View.OnClickListener,
    RingConnectBleCallback {
    private val devices by lazy {
        mutableListOf<BleDevice>()
    }

    private val adapter by lazy {
        DeviceAdapter(devices)
    }

    private val binding by lazy {
        ActivityScanBinding.inflate(layoutInflater)
    }

    private lateinit var permissionActivity: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        ScreenStatusReceive.registerScreenReceive(this)

        if (!BleHelper.isBleOpen(this)) {
            BleHelper.openBLE(this)
        }

        ScanBleService.getService().initBle(this)
        ScanBleService.getService().registerScanBleCallback(this)

        DHBleSdk.setConnectBleCallback(this)


        binding.deviceList.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        binding.deviceList.adapter = adapter

        adapter.setOnItemClickListener { _, _, position ->
            Log.i("test", "onCreate: $position")
            ScanBleService.getService().stopScan()
            binding.connecting.visibility = View.VISIBLE
            val bleDevice = devices[position]


            XXApplication.instance.currentConnectDevcie = bleDevice

            DHBleSdk.connectDeviceWithModel(bleDevice)

        }

        binding.refreshBtn.setOnClickListener(this)
        binding.stopBtn.setOnClickListener(this)
        binding.connecting.setOnClickListener(this)


        permissionActivity = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            val blueScan = it[Manifest.permission.BLUETOOTH_SCAN] ?: false
            val blueAdvertise = it[Manifest.permission.BLUETOOTH_ADVERTISE] ?: false
            val blueConnect = it[Manifest.permission.BLUETOOTH_CONNECT] ?: false
            val coarseLocation = it[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            val fileLocation = it[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            if (blueScan && blueAdvertise && blueConnect && coarseLocation && fileLocation) {
                binding.refreshBtn.isEnabled = false
                binding.refreshBtn.alpha = 0.5f
                ScanBleService.getService().startScan(true,null)
            }
        }

        refresh()
    }

    private fun refresh(){
//        permissionActivity.launch(getBluePermissionsArray())
        XXPermissions.with(this).permission(getBluePermissions()).request { _, all ->
            if (all){
                binding.refreshBtn.isEnabled = false
                binding.refreshBtn.alpha = 0.5f
                ScanBleService.getService().startScan(true,null)
            }
        }
    }
    override fun onScanFinish() {
        binding.refreshBtn.isEnabled = true
        binding.refreshBtn.alpha = 1f
    }

    override fun onScanDevice(device: BleDevice?) {
        device?.let {
            val find = devices.find { local ->
                local.bleMac == it.bleMac && local.bleName == it.bleName
            }
            find?.let {f ->
                devices -= f
            }
            devices += it
            devices.sortByDescending {ble ->
                ble.bleRssi
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun onError(errorCode: Int, e: Exception?) {

    }

    private fun getBluePermissions(): List<String> {
        val permissions = mutableListOf<String>()
        permissions += Permission.BLUETOOTH_SCAN
        permissions += Permission.BLUETOOTH_ADVERTISE
        permissions += Permission.BLUETOOTH_CONNECT
        permissions += Permission.ACCESS_COARSE_LOCATION
        permissions += Permission.ACCESS_FINE_LOCATION
        return permissions
    }

    private fun getBluePermissionsArray(): Array<String> {
        return arrayOf(
            Permission.BLUETOOTH_SCAN, Permission.BLUETOOTH_ADVERTISE,
            Permission.BLUETOOTH_CONNECT, Permission.ACCESS_COARSE_LOCATION,
            Permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.refreshBtn ->{
                refresh()
            }
            R.id.stopBtn -> {
                ScanBleService.getService().stopScan()
            }
        }
    }


    override fun onBackPressed() {
        val home = Intent(Intent.ACTION_MAIN)
        home.addCategory(Intent.CATEGORY_HOME)
        startActivity(home)
    }

    override fun onRingConnecting(device: BleDevice?) {

    }

    override fun onRingConnected(device: BleDevice?) {

    }

    override fun onRingConnectFailed(device: BleDevice?, reason: RingBleError) {

        runOnUiThread {
            binding.connecting.visibility = View.INVISIBLE
            Toast.makeText(this,"连接失败，请重试",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRingDidFunctionMenu(device: BleDevice?, supportMenuBean: SupportMenuBean) {
        runOnUiThread {
            binding.connecting.visibility = View.INVISIBLE
            Log.e("RWSDK", "scan onRingDidFunctionMenu " + supportMenuBean)
            XXApplication.instance.currentSupportMenuBean = supportMenuBean

            XXApplication.instance.isEnterScanUIPage = false
            finish()
        }
    }
}