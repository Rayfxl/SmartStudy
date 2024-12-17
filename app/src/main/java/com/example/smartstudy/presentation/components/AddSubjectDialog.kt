package com.example.smartstudy.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smartstudy.domain.model.Subject

@Composable
fun AddSubjectDialog(
    isOpen: Boolean,
    title: String = "添加/修改科目",
    selectedColors: List<Color>,
    subjectName: String,
    goalHours: String,
    onColorChange: (List<Color>) -> Unit,
    onSubjectNameChange: (String) -> Unit,
    onGoalHoursChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmButtonClick: () -> Unit
) {
    // 用于保存错误信息的变量，存储科目名称和目标时长的错误信息
    var subjectNameError by rememberSaveable { mutableStateOf<String?>(null) }
    var goalHoursError by rememberSaveable { mutableStateOf<String?>(null) }

    // 校验科目名称的逻辑
    subjectNameError = when {
        subjectName.isBlank() -> "请输入科目名称。"
        subjectName.length < 2 -> "科目名称过短。"
        subjectName.length > 20 -> "科目名称过长。"
        else -> null
    }
    // 校验目标学习时长的逻辑
    goalHoursError = when {
        goalHours.isBlank() -> "请输入目标学习时长。"
        goalHours.toFloatOrNull() == null -> "无效数字。"
        goalHours.toFloat() < 1f -> "请至少设置1小时。"
        goalHours.toFloat() > 1000f -> "目标时长不能超过1000小时。"
        else -> null
    }

    // 只有当 isOpen 为 true 时，才显示弹窗
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(text = title) },
            text = {
                Column {
                    // 颜色选择部分：展示科目卡片颜色并允许选择
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Subject.subjectCardColors.forEach { colors ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 1.dp,
                                        color = if (colors == selectedColors) Color.Black
                                        else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .background(brush = Brush.verticalGradient(colors))
                                    .clickable { onColorChange(colors) }
                            )
                        }
                    }
                    // 科目名称输入框，带有错误提示
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = onSubjectNameChange,
                        label = { Text(text = "科目名称") },
                        singleLine = true,
                        isError = subjectNameError != null && subjectName.isNotBlank(),
                        supportingText = { Text(text = subjectNameError.orEmpty())}
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // 目标学习时长输入框，带有错误提示
                    OutlinedTextField(
                        value = goalHours,
                        onValueChange = onGoalHoursChange,
                        label = { Text(text = "目标学习时长") },
                        singleLine = true,
                        isError = goalHoursError != null && goalHours.isNotBlank(),
                        supportingText = { Text(text = goalHoursError.orEmpty())},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },

            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = "取消")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmButtonClick,
                    enabled = subjectNameError == null && goalHoursError == null
                ) {
                    Text(text = "保存")
                }
            }
        )
    }
}