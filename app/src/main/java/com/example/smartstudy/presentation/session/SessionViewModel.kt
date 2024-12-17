package com.example.smartstudy.presentation.session

import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartstudy.domain.model.Session
import com.example.smartstudy.domain.repository.SessionRepository
import com.example.smartstudy.domain.repository.SubjectRepository
import com.example.smartstudy.util.SnackbarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
// SessionViewModel 类负责处理学习记录相关的逻辑，包括展示记录数据和处理用户事件。
class SessionViewModel @Inject constructor(
    subjectRepository: SubjectRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    // 当前状态的 MutableStateFlow，用于保存和更新视图状态
    private val _state = MutableStateFlow(SessionState())
    val state = combine(
        _state,
        subjectRepository.getAllSubjects(),
        sessionRepository.getAllSessions()
    ) { state, subjects, sessions ->
        state.copy(
            subjects = subjects,
            sessions = sessions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = SessionState()
    )

    // Snackbar 事件流，用于在视图层显示 Snackbar 消息
    private val _snackbarEventFlow = MutableSharedFlow<SnackbarEvent>()
    val snackbarEventFlow = _snackbarEventFlow.asSharedFlow()

    // 处理用户触发的事件，根据不同的事件类型执行相应的操作
    fun onEvent(event: SessionEvent) {
        when (event) {
            SessionEvent.NotifyToUpdateSubject -> notifyToUpdateSubject()
            SessionEvent.DeleteSession -> deleteSession()
            is SessionEvent.OnDeleteSessionButtonClick -> {
                _state.update {
                    it.copy(session = event.session)
                }
            }
            is SessionEvent.OnRelatedSubjectChange -> {
                _state.update {
                    it.copy(
                        relatedToSubject = event.subject.name,
                        subjectId = event.subject.subjectId
                    )
                }
            }

            is SessionEvent.SaveSession -> insertSession(event.duration)
            is SessionEvent.UpdateSubjectIdAndRelatedSubject -> {
                _state.update {
                    it.copy(
                        relatedToSubject = event.relatedToSubject,
                        subjectId = event.subjectId
                    )
                }
            }

            else -> {}
        }
    }

    // 当没有选择学科时，显示 Snackbar 提示用户选择学科
    private fun notifyToUpdateSubject() {
        viewModelScope.launch {
            if (state.value.subjectId == null || state.value.relatedToSubject == null) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        message = "请选择要学习的科目。"
                    )
                )
            }
        }
    }

    // 删除当前记录，并通过 Snackbar 提示用户删除成功或失败
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

    // 保存新的学习会话记录。如果时长小于 36 秒，则显示提示。
    private fun insertSession(duration: Long) {
        viewModelScope.launch {
            if (duration < 36) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        message = "单次时长不能少于36秒。"
                    )
                )
                return@launch
            }
            try {
                sessionRepository.insertSession(
                    session = Session(
                        sessionSubjectId = state.value.subjectId ?: -1,
                        relatedToSubject = state.value.relatedToSubject ?: "",
                        date = Instant.now().toEpochMilli(),
                        duration = duration
                    )
                )
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(message = "记录保存成功。")
                )
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackbarEvent.ShowSnackbar(
                        message = "未能保存记录。 ${e.message}",
                        duration = SnackbarDuration.Long
                    )
                )
            }
        }
    }


}