package com.utilkit

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.facebook.react.ReactActivity
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.gson.Gson
import com.utilkit.lib.events.Channels
import com.utilkit.lib.events.Event
import com.utilkit.lib.events.EventBus
import com.utilkit.lib.service.UtilkitForegroundService
import com.utilkit.lib.service.transfers.CloudFile
import com.utilkit.lib.service.transfers.CloudProvider
import com.utilkit.lib.service.transfers.DownloadManagerService
import com.utilkit.lib.service.transfers.FileTransferModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Call
import org.json.JSONObject


@Suppress("UNCHECKED_CAST")
class UtilkitModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  var eventBus: EventBus? = null
  var transferModel: FileTransferModel? = null
  var client = OkHttpClient()

  companion object {
    const val NAME = "Utilkit"
    val gson = Gson()
    val PERMISSION_REQUEST_CODE = 1001
  }

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  fun multiply(a: Double, b: Double, promise: Promise) {
    promise.resolve(a * b)
  }

  @ReactMethod
  fun readAndUploadChunk(
    uploadUrl: String,
    headers: String,
    bytesProcessed: Int,
    totalBytes: Int,
    chunkSize: Int,
    file: String,
    onComplete: Promise
  ) {
    val filePath = JSONObject(file).optString("uri")
    if (filePath.isBlank()) {
      onComplete.reject(RuntimeException("File path=$filePath is invalid"))
      return
    }
    val localFile = File(filePath)

    if (!localFile.exists()) {
      onComplete.reject(RuntimeException("File not found"))
      return
    }

    try {
      val fileInputStream = FileInputStream(localFile)
      fileInputStream.skip(bytesProcessed.toLong())
      val buffer = ByteArray(chunkSize)
      val bytesRead = fileInputStream.read(buffer)
      fileInputStream.close()

      if (bytesRead > 0) {
        val requestBody =
          buffer.toRequestBody("application/octet-stream".toMediaTypeOrNull(), 0, bytesRead)

        val requestBuilder = Request.Builder()
          .url(uploadUrl)
          .post(requestBody)

        val headersJson = JSONObject(headers)
        headersJson.keys().forEach {
          val value = headersJson.getString(it)
          requestBuilder.addHeader(it, value)
        }

        val request = requestBuilder.build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
          override fun onFailure(call: Call, e: IOException) {
            Log.e("Utilkit", "Upload Err: ${e.message}")
            onComplete.reject(e)
          }

          override fun onResponse(call: Call, response: Response) {
            val body = response.body?.string()
            onComplete.resolve(
              gson.toJson(
                mapOf(
                  "status" to response.code,
                  "body" to body,
                  "headers" to response.headers
                )
              )
            )
          }
        })
      } else {
        onComplete.reject(RuntimeException("No data read"))
      }
    } catch (e: IOException) {
      onComplete.reject(e)
    }
  }


  @ReactMethod
  fun checkPermission(permissionsCsv: String, promise: Promise?) {
    val context = this.currentActivity
    if (context == null) {
      promise?.reject(RuntimeException("Unable to start in background"))
      return
    }
    val permissions = permissionsCsv.split(",").map { it.trim() }

    if (permissions.any {
        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
      }) {
      if (permissions.any {
          ActivityCompat.shouldShowRequestPermissionRationale(context, it)
        }) {
        ActivityCompat.requestPermissions(
          context,
          permissions.toTypedArray(),
          PERMISSION_REQUEST_CODE
        )
      } else {
        ActivityCompat.requestPermissions(
          context,
          permissions.toTypedArray(),
          PERMISSION_REQUEST_CODE
        )
      }
      promise?.resolve("requested")
    } else {
      promise?.resolve("granted")
    }
  }

  @ReactMethod
  fun initEventBus(promise: Promise) {
    try {
      if (eventBus == null) {
        checkPermission(
          "android.permission.READ_EXTERNAL_STORAGE," +
                        "android.permission.WRITE_EXTERNAL_STORAGE",
          null
        )
        eventBus = ViewModelProvider(
          EventBus.mViewModelStore,
          ViewModelProvider.AndroidViewModelFactory.getInstance(this.currentActivity?.application!!)
        )[EventBus::class.java]


        transferModel = ViewModelProvider(
          EventBus.mViewModelStore,
          ViewModelProvider.AndroidViewModelFactory.getInstance(this.currentActivity?.application!!)
        )[FileTransferModel::class.java]

        @OptIn(DelicateCoroutinesApi::class) GlobalScope.launch(Dispatchers.Main) {
          eventBus?.nativeToReactBus?.observeForever { event ->
            val emitterParams = Arguments.createMap()
            emitterParams.putString("channel", event.channel)
            emitterParams.putString("payload", event.payload)
            Log.d("Utilkit", "nativeToReactBus got event $event")
            sendEventToReact(reactApplicationContext, event.channel, emitterParams)
          }

          transferModel?.transfers?.observeForever { transferResults ->
            val emitterParams = Arguments.createMap()
            emitterParams.putString("channel", Channels.Transfers)
            emitterParams.putString("payload", gson.toJson(transferResults))
            // Log.d("Utilkit", "transferResults Running ${transferResults.size} downloads")
            sendEventToReact(reactApplicationContext, Channels.Transfers, emitterParams)
          }
        }
        promise.resolve("initialized")
      } else {
        promise.resolve("already initialized")
      }
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  @ReactMethod
  fun startService(title: String, promise: Promise) {
    Log.d("Utilkit", "Starting foreground service...")
    val activity = this.currentActivity as ReactActivity?
    if (activity != null) {
      try {
        val intent = Intent(this.reactApplicationContext, UtilkitForegroundService::class.java)
        intent.putExtra("title", title)
        ContextCompat.startForegroundService(this.reactApplicationContext, intent)
        promise.resolve("Service started successfully")
      } catch (e: Exception) {
        promise.reject(e)
      }
    } else {
      promise.reject(Error("Cannot start service from background"))
    }
  }

  private fun sendEventToReact(reactContext: ReactContext, eventName: String, params: WritableMap) {
    try {
      val catalystInstance = reactContext.catalystInstance
      if (catalystInstance != null) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
          .emit(eventName, params)
      } else {
        Log.w("Utilkit", "Cannot send event: CatalystInstance is not available.")
      }
    } catch (e: Exception) {
      Log.e("Utilkit", "Error sending event to React Native: ${e.message}")
    }
  }


  @ReactMethod
  fun sendEvent(channel: String, payload: String, promise: Promise) {
    Log.d("utilkit", "trying to sendEvent")
    if (this.currentActivity?.application != null) {
      if (channel == Channels.Echo) {
        val params = Arguments.createMap()
        params.putString("message", "kotlin says $payload")
        sendEventToReact(reactApplicationContext, channel, params)
      } else {
        Log.d("utilkit", "called emitReactToNative eventBus=${eventBus}")

        if (eventBus != null) {
          @OptIn(DelicateCoroutinesApi::class)
          GlobalScope.launch(Dispatchers.Main) {
            eventBus?.emitReactToNative(Event(channel, payload))
          }

        } else {
          promise.reject(Error("Unable to send event. Did you forget to call Utilkit.initEventBus() ?"))
        }
      }
      promise.resolve("Sent")
    } else {
      promise.reject(Error("Unable to send event from background"))
    }
  }


  @ReactMethod
  fun addListener(eventName: String?) {
  }

  @ReactMethod
  fun removeListeners(count: Int?) {
  }


  @ReactMethod
  fun download(
    _cloudFile: String,
    url: String,
    _headers: String,
    _provider: String,
    targetPath: String,
    sourcePath: String,
    promise: Promise
  ) {
    Log.d("Utilkit", "Starting download service...")
    val activity = this.currentActivity as ReactActivity?
    if (activity != null) {
      try {
        val cloudFile = gson.fromJson(_cloudFile, CloudFile::class.java)
        val headers = gson.fromJson(_headers, Map::class.java)
        val provider = gson.fromJson(_provider, CloudProvider::class.java)
        val result = DownloadManagerService.getInstance(activity.application)
          .download(
            cloudFile,
            url,
            headers as Map<String, String>,
            provider,
            targetPath,
            sourcePath
          )

        promise.resolve(gson.toJson(result))
      } catch (e: Exception) {
        e.printStackTrace()
        promise.reject(e)
      }
    } else {
      promise.reject(Error("Cannot start service from background"))
    }
  }

}
