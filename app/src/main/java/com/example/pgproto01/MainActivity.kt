@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.pgproto01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.time.LocalDate
import java.time.LocalTime
import com.google.accompanist.pager.*
import java.time.YearMonth
// ▼ clickable修正用
import androidx.compose.foundation.clickable

// ▼ ViewModelの参照に必要
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pgproto01.viewmodel.PunchLogViewModel

// ▼ ManualPunchDialog を定義済みなら、それを import
import com.example.pgproto01.ui.component.ManualPunchDialog
import com.example.pgproto01.ui.component.ManualPunchDialogSimple
 // パスは調整してください

// ▼ PunchLogエンティティの参照
import com.example.pgproto01.data.model.PunchLog
import com.example.pgproto01.data.model.PunchType

import java.time.Instant
import java.time.ZoneId
import androidx.activity.viewModels

import androidx.compose.foundation.lazy.rememberLazyListState

import com.example.attendance.ui.SettingsScreen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
//import com.example.pgproto01.ui.SettingsScreen
import com.example.pgproto01.ui.StaffMasterScreen
import com.example.pgproto01.viewmodel.StaffViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
// imports

import com.example.pgproto01.data.model.StaffEntity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color




//data class Staff(
//    val id: String = UUID.randomUUID().toString(),
//    val name: String
//)

data class DailyAttendance(
    val date: LocalDate,
    var clockIn: LocalTime? = null,
    var breakOut: LocalTime? = null,
    var breakIn: LocalTime? = null,
    var clockOut: LocalTime? = null,
    var comment: String = ""
)
fun convertRecordsToDaily(records: List<AttendanceRecord>): List<DailyAttendance> {
    val grouped = records.groupBy { it.timestamp.toLocalDate() }

    return grouped.map { (date, punches) ->
        val daily = DailyAttendance(date = date)

        punches.sortedBy { it.timestamp.toLocalTime() }.forEach {
            when (it.type) {
                PunchType.IN -> if (daily.clockIn == null) daily.clockIn = it.timestamp.toLocalTime()
                PunchType.BREAK_OUT -> if (daily.breakOut == null) daily.breakOut = it.timestamp.toLocalTime()
                PunchType.BREAK_IN -> if (daily.breakIn == null) daily.breakIn = it.timestamp.toLocalTime()
                PunchType.OUT -> if (daily.clockOut == null) daily.clockOut = it.timestamp.toLocalTime()
            }
        }

        daily
    }.sortedBy { it.date }
}
@OptIn(ExperimentalPagerApi::class)
@Composable
fun MonthlyPager(
    staffId: Long,
    punchLogViewModel: PunchLogViewModel,
    records: List<AttendanceRecord>,
    onManualPunchRequested: (LocalDate, PunchType) -> Unit) {
    val allMonths = remember(records) {
        records
            .map { YearMonth.from(it.timestamp) }
            .distinct()
            .sorted()
    }

    val pagerState = rememberPagerState()

    Column {
        if (allMonths.isNotEmpty()) {
            val currentMonth = allMonths.getOrNull(pagerState.currentPage) ?: YearMonth.now()
            Text(
                "${currentMonth.year}年 ${currentMonth.monthValue}月",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(8.dp)
            )

            HorizontalPager(
                count = allMonths.size,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                val month = allMonths[pageIndex]
                val daily = buildFullMonthDaily(records, month)
                MonthlyAttendanceTable(
                    staffId = staffId,                     // ← StaffDetailScreenから渡す必要あり
                    dailyRecords = daily,
                    punchLogViewModel = punchLogViewModel, // ← ViewModelを渡す
                    onManualPunchRequested = onManualPunchRequested
                )
                // ← 追加
            }
        } else {
            Text("記録がありません", modifier = Modifier.padding(16.dp))
        }
    }
}

