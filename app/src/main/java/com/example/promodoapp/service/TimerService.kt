package com.example.promodoapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.promodoapp.MainActivity
import com.example.promodoapp.R
import com.example.promodoapp.model.Session
import com.example.promodoapp.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimerService : Service() {
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var timerJob: Job? = null
    private var currentTime = 0
    private var workTime = 25
    private var breakTime = 5
    private var isWorkPhase = true
    private var timerState = TimerState.Idle
    private var currentSessionStartTime: Date? = null
    private var mode = "pomodoro"

    companion object {
        const val CHANNEL_ID = "TimerServiceChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val EXTRA_WORK_TIME = "EXTRA_WORK_TIME"
        const val EXTRA_BREAK_TIME = "EXTRA_BREAK_TIME"
        const val EXTRA_CURRENT_TIME = "EXTRA_CURRENT_TIME"
        const val EXTRA_IS_WORK_PHASE = "EXTRA_IS_WORK_PHASE"
        const val EXTRA_MODE = "EXTRA_MODE"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        when (intent.action) {
            ACTION_START -> {
                workTime = intent.getIntExtra(EXTRA_WORK_TIME, 25)
                breakTime = intent.getIntExtra(EXTRA_BREAK_TIME, 5)
                currentTime = intent.getIntExtra(EXTRA_CURRENT_TIME, workTime * 60)
                isWorkPhase = intent.getBooleanExtra(EXTRA_IS_WORK_PHASE, true)
                mode = intent.getStringExtra(EXTRA_MODE) ?: "pomodoro"
                try {
                    startForeground(NOTIFICATION_ID, createNotification())
                    startTimer()
                    Log.d("TimerService", "Service started with time: $currentTime seconds")
                } catch (e: Exception) {
                    Log.e("TimerService", "Failed to start foreground service: ${e.message}")
                    stopSelf()
                }
            }
            ACTION_PAUSE -> {
                pauseTimer()
                updateNotification()
                Log.d("TimerService", "Service paused")
            }
            ACTION_STOP -> {
                stopTimer()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                Log.d("TimerService", "Service stopped")
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Timer Service",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(this, TimerService::class.java).apply { action = ACTION_PAUSE }
        val pausePendingIntent = PendingIntent.getService(
            this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, TimerService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val iconRes = try {
            R.drawable.ic_timer
        } catch (e: Resources.NotFoundException) {
            Log.e("TimerService", "Icon resource not found: ${e.message}")
            R.drawable.ic_launcher_foreground
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (isWorkPhase) "Hết thời gian tập trung nghỉ ngơi thôi" else "Hết thời gian nghỉ ngơi quay lại tập trung nào")
            .setContentText("${currentTime / 60}:${String.format("%02d", currentTime % 60)}")
            .setSmallIcon(iconRes)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_pause, "Pause", pausePendingIntent)
            .addAction(R.drawable.ic_close, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification() {
        try {
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(if (isWorkPhase) "Thời Gian Tập Trung Còn Lại " else "Thời Gian Nghỉ Ngơi Còn Lại ")
                .setContentText("${currentTime / 60}:${String.format("%02d", currentTime % 60)}")
                .setSmallIcon(R.drawable.ic_timer)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .build()
            val manager = getSystemService(NotificationManager::class.java)
            manager.notify(NOTIFICATION_ID, notification)
            Log.d("TimerService", "Notification updated: ${currentTime / 60}:${String.format("%02d", currentTime % 60)}")
        } catch (e: Exception) {
            Log.e("TimerService", "Failed to update notification: ${e.message}")
        }
    }

    private fun startTimer() {
        if (timerState != TimerState.Running) {
            timerState = TimerState.Running
            currentSessionStartTime = Date()
        }
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (currentTime > 0 && timerState == TimerState.Running) {
                delay(1000)
                currentTime--
                updateNotification()
                Log.d("TimerService", "Timer running: $currentTime seconds remaining")
                if (currentTime == 0) {
                    switchPhase()
                }
            }
        }
    }

    private fun pauseTimer() {
        if (timerState == TimerState.Running) {
            timerState = TimerState.Paused
            timerJob?.cancel()
            updateNotification()
        }
    }

    private fun stopTimer() {
        saveSession(completed = false)
        timerJob?.cancel()
        timerState = TimerState.Idle
        isWorkPhase = true
        currentTime = workTime * 60
        currentSessionStartTime = null
    }

    private fun switchPhase() {
        if (isWorkPhase) {
            saveSession(completed = true)
        }
        isWorkPhase = !isWorkPhase
        currentTime = if (isWorkPhase) workTime * 60 else breakTime * 60
        currentSessionStartTime = Date()
        timerState = TimerState.Paused
        updateNotification()
    }

    private fun saveSession(completed: Boolean) {
        val currentUser = auth.currentUser ?: return
        if (currentSessionStartTime == null) return

        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val date = dateFormatter.format(currentSessionStartTime)
        val session = Session(
            userId = currentUser.uid,
            type = mode,
            duration = if (isWorkPhase) workTime else breakTime,
            completed = completed,
            date = date,
            clientStartTime = currentSessionStartTime?.time, // Sử dụng clientStartTime thay vì startTime
            startTime = null // Đặt startTime thành null vì không còn sử dụng
        )

        serviceScope.launch {
            try {
                userRepository.saveSession(session, "sessions_new")
                Log.d("TimerService", "Saved session: ${session.type}, clientStartTime: ${session.clientStartTime}")
            } catch (e: Exception) {
                Log.e("TimerService", "Failed to save session: ${e.message}")
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d("TimerService", "Service destroyed")
    }
}

enum class TimerState {
    Idle, Running, Paused
}