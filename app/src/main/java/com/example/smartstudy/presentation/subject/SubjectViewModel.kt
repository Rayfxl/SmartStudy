package com.example.smartstudy.presentation.subject

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartstudy.domain.model.Subject
import com.example.smartstudy.domain.model.Task
import com.example.smartstudy.domain.repository.SessionRepository
import com.example.smartstudy.domain.repository.SubjectRepository
import com.example.smartstudy.domain.repository.TaskRepository
import com.example.smartstudy.presentation.navArgs
import com.example.smartstudy.util.SnackbarEvent
import com.example.smartstudy.util.toHours
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// 用于处理科目相关业务逻辑的 ViewModel，管理科目的状态和操作
@HiltViewModel
class SubjectViewModel @Inject constructor(
    // 注入的依赖
    private val subjectRepository: SubjectRepository,
    private val taskRepository: TaskRepository,
    private val sessionRepository: SessionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 获取导航参数，包含科目 ID
    private val navArgs: SubjectScreenNavArgs = savedStateHandle.navArgs()

    // 定义科目的状态流，保持科目相关的数据
    private val _state = MutableStateFlow(SubjectState())
    val state = combine(
        _state,
        taskRepository.getUpcomingTasksForSubject(navArgs.subjectId),
        taskRepository.getCompletedTasksForSubject(navArgs.subjectId),
        sessionRepository.getRecentTenSessionsForSubject(navArgs.subjectId),
        sessionRepository.getTotalSessionsDurationBySubject(navArgs.subjectId)
    ) { state, upcomingTasks, completedTask, recentSessions, totalSessionsDuration ->
        state.copy(
            upcomingTasks = upcomingTasks,
            completedTasks = completedTask,
            recentSessions = recentSessions,
            studiedHours = totalSessionsDuration.toHours()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = SubjectState()
    )

    // 定义用于显示 Snackbar 消息的流
    private val _snackbarEventFlow = MutableSharedFlow<SnackbarEvent>()
    val snackbarEventFlow = _snackbarEventFlow.asSharedFlow()

    // 初始化时加载科目数据
    init {
        fetchSubject()
    }

    // 处理 UI 触发的事件
    fun onEvent(event: SubjectEvent) {
        when (event) {
            is SubjectEvent.OnSubjectCardColorChange -> {
                _state.update {
                    it.copy(subjectCardColors = event.color)
                }
            }

            is SubjectEvent.OnSubjectNameChange -> {
                _state.update {
                    it.copy(subjectName = event.name)
                }
            }

            is SubjectEvent.OnGoalStudyHoursChange -> {
                _state.update {
                    it.copy(goalStudyHours = event.hours)
                }
            }

            is SubjectEvent.OnDeleteSessionButtonClick -> {
                _state.update {
                    it.copy(session = event.session)
                }
            }
            is SubjectEvent.OnTaskIsCompleteChange -> {
                updateTask(event.task)
            }

            SubjectEvent.UpdateSubject -> updateSubject()
            SubjectEvent.DeleteSubject -> deleteSubject()
            SubjectEvent.DeleteSession -> deleteSession()

            SubjectEvent.UpdateProgress -> {
                val goalStudyHours = state.value.goalStudyHours.toFloatOrNull() ?: 1f
                _state.update {
                    it.copy(
                        progress = (state.value.studiedHours / goalStudyHours).coerceIn(0f, 1f)
                    )
                }
            }

            else -> {}
        }
    }

    // 更新科目数据
    private fun updateSubject() {
        viewModelScope.launch {
            try {
                subjectRepository.upsertSubject(
                    subject = Subject(
                        subjectId = state.value.currentSubjectId,
                        name = state.value.subjectName,
                        goalHours = state.value.goalStudyHours.toFloatOrNull() ?: 1f,
                        colors = state.value.subjectCardColors.map { it.toArgb() }
                    )
                )
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(message = "科目更新成功。")
                )
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        message = "未能更新科目。 ${e.message}",
                        SnackbarDuration.Long
                    )
                )
            }
        }
    }

    // 获取科目信息
    private fun fetchSubject() {
        viewModelScope.launch {
            subjectRepository
                .getSubjectById(navArgs.subjectId)?.let { subject ->
                    _state.update {
                        it.copy(
                            subjectName = subject.name,
                            goalStudyHours = subject.goalHours.toString(),
                            subjectCardColors = subject.colors.map { Color(it) },
                            currentSubjectId = subject.subjectId
                        )
                    }
                }
        }
    }

    // 删除科目
    private fun deleteSubject() {
        viewModelScope.launch {
            try {
                val currentSubjectId = state.value.currentSubjectId
                if (currentSubjectId != null) {
                    withContext(Dispatchers.IO) {
                        subjectRepository.deleteSubject(subjectId = currentSubjectId)
                    }
                    _snackbarEventFlow.emit(
                        SnackbarEvent.ShowSnackbar(message = "科目删除成功。")
                    )
                    _snackbarEventFlow.emit(SnackbarEvent.NavigateUp)
                } else {
                    _snackbarEventFlow.emit(
                        SnackbarEvent.ShowSnackbar(message = "没有科目可删除。")
                    )
                }
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        message = "未能删除科目。 ${e.message}",
                        duration = SnackbarDuration.Long
                    )
                )
            }
        }
    }

    // 更新任务状态（完成或未完成）
    private fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.upsertTask(
                    task = task.copy(isComplete = !task.isComplete)
                )
                if (task.isComplete) {
                    _snackbarEventFlow.emit(
                        SnackbarEvent.ShowSnackbar(message = "任务未完成。")
                    )
                } else {
                    _snackbarEventFlow.emit(
                        SnackbarEvent.ShowSnackbar(message = "任务已完成。")
                    )
                }
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        message = "未能更新任务状态。 ${e.message}",
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