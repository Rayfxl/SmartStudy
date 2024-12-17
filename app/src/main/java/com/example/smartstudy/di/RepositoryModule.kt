package com.example.smartstudy.di

import com.example.smartstudy.data.repository.SessionRepositoryImpl
import com.example.smartstudy.data.repository.SubjectRepositoryImpl
import com.example.smartstudy.data.repository.TaskRepositoryImpl
import com.example.smartstudy.domain.repository.SessionRepository
import com.example.smartstudy.domain.repository.SubjectRepository
import com.example.smartstudy.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Dagger Hilt 模块，用于提供 Repository 层的依赖注入
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // 提供 SubjectRepository 的实现
    @Singleton
    // 将接口 SubjectRepository 的实现绑定为 SubjectRepositoryImpl
    @Binds
    abstract fun bindSubjectRepository(
        impl: SubjectRepositoryImpl
    ): SubjectRepository

    @Singleton
    @Binds
    abstract fun bindTaskRepository(
        impl: TaskRepositoryImpl
    ): TaskRepository

    @Singleton
    @Binds
    abstract fun bindSessionRepository(
        impl: SessionRepositoryImpl
    ): SessionRepository
}