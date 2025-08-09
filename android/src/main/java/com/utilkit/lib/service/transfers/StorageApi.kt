package com.utilkit.lib.service.transfers

interface StorageApi {

  suspend fun upload(
    file: PickedFile,
    targetPath: String,
    listener: FileOpListener? = null
  ): FileTransferResult

  suspend fun uploadChunk(
    uploadUrl: String,
    chunk: ByteArray,
    bytesProcessed: Long,
    totalBytes: Long,
    file: PickedFile,
    onUploadProgress: (progressEvent: Any?) -> Unit
  ): Any?

  suspend fun chunked(
    uploadResult: FileTransferResult,
    uploadUrl: String,
    file: PickedFile,
    listener: FileOpListener
  ): FileTransferResult
}

data class PickedFile(
  val id: String,
  val mimeType: String,
  val name: String, val size: Long,
  val uri: String,
  val updated: String? = null,
  val reader: Reader
)

typealias FileOpListener = (update: FileTransferResult, cancel: (() -> Unit)?) -> Unit

interface Reader {
  suspend fun getChunk(offset: Long, chunkSize: Long): ByteArray
}
