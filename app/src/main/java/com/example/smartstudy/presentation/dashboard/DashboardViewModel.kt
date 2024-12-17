package com.example.smartstudy.presentation.dashboard

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartstudy.domain.model.Session
import com.example.smartstudy.domain.model.Subject
import com.example.smartstudy.domain.model.Task
import com.example.smartstudy.domain.repository.SessionRepository
import com.example.smartstudy.domain.repository.SubjectRepository
import com.example.smartstudy.domain.repository.TaskRepository
import com.example.smartstudy.util.SnackbarEvent
import com.example.smartstudy.util.toHours
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 使用 Hilt 注解进行依赖注入
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val sessionRepository: SessionRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    // 用于存储 Dashboard 页面状态的 MutableStateFlow
    private val _state = MutableStateFlow(DashboardState())
    val state = combine(
        _state,
        subjectRepository.getTotalSubjectCount(),
        subjectRepository.getTotalGoalHours(),
        subjectRepository.getAllSubjects(),
        sessionRepository.getTotalSessionsDuration()
    ) { state, subjectCount, goalHours, subjects, totalSessionDuration ->
        state.copy(
            totalSubjectCount = subjectCount,
            totalGoalStudyHours = goalHours,
            subjects = subjects,
            totalStudiedHours = totalSessionDuration.toHours()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = DashboardState()
    )

    // 获取所有待办任务
    val tasks: StateFlow<List<Task>> = taskRepository.getAllUpcomingTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = emptyList()
        )

    // 获取最近的 5 条学习记录
    val recentSessions: StateFlow<List<Session>> = sessionRepository.getRecentFiveSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = emptyList()
        )

    // 用于显示 SnackBar 消息的 SharedFlow
    private val _snackbarEventFlow = MutableSharedFlow<SnackbarEvent>()
    val snackbarEventFlow = _snackbarEventFlow.asSharedFlow()

    // 处理页面事件的方法
    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.OnSubjectNameChange -> {
                _state.update {
                    it.copy(subjectName = event.name)
                }
            }

            is DashboardEvent.OnGoalStudyHoursChange -> {
                _state.update {
                    it.copy(goalStudyHours = event.hours)
                }
            }

            is DashboardEvent.OnSubjectCardColorChange -> {
                _state.update {
                    it.copy(subjectCardColors = event.colors)
                }
            }

            is DashboardEvent.OnDeleteSessionButtonClick -> {
                _state.update {
                    it.copy(session = event.session)
                }
            }

            DashboardEvent.SaveSubject -> saveSubject()
            DashboardEvent.DeleteSession -> deleteSession()
            is DashboardEvent.OnTaskIsCompleteChange -> {
                updateTask(event.task)
            }
            else -> {}
        }
    }

    // 更新任务的完成状态
    private fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.upsertTask(
                    task = task.copy(isComplete = !task.isComplete)
                )
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(message = "任务已完成。")
                )
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        "未能更新任务状态。 ${e.message}",
                        SnackbarDuration.Long
                    )
                )
            }
        }
    }

    // 保存新的科目信息
    private fun saveSubject() {
        viewModelScope.launch {
            try {
                subjectRepository.upsertSubject(
                    subject = Subject(
                        name = state.value.subjectName,
                        goalHours = state.value.goalStudyHours.toFloatOrNull() ?: 1f,
                        colors = state.value.subjectCardColors.map { it.toArgb() }
                    )
                )
                _state.update {
                    it.copy(
                        subjectName = "",
                        goalStudyHours = "",
                        subjectCardColors = Subject.subjectCardColors.random()
                    )
                }
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(message = "科目保存成功。")
                )
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        message = "未能保存科目。 ${e.message}",
                        SnackbarDuration.Long
                    )
                )
            }
        }
    }

    // 删除学习记录
    private fun deleteSession() {
        viewModelScope.launch {
            try {
                state.value.session?.let {
                    sessionRepository.deleteSession(it)
                    _snackbarEventFlow.emit(
                        SnackbarEvent.ShowSnackbar(message = "记录删除成功。")
                    )
                }
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        message = "未能删除记录。 ${e.message}",
                        duration = SnackbarDuration.Long
                    )
                )
            }
        }
    }

}