package com.example.myapplication

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import kotlinx.coroutines.*


class ServiceTest : Service() {

    private val myService = MyService()

    private var notificationManager: NotificationManager? = null

    private var notification: Notification.Builder? = null
    private var job: Job? = null

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onBind(p0: Intent?): IBinder? {
        return myService
    }

    inner class MyService : Binder() {
        fun getService() = this@ServiceTest
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "Service Start", Toast.LENGTH_SHORT).show()
        getNotification(this)
        return START_STICKY
    }

    companion object {
        const val CHANNEL_ID = "com.example.myapplication.CHANNEL_ID"
        const val CHANNEL_NAME = "Sample Notification"
        const val NOTICE_ID = 100
    }

    private fun createChannel(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val importance = NotificationManager.IMPORTANCE_LOW
        val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
        notificationChannel.apply {
            enableVibration(false)
            setShowBadge(true)
            enableLights(true)
            lightColor = Color.parseColor("#e8334a")
            description = "notification channel description"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(this)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getNotification(context: Context) {
        createChannel(context)

        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifyIntent = Intent(context, MainActivity::class.java)

        val title = "Sample Notification"
        var message = "0"

        job = coroutineScope.launch {
            repeat(Int.MAX_VALUE) {
                if (it == 20) {
                    job?.cancel()
                }
                ensureActive()
                delay(1000)
                message = (message.toInt().plus(1)).toString()
                updateNotice(message)
            }
        }

        notifyIntent.apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("notification", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent =
            PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val deleteIntent = Intent(this, ServiceTest::class.java)
//        val deletePendingIntent = PendingIntent.getService(
//            this,
//            0,
//            deleteIntent,
//            PendingIntent.FLAG_CANCEL_CURRENT
//        )

        notification = Notification.Builder(context, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentText(message)
//            .setDeleteIntent(deletePendingIntent)

        startForeground(NOTICE_ID, notification?.build())
    }

    private fun updateNotice(message: String) {
        notification?.setContentText(message)
        notificationManager?.notify(NOTICE_ID, notification?.build())
    }
}