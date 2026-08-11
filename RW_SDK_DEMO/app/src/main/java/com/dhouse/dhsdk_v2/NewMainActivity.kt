package com.dhouse.dhsdk_v2

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.dhouse.dhsdk_v2.databinding.ActivityNewMainBinding
import com.dhouse.dhsdk_v2.demo.DemoStateStore
import com.dhouse.dhsdk_v2.ui.device.DeviceFragment
import com.dhouse.dhsdk_v2.ui.home.HomeFragment
import com.example.blesdk.DHBleSdk

class NewMainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityNewMainBinding.inflate(layoutInflater) }
    private var isSharingLogs = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        DHBleSdk.initSDK(this)
        // Demo密码。客户App应在连接前传入当前账号对应的4位密码。
        DHBleSdk.prepareAutoPassword("1234")
        DemoStateStore.attach(this)

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_health -> {
                    showPage(HomeFragment(), getString(R.string.demo_health))
                    true
                }
                R.id.navigation_device -> {
                    showPage(DeviceFragment(), getString(R.string.demo_device))
                    true
                }
                else -> false
            }
        }
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.navigation_health
        }
    }

    override fun onResume() {
        super.onResume()
        DemoStateStore.attach(this)
    }

    private fun showPage(fragment: Fragment, title: String) {
        binding.toolbar.title = title
        supportFragmentManager.beginTransaction()
            .replace(R.id.page_container, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share_logs -> {
                shareLogs()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun shareLogs() {
        if (isSharingLogs) return
        isSharingLogs = true
        Toast.makeText(this, R.string.preparing_log_archive, Toast.LENGTH_SHORT).show()
        Thread {
            val result = runCatching { LogShareUtils.createLogArchive(applicationContext) }
            runOnUiThread {
                isSharingLogs = false
                if (isFinishing || isDestroyed) return@runOnUiThread
                val archive = result.getOrNull()
                if (archive == null) {
                    val message = if (result.isFailure) {
                        Log.e("RWSDK", "share logs failed", result.exceptionOrNull())
                        R.string.share_logs_failed
                    } else {
                        R.string.no_logs_to_share
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                runCatching {
                    val uri = FileProvider.getUriForFile(this, "$packageName.provider", archive)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/zip"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        clipData = ClipData.newUri(contentResolver, archive.name, uri)
                        putExtra(Intent.EXTRA_STREAM, uri)
                    }
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_logs_chooser)))
                }.onFailure {
                    Log.e("RWSDK", "open log share chooser failed", it)
                    Toast.makeText(this, R.string.share_logs_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
