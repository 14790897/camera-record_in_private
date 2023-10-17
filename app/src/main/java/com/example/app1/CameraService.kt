package com.example.app1

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class CameraService : Service() {//我们这边继承了service不是activity

    private lateinit var cameraManager: CameraManager
    private var cameraDevice: CameraDevice? = null
    private var imageReader: ImageReader? = null  // Declare imageReader

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "cameraServiceChannel",
                "Camera Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
//        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("CameraService", "onStartCommand triggered.")

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, "cameraServiceChannel")
            .setContentTitle("Camera Service")
            .setContentText("Service is running...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        // 检查相机和存储权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d("CameraService", "Camera permission granted.")
            val imageDimension = Size(640, 480)
            imageReader = ImageReader.newInstance(
                imageDimension.width,
                imageDimension.height,
                ImageFormat.JPEG,
                5
            )
            val readerSurface = imageReader!!.surface

            imageReader!!.setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                // 在这里处理和保存图像
                if (image != null) {
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    saveImageToExternalStorage(bytes)
                    image.close()
                } else {
                    Log.e("CameraService", "Image is null.")
                }

            }, null)

            cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.find { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val capabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                capabilities?.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE) == true
            }


            cameraManager.openCamera(cameraId.toString(), object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    Log.d("CameraService", "Camera opened.")

                    cameraDevice = camera
                    takePicture(readerSurface)
                    Log.d("CameraService", "Image captured successfully.")
                }

                override fun onDisconnected(camera: CameraDevice) {
                    Log.d("CameraService", "Camera disconnected.")

                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e("CameraService", "Camera error: $error")

                    camera.close()
                    stopSelf()

                }
            }, null)
        } else {
            // 如果没有权限，您可以选择停止服务或进行其他操作
            Log.e("CameraService", "Camera permission not granted. Stopping service.")

            stopSelf()
        }
        return START_STICKY
    }

    private fun takePicture(readerSurface: Surface) {
        Log.d("CameraService", "takePicture triggered.")

        val captureRequestBuilder =
            cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequestBuilder?.addTarget(readerSurface)

        val outputConfigurations = listOf(OutputConfiguration(readerSurface))
        val sessionConfig = SessionConfiguration(SessionConfiguration.SESSION_REGULAR,
            outputConfigurations,
            ContextCompat.getMainExecutor(this),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        session.capture(
                            captureRequestBuilder?.build()!!,
                            object : CameraCaptureSession.CaptureCallback() {
                                override fun onCaptureCompleted(
                                    session: CameraCaptureSession,
                                    request: CaptureRequest,
                                    result: TotalCaptureResult
                                ) {
                                    // 图像捕获完成
                                    Log.d("CameraService", "Image capture completed.")
                                }
                            },
                            null
                        )
                    } catch (e: CameraAccessException) {
                        // Handle error
                        Log.e("takePicture in CameraService", "Camera access error: ", e)
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    // Handle failure
                }
            })

        cameraDevice?.createCaptureSession(sessionConfig)
    }

    private fun saveImageToExternalStorage(imageBytes: ByteArray) {
        val folder = File(Environment.getExternalStorageDirectory().toString() + "/MyFolder_app1")
        if (!folder.exists()) {
            val success = folder.mkdirs()
            if (!success) {
                Log.e("CameraService", "Failed to create directory.")
                return
            }
            else{
                Log.d("CameraService", "Directory created.")
            }
        }

        val file = File(folder, "MyImage.jpg")

        try {
            val fos = FileOutputStream(file)
            fos.write(imageBytes)
            fos.close()

            // 刷新媒体库
            MediaScannerConnection.scanFile(this, arrayOf(file.path), null) { _, uri ->
                // 图片已保存并且媒体库已更新
                Log.d("CameraService", "Image saved: $uri")
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraDevice?.close()
    }
}
