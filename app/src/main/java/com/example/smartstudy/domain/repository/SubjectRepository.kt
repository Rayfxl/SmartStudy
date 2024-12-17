package com.example.smartstudy.domain.repository

import com.example.smartstudy.domain.model.Subject
import kotlinx.coroutines.flow.Flow

// 定义与科目相关的数据操作接口
interface SubjectRepository {

    suspend fun upsertSubject(subject: Subject)

    fun getTotalSubjectCount(): Flow<Int>

    fun getTotalGoalHours(): Flow<Float>

    suspend fun deleteSubject(subjectId: Int)

    suspend fun getSubjectById(subjectId: Int): Subject?

    fun getAllSubjects(): Flow<List<Subject>>
}