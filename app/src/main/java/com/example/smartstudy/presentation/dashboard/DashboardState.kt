package com.example.smartstudy.presentation.dashboard

import androidx.compose.ui.graphics.Color
import com.example.smartstudy.domain.model.Session
import com.example.smartstudy.domain.model.Subject

// DashboardState 用于表示 Dashboard 页面上的状态，存储了与 Dashboard 相关的所有数据。
// 包括科目的统计信息、学习时长、目标时长等，以及当前正在操作的学习记录。
data class DashboardState(
    val totalSubjectCount: Int = 0,
    val totalStudiedHours: Float = 0f,
    val totalGoalStudyHours: Float = 0f,
    val subjects: List<Subject> = emptyList(),
    val subjectName: String = "",
    val goalStudyHours: String = "",
    val subjectCardColors: List<Color> = Subject.subjectCardColors.random(),
    val session: Session? = null
)
