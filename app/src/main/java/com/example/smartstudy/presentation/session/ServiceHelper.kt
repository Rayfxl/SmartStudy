package com.example.smartstudy.presentation.session

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.example.smartstudy.MainActivity
import com.example.smartstudy.util.Constants.CLICK_REQUEST_CODE

// ServiceHelper 对象封装了与服务相关的帮助方法
object ServiceHelper {

    // 创建 PendingIntent，用于点击事件
    fun clickPendingIntent(context: Context): PendingIntent {
        // 创建一个深度链接 Intent
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            "smart_study://dashboard/session".toUri(),// 使用 URI 协议指定目标页面
            context,
            MainActivity::class.java
        )
        // 使用 TaskStackBuilder 来管理 Activity 栈，确保用户可以返回到当前的界面
        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(
                CLICK_REQUEST_CODE,
                PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
    // 启动前台服务并传递特定的操作
    fun triggerForegroundService(context: Context, action: String) {
        Intent(context, StudySessionTimerService::class.java).apply {
            this.action = action
            context.startService(this)
        }
    }
}