package com.example.smartstudy.domain.repository

import com.example.smartstudy.domain.model.Task
import kotlinx.coroutines.flow.Flow

// 定义与学习任务相关的数据操作接口
interface TaskRepository {

    suspend fun upsertTask(task: Task)

    suspend fun deleteTask(taskId: Int)

    suspend fun getTaskById(taskId: Int): Task?

    fun getUpcomingTasksForSubject(subjectId: Int): Flow<List<Task>>

    fun getCompletedTasksForSubject(subjectId: Int): Flow<List<Task>>

    fun getAllUpcomingTasks(): Flow<List<Task>>
}