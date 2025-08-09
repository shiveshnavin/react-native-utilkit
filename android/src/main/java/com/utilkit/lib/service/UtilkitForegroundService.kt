package com.utilkit.lib.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelProvider
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.ReactActivity
import com.utilkit.R
import com.utilkit.UtilkitModule.Companion.gson
import com.utilkit.lib.events.Channels
import com.utilkit.lib.events.EventBus
import com.utilkit.lib.service.transfers.Reader

data class HeadlessTaskParams(
  val name: String,
  val params: String
)

class UtilkitForegroundService : LifecycleService() {

  companion object {
    const val CHANNEL_ID = "Uploads and Downloads"
    private const val NOTIFICATION_ID = 1
  }

  private lateinit var eventBus: EventBus

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
    eventBus = ViewModelProvider(
      EventBus.mViewModelStore, ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    )[EventBus::class.java]

    eventBus.reactToNativeBus.observe(this) { event ->
      Log.d("Utilkit", "reactToNativeBus got event $event")
      val payload = event.payload

      if (event.channel == Channels.HeadlessTask) {

      } else if (event.channel == Channels.StopService) {
        stopSelf()
      }
    }
  }


  private fun getAppName(): String {
    return applicationContext.applicationInfo.loadLabel(packageManager).toString()
  }


  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    super.onStartCommand(intent, flags, startId)
    val title = intent?.getStringExtra("title") ?: "Foreground Service"
    // Use FLAG_IMMUTABLE for PendingIntent
    val notificationIntent = Intent(this, ReactActivity::class.java)
    val pendingIntent: PendingIntent = PendingIntent.getActivity(
      this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
    )

    val notification: Notification =
      NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("${getAppName()} Service")
        .setContentText("Running in background")
        .setSmallIcon(R.drawable.rocket).setContentIntent(pendingIntent)
        .setOngoing(true).build()

    startForeground(NOTIFICATION_ID, notification)

    return START_NOT_STICKY
  }

  private fun updateNotification(msg: String) {
    val notificationManager = getSystemService(NotificationManager::class.java)

    val notification: Notification =
      NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("${getAppName()} Service")
        .setContentText(msg).setSound(null)
        .setSmallIcon(R.drawable.rocket)
        .setOngoing(true)
        .build()

    notificationManager.notify(NOTIFICATION_ID, notification)
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val serviceChannel = NotificationChannel(
        CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT
      )
      serviceChannel.setSound(null, null)
      val manager: NotificationManager = getSystemService(NotificationManager::class.java)
      manager.createNotificationChannel(serviceChannel)
    }
  }

}
