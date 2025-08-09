package com.utilkit.lib.service.transfers

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.utilkit.lib.events.EventBus
import java.io.File

class DownloadManagerService(private var application: Application) {

  private val handler = Handler(Looper.getMainLooper())
  private var isListenerRunning = false

  private var transferModel: FileTransferModel = ViewModelProvider(
    EventBus.mViewModelStore,
    ViewModelProvider.AndroidViewModelFactory.getInstance(application)
  )[FileTransferModel::class.java]

  companion object {
    @Volatile
    private var instance: DownloadManagerService? = null

    fun getInstance(application: Application): DownloadManagerService {
      if (instance == null) {
        synchronized(this) {
          if (instance == null) {
            instance = DownloadManagerService(application)
          }
        }
      }
      return instance!!
    }
  }


  fun download(
    cloudFile: CloudFile,
    downloadUrl: String,
    headers: Map<String, String>,
    provider: CloudProvider,
    _targetPath: String,
    sourcePath: String
  ): FileTransferResult {

    val downloadManager = application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val request = DownloadManager.Request(Uri.parse(downloadUrl))
    headers.forEach { (t, u) ->
      request.addRequestHeader(t, u)
    }
    var targetPath = _targetPath

    if (targetPath.isBlank()) {
      targetPath = File(
        application.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
        cloudFile.name.toString()
      ).absolutePath
      request.setDestinationInExternalPublicDir(
        Environment.DIRECTORY_DOWNLOADS,
        cloudFile.name
      )
      if (File(targetPath).exists()) {
        targetPath = File(
          application.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
          "${System.currentTimeMillis()}_${cloudFile.name}"
        ).absolutePath
        request.setDestinationInExternalPublicDir(
          Environment.DIRECTORY_DOWNLOADS,
          "${System.currentTimeMillis()}_${cloudFile.name}"
        )
      }
    } else {
      var targetFile = File(targetPath)
      if (targetFile.exists()) {
        val targetFileName = "${System.currentTimeMillis()}_${targetFile.name}"
        targetFile = File(targetFile.parentFile, targetFileName)
      }
      targetPath = targetFile.absolutePath
      request.setDestinationUri(Uri.fromFile(targetFile))
    }

    Log.d("Utilkit", "Starting download $downloadUrl -> $targetPath")

    val fileTransferResult = FileTransferResult(
      transferType = TransferType.DOWNLOAD,
      provider = provider,
      id = cloudFile.id,
      statusMessage = "Starting download",
      status = Status.ACTIVE,
      bytesProcessed = 0,
      totalBytes = cloudFile.size?.toLong() ?: 1,
      localFilePath = targetPath,
      targetPath = targetPath,
      sourcePath = sourcePath,
      _nativeTransferId = "-1"
    )

    transferModel.updateTransfer(fileTransferResult)
    try {
      request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)
      val downloadId = downloadManager.enqueue(request)
      fileTransferResult._nativeTransferId = "$downloadId"
    } catch (e: Exception) {
      fileTransferResult.status = Status.FAILURE
      fileTransferResult.statusMessage = e.message
      transferModel.updateTransfer(fileTransferResult)
    }

    if (!isListenerRunning) {
      isListenerRunning = true
      trackDownloadProgress(downloadManager)
    }

    return fileTransferResult
  }

  fun getStatusString(statusCode: Int): String {
    return when (statusCode) {
      DownloadManager.STATUS_PENDING -> "Pending"
      DownloadManager.STATUS_RUNNING -> "Running"
      DownloadManager.STATUS_PAUSED -> "Paused"
      DownloadManager.STATUS_SUCCESSFUL -> "Successful"
      DownloadManager.STATUS_FAILED -> "Failed"
      else -> "Unknown Status"
    }
  }

  private fun updateDownloadStatus(
    downloadManager: DownloadManager,
    fileTransferResult: FileTransferResult
  ) {
    val downloadId = fileTransferResult._nativeTransferId?.toLong() ?: -1
    if (downloadId == -1L) {
      return
    }
    val query = DownloadManager.Query().setFilterById(downloadId)
    val cursor: Cursor = downloadManager.query(query)

    if (cursor.moveToFirst()) {
      val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
      val totalBytes =
        cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
      val downloadedBytes =
        cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
        // Log.d("Utilkit", "Progress for $downloadId $downloadedBytes $totalBytes ${getStatusString(status)}")

      if (status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PAUSED) {
        if (totalBytes > 0) {
          fileTransferResult.status = Status.ACTIVE
          fileTransferResult.totalBytes = totalBytes
          fileTransferResult.bytesProcessed = downloadedBytes
          fileTransferResult.statusMessage = "Downloading..."
          sendUpdate(fileTransferResult)
        }
      } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
        fileTransferResult.status = Status.SUCCESS
        fileTransferResult.totalBytes = totalBytes
        fileTransferResult.bytesProcessed = totalBytes
        fileTransferResult.statusMessage = "Download Complete"
        sendUpdate(fileTransferResult)
      } else if (status == DownloadManager.STATUS_FAILED) {
        fileTransferResult.status = Status.FAILURE
        fileTransferResult.totalBytes = totalBytes
        fileTransferResult.bytesProcessed = totalBytes
        fileTransferResult.statusMessage = "Download Failed"
        sendUpdate(fileTransferResult)
      }
    } else {
      Log.w("Utilkit", "Checking progress failed")
    }
    cursor.close()
  }

  private fun trackDownloadProgress(
    downloadManager: DownloadManager
  ) {
    Log.d("Utilkit", "Starting periodic trackDownloadProgress")
    handler.postDelayed(object : Runnable {
      override fun run() {

        val runningDownloads =
          transferModel.transfers.value?.filter { tx ->
            tx.status == Status.ACTIVE &&
              tx.transferType == TransferType.DOWNLOAD
          }
        runningDownloads?.forEach {
          updateDownloadStatus(downloadManager, it)
        }
        if (runningDownloads?.isEmpty() == false) {
          handler.postDelayed(this, 1000)
        } else {
          Log.d("Utilkit", "All downloads completed ${transferModel.transfers.value}")
          transferModel.transfers.value?.clear()
          isListenerRunning = false
        }
      }
    }, 1000)
  }

  fun sendUpdate(fileTransferResult: FileTransferResult) {
    transferModel.updateTransfer(fileTransferResult)
  }


}
