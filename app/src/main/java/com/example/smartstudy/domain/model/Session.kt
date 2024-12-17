package com.example.smartstudy.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// 这是一个 Room 数据库实体类，表示一张数据库表
@Entity
data class Session(
    val sessionSubjectId: Int,
    val relatedToSubject: String,
    val date: Long,
    val duration: Long,
    @PrimaryKey(autoGenerate = true)
    val sessionId: Int? = null
)
