package com.utilkit

import android.app.Activity
import android.content.Context
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
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import com.facebook.react.bridge.Callback


@Suppress("UNCHECKED_CAST")
class UtilkitModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  var eventBus: EventBus? = null
  var transferModel: FileTransferModel? = null

  companion object {
    const val NAME = "Utilkit"
    val gson = Gson()
    val STORAGE_PERMISSION_REQUEST_CODE = 1001
  }

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  fun multiply(a: Double, b: Double, promise: Promise) {
    promise.resolve(a * b)
  }

  @ReactMethod
  fun readFileChunk(fileUri: String, offset: Int, chunkSize: Int, callback: Callback) {
    try {
      val fileInputStream = FileInputStream(fileUri)
      val fileChannel: FileChannel = fileInputStream.channel
      val buffer = ByteBuffer.allocate(chunkSize)
      fileChannel.position(offset.toLong())
      val bytesRead = fileChannel.read(buffer)

      if (bytesRead > 0) {
        val byteArray = ByteArray(bytesRead)
        buffer.flip()
        buffer.get(byteArray)
        fileInputStream.close()
        callback.invoke(null, byteArray)
      } else {
        fileInputStream.close()
        callback.invoke("No data read", null)
      }
    } catch (e: IOException) {
      callback.invoke(e.message, null)
    }
  }


  fun checkPermission(context: Activity) {
    val permissions = arrayOf(
      android.Manifest.permission.READ_EXTERNAL_STORAGE,
      android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    if (permissions.any {
        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
      }) {
      if (permissions.any {
          ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, it)
        }) {
        ActivityCompat.requestPermissions(context, permissions, STORAGE_PERMISSION_REQUEST_CODE)
      } else {
        ActivityCompat.requestPermissions(context, permissions, STORAGE_PERMISSION_REQUEST_CODE)
      }
    } else {
      // All permissions are granted
      // ... proceed with your logic
    }
  }

  @ReactMethod
  fun initEventBus(promise: Promise) {
    try {
      if (eventBus == null) {
        checkPermission(this.currentActivity as Activity)
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
