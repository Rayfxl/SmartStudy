package com.example.smartstudy.di

import android.app.Application
import androidx.room.Room
import com.example.smartstudy.data.local.AppDatabase
import com.example.smartstudy.data.local.SessionDao
import com.example.smartstudy.data.local.SubjectDao
import com.example.smartstudy.data.local.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// 使用 Dagger Hilt 进行依赖注入，提供数据库相关的依赖项
@Module
// 说明该模块在应用程序级别的生命周期中存在
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // 提供数据库实例的函数
    @Provides
    // 确保这个数据库实例是单例的
    @Singleton
    fun provideDatabase(
        application: Application
    ): AppDatabase {
        // 使用 Room 创建数据库实例，并指定数据库名称为 "smartstudy.db"
        return Room
            .databaseBuilder(
                application,
                AppDatabase::class.java,
                "smartstudy.db"
            )
            .build()
    }
    // 提供 SubjectDao 实例的函数
    @Provides
    @Singleton
    fun provideSubjectDao(database: AppDatabase): SubjectDao {
        return database.subjectDao()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideSessionDao(database: AppDatabase): SessionDao {
        return database.sessionDao()
    }
}