fun buildFullMonthDaily(records: List<AttendanceRecord>, yearMonth: YearMonth): List<DailyAttendance> {
    val grouped = convertRecordsToDaily(records).associateBy { it.date }

    return (1..yearMonth.lengthOfMonth()).map { day ->
        val date = yearMonth.atDay(day)
        grouped[date] ?: DailyAttendance(date = date)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthlyAttendanceTable(staffId: Long,
                           dailyRecords: List<DailyAttendance>,
                           punchLogViewModel: PunchLogViewModel,
                           onManualPunchRequested: (LocalDate, PunchType) -> Unit) {
    val commentState = remember { mutableStateMapOf<LocalDate, String>() }
    val dateFormatter = DateTimeFormatter.ofPattern("MM/dd")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val columnWeights = listOf(1f, 1f, 1f, 1f, 1f, 2f) // コメント欄を広く

    // ▼ 今日の行を探す
    val today = LocalDate.now()
    val initialIndex = dailyRecords.indexOfFirst { it.date == today }.coerceAtLeast(0)

    // ▼ スクロール位置を今日に合わせる
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    LazyColumn(
        state = listState, // ← 追加！
        modifier = Modifier.fillMaxSize()) {
        stickyHeader {
            Divider(thickness = 1.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .background(MaterialTheme.colorScheme.surface) // 背景つけて見やすく
            ) {
                listOf("日付", "出勤", "外出", "戻り", "退勤", "コメント").forEachIndexed { i, title ->
                    Box(
                        modifier = Modifier
                            .weight(columnWeights[i])
                            .fillMaxHeight()
                            .padding(4.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        items(dailyRecords) { day ->
            val dailyComment by punchLogViewModel
                .getCommentForDay(staffId, day.date)
                .collectAsState(initial = null)

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                val values = listOf(
                    day.date.format(dateFormatter),
                    day.clockIn?.format(timeFormatter) ?: "-",
                    day.breakOut?.format(timeFormatter) ?: "-",
                    day.breakIn?.format(timeFormatter) ?: "-",
                    day.clockOut?.format(timeFormatter) ?: "-"
                )

                values.forEachIndexed { i, value ->
                    if (value == "-") {
                        Box(
                            modifier = Modifier
                                .weight(columnWeights[i])
                                .fillMaxHeight()
                                .border(1.dp, MaterialTheme.colorScheme.outline)
                                .padding(4.dp)
                                .clickable {
                                    val type = when (i) {
                                        1 -> PunchType.IN
                                        2 -> PunchType.BREAK_OUT
                                        3 -> PunchType.BREAK_IN
                                        4 -> PunchType.OUT
                                        else -> null
                                    }
                                    if (type != null) {
                                        onManualPunchRequested(day.date, type)
                                    }
                                }
                            ,
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", fontSize = 15.sp)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(columnWeights[i])
                                .fillMaxHeight()
                                .border(1.dp, MaterialTheme.colorScheme.outline)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(value, fontSize = 15.sp)
                        }
                    }
                }


                // コメント欄だけ TextField に
                var text by remember(dailyComment) {
                    mutableStateOf(dailyComment?.comment ?: "")
                }
                Box(
                    modifier = Modifier
                        .weight(columnWeights[5])
                        .fillMaxHeight()
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                        .padding(4.dp),
                ) {
                    TextField(
                        value = text,
                        onValueChange = {
                            text = it
                            punchLogViewModel.setComment(staffId, day.date, it) // ← DBに保存
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp)
                            .heightIn(min = 40.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )

                }
            }
        }
    }
}



//enum class PunchType {
//    IN,          // 出勤
//    BREAK_OUT,   // 外出
//    BREAK_IN,    // 戻り
//    OUT          // 退勤
//}
fun PunchType.displayName(): String = when (this) {
    PunchType.IN -> "出勤"
    PunchType.OUT -> "退勤"
    PunchType.BREAK_OUT -> "外出"
    PunchType.BREAK_IN -> "戻り"
}
data class AttendanceRecord(
    val timestamp: LocalDateTime,
    val type: PunchType
)
//
//// スタッフ一覧だけ管理する
//object InMemoryRepository {
//    val staffList = mutableStateListOf(
//        Staff(id = "1", name = "山田 太郎"),
//        Staff(id = "2", name = "佐藤 花子"),
//        Staff(id = "3", name = "鈴木 次郎"),
//        Staff(id = "4", name = "田中 三郎"),
//        Staff(id = "5", name = "中村 四季"),
//        Staff(id = "6", name = "高橋 桜"),
//    )
//
//    // 余計なものは削除！記録はDBに任せる
//    fun findStaff(staffId: String): Staff? =
//        staffList.find { it.id == staffId }
//}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ ViewModelをActivityスコープで取得
        val punchLogViewModel: PunchLogViewModel by viewModels()
        // ⛳ 修正ポイント：AttendanceAppにViewModelを渡す！
        setContent {
            AttendanceApp(punchLogViewModel = punchLogViewModel)
        }
    }
}

@Composable
fun AttendanceApp(punchLogViewModel: PunchLogViewModel) { // ← ★引数を追加
    val navController = rememberNavController()
    MaterialTheme {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                val staffViewModel: StaffViewModel = viewModel()
                val activeStaff by staffViewModel.activeStaffList.collectAsState(initial = emptyList())

                HomeScreen(
                    staff = activeStaff,
                    onStaffClick = { staffId ->
                        navController.navigate("detail/$staffId")
                    },
                    onSettingsClick = { navController.navigate("settings") } // ← 追加
                )
            }
            composable(
                route = "detail/{staffId}",
                arguments = listOf(navArgument("staffId") { type = NavType.LongType })
            ) { backStackEntry ->
                val staffId = backStackEntry.arguments?.getLong("staffId")!!
                StaffDetailScreen(
                    staffId = staffId,
                    punchLogViewModel = punchLogViewModel, // ← 渡す
                    onBack = { navController.popBackStack() },
                    onPunched = {
                        navController.popBackStack(route = "home", inclusive = false)
                    }
                )
            }
            composable("settings") {
                SettingsScreen(navController = navController) // ← 設定画面
            }
            composable("staffMaster") {
                StaffMasterScreen(navController = navController)
            }

        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    staff: List<StaffEntity>,            // ← Staff → StaffEntity
    onStaffClick: (Long) -> Unit,        // ← String → Long
    onSettingsClick: () -> Unit // ← 引数追加
) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000)
        }
    }

    val timeText = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    val dateText = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd (E)"))

    Scaffold (
        topBar = {   // ← ここを追加！
            TopAppBar(
                title = { Text("勤怠アプリ") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "設定"
                        )
                    }
                }
            )
        }
    ){ inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeText,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = dateText,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Text(
                "スタッフ",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.SemiBold
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(staff, key = { it.id }) { s ->
                    StaffCard(
                        name = s.name,
                        onClick = { onStaffClick(s.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun StaffCard(
    name: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StaffDetailScreen(
    staffId: Long,
    punchLogViewModel: PunchLogViewModel, // ← 追加
    onBack: () -> Unit,
    onPunched: () -> Unit
) {
//    val punchLogViewModel: PunchLogViewModel = viewModel()
    val scope = rememberCoroutineScope()
    var punchDone by remember { mutableStateOf(false) }
    val staffViewModel: StaffViewModel = viewModel()
    val allStaff by staffViewModel.staffList.collectAsState(initial = emptyList())
    val staff = allStaff.firstOrNull { it.id == staffId }

//    val staffLongId = staffId.toLongOrNull() ?: return // staffId が不正なら早期リターン
    val logs: List<PunchLog> by punchLogViewModel
        .getPunchLogsForStaff(staffId)  // ← 直接渡せる
        .collectAsState(initial = emptyList())

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

    val records = logs.map {
        val dateTime = Instant.ofEpochMilli(it.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()


        AttendanceRecord(
            timestamp = dateTime,
            type = PunchType.valueOf(it.type)
        )
    }



//    staffId) }
    var punchEnabled by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    val pendingType = punchLogViewModel.pendingType

    var manualDialogVisible by remember { mutableStateOf(false) }
    var manualDialogDate by remember { mutableStateOf<LocalDate?>(null) }
    var manualDialogType by remember { mutableStateOf<PunchType?>(null) }

    val status = getTodayStatus(logs)
//    val canClockIn = true
//    val canGoOut = true
//    val canReturn = true
//    val canClockOut = true
//    打刻ボタンのずいじ表示切り替え機能↑↓ーーーーーーーーーーーーーーーーーーーーーーーーーーーーーーー
    val canClockIn = !status.hasClockIn
    val canGoOut = !status.hasGoOut && !status.hasClockOut   // 退勤していない場合のみ外出可能
    val canReturn = !status.hasReturn && !status.hasClockOut // 退勤していない場合のみ戻り可能
    val canClockOut = !status.hasClockOut  // ← ここを修正

//    var pendingType by remember { mutableStateOf<PunchType?>(null) }
    var missingPunches by remember { mutableStateOf<List<PunchType>>(emptyList()) }
    var showManualDialog by remember { mutableStateOf(false) }


    fun requestPunch(type: PunchType) {
        if (!punchEnabled) return

        // 🔽 今日のログから足りない打刻を判定
        val missing = punchLogViewModel.getMissingPunchesForType(logs, type)

        if (missing.isNotEmpty()) {
            punchLogViewModel.updateMissingPunches(missing)
            punchLogViewModel.updatePendingType(type)
            punchLogViewModel.updateShowManualDialog(true)
        } else {
            // 🔽 通常の即時打刻（確認アラート表示）
            punchLogViewModel.updatePendingType(type)
            showDialog = true
        }
    }

//    fun requestPunch(type: PunchType) {
//        if (!punchEnabled) return
//
//        val status = getTodayStatus(logs)
//
//        when (type) {
//            PunchType.IN -> {
//                if (status.hasClockIn) return // すでに出勤済みなら無視
//                punchLogViewModel.updatePendingType(PunchType.IN)
//                punchLogViewModel.updateMissingPunches(emptyList())
//                punchLogViewModel.updateShowManualDialog(true)
//            }
//            PunchType.BREAK_OUT -> {
//                if (!status.hasClockIn) {
//                    // 出勤がないので出勤を手動入力 → 外出は現在時刻で補完
//                    punchLogViewModel.updatePendingType(PunchType.BREAK_OUT)
//                    punchLogViewModel.updateMissingPunches(listOf(PunchType.IN))
//                    punchLogViewModel.updateShowManualDialog(true)
//                } else {
//                    punchLogViewModel.updatePendingType(PunchType.BREAK_OUT)
//                    punchLogViewModel.updateMissingPunches(emptyList())
//                    punchLogViewModel.updateShowManualDialog(true)
//                }
//            }
//            PunchType.BREAK_IN -> {
//                if (!status.hasClockIn && !status.hasGoOut) {
//                    punchLogViewModel.updatePendingType(PunchType.BREAK_IN)
//                    punchLogViewModel.updateMissingPunches(listOf(PunchType.IN, PunchType.BREAK_OUT))
//                    punchLogViewModel.updateShowManualDialog(true)
//                } else if (!status.hasGoOut) {
//                    punchLogViewModel.updatePendingType(PunchType.BREAK_IN)
//                    punchLogViewModel.updateMissingPunches(listOf(PunchType.BREAK_OUT))
//                    punchLogViewModel.updateShowManualDialog(true)
//                } else {
//                    punchLogViewModel.updatePendingType(PunchType.BREAK_IN)
//                    punchLogViewModel.updateMissingPunches(emptyList())
//                    punchLogViewModel.updateShowManualDialog(true)
//                }
//            }
//            PunchType.OUT -> {
//                if (!status.hasClockIn) {
//                    punchLogViewModel.updatePendingType(PunchType.OUT)
//                    punchLogViewModel.updateMissingPunches(listOf(PunchType.IN))
//                    punchLogViewModel.updateShowManualDialog(true)
//                } else if (status.hasGoOut && !status.hasReturn) {
//                    punchLogViewModel.updatePendingType(PunchType.OUT)
//                    punchLogViewModel.updateMissingPunches(listOf(PunchType.BREAK_IN))
//                    punchLogViewModel.updateShowManualDialog(true)
//                } else {
//                    punchLogViewModel.updatePendingType(PunchType.OUT)
//                    punchLogViewModel.updateMissingPunches(emptyList())
//                    punchLogViewModel.updateShowManualDialog(true)
//                }
//            }
//        }
//    }


    Scaffold(
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onBack) {
                        Text("ホームに戻る")
                    }
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            Text(
                text = staff?.name ?: "不明なスタッフ",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = canClockIn,
                    onClick = { requestPunch(PunchType.IN) }
                ) { Text("出勤") }

//                Button(
//                    modifier = Modifier.weight(1f),
//                    enabled = canGoOut,
//                    onClick = { requestPunch(PunchType.BREAK_OUT) }
//                ) { Text("外出") }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = canGoOut,
                    onClick = { requestPunch(PunchType.BREAK_OUT) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status.hasClockIn) Color(0xFF4CAF50) else Color.Gray, // 出勤済みなら緑、未出勤ならグレー
                        contentColor = Color.White
                    )
                ) {
                    Text("外出")
                }

            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = canReturn,
                    onClick = { requestPunch(PunchType.BREAK_IN) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status.hasGoOut) Color(0xFF4CAF50) else Color.Gray,
                        contentColor = Color.White
                    )

                ) { Text("戻り") }

                Button(
                    modifier = Modifier.weight(1f),
                    enabled = canClockOut,
                    onClick = { requestPunch(PunchType.OUT) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (
                            (status.hasClockIn && !status.hasGoOut) || status.hasReturn
                        ) {
                            Color(0xFF4CAF50) // 緑
                        } else {
                            Color.Gray // グレー
                        },
                        contentColor = Color.White
                    )
                ) { Text("退勤") }
            }


            Spacer(Modifier.height(12.dp))

            Text(
                "勤怠履歴",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.SemiBold
            )
            MonthlyPager(
                staffId = staffId,
                punchLogViewModel = punchLogViewModel,
                records = records,
                onManualPunchRequested = { date, type ->
//                    punchLogViewModel.openManualDialog(date, type)
                    manualDialogDate = date
                    manualDialogType = type
                    manualDialogVisible = true
                }
            )


//            MonthlyPager(
//                records = records,
//                onManualPunchRequested = { date, type ->
//                    manualDialogDate = date
//                    manualDialogType = type
//                    manualDialogVisible = true
//                }
//            )



        }
    }
    if (punchLogViewModel.showManualDialog) {
        staff?.let {
            ManualPunchDialog(
                viewModel = punchLogViewModel,
                staffId = it.id.toString(),
                onDismiss = { punchLogViewModel.updateShowManualDialog(false) }
            )
        }

    }

    if (showDialog && staff != null && pendingType != null) {
//        val label = if (pendingType == PunchType.IN) "出勤" else "退勤"
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        android.util.Log.d("StaffDetailScreen", "✅ OKボタン押された")
                        punchEnabled = false
                        punchDone = true

                        android.util.Log.d("StaffDetailScreen", "💡 punchLogViewModel = $punchLogViewModel")
                        android.util.Log.d("StaffDetailScreen", "💡 staffId = $staffId")

                        scope.launch {
                            android.util.Log.d("StaffDetailScreen", "📍 scope.launch に入った")
                            try {
                                val type = pendingType ?: run {
                                    android.util.Log.e("StaffDetailScreen", "❗ pendingType が null")
                                    return@launch
                                }

                                val now = LocalDateTime.now()
                                val epochMilli = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                                val punchLog = PunchLog(
                                    staffId = staffId,
                                    timestamp = epochMilli,
                                    type = type.name,
                                    isManual = false
                                )
                                android.util.Log.d("StaffDetailScreen", "出勤ボタンで insert 呼び出し: $punchLog")

                                punchLogViewModel.insert(punchLog)
                            } catch (e: Exception) {
                                android.util.Log.e("StaffDetailScreen", "❌ insert 中にエラー", e)
                            }
                        }



                        showDialog = false
                        punchLogViewModel.updatePendingType(null)

                        scope.launch {
                            delay(150)
                            punchEnabled = true
                            punchDone = false
                            onPunched()
                        }
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    punchLogViewModel.updatePendingType(null)
                }) { Text("キャンセル") }
            },
            title = { Text("確認") },
            text = { Text("${staff.name}さんの${pendingType.displayName()}を記録します。") }
        )
    }
