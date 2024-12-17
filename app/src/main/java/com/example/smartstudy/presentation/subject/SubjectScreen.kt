package com.example.smartstudy.presentation.subject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smartstudy.presentation.components.AddSubjectDialog
import com.example.smartstudy.presentation.components.CountCard
import com.example.smartstudy.presentation.components.DeleteDialog
import com.example.smartstudy.presentation.components.studySessionsList
import com.example.smartstudy.presentation.components.tasksList
import com.example.smartstudy.presentation.destinations.TaskScreenRouteDestination
import com.example.smartstudy.presentation.task.TaskScreenNavArgs
import com.example.smartstudy.util.SnackbarEvent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

data class SubjectScreenNavArgs(
    val subjectId: Int
)

@Destination(navArgsDelegate = SubjectScreenNavArgs::class)
@Composable
fun SubjectScreenRoute(
    navigator: DestinationsNavigator
) {
    val viewModel: SubjectViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 渲染 SubjectScreen，并传入必要的参数
    SubjectScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarEvent = viewModel.snackbarEventFlow,
        onBackButtonClick = { navigator.navigateUp() },
        onAddTaskButtonClick = {
            // 传递到 TaskScreen 的导航参数
            val navArg = TaskScreenNavArgs(taskId = null, subjectId = state.currentSubjectId)
            navigator.navigate(TaskScreenRouteDestination(navArgs = navArg))
        },
        onTaskCardClick = { taskId ->
            val navArg = TaskScreenNavArgs(taskId = taskId, subjectId = null)
            navigator.navigate(TaskScreenRouteDestination(navArgs = navArg))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectScreen(
    state: SubjectState,
    onEvent: (SubjectEvent) -> Unit,
    snackbarEvent: SharedFlow<SnackbarEvent>,
    onBackButtonClick: () -> Unit,
    onAddTaskButtonClick: () -> Unit,
    onTaskCardClick: (Int?) -> Unit
) {

    // 用于实现滚动行为的配置
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    val isFABExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    // 控制对话框的开关状态
    var isEditSubjectDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isDeleteSubjectDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isDeleteSessionDialogOpen by rememberSaveable { mutableStateOf(false) }

    // Snackbar 状态
    val snackbarHostState = remember { SnackbarHostState() }

    // 处理 Snackbar 事件
    LaunchedEffect(key1 = true) {
        snackbarEvent.collectLatest { event ->
            when (event) {
                is SnackbarEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = event.duration
                    )
                }

                SnackbarEvent.NavigateUp -> {
                    onBackButtonClick()
                }
            }
        }
    }

    // 更新学习进度
    LaunchedEffect(key1 = state.studiedHours, key2 = state.goalStudyHours) {
        onEvent(SubjectEvent.UpdateProgress)
    }

    // 编辑科目对话框
    AddSubjectDialog(
        isOpen = isEditSubjectDialogOpen,
        subjectName = state.subjectName,
        goalHours = state.goalStudyHours,
        onSubjectNameChange = { onEvent(SubjectEvent.OnSubjectNameChange(it)) },
        onGoalHoursChange = { onEvent(SubjectEvent.OnGoalStudyHoursChange(it)) },
        selectedColors = state.subjectCardColors,
        onColorChange = { onEvent(SubjectEvent.OnSubjectCardColorChange(it)) },
        onDismissRequest = { isEditSubjectDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(SubjectEvent.UpdateSubject)
            isEditSubjectDialogOpen = false
        }
    )

    // 删除科目对话框
    DeleteDialog(
        isOpen = isDeleteSubjectDialogOpen,
        title = "删除科目？",
        bodyText = "您确定想要删除该科目吗？所有相关任务和学习记录都将被永久删除。" +
                "该行为不可被撤销。",
        onDismissRequest = { isDeleteSubjectDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(SubjectEvent.DeleteSubject)
            isDeleteSubjectDialogOpen = false
        }
    )

    // 删除学习记录对话框
    DeleteDialog(
        isOpen = isDeleteSessionDialogOpen,
        title = "删除学习时长记录？",
        bodyText = "您确定想要删除该记录？这将减少您的已学习时长，且该行为不可被撤销。",
        onDismissRequest = { isDeleteSessionDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(SubjectEvent.DeleteSession)
            isDeleteSessionDialogOpen = false
        }
    )

    // 主要内容布局
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // 顶部导航栏
            SubjectScreenTopBar(
                title = state.subjectName,
                onBackButtonClick = onBackButtonClick,
                onDeleteButtonClick = { isDeleteSubjectDialogOpen = true },
                onEditButtonClick = { isEditSubjectDialogOpen = true },
                scrollBehavior = scrollBehavior
            )
        },
        // 添加任务按钮
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTaskButtonClick,
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add") },
                text = { Text(text = "添加任务") },
                expanded = isFABExpanded
            )
        }
    ) { paddingValue ->
        // 列表内容展示
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue)
        ) {
            item {
                // 显示学习进度和目标时长
                SubjectOverviewSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    studiedHours = state.studiedHours.toString(),
                    goalHours = state.goalStudyHours,
                    progress = state.progress
                )
            }
            // 显示未完成的任务
            tasksList(
                sectionTitle = "未完成的任务",
                emptyListText = "您没有任何未完成的任务。\n " +
                        "点击+号按钮来添加一个新任务。",
                tasks = state.upcomingTasks,
                onCheckBoxClick = { onEvent(SubjectEvent.OnTaskIsCompleteChange(it)) },
                onTaskCardClick = onTaskCardClick
            )
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            // 显示已完成的任务
            tasksList(
                sectionTitle = "已完成的任务",
                emptyListText = "您没有任何已完成的任务。\n " +
                        "开始学习来完成任务。",
                tasks = state.completedTasks,
                onCheckBoxClick = { onEvent(SubjectEvent.OnTaskIsCompleteChange(it)) },
                onTaskCardClick = onTaskCardClick
            )
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            // 显示最近的学习记录
            studySessionsList(
                sectionTitle = "最近学习记录",
                emptyListText = "您最近没有任何学习记录。\n " +
                        "马上开始学习来记录您的学习过程。",
                sessions = state.recentSessions,
                onDeleteIconClick = {
                    isDeleteSessionDialogOpen = true
                    onEvent(SubjectEvent.OnDeleteSessionButtonClick(it))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectScreenTopBar(
    title: String,
    onBackButtonClick: () -> Unit,
    onDeleteButtonClick: () -> Unit,
    onEditButtonClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    LargeTopAppBar(
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            // 返回按钮
            IconButton(onClick = onBackButtonClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "navigate back"
                )
            }
        },
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        actions = {
            // 删除按钮
            IconButton(onClick = onDeleteButtonClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Subject"
                )
            }
            // 编辑按钮
            IconButton(onClick = onEditButtonClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Subject"
                )
            }
        }
    )
}

@Composable
private fun SubjectOverviewSection(
    modifier: Modifier,
    studiedHours: String,
    goalHours: String,
    progress: Float
) {
    val percentageProgress = remember(progress) {
        (progress * 100).toInt().coerceIn(0, 100)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 显示目标时长
        CountCard(
            modifier = Modifier.weight(1f),
            headingText = "目标时长",
            count = goalHours
        )
        Spacer(modifier = Modifier.width(10.dp))
        // 显示已学时长
        CountCard(
            modifier = Modifier.weight(1f),
            headingText = "已学时长",
            count = studiedHours
        )
        Spacer(modifier = Modifier.width(10.dp))
        // 显示进度条
        Box(
            modifier = Modifier.size(75.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                progress = 1f,
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                progress = progress,
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round
            )
            Text(text = "$percentageProgress%")
        }
    }
}