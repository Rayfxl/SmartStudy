package com.example.smartstudy.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import com.example.smartstudy.presentation.theme.Green
import com.example.smartstudy.presentation.theme.Orange
import com.example.smartstudy.presentation.theme.Red
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// 定义优先级枚举类，用于表示任务的优先级，包含三个级别：低、中、高
enum class Priority(val title: String, val color: Color, val value: Int) {
    LOW(title = "低", color = Green, value = 0),
    MEDIUM(title = "中", color = Orange, value = 1),
    HIGH(title = "高", color = Red, value = 2);

    // 伴生对象，提供根据 `value` 查找优先级的方法
    companion object {
        fun fromInt(value: Int) = values().firstOrNull { it.value == value } ?: MEDIUM
    }
}

// 扩展函数：将毫秒值转换为日期字符串（格式为 "yyyy年 MM月 dd日"）
fun Long?.changeMillisToDateString(): String {
    val date: LocalDate = this?.let {
        Instant
            .ofEpochMilli(it)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    } ?: LocalDate.now()
    return date.format(DateTimeFormatter.ofPattern("yyyy年 MM月 dd日"))
}

// 扩展函数：将秒数（Long 类型）转换为小时（Float 类型），保留两位小数
fun Long.toHours(): Float {
    val hours = this.toFloat() / 3600f
    return "%.2f".format(hours).toFloat()
}

// 定义一个 Snackbar 事件的密封类，用于展示 Snackbar 或进行导航操作
sealed class SnackbarEvent {
    // 显示 Snackbar 事件
    data class ShowSnackbar(
        val message: String,
        val duration: SnackbarDuration = SnackbarDuration.Short
    ) : SnackbarEvent()

    // 导航回退事件
    data object NavigateUp: SnackbarEvent()
}

// 扩展函数：为整数添加前导零，确保结果字符串长度为2
fun Int.pad(): String {
    return this.toString().padStart(length = 2, padChar = '0')
}