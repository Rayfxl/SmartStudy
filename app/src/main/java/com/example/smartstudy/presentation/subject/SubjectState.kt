package com.example.smartstudy.presentation.subject

import androidx.compose.ui.graphics.Color
import com.example.smartstudy.domain.model.Session
import com.example.smartstudy.domain.model.Subject
import com.example.smartstudy.domain.model.Task

// 数据类 SubjectState，用于表示科目的各种状态
data class SubjectState(
    val currentSubjectId: Int? = null,
    val subjectName: String = "",
    val goalStudyHours: String = "",
    val subjectCardColors: List<Color> = Subject.subjectCardColors.random(),
    val studiedHours: Float = 0f,
    val progress: Float = 0f,
    val recentSessions: List<Session> = emptyList(),
    val upcomingTasks: List<Task> = emptyList(),
    val completedTasks: List<Task> = emptyList(),
    val session: Session? = null
)