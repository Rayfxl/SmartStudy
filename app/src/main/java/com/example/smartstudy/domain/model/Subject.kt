package com.example.smartstudy.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.smartstudy.presentation.theme.gradient1
import com.example.smartstudy.presentation.theme.gradient2
import com.example.smartstudy.presentation.theme.gradient3
import com.example.smartstudy.presentation.theme.gradient4
import com.example.smartstudy.presentation.theme.gradient5

// 定义学习科目数据模型
@Entity
data class Subject(
    val name: String,
    val goalHours: Float,
    val colors: List<Int>,
    @PrimaryKey(autoGenerate = true)
    val subjectId: Int? = null
) {
    companion object {
        val subjectCardColors = listOf(gradient1, gradient2, gradient3, gradient4, gradient5)
    }
}

