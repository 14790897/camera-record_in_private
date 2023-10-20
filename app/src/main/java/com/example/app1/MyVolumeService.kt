package com.example.app1;

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.session.MediaSession
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat

class MyVolumeService : Service() {

    private lateinit var mediaSession: MediaSession

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        Log.d("MyVolumeService", "onCreate called")

        // 创建一个前台通知，以便将该服务设置为前台服务
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("volume_channel", "Volume Service", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            channel.id
        } else {
            ""
        }
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Volume Service")
            .setContentText("Listening to volume key presses...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        Log.d("MyVolumeService", "Starting foreground service")

        startForeground(1, notification)
        Log.d("MyVolumeService", "Foreground service started")

        // 创建并设置 MediaSession
        mediaSession = MediaSession(this, "MyVolumeService")
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS)
        mediaSession.setCallback(object : MediaSession.Callback() {
            override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                val keyEvent = mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                if (keyEvent?.action == KeyEvent.ACTION_DOWN) {
                    when (keyEvent.keyCode) {
                        KeyEvent.KEYCODE_VOLUME_UP -> {
                            // 处理音量增加键
                            Log.d("VolumeReceiver", "Volume button pressed.")
                            val serviceIntent = Intent(this@MyVolumeService, CameraService::class.java)
                            startService(serviceIntent)
                            Log.d("MyVolumeReceiver", "CameraService started.")
                            return true
                        }
                        KeyEvent.KEYCODE_VOLUME_DOWN -> {
                            // 处理音量减少键
                            return true
                        }
                    }
                }
                return super.onMediaButtonEvent(mediaButtonIntent)
            }
        })
        mediaSession.isActive = true
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.isActive = false
        mediaSession.release()
    }
}
