package com.dhouse.dhsdk_v2

import android.content.Context
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal object LogShareUtils {

    private const val BUFFER_SIZE = 8 * 1024

    /**
     * 将 files/logger 整体压缩到 cache/shared_logs，返回可通过 FileProvider 分享的 ZIP。
     * ZIP 不放在日志目录中，避免递归压缩自身。
     */
    fun createLogArchive(context: Context): File? {
        val logDirectory = File(context.filesDir, "logger")
        if (!logDirectory.isDirectory || !containsFile(logDirectory)) return null

        val shareDirectory = File(context.cacheDir, "shared_logs")
        check(shareDirectory.exists() || shareDirectory.mkdirs()) {
            "Unable to create log share directory"
        }
        shareDirectory.listFiles()
            ?.filter { it.isFile && it.extension.equals("zip", ignoreCase = true) }
            ?.forEach(File::delete)

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val archive = File(shareDirectory, "RW_SDK_logs_$timestamp.zip")
        try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(archive))).use { output ->
                addToArchive(output, logDirectory, logDirectory.parentFile ?: context.filesDir)
            }
        } catch (error: Exception) {
            archive.delete()
            throw error
        }
        return archive.takeIf { it.isFile && it.length() > 0L }
    }

    private fun containsFile(directory: File): Boolean {
        return directory.listFiles()?.any { child ->
            child.isFile || child.isDirectory && containsFile(child)
        } == true
    }

    private fun addToArchive(output: ZipOutputStream, file: File, root: File) {
        val relativePath = file.relativeTo(root).path.replace(File.separatorChar, '/')
        if (file.isDirectory) {
            val entryName = "$relativePath/"
            output.putNextEntry(ZipEntry(entryName).apply { time = file.lastModified() })
            output.closeEntry()
            file.listFiles()?.sortedBy(File::getName)?.forEach { child ->
                addToArchive(output, child, root)
            }
            return
        }

        output.putNextEntry(ZipEntry(relativePath).apply { time = file.lastModified() })
        BufferedInputStream(FileInputStream(file)).use { input ->
            val buffer = ByteArray(BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                output.write(buffer, 0, count)
            }
        }
        output.closeEntry()
    }
}
