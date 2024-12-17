// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    // Hilt插件，用于依赖注入
    id("com.google.dagger.hilt.android") version "2.48" apply false
    // KSP插件，支持Kotlin编译时注解处理
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
}