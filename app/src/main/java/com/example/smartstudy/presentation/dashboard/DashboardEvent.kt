package com.example.smartstudy.presentation.dashboard

import androidx.compose.ui.graphics.Color
import com.example.smartstudy.domain.model.Session
import com.example.smartstudy.domain.model.Task

// DashboardEvent 是所有与 Dashboard 页面交互相关的事件的密封类。
sealed class DashboardEvent {
    data object SaveSubject : DashboardEvent()
    data object DeleteSession : DashboardEvent()
    data class OnDeleteSessionButtonClick(val session: Session): DashboardEvent()
    data class OnTaskIsCompleteChange(val task: Task): DashboardEvent()
    data class OnSubjectCardColorChange(val colors: List<Color>): DashboardEvent()
    data class OnSubjectNameChange(val name: String): DashboardEvent()
    data class OnGoalStudyHoursChange(val hours: String): DashboardEvent()
}