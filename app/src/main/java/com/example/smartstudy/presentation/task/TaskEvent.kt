package com.example.smartstudy.presentation.task

import com.example.smartstudy.domain.model.Subject
import com.example.smartstudy.util.Priority

// 定义一个密封类 TaskEvent，表示任务相关的所有事件
sealed class TaskEvent {
    data class OnTitleChange(val title: String) : TaskEvent()
    data class OnDescriptionChange(val description: String) : TaskEvent()
    data class OnDateChange(val millis: Long?) : TaskEvent()
    data class OnPriorityChange(val priority: Priority) : TaskEvent()
    data class OnRelatedSubjectSelect(val subject: Subject) : TaskEvent()
    data object OnIsCompleteChange : TaskEvent()
    data object SaveTask : TaskEvent()
    data object DeleteTask : TaskEvent()

}