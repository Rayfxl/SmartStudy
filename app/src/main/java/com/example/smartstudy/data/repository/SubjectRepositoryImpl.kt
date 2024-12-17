package com.example.smartstudy.data.repository

import com.example.smartstudy.data.local.SessionDao
import com.example.smartstudy.data.local.SubjectDao
import com.example.smartstudy.data.local.TaskDao
import com.example.smartstudy.domain.model.Subject
import com.example.smartstudy.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// SubjectRepositoryImpl 类实现了 SubjectRepository 接口，负责处理与科目相关的数据库操作
class SubjectRepositoryImpl @Inject constructor(
    private val subjectDao: SubjectDao,
    private val taskDao: TaskDao,
    private val sessionDao: SessionDao
): SubjectRepository {

    override suspend fun upsertSubject(subject: Subject) {
        subjectDao.upsertSubject(subject)
    }

    override fun getTotalSubjectCount(): Flow<Int> {
        return subjectDao.getTotalSubjectCount()
    }

    override fun getTotalGoalHours(): Flow<Float> {
        return subjectDao.getTotalGoalHours()
    }

    override suspend fun deleteSubject(subjectId: Int) {
        taskDao.deleteTasksBySubjectId(subjectId)
        sessionDao.deleteSessionsBySubjectId(subjectId)
        subjectDao.deleteSubject(subjectId)
    }

    override suspend fun getSubjectById(subjectId: Int): Subject? {
        return subjectDao.getSubjectById(subjectId)
    }

    override fun getAllSubjects(): Flow<List<Subject>> {
        return subjectDao.getAllSubjects()
    }

}