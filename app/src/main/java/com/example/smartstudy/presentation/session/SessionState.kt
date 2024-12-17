package com.example.smartstudy.presentation.session

import com.example.smartstudy.domain.model.Session
import com.example.smartstudy.domain.model.Subject

// 数据类 SessionState，用于表示学习记录的各种状态
data class SessionState(
    val subjects: List<Subject> = emptyList(),
    val sessions: List<Session> = emptyList(),
    val relatedToSubject: String? = null,
    val subjectId: Int? = null,
    val session: Session? = null
)
