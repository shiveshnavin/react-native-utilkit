package com.utilkit

import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import com.utilkit.lib.events.Channels
import com.utilkit.lib.events.Event
import com.utilkit.lib.events.EventBus
import com.utilkit.lib.service.UtilkitForegroundService
import com.utilkit.lib.service.UtilkitForegroundService.Companion.mViewModelStore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class UtilkitModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  var eventBus: EventBus? = null

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  fun multiply(a: Double, b: Double, promise: Promise) {
    promise.resolve(a * b)
  }

  @ReactMethod
  fun initEventBus(promise: Promise) {
    if (eventBus == null) {

      eventBus = ViewModelProvider(
        mViewModelStore,
        ViewModelProvider.AndroidViewModelFactory.getInstance(this.currentActivity?.application!!)
      )[EventBus::class.java]

      @OptIn(DelicateCoroutinesApi::class) GlobalScope.launch(Dispatchers.Main) {
        eventBus?.nativeToReactBus?.observeForever { event ->
          val emitterParams = Arguments.createMap()
          emitterParams.putString("channel", event.channel)
          emitterParams.putString("payload", event.payload)
          Log.d("Utilkit", "nativeToReactBus got event $event")
          sendEventToReact(reactApplicationContext, event.channel, emitterParams)
        }
      }

      promise.resolve("initialized")
    } else {
      promise.resolve("already initialized")
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  @ReactMethod
  fun startService(title: String, promise: Promise) {
    Log.d("Utilkit", "Starting foreground service...")
    val activity = this.currentActivity as ReactActivity?
    if (activity != null) {
      val intent = Intent(this.reactApplicationContext, UtilkitForegroundService::class.java)
      intent.putExtra("title", title)
      ContextCompat.startForegroundService(this.reactApplicationContext, intent)
      promise.resolve("Service started successfully")
    } else {
      promise.reject(Error("Cannot start service from background"))
    }
  }

  private fun sendEventToReact(reactContext: ReactContext, eventName: String, params: WritableMap) {
    val catalystInstance = reactContext.catalystInstance
    if (catalystInstance != null) {
      reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        .emit(eventName, params)
    } else {
      Log.w("Utilkit", "Cannot send event: CatalystInstance is not available.")
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


  companion object {
    const val NAME = "Utilkit"
  }
}
