package com.example.smartstudy

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import com.example.smartstudy.presentation.NavGraphs
import com.example.smartstudy.presentation.destinations.SessionScreenRouteDestination
import com.example.smartstudy.presentation.session.StudySessionTimerService
import com.example.smartstudy.presentation.theme.SmartStudyTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import dagger.hilt.android.AndroidEntryPoint

// 该注解将标记该 Activity，表示 Hilt 将为该类提供依赖注入
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 定义状态表示是否已绑定服务
    private var isBound by mutableStateOf(false)
    private lateinit var timerService: StudySessionTimerService
    // 管理与服务的连接
    private val connection = object : ServiceConnection {
        // 服务连接时触发，获取服务实例并标记已绑定
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val binder = service as StudySessionTimerService.StudySessionTimerBinder
            timerService = binder.getService()
            isBound = true
        }
        // 服务断开时触发，标记服务已断开
        override fun onServiceDisconnected(p0: ComponentName?) {
            isBound = false
        }
    }
    // 在 Activity 启动时绑定服务
    override fun onStart() {
        super.onStart()
        Intent(this, StudySessionTimerService::class.java).also { intent ->
            // 启动服务并绑定，Context.BIND_AUTO_CREATE 表示服务会在绑定时自动创建
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }
    // Activity 创建时调用，设置 Compose 布局内容
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (isBound) {
                SmartStudyTheme {
                    // 使用 DestinationsNavHost 设置导航图和依赖
                    DestinationsNavHost(
                        navGraph = NavGraphs.root,
                        dependenciesContainerBuilder = {
                            dependency(SessionScreenRouteDestination) { timerService }
                        }
                    )
                }
            }
        }
        requestPermission()
    }
    // 请求权限
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
    }
    // 在 Activity 停止时，解绑服务
    override fun onStop() {
        super.onStop()
        unbindService(connection)
        isBound = false
    }
}