//     🔽 手動打刻ダイアログを表示
    if (manualDialogVisible && manualDialogDate != null && manualDialogType != null) {
        ManualPunchDialogSimple(
            date = manualDialogDate!!,
            punchType = manualDialogType!!,
            onDismiss = { manualDialogVisible = false },
            onSave = { dateTime ->
                val epoch = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val punchLog = PunchLog(
                    staffId = staffId,
                    timestamp = epoch,
                    type = manualDialogType!!.name,
                    isManual = true
                )
                punchLogViewModel.insert(punchLog)
                manualDialogVisible = false
            }
        )
    }




}
// StaffDetailScreen.kt の中、例えば末尾などに以下を追加してください：
data class TodayPunchStatus(
    val hasClockIn: Boolean,
    val hasGoOut: Boolean,
    val hasReturn: Boolean,
    val hasClockOut: Boolean
)

fun getTodayStatus(logs: List<PunchLog>): TodayPunchStatus {
    val today = LocalDate.now()
    val todayLogs = logs.filter {
        val date = Instant.ofEpochMilli(it.timestamp)  // ← ここもミリ秒で変換
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        date == today
    }

    return TodayPunchStatus(
        hasClockIn = todayLogs.any { it.type == PunchType.IN.name },
        hasGoOut = todayLogs.any { it.type == PunchType.BREAK_OUT.name },
        hasReturn = todayLogs.any { it.type == PunchType.BREAK_IN.name },
        hasClockOut = todayLogs.any { it.type == PunchType.OUT.name }
    )
}