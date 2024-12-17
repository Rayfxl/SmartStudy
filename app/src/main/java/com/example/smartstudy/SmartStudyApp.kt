package com.example.smartstudy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// 该注解表示这是一个支持 Dagger Hilt 依赖注入的 Android 应用
@HiltAndroidApp
class SmartStudyApp: Application()