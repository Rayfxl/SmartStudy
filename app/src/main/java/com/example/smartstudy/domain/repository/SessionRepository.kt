package com.example.smartstudy.domain.repository

import com.example.smartstudy.domain.model.Session
import kotlinx.coroutines.flow.Flow

// 定义与学习记录相关的数据操作接口
interface SessionRepository {

    suspend fun insertSession(session: Session)

    suspend fun deleteSession(session: Session)

    fun getAllSessions(): Flow<List<Session>>

    fun getRecentFiveSessions(): Flow<List<Session>>

    fun getRecentTenSessionsForSubject(subjectId: Int): Flow<List<Session>>

    fun getTotalSessionsDuration(): Flow<Long>

    fun getTotalSessionsDurationBySubject(subjectId: Int): Flow<Long>
}