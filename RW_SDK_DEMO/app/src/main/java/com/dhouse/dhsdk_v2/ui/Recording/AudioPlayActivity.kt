package com.dhouse.dhsdk_v2.ui.Recording

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dhouse.dhsdk_v2.databinding.ActivityAudioPlayBinding
import java.io.File

/**
 * 本地录音文件播放页。
 *
 * 自 RingRecording 分支的 SyFileAsrActivity 移植，仅保留 MediaPlayer 播放能力；
 * 文件转写/会议纪要依赖的 AI SDK 不在本 Demo 依赖内，故未随页迁移。
 */
class AudioPlayActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FILE_PATH = "extra_file_path"
        private const val TAG = "AudioPlayActivity"
    }

    private val binding by lazy { ActivityAudioPlayBinding.inflate(layoutInflater) }
    private var audioFile: File? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Audio Play"

        val path = intent.getStringExtra(EXTRA_FILE_PATH).orEmpty()
        audioFile = File(path).takeIf { it.exists() && it.isFile }
        binding.fileName.text = "File: ${audioFile?.name ?: "-"}"
        if (audioFile == null) {
            binding.status.text = "Status: file not found"
            binding.playAudio.isEnabled = false
        }

        binding.playAudio.setOnClickListener { togglePlayAudio() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        stopPlayAudio()
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        // 页面不可见即停止播放，避免后台持续占用音频焦点
        stopPlayAudio()
    }

    private fun togglePlayAudio() {
        if (mediaPlayer != null) {
            stopPlayAudio()
            return
        }
        val file = audioFile ?: return
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(file.absolutePath)
                setOnCompletionListener { stopPlayAudio() }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error what=$what extra=$extra")
                    runOnUiThread {
                        Toast.makeText(this@AudioPlayActivity, "Play failed", Toast.LENGTH_SHORT).show()
                    }
                    stopPlayAudio()
                    true
                }
                prepare()
                start()
            } catch (e: Exception) {
                Log.e(TAG, "Play audio failed", e)
                Toast.makeText(this@AudioPlayActivity, "Play failed: ${e.message}", Toast.LENGTH_SHORT).show()
                release()
                mediaPlayer = null
                return
            }
        }
        binding.playAudio.text = "Stop"
    }

    private fun stopPlayAudio() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
            } catch (_: IllegalStateException) {
            }
            it.release()
        }
        mediaPlayer = null
        binding.playAudio.text = "Play"
    }
}
