package com.example.smartstudy.presentation.task

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartstudy.domain.model.Task
import com.example.smartstudy.domain.repository.SubjectRepository
import com.example.smartstudy.domain.repository.TaskRepository
import com.example.smartstudy.presentation.navArgs
import com.example.smartstudy.util.Priority
import com.example.smartstudy.util.SnackbarEvent
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
import java.time.Instant
import javax.inject.Inject

// Hilt 注入 ViewModel，自动提供依赖
@HiltViewModel
class TaskViewModel @Inject constructor(
    // 任务数据源
    private val taskRepository: TaskRepository,
    // 学科数据源
    private val subjectRepository: SubjectRepository,
    // 用于处理保存的状态
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 获取导航参数
    private val navArgs: TaskScreenNavArgs = savedStateHandle.navArgs()

    // 定义状态流，用于更新和显示任务相关状态
    private val _state = MutableStateFlow(TaskState())
    val state = combine(
        _state,
        subjectRepository.getAllSubjects()
    ) { state, subjects ->
        state.copy(subjects = subjects)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = TaskState()
    )

    // 用于处理 Snackbar 事件的流
    private val _snackbarEventFlow = MutableSharedFlow<SnackbarEvent>()
    val snackbarEventFlow = _snackbarEventFlow.asSharedFlow()

    // 初始化时获取任务和学科数据
    init {
        fetchTask()
        fetchSubject()
    }

    // 处理 UI 事件的方法
    fun onEvent(event: TaskEvent) {
        when (event) {
            is TaskEvent.OnTitleChange -> {
                _state.update {
                    it.copy(title = event.title)
                }
            }

            is TaskEvent.OnDescriptionChange -> {
                _state.update {
                    it.copy(description = event.description)
                }
            }

            is TaskEvent.OnDateChange -> {
                _state.update {
                    it.copy(dueDate = event.millis)
                }
            }

            is TaskEvent.OnPriorityChange -> {
                _state.update {
                    it.copy(priority = event.priority)
                }
            }

            TaskEvent.OnIsCompleteChange -> {
                _state.update {
                    it.copy(isTaskComplete = !_state.value.isTaskComplete)
                }
            }

            is TaskEvent.OnRelatedSubjectSelect -> {
                _state.update {
                    it.copy(
                        relatedToSubject = event.subject.name,
                        subjectId = event.subject.subjectId
                    )
                }
            }

            TaskEvent.SaveTask -> saveTask()
            TaskEvent.DeleteTask -> deleteTask()
            else -> {}
        }
    }

    // 删除任务方法
    private fun deleteTask() {
        viewModelScope.launch {
            try {
                val currentTaskId = state.value.currentTaskId
                if (currentTaskId != null) {
                    withContext(Dispatchers.IO) {
                        taskRepository.deleteTask(taskId = currentTaskId)
                    }
                    _snackbarEventFlow.emit(
                        SnackbarEvent.ShowSnackbar(message = "任务删除成功。")
                    )
                    _snackbarEventFlow.emit(SnackbarEvent.NavigateUp)
                } else {
                    _snackbarEventFlow.emit(
                        SnackbarEvent.ShowSnackbar(message = "没有任务可删除。")
                    )
                }
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        message = "未能删除任务。 ${e.message}",
                        duration = SnackbarDuration.Long
                    )
                )
            }
        }
    }

    // 保存任务方法
    private fun saveTask() {
        viewModelScope.launch {
            val state = _state.value
            if (state.subjectId == null || state.relatedToSubject == null) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        message = "请选择相关的科目。"
                    )
                )
                return@launch
            }
            try {
                taskRepository.upsertTask(
                    task = Task(
                        title = state.title,
                        description = state.description,
                        dueDate = state.dueDate ?: Instant.now().toEpochMilli(),
                        relatedToSubject = state.relatedToSubject,
                        priority = state.priority.value,
                        isComplete = state.isTaskComplete,
                        taskSubjectId = state.subjectId,
                        taskId = state.currentTaskId
                    )
                )
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(message = "任务保存成功。")
                )
                _snackbarEventFlow.emit(SnackbarEvent.NavigateUp)
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        message = "未能保存任务。 ${e.message}",
                        SnackbarDuration.Long
                    )
                )
            }
        }
    }

    // 获取任务数据方法
    private fun fetchTask() {
        viewModelScope.launch {
            navArgs.taskId?.let { id ->
                taskRepository.getTaskById(id)?.let { task ->
                    _state.update {
                        it.copy(
                            title = task.title,
                            description = task.description,
                            dueDate = task.dueDate,
                            isTaskComplete = task.isComplete,
                            relatedToSubject = task.relatedToSubject,
                            priority = Priority.fromInt(task.priority),
                            subjectId = task.taskSubjectId,
                            currentTaskId = task.taskId
                        )
                    }
                }
            }
        }
    }

    // 获取学科数据方法
    private fun fetchSubject() {
        viewModelScope.launch {
            navArgs.subjectId?.let { id ->
                subjectRepository.getSubjectById(id)?.let { subject ->
                    _state.update {
                        it.copy(
                            subjectId = subject.subjectId,
                            relatedToSubject = subject.name
                        )
                    }
                }
            }
        }
    }

}