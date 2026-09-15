package com.example.data.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.data.local.PeliculaPreferences
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import com.example.data.model.Pelicula
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class DownloadHelper(
    private val context: Context,
    private val preferences: PeliculaPreferences,
    private val scope: CoroutineScope
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val activeJobs = ConcurrentHashMap<String, Job>()
    private val lastNotificationUpdate = ConcurrentHashMap<String, Long>()
    private val totalBandwidthBytesPerSec = AtomicLong(4 * 1024 * 1024L)

    companion object {
        const val CHANNEL_ID = "downloads_channel_v2"
        const val CHANNEL_NAME = "Descargas de películas"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Progreso de descargas de películas y videos"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getNotificationId(id: String): Int {
        return id.hashCode()
    }

    fun startDownload(pelicula: Pelicula) {
        val videoUrl = pelicula.safeVideoUrl
        if (videoUrl.isEmpty()) {
            Toast.makeText(context, "URL de video no válida", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch(Dispatchers.IO) {
            try {
                val sanitizedTitle = pelicula.safeTitle.replace(Regex("[^a-zA-Z0-9.-]"), "_")
                val hashSuffix = kotlin.math.abs(pelicula.id.hashCode()).toString().takeLast(6)
                val fileName = "${sanitizedTitle}_${hashSuffix}.mp4"
                val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }
                val destFile = File(targetDir, fileName)

                if (destFile.exists() && destFile.length() > 1024 * 1024) {
                    val currentList = preferences.downloads.first()
                    val existing = currentList.find { it.id == pelicula.id }
                    if (existing?.status == DownloadStatus.COMPLETED) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Película ya descargada", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }
                }

                val currentList = preferences.downloads.first()
                val maxLimit = preferences.maxConcurrentDownloads.first()
                val activeCount = currentList.count { it.status == DownloadStatus.DOWNLOADING }

                if (activeCount >= maxLimit) {
                    val pendingItem = DownloadItem(
                        id = pelicula.id,
                        title = pelicula.safeTitle,
                        originalVideoUrl = videoUrl,
                        coverUrl = pelicula.safeCoverUrl,
                        year = pelicula.safeYear,
                        type = pelicula.tp ?: "pl",
                        localFilePath = destFile.absolutePath,
                        status = DownloadStatus.PENDING,
                        progress = 0
                    )
                    preferences.addOrUpdateDownload(pendingItem)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "En cola de espera: Límite de $maxLimit simultáneas alcanzado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val downloadItem = DownloadItem(
                        id = pelicula.id,
                        title = pelicula.safeTitle,
                        originalVideoUrl = videoUrl,
                        coverUrl = pelicula.safeCoverUrl,
                        year = pelicula.safeYear,
                        type = pelicula.tp ?: "pl",
                        localFilePath = destFile.absolutePath,
                        status = DownloadStatus.DOWNLOADING,
                        progress = 0
                    )
                    preferences.addOrUpdateDownload(downloadItem)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Iniciando descarga...", Toast.LENGTH_SHORT).show()
                    }
                    launchDownloadJob(downloadItem, destFile)
                }
            } catch (e: Exception) {
                // Keep error feedback safe
            }
        }
    }

    fun pauseDownload(item: DownloadItem) {
        scope.launch(Dispatchers.IO) {
            try {
                activeJobs[item.id]?.cancel()
                activeJobs.remove(item.id)
                val pausedItem = item.copy(
                    status = DownloadStatus.PAUSED,
                    speedBytesPerSec = 0L,
                    etaSeconds = 0L
                )
                preferences.addOrUpdateDownload(pausedItem)
                showPausedNotification(pausedItem)
                checkAndStartNextPending()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun resumeDownload(item: DownloadItem) {
        scope.launch(Dispatchers.IO) {
            try {
                val currentList = preferences.downloads.first()
                val maxLimit = preferences.maxConcurrentDownloads.first()
                val activeCount = currentList.count { it.status == DownloadStatus.DOWNLOADING }
                val file = File(item.localFilePath)

                if (activeCount >= maxLimit) {
                    val pendingItem = item.copy(
                        status = DownloadStatus.PENDING,
                        speedBytesPerSec = 0L,
                        etaSeconds = 0L
                    )
                    preferences.addOrUpdateDownload(pendingItem)
                } else {
                    val resumingItem = item.copy(status = DownloadStatus.DOWNLOADING)
                    preferences.addOrUpdateDownload(resumingItem)
                    launchDownloadJob(resumingItem, file)
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun cancelDownload(item: DownloadItem) {
        scope.launch(Dispatchers.IO) {
            try {
                activeJobs[item.id]?.cancel()
                activeJobs.remove(item.id)
                val file = File(item.localFilePath)
                if (file.exists()) {
                    file.delete()
                }
                notificationManager.cancel(getNotificationId(item.id))
                preferences.removeDownload(item.id)
                checkAndStartNextPending()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun forceStartPending(item: DownloadItem) {
        resumeDownload(item)
    }

    fun pauseAllDownloads() {
        scope.launch(Dispatchers.IO) {
            try {
                val currentList = preferences.downloads.first()
                val downloadingOrPending = currentList.filter {
                    it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PENDING
                }
                downloadingOrPending.forEach { item ->
                    activeJobs[item.id]?.cancel()
                    activeJobs.remove(item.id)
                    val pausedItem = item.copy(
                        status = DownloadStatus.PAUSED,
                        speedBytesPerSec = 0L,
                        etaSeconds = 0L
                    )
                    preferences.addOrUpdateDownload(pausedItem)
                    showPausedNotification(pausedItem)
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun resumeAllDownloads() {
        scope.launch(Dispatchers.IO) {
            try {
                val currentList = preferences.downloads.first()
                val pausedOrPending = currentList.filter {
                    it.status == DownloadStatus.PAUSED || it.status == DownloadStatus.PENDING || it.status == DownloadStatus.FAILED
                }
                val maxLimit = preferences.maxConcurrentDownloads.first()
                var activeCount = currentList.count { it.status == DownloadStatus.DOWNLOADING }

                pausedOrPending.forEach { item ->
                    val file = File(item.localFilePath)
                    if (activeCount < maxLimit) {
                        activeCount++
                        val resumingItem = item.copy(status = DownloadStatus.DOWNLOADING)
                        preferences.addOrUpdateDownload(resumingItem)
                        launchDownloadJob(resumingItem, file)
                    } else {
                        val pendingItem = item.copy(
                            status = DownloadStatus.PENDING,
                            speedBytesPerSec = 0L,
                            etaSeconds = 0L
                        )
                        preferences.addOrUpdateDownload(pendingItem)
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun cancelAllDownloads() {
        scope.launch(Dispatchers.IO) {
            try {
                val currentList = preferences.downloads.first()
                val activeItems = currentList.filter {
                    it.status == DownloadStatus.DOWNLOADING ||
                    it.status == DownloadStatus.PAUSED ||
                    it.status == DownloadStatus.PENDING ||
                    it.status == DownloadStatus.FAILED
                }
                activeItems.forEach { item ->
                    activeJobs[item.id]?.cancel()
                    activeJobs.remove(item.id)
                    val file = File(item.localFilePath)
                    if (file.exists()) {
                        file.delete()
                    }
                    notificationManager.cancel(getNotificationId(item.id))
                    preferences.removeDownload(item.id)
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun launchDownloadJob(item: DownloadItem, destFile: File) {
        activeJobs[item.id]?.cancel()
        val job = scope.launch(Dispatchers.IO) {
            var input: InputStream? = null
            var raf: RandomAccessFile? = null
            var downloaded = if (destFile.exists()) destFile.length() else 0L

            try {
                val requestBuilder = Request.Builder().url(item.originalVideoUrl)
                if (downloaded > 0) {
                    requestBuilder.addHeader("Range", "bytes=$downloaded-")
                }

                val response = okHttpClient.newCall(requestBuilder.build()).execute()
                if (!response.isSuccessful && response.code != 206) {
                    if (response.code == 416) {
                        downloaded = 0L
                        destFile.delete()
                    } else {
                        throw Exception("HTTP ${response.code}: ${response.message}")
                    }
                }

                val body = response.body ?: throw Exception("Cuerpo de respuesta vacío")
                val contentLength = body.contentLength()
                val totalBytes = if (response.code == 206) downloaded + contentLength else if (contentLength > 0) contentLength else item.totalBytes

                raf = RandomAccessFile(destFile, "rw")
                if (response.code == 206) {
                    raf.seek(downloaded)
                } else {
                    raf.setLength(0)
                    downloaded = 0L
                }

                input = body.byteStream()
                val buffer = ByteArray(32 * 1024)
                var bytesRead = 0
                var lastSpeedCalcTime = System.currentTimeMillis()
                var bytesSinceLastCalc = 0L
                var currentSpeed = 0L
                var currentEta = 0L

                while (isActive && input.read(buffer).also { bytesRead = it } != -1) {
                    raf.write(buffer, 0, bytesRead)
                    downloaded += bytesRead
                    bytesSinceLastCalc += bytesRead

                    // Equal bandwidth sharing across all concurrent active downloads
                    val activeCount = activeJobs.size.coerceAtLeast(1)
                    if (activeCount > 1) {
                        val maxSpeedPerStream = totalBandwidthBytesPerSec.get() / activeCount
                        if (maxSpeedPerStream > 10 * 1024) {
                            val expectedChunkMs = (bytesRead * 1000L) / maxSpeedPerStream
                            if (expectedChunkMs in 2..150) {
                                delay(expectedChunkMs)
                            }
                        }
                    }

                    val now = System.currentTimeMillis()
                    val elapsed = now - lastSpeedCalcTime
                    if (elapsed >= 800) {
                        val instantSpeed = (bytesSinceLastCalc * 1000L) / elapsed
                        currentSpeed = if (currentSpeed > 0) ((currentSpeed * 0.6) + (instantSpeed * 0.4)).toLong() else instantSpeed
                        val currentActive = activeJobs.size.coerceAtLeast(1)
                        totalBandwidthBytesPerSec.set((currentSpeed * currentActive).coerceAtLeast(512 * 1024L))
                        lastSpeedCalcTime = now
                        bytesSinceLastCalc = 0L

                        val remainingBytes = (totalBytes - downloaded).coerceAtLeast(0L)
                        currentEta = if (currentSpeed > 2048 && remainingBytes > 0) {
                            remainingBytes / currentSpeed
                        } else 0L

                        val progress = if (totalBytes > 0) ((downloaded * 100) / totalBytes).toInt().coerceIn(0, 99) else 0

                        val updated = item.copy(
                            status = DownloadStatus.DOWNLOADING,
                            progress = progress,
                            downloadedBytes = downloaded,
                            totalBytes = totalBytes,
                            speedBytesPerSec = currentSpeed,
                            etaSeconds = currentEta
                        )
                        preferences.addOrUpdateDownload(updated)
                        updateProgressNotification(updated)
                    }
                }

                if (isActive) {
                    val finalSize = destFile.length()
                    val completed = item.copy(
                        status = DownloadStatus.COMPLETED,
                        progress = 100,
                        downloadedBytes = finalSize,
                        totalBytes = finalSize,
                        speedBytesPerSec = 0L,
                        etaSeconds = 0L
                    )
                    preferences.addOrUpdateDownload(completed)
                    showCompletedNotification(completed)
                    checkAndStartNextPending()
                }
            } catch (e: CancellationException) {
                // Paused or cancelled intentionally
            } catch (e: Exception) {
                val failed = item.copy(
                    status = DownloadStatus.FAILED,
                    speedBytesPerSec = 0L,
                    etaSeconds = 0L
                )
                preferences.addOrUpdateDownload(failed)
                showFailedNotification(failed, e.localizedMessage ?: "Error desconocido")
                checkAndStartNextPending()
            } finally {
                try { input?.close() } catch (e: Exception) {}
                try { raf?.close() } catch (e: Exception) {}
                activeJobs.remove(item.id)
            }
        }
        activeJobs[item.id] = job
    }

    private fun updateProgressNotification(item: DownloadItem) {
        val now = System.currentTimeMillis()
        val lastUpdate = lastNotificationUpdate[item.id] ?: 0L
        if (now - lastUpdate < 1000) return
        lastNotificationUpdate[item.id] = now

        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(item.title)
                .setContentText("${item.formattedSpeed} • ${item.progress}% • ${item.formattedEta}")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(100, item.progress, item.totalBytes <= 0)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build()
            notificationManager.notify(getNotificationId(item.id), notification)
        } catch (e: Exception) {
            // Notification safety
        }
    }

    private fun showCompletedNotification(item: DownloadItem) {
        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Descarga completada")
                .setContentText(item.title)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setAutoCancel(true)
                .setOngoing(false)
                .build()
            notificationManager.notify(getNotificationId(item.id), notification)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun showPausedNotification(item: DownloadItem) {
        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Descarga pausada")
                .setContentText("${item.title} (${item.progress}%)")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setAutoCancel(true)
                .setOngoing(false)
                .build()
            notificationManager.notify(getNotificationId(item.id), notification)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun showFailedNotification(item: DownloadItem, error: String) {
        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Error al descargar")
                .setContentText("${item.title}: $error")
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setAutoCancel(true)
                .setOngoing(false)
                .build()
            notificationManager.notify(getNotificationId(item.id), notification)
        } catch (e: Exception) {
            // ignore
        }
    }

    private suspend fun checkAndStartNextPending() {
        try {
            val list = preferences.downloads.first()
            val maxLimit = preferences.maxConcurrentDownloads.first()
            val activeCount = list.count { it.status == DownloadStatus.DOWNLOADING }
            if (activeCount < maxLimit) {
                val nextPending = list.firstOrNull { it.status == DownloadStatus.PENDING }
                if (nextPending != null) {
                    val file = File(nextPending.localFilePath)
                    val toStart = nextPending.copy(status = DownloadStatus.DOWNLOADING)
                    preferences.addOrUpdateDownload(toStart)
                    launchDownloadJob(toStart, file)
                }
            }
        } catch (e: Exception) {
            // ignore
        }
    }
}
