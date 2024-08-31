package com.utilkit

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.PermissionResult
import com.facebook.react.ReactActivity
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.utilkit.lib.service.UtilkitForegroundService
import java.util.UUID
import java.util.jar.Manifest

class UtilkitModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  fun multiply(a: Double, b: Double, promise: Promise) {
    promise.resolve(a * b)
  }

  fun <I, O> ComponentActivity.registerActivityResultLauncher(
    contract: ActivityResultContract<I, O>,
    callback: ActivityResultCallback<O>
  ): ActivityResultLauncher<I> {
    val key = UUID.randomUUID().toString()
    return activityResultRegistry.register(key, contract, callback)
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

  companion object {
    const val NAME = "Utilkit"
  }
}
