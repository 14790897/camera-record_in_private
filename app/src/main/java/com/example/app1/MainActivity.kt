package com.example.app1

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.app1.ui.theme.App1Theme

class MainActivity : ComponentActivity() {

    private val REQUESTCODE = 1
    //动态注册，取消注册
    private var volumeReceiver: VolumeReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置MainActivity用于显示的布局
        setContentView(R.layout.activity_main)

        // 启动 MyVolumeService 来监听音量键
//        val serviceIntent = Intent(this, MyVolumeService::class.java)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(serviceIntent)
//        } else {
//            startService(serviceIntent)
//        }


        // 检查摄像头和存储权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // 请求权限
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUESTCODE)
        }

        try {
            volumeReceiver = VolumeReceiver()
            val filter = IntentFilter()
            filter.addAction("android.intent.action.VOLUME_CHANGED")
            val receiver = registerReceiver(volumeReceiver, filter)
            if (receiver != null) {
                Toast.makeText(this, "VolumeReceiver 注册成功", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "VolumeReceiver 注册失败", Toast.LENGTH_SHORT).show()
            Log.e("MainActivity", "VolumeReceiver 注册失败: ${e.message}")
        }



        setContent {
            App1Theme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Greeting("liuweiqing")

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = {
                            // 启动 CameraService 来拍照
                            val serviceIntent = Intent(this@MainActivity, CameraService::class.java)
                            startService(serviceIntent)
                        }) {
                            Text("Take Photo")
                        }
                    }
                }
            }
        }
    }


    // 处理权限请求结果
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUESTCODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // 权限被用户授予
                // 在这里执行你的功能代码
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show()
            } else {
                // 权限被用户拒绝
                // 在这里解释为什么需要这些权限，并引导用户去设置中开启权限
                AlertDialog.Builder(this)
                    .setTitle("权限被拒绝")
                    .setMessage("这些权限是应用运行所必需的。请在设置中开启这些权限。")
                    .setPositiveButton("确定") { _, _ ->
                        // 引导用户去设置中开启权限
                    }
                    .setNegativeButton("取消") { _, _ ->
                        // 用户拒绝去设置中开启权限
                    }
                    .show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (volumeReceiver != null) {
            unregisterReceiver(volumeReceiver)
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    Log.d("MainActivity", "Volume UP pressed")
                    val serviceIntent = Intent(this@MainActivity, CameraService::class.java)
                    startService(serviceIntent)
                    return true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    Log.d("MainActivity", "Volume DOWN pressed")
                    val serviceIntent = Intent(this@MainActivity, CameraService::class.java)
                    startService(serviceIntent)
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    App1Theme {
        Greeting("Android")
    }
}
