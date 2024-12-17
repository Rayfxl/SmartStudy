package com.example.smartstudy.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// 定义任务数据模型
@Entity
data class Task(
    val title: String,
    val description: String,
    val dueDate: Long,
    val priority: Int,
    val relatedToSubject: String,
    val isComplete: Boolean,
    val taskSubjectId: Int,
    @PrimaryKey(autoGenerate = true)
    val taskId: Int? = null
)
