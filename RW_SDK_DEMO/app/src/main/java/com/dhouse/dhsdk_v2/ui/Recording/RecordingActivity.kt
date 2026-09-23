package com.dhouse.dhsdk_v2.ui.Recording

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.dhouse.dhsdk_v2.databinding.ActivityRecordingBinding
import com.example.blesdk.DHBleSdk
import com.example.blesdk.bean.function.RecordFileItemBean
import com.example.blesdk.bean.function.RecordFileTransferBean
import com.example.blesdk.bean.function.RecordStatusBean
import com.example.blesdk.callback.data.RecordControlCallback
import com.example.blesdk.callback.data.RecordFileDeleteCallback
import com.example.blesdk.callback.data.RecordFileListCallback
import com.example.blesdk.callback.data.RecordFileTransferCallback
import com.example.blesdk.callback.data.RecordFormatCallback
import com.example.blesdk.callback.data.RecordStatusCallback
import com.example.blesdk.blering.DevicePushType
import com.example.blesdk.blering.OnDevicePushListener
import com.example.blesdk.blering.PushData
import com.example.blesdk.utils.OpusBinConverter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingActivity : AppCompatActivity() {
    private val binding by lazy { ActivityRecordingBinding.inflate(layoutInflater) }
    private val fileItems = mutableListOf<RecordFileItemBean>()
    private val localFiles = mutableListOf<File>()
    private val rows = mutableListOf<RecordingRow>()
    private lateinit var adapter: RecordingAdapter
    private var isRecording = false
    private var downloadingFile: RecordFileItemBean? = null

    private sealed class RecordingRow {
        data class Header(val title: String) : RecordingRow()
        data class DeviceFile(val item: RecordFileItemBean) : RecordingRow()
        data class LocalFile(val file: File) : RecordingRow()
        data class Empty(val text: String) : RecordingRow()
    }

    private val recordStatusCallback = object : RecordStatusCallback {
        override fun onSuccess() {
        }

        override fun onFail(errorCode: Int) {
            runOnUiThread {
                binding.recordStatus.text = "Record status: query failed $errorCode"
            }
        }

        override fun onResult(data: RecordStatusBean) {
            runOnUiThread {
                isRecording = data.isRecording
                updateRecordStatus(data)
            }
        }
    }

    private val devicePushListener = object : OnDevicePushListener {
        override fun onPush(data: PushData) {
            Log.e(
                "RWSDK",
                "OnDevicePushListener type=${data.type} value=${data.value} timestamp=${data.timestamp}"
            )
            val recordStatus = data.value as? RecordStatusBean
            if (data.type == DevicePushType.RECORD_STATUS && recordStatus != null) {
                Log.e("RWSDK", "BLE_KEY_RECORD_STATUS push=$recordStatus")
                runOnUiThread {
                    isRecording = recordStatus.isRecording
                    updateRecordStatus(recordStatus)
                }
            }
        }
    }

    private val recordControlCallback = object : RecordControlCallback {
        override fun onSuccess() {
            queryRecordStatus()
        }

        override fun onFail(errorCode: Int) {
            toast("Record control failed $errorCode")
        }

        override fun onResult(data: Int) {
            Log.e("RWSDK", "RecordControlCallback onResult $data")
            queryRecordStatus()
        }
    }

    private val recordFileListCallback = object : RecordFileListCallback {
        override fun onSuccess() {
        }

        override fun onFail(errorCode: Int) {
            runOnUiThread {
                binding.loadFileList.isEnabled = true
                toast("Load file list failed $errorCode")
            }
        }

        override fun onResult(data: MutableList<RecordFileItemBean>) {
            runOnUiThread {
                binding.loadFileList.isEnabled = true
                fileItems.clear()
                fileItems.addAll(data)
                refreshFileList()
                toast("File list loaded: ${fileItems.size}")
            }
        }
    }

    private val recordTransferCallback = object : RecordFileTransferCallback {
        override fun onSuccess() {
        }

        override fun onFail(errorCode: Int) {
            runOnUiThread {
                binding.transferStatus.text = "Transfer: failed $errorCode"
                binding.transferProgress.progress = 0
                downloadingFile = null
            }
        }

        override fun onResult(data: RecordFileTransferBean) {
            runOnUiThread {
                val percent = (data.progress * 100).toInt().coerceIn(0, 100)
                binding.transferProgress.progress = percent
                binding.transferStatus.text = "Transfer: ${data.received}/${data.fileSize} bytes ($percent%)"
                if (data.isComplete) {
                    val filePath = saveRecordFile(data)
                    downloadingFile = null
                    Log.e("RWSDK", "Record transfer complete filePath=$filePath")
                }
            }
        }
    }

    private val recordDeleteCallback = object : RecordFileDeleteCallback {
        override fun onSuccess() {
            toast("Delete success")
            loadFileList()
        }

        override fun onFail(errorCode: Int) {
            toast("Delete failed $errorCode")
        }

        override fun onResult(data: Int) {
            if (data == 0) {
                toast("Delete success")
                loadFileList()
            } else {
                toast("Delete failed status=$data")
            }
        }
    }

    private val recordFormatCallback = object : RecordFormatCallback {
        override fun onSuccess() {
            toast("Format success")
            fileItems.clear()
            refreshFileList()
        }

        override fun onFail(errorCode: Int) {
            toast("Format failed $errorCode")
        }

        override fun onResult(data: Int) {
            if (data == 0) {
                toast("Format success")
                fileItems.clear()
                refreshFileList()
            } else {
                toast("Format failed status=$data")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Recording"

        adapter = RecordingAdapter()
        binding.fileList.adapter = adapter

        DHBleSdk.addOnDevicePushListener(devicePushListener)

        binding.recordControl.setOnClickListener {
            DHBleSdk.recordControl(!isRecording, recordControlCallback)
        }
        binding.recordFormat.setOnClickListener { confirmFormat() }
        binding.loadFileList.setOnClickListener { loadFileList() }
        binding.fileList.setOnItemClickListener { _, _, position, _ ->
            when (val row = rows.getOrNull(position)) {
                is RecordingRow.DeviceFile -> showDeviceFileActions(row.item)
                is RecordingRow.LocalFile -> showLocalFileActions(row.file)
                else -> Unit
            }
        }

        refreshFileList()
        queryRecordStatus()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        DHBleSdk.removeOnDevicePushListener(devicePushListener)
    }

    private fun queryRecordStatus() {
        DHBleSdk.getRecordStatus(recordStatusCallback)
    }

    private fun loadFileList() {
        binding.loadFileList.isEnabled = false
        DHBleSdk.getRecordFileList(recordFileListCallback)
    }

    private fun updateRecordStatus(data: RecordStatusBean) {
        val statusText = if (data.isRecording && data.startTime > 0) {
            val timeText = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(data.startTime * 1000))
            "Record status: recording $timeText / ${data.duration}s"
        } else {
            if (data.isRecording) "Record status: recording" else "Record status: idle"
        }
        binding.recordStatus.text = "$statusText\nStorage: ${fileSizeText(data.remainingCapacity)} / ${fileSizeText(data.totalCapacity)}"
        binding.recordControl.text = if (data.isRecording) "Stop Record" else "Start Record"
    }

    private fun refreshFileList() {
        loadLocalFiles()
        rows.clear()
        rows.add(RecordingRow.Header("Device Record Files (${fileItems.size})"))
        if (fileItems.isEmpty()) {
            rows.add(RecordingRow.Empty("No device files"))
        } else {
            rows.addAll(fileItems.map { RecordingRow.DeviceFile(it) })
        }
        rows.add(RecordingRow.Header("Local Recording Files (${localFiles.size})"))
        if (localFiles.isEmpty()) {
            rows.add(RecordingRow.Empty("No local files"))
        } else {
            rows.addAll(localFiles.map { RecordingRow.LocalFile(it) })
        }
        adapter.notifyDataSetChanged()
    }

    private fun loadLocalFiles() {
        val dir = recordingDir()
        localFiles.clear()
        val files = dir.listFiles { file ->
            file.isFile
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
        localFiles.addAll(files)
    }

    private fun showDeviceFileActions(item: RecordFileItemBean) {
        val localFile = findLocalFile(item.fileId)
        val actions = if (localFile != null) {
            arrayOf("Share Local File", "Download Again", "Delete From Device")
        } else {
            arrayOf("Download File", "Delete From Device")
        }
        AlertDialog.Builder(this)
            .setTitle("Device File ID: ${item.fileId}")
            .setItems(actions) { _, which ->
                if (localFile != null) {
                    when (which) {
                        0 -> shareLocalFile(localFile)
                        1 -> downloadFile(item)
                        2 -> DHBleSdk.deleteRecordFile(item.fileId, recordDeleteCallback)
                    }
                } else {
                    when (which) {
                        0 -> downloadFile(item)
                        1 -> DHBleSdk.deleteRecordFile(item.fileId, recordDeleteCallback)
                    }
                }
            }
            .show()
    }

    private fun showLocalFileActions(file: File) {
        AlertDialog.Builder(this)
            .setTitle(file.name)
            .setItems(arrayOf("Play File", "Share File", "Delete Local File")) { _, which ->
                when (which) {
                    0 -> playLocalFile(file)
                    1 -> shareLocalFile(file)
                    2 -> confirmDeleteLocalFile(file)
                }
            }
            .show()
    }

    private fun playLocalFile(file: File) {
        startActivity(Intent(this, AudioPlayActivity::class.java).apply {
            putExtra(AudioPlayActivity.EXTRA_FILE_PATH, file.absolutePath)
        })
    }

    private fun downloadFile(item: RecordFileItemBean) {
        downloadingFile = item
        binding.transferProgress.progress = 0
        binding.transferStatus.text = "Transfer: starting ID=${item.fileId}"
        DHBleSdk.transferRecordFile(item.fileId, recordTransferCallback)
    }

    private fun saveRecordFile(data: RecordFileTransferBean): String? {
        val bytes = data.fileData ?: ByteArray(0)
        val opusBytes = runCatching {
            OpusBinConverter.convert(bytes)
        }.onFailure {
            Log.e("RWSDK", "Convert record file to opus failed", it)
            binding.transferStatus.text = "Transfer: convert failed ${it.message}"
            toast("Convert opus failed")
        }.getOrNull() ?: return null
        val item = downloadingFile
        val dir = recordingDir()
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val duration = item?.duration ?: data.duration
        val completedAt = data.completedAt.takeIf { it > 0 } ?: System.currentTimeMillis() / 1000L
        val file = File(dir, "${data.fileId}_${duration}_${completedAt}.opus")
        file.writeBytes(opusBytes)
        data.filePath = file.absolutePath
        binding.transferStatus.text = "Transfer: saved ${file.absolutePath}"
        Log.e("RWSDK", "Record file saved ${file.absolutePath} rawSize=${bytes.size} opusSize=${opusBytes.size} completedAt=$completedAt")
        refreshFileList()
        toast("Saved: ${file.name}")
        return file.absolutePath
    }

    private fun recordingDir(): File {
        return File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Recording")
    }

    private fun findLocalFile(fileId: Long): File? {
        return localFiles.firstOrNull { it.name.startsWith("${fileId}_") }
    }

    private fun deviceTimeText(timestamp: Long): String {
        if (timestamp <= 0) {
            return "-"
        }
        val unixTime = timestamp // SDK 已统一返回 Unix 秒时间戳。
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(unixTime * 1000))
    }

    private fun localTimeText(file: File): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(file.lastModified()))
    }

    private fun fileSizeText(size: Long): String {
        return when {
            size >= 1024L * 1024L -> String.format(Locale.US, "%.2f MB", size / 1024f / 1024f)
            size >= 1024L -> String.format(Locale.US, "%.1f KB", size / 1024f)
            else -> "${size}B"
        }
    }

    private fun fileUri(file: File): Uri {
        return FileProvider.getUriForFile(this, "${packageName}.provider", file)
    }

    private fun shareLocalFile(file: File) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = opusMimeType()
            putExtra(Intent.EXTRA_STREAM, fileUri(file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share File"))
    }

    private fun opusMimeType(): String {
        return "audio/ogg"
    }

    private fun confirmDeleteLocalFile(file: File) {
        AlertDialog.Builder(this)
            .setTitle("Delete Local File")
            .setMessage(file.name)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                if (file.delete()) {
                    refreshFileList()
                    toast("Deleted: ${file.name}")
                } else {
                    toast("Delete failed")
                }
            }
            .show()
    }

    private fun confirmFormat() {
        AlertDialog.Builder(this)
            .setTitle("Confirm")
            .setMessage("Format will delete all recording files")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Format") { _, _ -> DHBleSdk.formatRecordStorage(recordFormatCallback) }
            .show()
    }

    private fun toast(text: String) {
        runOnUiThread {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }
    }

    private inner class RecordingAdapter : BaseAdapter() {
        override fun getCount(): Int = rows.size

        override fun getItem(position: Int): Any = rows[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun isEnabled(position: Int): Boolean {
            return rows.getOrNull(position) is RecordingRow.DeviceFile ||
                rows.getOrNull(position) is RecordingRow.LocalFile
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val row = rows[position]
            return when (row) {
                is RecordingRow.Header -> headerView(convertView, row.title)
                is RecordingRow.DeviceFile -> twoLineView(
                    convertView,
                    "ID:${row.item.fileId}  ${deviceTimeText(row.item.timestamp)}",
                    "Device  ${fileSizeText(row.item.fileSize)}  Duration:${row.item.duration}s"
                )
                is RecordingRow.LocalFile -> twoLineView(
                    convertView,
                    row.file.name,
                    "Local  ${fileSizeText(row.file.length())}  ${localTimeText(row.file)}"
                )
                is RecordingRow.Empty -> emptyView(convertView, row.text)
            }
        }

        private fun headerView(convertView: View?, title: String): View {
            val textView = (convertView as? TextView) ?: TextView(this@RecordingActivity).apply {
                setPadding(32, 22, 32, 10)
                textSize = 14f
            }
            textView.alpha = 1f
            textView.setTypeface(textView.typeface, Typeface.BOLD)
            textView.text = title
            return textView
        }

        private fun emptyView(convertView: View?, text: String): View {
            val textView = (convertView as? TextView) ?: TextView(this@RecordingActivity).apply {
                setPadding(32, 20, 32, 20)
                textSize = 14f
            }
            textView.alpha = 0.55f
            textView.setTypeface(textView.typeface, Typeface.NORMAL)
            textView.text = text
            return textView
        }

        private fun twoLineView(convertView: View?, title: String, subtitle: String): View {
            val holder: RowHolder
            val view: LinearLayout
            if (convertView is LinearLayout && convertView.tag is RowHolder) {
                view = convertView
                holder = convertView.tag as RowHolder
            } else {
                view = LinearLayout(this@RecordingActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(32, 18, 32, 18)
                }
                holder = RowHolder(
                    TextView(this@RecordingActivity).apply {
                        textSize = 15f
                        setTypeface(typeface, Typeface.BOLD)
                    },
                    TextView(this@RecordingActivity).apply {
                        textSize = 13f
                        alpha = 0.7f
                    }
                )
                view.addView(holder.title)
                view.addView(holder.subtitle)
                view.tag = holder
            }
            holder.title.text = title
            holder.subtitle.text = subtitle
            return view
        }
    }

    private data class RowHolder(
        val title: TextView,
        val subtitle: TextView
    )
}
