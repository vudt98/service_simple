package com.example.myapplication

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.TrafficStats
import android.os.Binder
import android.os.IBinder
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.myapplication.utils.Speed
import kotlinx.coroutines.*


class ServiceTest : Service() {

    private val myService = MyService()

    private var notificationManager: NotificationManager? = null

    private var notification: NotificationCompat.Builder? = null
    private var job: Job? = null

    private var contentView: RemoteViews? = null

    private var mSpeed: Speed? = null

    private var mIconCanvas: Canvas? = null

    private var mLastRxBytes: Long = 0
    private var mLastTxBytes: Long = 0
    private var mLastTime: Long = 0

    private lateinit var mIconBitmap: Bitmap
    private lateinit var mIconSpeedPaint: Paint
    private lateinit var mIconUnitPaint: Paint

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onBind(p0: Intent?): IBinder? {
        return myService
    }

    inner class MyService : Binder() {
        fun getService() = this@ServiceTest
    }

    override fun onCreate() {
        super.onCreate()
        mLastRxBytes = TrafficStats.getTotalRxBytes()
        mLastTxBytes = TrafficStats.getTotalTxBytes()
        mLastTime = System.currentTimeMillis()
        mSpeed = Speed(this)
        mIconBitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
        mIconCanvas = Canvas(mIconBitmap)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "stop") {
            stopForeground(true)
            stopSelf()
        } else {
            Toast.makeText(this, "Service Start", Toast.LENGTH_SHORT).show()
            getNotification(this)
        }
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

    @SuppressLint("UnspecifiedImmutableFlag", "RemoteViewLayout")
    private fun getNotification(context: Context) {
        createChannel(context)

        setupIcon()

        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifyIntent = Intent(context, MainActivity::class.java)

        val title = "Sample Notification"
        var message = "0"

        job = coroutineScope.launch {
            repeat(Int.MAX_VALUE) {
                if (it == -1) {
                    job?.cancel()
                }
                ensureActive()
                delay(1000)
                getData()
                val data = mSpeed?.getHumanSpeed("indicatorSpeedToShow")
                message = (message.toInt().plus(1)).toString()
                updateNotice(message, data!!)
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

        contentView = RemoteViews(packageName, R.layout.layout_notice)
        contentView?.setTextViewText(R.id.count, message)

        notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setCustomContentView(contentView)
            .setSmallIcon(getIndicatorIcon("", "")!!)
            .setAutoCancel(true)
            .setContent(contentView)

        startForeground(NOTICE_ID, notification?.build())
    }

    private fun updateNotice(message: String, data: Speed.HumanSpeed) {
        notification?.setSmallIcon(
            getIndicatorIcon(data.speedValue.toString(), data.speedUnit.toString())!!
        )
        contentView?.setTextViewText(R.id.downSp, "${data.speedValue} ${data.speedUnit}")
        contentView?.setTextViewText(R.id.upSp, "${data.speedUnit}")
        contentView?.setTextViewText(R.id.count, message)
        notificationManager?.notify(NOTICE_ID, notification?.build())
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }

    private fun getData() {
        val currentRxBytes = TrafficStats.getTotalRxBytes()
        val currentTxBytes = TrafficStats.getTotalTxBytes()
        val usedRxBytes = currentRxBytes - mLastRxBytes
        val usedTxBytes = currentTxBytes - mLastTxBytes
        val currentTime = System.currentTimeMillis()
        val usedTime = currentTime - mLastTime
        mLastRxBytes = currentRxBytes
        mLastTxBytes = currentTxBytes
        mLastTime = currentTime
        mSpeed!!.calcSpeed(usedTime, usedRxBytes, usedTxBytes)
    }

    private fun getIndicatorIcon(speedValue: String, speedUnit: String): IconCompat? {
        mIconCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        mIconCanvas?.drawText(speedValue, 48F, 52F, mIconSpeedPaint)
        mIconCanvas?.drawText(speedUnit, 48F, 95F, mIconUnitPaint)
        return IconCompat.createWithBitmap(mIconBitmap)
    }

    private fun setupIcon() {
        mIconSpeedPaint = Paint()
        mIconSpeedPaint.color = Color.WHITE
        mIconSpeedPaint.isAntiAlias = true
        mIconSpeedPaint.textSize = 65F
        mIconSpeedPaint.textAlign = Paint.Align.CENTER
        mIconSpeedPaint.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)

        mIconUnitPaint = Paint()
        mIconUnitPaint.color = Color.WHITE
        mIconUnitPaint.isAntiAlias = true
        mIconUnitPaint.textSize = 40F
        mIconUnitPaint.textAlign = Paint.Align.CENTER
        mIconUnitPaint.typeface = Typeface.DEFAULT_BOLD
    }
}