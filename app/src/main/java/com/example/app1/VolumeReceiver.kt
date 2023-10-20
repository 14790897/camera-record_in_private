package com.example.app1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class VolumeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("VolumeReceiver", "onReceive called")

        val action: String? = intent?.action

        if (action == "android.media.VOLUME_CHANGED_ACTION") {
            // 检测到音量键（+）被按下
            Log.d("VolumeReceiver", "Volume UP button pressed.")
            val serviceIntent = Intent(context, CameraService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.startForegroundService(serviceIntent)
            } else {
                context?.startService(serviceIntent)
            }
        }
        else{
            Log.e("VolumeReceiver", "Received unexpected action or keyCode: action=$action")
        }
    }
}
