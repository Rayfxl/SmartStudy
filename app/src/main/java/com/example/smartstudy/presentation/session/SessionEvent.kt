package com.example.smartstudy.presentation.session

import com.example.smartstudy.domain.model.Session
import com.example.smartstudy.domain.model.Subject

// 定义一个密封类 SessionEvent，表示学习记录相关的所有事件
sealed class SessionEvent {
    data class OnRelatedSubjectChange(val subject: Subject) : SessionEvent()
    data class SaveSession(val duration: Long) : SessionEvent()
    data class OnDeleteSessionButtonClick(val session: Session) : SessionEvent()
    data object DeleteSession : SessionEvent()
    data object NotifyToUpdateSubject : SessionEvent()
    data class UpdateSubjectIdAndRelatedSubject(
        val subjectId: Int?,
        val relatedToSubject: String?
    ) : SessionEvent()
}