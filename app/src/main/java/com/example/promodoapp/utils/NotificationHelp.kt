package com.example.promodoapp.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.promodoapp.R

object NotificationHelper {
    private const val CHANNEL_ID = "pomodoro_channel"
    private const val NOTIFICATION_ID = 1

    // Tạo kênh thông báo (cho Android 8.0 trở lên)
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pomodoro Notifications"
            val descriptionText = "Notifications for Pomodoro timer events"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setSound(null, null)
                enableVibration(false)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.deleteNotificationChannel(CHANNEL_ID)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Hiển thị thông báo đẩy và phát âm thanh
    fun showNotification(context: Context, title: String, message: String) {
        // Kiểm tra quyền POST_NOTIFICATIONS trên Android 13 trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                Log.w("NotificationHelper", "Cannot show notification: POST_NOTIFICATIONS permission not granted")
                // Vẫn phát âm thanh dù không có quyền thông báo
                playSound(context)
                return
            }
        }

        // Tạo kênh thông báo
        createNotificationChannel(context)

        // Xây dựng thông báo
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(null)
            .setDefaults(0) // Tắt tất cả các mặc định (âm thanh, rung, đèn)
            .setCategory(NotificationCompat.CATEGORY_ALARM) // Tăng khả năng hiển thị heads-up

        // Hiển thị thông báo
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        } catch (e: SecurityException) {
            Log.e("NotificationHelper", "SecurityException: ${e.message}")
            // Vẫn phát âm thanh dù không thể hiển thị thông báo
            playSound(context)
        }

        // Phát âm thanh
        playSound(context)
    }

    // Phát âm thanh thông báo
    private fun playSound(context: Context) {
        val soundPool: SoundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build()
        } else {
            SoundPool(1, android.media.AudioManager.STREAM_NOTIFICATION, 0)
        }

        val soundId = soundPool.load(context, R.raw.notify_end, 1)
        soundPool.setOnLoadCompleteListener { _, _, _ ->
            soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
        }
    }
}