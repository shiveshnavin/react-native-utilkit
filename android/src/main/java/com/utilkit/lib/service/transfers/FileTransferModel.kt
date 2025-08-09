package com.utilkit.lib.service.transfers

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.util.Base64

class FileTransferModel : ViewModel() {

  private val _transfers = MutableLiveData<ArrayList<FileTransferResult>>(ArrayList())
  val transfers: LiveData<ArrayList<FileTransferResult>> get() = _transfers
  fun updateTransfer(update: FileTransferResult) {
    var list = _transfers.value
    if (list == null) {
      list = ArrayList()
    }
    val idx = list.indexOfFirst { cur -> cur.id == update.id }
    if (idx > -1) {
      list[idx] = update
    } else {
      list.add(update)
    }
    _transfers.postValue(list)
  }
}

data class CloudFile(
  val ext: String? = null,
  val fileRefId: String? = null,
  val id: String,
  val name: String? = null,
  val size: String? = null,
)

data class FileTransferResult(
  val transferType: TransferType,
  val provider: CloudProvider,
  val id: String,
  var statusMessage: String? = null,
  var status: Status,
  var bytesProcessed: Long,
  var totalBytes: Long,
  val localFilePath: String? = null,
  val targetPath: String,
  val sourcePath: String? = null,
  val sourceProvider: CloudProvider? = null,
  val targetRef: String? = null,
  val cancel: (() -> Unit)? = null,

  var _nativeTransferId: String?
)


enum class TransferType(val value: String) {
  DOWNLOAD("download"),
  UPLOAD("upload");

  override fun toString(): String {
    return value
  }
}


data class CloudProvider(
  val accessToken: String? = null,
  val createdTs: Float? = null,
  val creds: String? = null,
  val expiresTs: Float? = null,
  val id: String,
  val name: String,
  val provider: String,
  val refreshToken: String? = null,
  val status: Status? = null,
  val userid: String
)


enum class Status(val value: String) {
  ACTIVE("ACTIVE"),
  FAILURE("FAILURE"),
  INACTIVE("INACTIVE"),
  SUCCESS("SUCCESS");

  override fun toString(): String {
    return value
  }
}


fun readChunk(context: Context, filePath: String, bytesProcessed: Int, buffer: ByteArray): Int {
  return if (filePath.startsWith("content://")) {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(Uri.parse(filePath))
    readChunkFromStream(inputStream, bytesProcessed, buffer)
  } else if (filePath.startsWith("file://")) {
    val file = File(Uri.parse(filePath).path!!)
    val fileInputStream = FileInputStream(file)
    readChunkFromStream(fileInputStream, bytesProcessed, buffer)
  } else {
    val localFile = File(filePath)
    if (!localFile.exists()) {
      throw RuntimeException("File not found at $localFile")
    }
    val fileInputStream = FileInputStream(localFile)
    readChunkFromStream(fileInputStream, bytesProcessed, buffer)
  }
}

fun readChunkFromStream(inputStream: InputStream?, bytesProcessed: Int, buffer: ByteArray): Int {
  if (inputStream == null) {
    throw RuntimeException("Input Stream is null")
  }
  inputStream.skip(bytesProcessed.toLong())
  val bytesRead = inputStream.read(buffer)
  inputStream.close()
  return bytesRead
}




fun hashFile(context: Context, filePath: String, algorithm: String): String {
  val digest = MessageDigest.getInstance(algorithm)
  if (filePath.startsWith("content://")) {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(Uri.parse(filePath))
    updateDigestFromStream(digest, inputStream)
  } else if (filePath.startsWith("file://")) {
    val file = File(Uri.parse(filePath).path!!)
    val fileInputStream = FileInputStream(file)
    updateDigestFromStream(digest, fileInputStream)
  } else {
    val localFile = File(filePath)
    if (!localFile.exists()) {
      throw RuntimeException("File not found at $localFile")
    }
    val fileInputStream = FileInputStream(localFile)
    updateDigestFromStream(digest, fileInputStream)
  }

  val hashBytes = digest.digest()
  val base64Digest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    Base64.getEncoder().encodeToString(hashBytes)
  } else {
    android.util.Base64.encodeToString(hashBytes, android.util.Base64.DEFAULT)
  }
  return base64Digest
}

fun updateDigestFromStream(digest: MessageDigest, inputStream: InputStream?) {
  if (inputStream == null) {
    throw RuntimeException("Input Stream is null")
  }
  val buffer = ByteArray(1024)
  var bytesRead: Int
  while (inputStream.read(buffer).also { bytesRead = it } != -1) {
    digest.update(buffer, 0, bytesRead)
  }
  inputStream.close()
}
