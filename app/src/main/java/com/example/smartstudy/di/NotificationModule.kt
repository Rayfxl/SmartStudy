package com.example.smartstudy.di

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.smartstudy.R
import com.example.smartstudy.presentation.session.ServiceHelper
import com.example.smartstudy.util.Constants.NOTIFICATION_CHANNEL_ID
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

// Dagger Hilt 模块，用于提供与通知相关的依赖项
@Module
@InstallIn(ServiceComponent::class)
object NotificationModule {

    // 提供 NotificationCompat.Builder 实例，用于构建通知
    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat
            .Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("学习ing")
            .setContentText("00:00:00")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(ServiceHelper.clickPendingIntent(context))
    }

    // 提供 NotificationManager 实例，用于管理通知
    @ServiceScoped
    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}