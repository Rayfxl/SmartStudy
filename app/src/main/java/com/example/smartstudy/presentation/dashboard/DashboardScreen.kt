package com.example.smartstudy.presentation.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smartstudy.R
import com.example.smartstudy.domain.model.Session
import com.example.smartstudy.domain.model.Subject
import com.example.smartstudy.domain.model.Task
import com.example.smartstudy.presentation.components.AddSubjectDialog
import com.example.smartstudy.presentation.components.CountCard
import com.example.smartstudy.presentation.components.DeleteDialog
import com.example.smartstudy.presentation.components.SubjectCard
import com.example.smartstudy.presentation.components.studySessionsList
import com.example.smartstudy.presentation.components.tasksList
import com.example.smartstudy.presentation.destinations.SessionScreenRouteDestination
import com.example.smartstudy.presentation.destinations.SubjectScreenRouteDestination
import com.example.smartstudy.presentation.destinations.TaskScreenRouteDestination
import com.example.smartstudy.presentation.subject.SubjectScreenNavArgs
import com.example.smartstudy.presentation.task.TaskScreenNavArgs
import com.example.smartstudy.util.SnackbarEvent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

// 设置该屏幕为根导航图，并作为目标导航点
@RootNavGraph(start = true)
@Destination
@Composable
fun DashboardScreenRoute(
    // 导航器，用于页面跳转
    navigator: DestinationsNavigator
) {

    val viewModel: DashboardViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val recentSessions by viewModel.recentSessions.collectAsStateWithLifecycle()

    // 渲染Dashboard屏幕
    DashboardScreen(
        state = state,
        tasks = tasks,
        recentSessions = recentSessions,
        onEvent = viewModel::onEvent,
        snackbarEvent = viewModel.snackbarEventFlow,
        onSubjectCardClick = { subjectId ->
            subjectId?.let {
                val navArg = SubjectScreenNavArgs(subjectId = subjectId)
                navigator.navigate(SubjectScreenRouteDestination(navArgs = navArg))
            }
        },
        onTaskCardClick = { taskId ->
            val navArg = TaskScreenNavArgs(taskId = taskId, subjectId = null)
            navigator.navigate(TaskScreenRouteDestination(navArgs = navArg))
        },
        onStartSessionButtonClick = {
            navigator.navigate(SessionScreenRouteDestination())
        }
    )
}

