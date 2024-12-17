package com.example.smartstudy.presentation.session

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.example.smartstudy.util.Constants.ACTION_SERVICE_CANCEL
import com.example.smartstudy.util.Constants.ACTION_SERVICE_START
import com.example.smartstudy.util.Constants.ACTION_SERVICE_STOP
import com.example.smartstudy.util.Constants.NOTIFICATION_CHANNEL_ID
import com.example.smartstudy.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.smartstudy.util.Constants.NOTIFICATION_ID
import com.example.smartstudy.util.pad
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
// StudySessionTimerService 服务类，用于管理学习计时器，通知和计时操作。
class StudySessionTimerService : Service() {

    // 注入的 NotificationManager，用于管理通知
    @Inject
    lateinit var notificationManager: NotificationManager

    // 注入的 NotificationCompat.Builder，用于构建通知
    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    // 服务的 Binder，用于绑定服务并提供对服务实例的访问
    private val binder = StudySessionTimerBinder()

    // Timer 用于定期更新计时器
    private lateinit var timer: Timer
    // 用于存储计时器的总时长
    var duration: Duration = Duration.ZERO
        private set
    // 用于表示计时器的各个时间单位（小时、分钟、秒）
    var seconds = mutableStateOf("00")
        private set
    var minutes = mutableStateOf("00")
        private set
    var hours = mutableStateOf("00")
        private set
    // 当前计时器状态（IDLE: 空闲, STARTED: 启动, STOPPED: 停止）
    var currentTimerState = mutableStateOf(TimerState.IDLE)
        private set
    // 学科 ID，用于关联特定的学习会话
    var subjectId = mutableStateOf<Int?>(null)

    // 当服务被绑定时，返回绑定器
    override fun onBind(p0: Intent?) = binder

    // 当服务启动时，根据传入的 action 执行相应操作
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action.let {
            when (it) {
                // 开始计时服务
                ACTION_SERVICE_START -> {
                    // 启动前台服务
                    startForegroundService()
                    // 启动计时器并更新通知
                    startTimer { hours, minutes, seconds ->
                        updateNotification(hours, minutes, seconds)
                    }
                }
                // 停止计时
                ACTION_SERVICE_STOP -> {
                    stopTimer()
                }
                // 取消计时并停止服务
                ACTION_SERVICE_CANCEL -> {
                    stopTimer()
                    cancelTimer()
                    stopForegroundService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    // 启动前台服务，显示通知
    private fun startForegroundService() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }
    // 停止前台服务并取消通知
    private fun stopForegroundService() {
        notificationManager.cancel(NOTIFICATION_ID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        stopSelf()
    }
    // 创建通知通道
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 更新通知中的时间显示
    private fun updateNotification(hours: String, minutes: String, seconds: String) {
        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder
                .setContentText("$hours:$minutes:$seconds")
                .build()
        )
    }

    // 启动计时器，并在每次定时触发时更新时间
    private fun startTimer(
        onTick: (h: String, m: String, s: String) -> Unit
    ) {
        currentTimerState.value = TimerState.STARTED
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            duration = duration.plus(1.seconds)
            updateTimeUnits()
            onTick(hours.value, minutes.value, seconds.value)
        }
    }

    // 停止计时器
    private fun stopTimer() {
        if (this::timer.isInitialized) {
            timer.cancel()
        }
        currentTimerState.value = TimerState.STOPPED
    }

    // 取消计时器并重置时间
    private fun cancelTimer() {
        duration = Duration.ZERO
        updateTimeUnits()
        currentTimerState.value = TimerState.IDLE
    }

    // 更新时、分、秒的显示值
    private fun updateTimeUnits() {
        duration.toComponents { hours, minutes, seconds, _ ->
            this@StudySessionTimerService.hours.value = hours.toInt().pad()
            this@StudySessionTimerService.minutes.value = minutes.pad()
            this@StudySessionTimerService.seconds.value = seconds.pad()
        }
    }

    // 绑定器，用于服务与组件之间的交互
    inner class StudySessionTimerBinder : Binder() {
        fun getService(): StudySessionTimerService = this@StudySessionTimerService
    }
}

// 计时器状态枚举，表示计时器的不同状态
enum class TimerState {
    IDLE,
    STARTED,
    STOPPED
}