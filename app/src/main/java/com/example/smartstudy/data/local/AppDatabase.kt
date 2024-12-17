package com.example.smartstudy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.smartstudy.domain.model.Session
import com.example.smartstudy.domain.model.Subject
import com.example.smartstudy.domain.model.Task

// AppDatabase 类是 Room 数据库的抽象类，负责持久化数据库的操作
@Database(
    entities = [Subject::class, Session::class, Task::class],
    version = 1
)
// 使用 TypeConverters 来转换颜色列表
@TypeConverters(ColorListConverter::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun subjectDao(): SubjectDao
    abstract fun taskDao(): TaskDao
    abstract fun sessionDao(): SessionDao
}