@Composable
private fun DashboardScreen(
    state: DashboardState,
    tasks: List<Task>,
    recentSessions: List<Session>,
    onEvent: (DashboardEvent) -> Unit,
    snackbarEvent: SharedFlow<SnackbarEvent>,
    onSubjectCardClick: (Int?) -> Unit,
    onTaskCardClick: (Int?) -> Unit,
    onStartSessionButtonClick: () -> Unit
) {

    var isAddSubjectDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isDeleteSessionDialogOpen by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // 监听并处理Snackbar事件
    LaunchedEffect(key1 = true) {
        snackbarEvent.collectLatest { event ->
            when(event) {
                is SnackbarEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = event.duration
                    )
                }

                SnackbarEvent.NavigateUp -> {}
            }
        }
    }

    // 显示添加科目弹窗
    AddSubjectDialog(
        isOpen = isAddSubjectDialogOpen,
        subjectName = state.subjectName,
        goalHours = state.goalStudyHours,
        selectedColors = state.subjectCardColors,
        onSubjectNameChange = { onEvent(DashboardEvent.OnSubjectNameChange(it)) },
        onGoalHoursChange = { onEvent(DashboardEvent.OnGoalStudyHoursChange(it)) },
        onColorChange = { onEvent(DashboardEvent.OnSubjectCardColorChange(it)) },
        onDismissRequest = { isAddSubjectDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(DashboardEvent.SaveSubject)
            isAddSubjectDialogOpen = false
        }
    )

    // 显示删除学习记录弹窗
    DeleteDialog(
        isOpen = isDeleteSessionDialogOpen,
        title = "删除学习时长记录？",
        bodyText = "您确定想要删除该记录？这将减少您的已学习时长，且该行为不可被撤销。",
        onDismissRequest = { isDeleteSessionDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(DashboardEvent.DeleteSession)
            isDeleteSessionDialogOpen = false
        }
    )
    Scaffold (
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { DashboardScreenTopBar()}
    ){ paddingValues ->
       LazyColumn(
           modifier = Modifier
               .fillMaxSize()
               .padding(paddingValues)
       ) {
            item {
                // 统计卡片部分
                CountCardsSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    subjectCount = state.totalSubjectCount,
                    studiedHours = state.totalStudiedHours.toString(),
                    goalHours = state.totalGoalStudyHours.toString()
                )
            }

           item {
               // 科目卡片部分
               SubjectCardsSection(
                   modifier = Modifier.fillMaxWidth(),
                   subjectList = state.subjects,
                   onAddIconClicked = { isAddSubjectDialogOpen = true },
                   onSubjectCardClick = onSubjectCardClick
               )
           }

           item {
               // 开始学习按钮
               Button(
                   onClick = onStartSessionButtonClick,
                   modifier = Modifier
                       .fillMaxWidth()
                       .padding(horizontal = 48.dp, vertical = 20.dp)
               ) {
                    Text(text = "开始学习")
               }
           }

           // 未完成的任务列表
           tasksList(
               sectionTitle = "未完成的任务",
               emptyListText = "您没有任何未完成的任务。\n " +
                       "点击相关科目来添加一个新任务。",
               tasks = tasks,
               onCheckBoxClick = { onEvent(DashboardEvent.OnTaskIsCompleteChange(it)) },
               onTaskCardClick = onTaskCardClick
           )
           item {
               Spacer(modifier = Modifier.height(20.dp))
           }
           // 最近学习记录
           studySessionsList(
               sectionTitle = "最近学习记录",
               emptyListText = "您最近没有任何学习记录。\n " +
                       "马上开始学习来记录您的学习过程。",
               sessions = recentSessions,
               onDeleteIconClick = {
                   onEvent(DashboardEvent.OnDeleteSessionButtonClick(it))
                   isDeleteSessionDialogOpen = true
               }
           )
       }
    }
}

// 顶部应用栏
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreenTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "智能学习",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    )
}

@Composable
private fun CountCardsSection(
    modifier: Modifier,
    subjectCount: Int,
    studiedHours: String,
    goalHours: String
) {
    Row(modifier = modifier) {
        CountCard(
            modifier = Modifier.weight(1f),
            headingText = "科目总数",
            count = "$subjectCount"
        )
        Spacer(modifier = Modifier.width(10.dp))
        CountCard(
            modifier = Modifier.weight(1f),
            headingText = "已学时长",
            count = studiedHours
        )
        Spacer(modifier = Modifier.width(10.dp))
        CountCard(
            modifier = Modifier.weight(1f),
            headingText = "目标时长",
            count = goalHours
        )
    }
}

@Composable
private fun SubjectCardsSection(
    modifier: Modifier,
    subjectList: List<Subject>,
    emptyListText: String = "您没有任何科目。\n 点击+号按钮来添加一个新的科目。",
    onAddIconClicked: () -> Unit,
    onSubjectCardClick: (Int?) -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "科目列表",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 12.dp)
            )
            IconButton(onClick = onAddIconClicked) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Subject"
                )
            }
        }
        if (subjectList.isEmpty()) {
            Image(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally),
                painter = painterResource(R.drawable.img_books),
                contentDescription = emptyListText
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = emptyListText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp)
        ) {
            items(subjectList) { subject ->
                SubjectCard(
                    subjectName = subject.name,
                    gradientColors = subject.colors.map { Color(it) },
                    onClick = { onSubjectCardClick(subject.subjectId) }
                )
            }
        }
    }
}