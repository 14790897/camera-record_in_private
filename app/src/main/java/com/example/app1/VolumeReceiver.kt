package com.example.app1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.KeyEvent

class VolumeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("VolumeReceiver", "onReceive called")

        val action: String? = intent?.action
        val keyEvent = intent?.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
        val keyCode = keyEvent?.keyCode

        if (action == Intent.ACTION_MEDIA_BUTTON ) {
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
            Log.e("VolumeReceiver", "Received unexpected action or keyCode: action=$action, keyCode=$keyCode")
        }
    }
}
