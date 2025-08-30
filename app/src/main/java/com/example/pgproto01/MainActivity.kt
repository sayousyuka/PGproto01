package com.example.attendance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
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

data class Staff(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

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
@Composable
fun MonthlyAttendanceTable(dailyRecords: List<DailyAttendance>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                listOf("日付", "出勤", "外出", "戻り", "退勤", "コメント").forEach {
                    Text(it, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                }
            }
        }

        items(dailyRecords) { day ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(day.date.toString(), Modifier.weight(1f))
                Text(day.clockIn?.toString() ?: "-", Modifier.weight(1f))
                Text(day.breakOut?.toString() ?: "-", Modifier.weight(1f))
                Text(day.breakIn?.toString() ?: "-", Modifier.weight(1f))
                Text(day.clockOut?.toString() ?: "-", Modifier.weight(1f))
                Text(day.comment, Modifier.weight(1f))
            }
        }
    }
}

enum class PunchType {
    IN,          // 出勤
    BREAK_OUT,   // 外出
    BREAK_IN,    // 戻り
    OUT          // 退勤
}
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

object InMemoryRepository {
    val staffList = mutableStateListOf(
        Staff(name = "山田 太郎"),
        Staff(name = "佐藤 花子"),
        Staff(name = "鈴木 次郎"),
        Staff(name = "田中 三郎"),
        Staff(name = "中村 四季"),
        Staff(name = "高橋 桜"),
    )

    private val records = mutableStateMapOf<String, SnapshotStateList<AttendanceRecord>>()

    fun getRecords(staffId: String): SnapshotStateList<AttendanceRecord> {
        return records.getOrPut(staffId) { mutableStateListOf() }
    }

    fun addRecord(staffId: String, record: AttendanceRecord) {
        getRecords(staffId).add(0, record)
    }

    fun findStaff(staffId: String): Staff? = staffList.find { it.id == staffId }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AttendanceApp() }
    }
}

@Composable
fun AttendanceApp() {
    val navController = rememberNavController()
    MaterialTheme {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    staff = InMemoryRepository.staffList,
                    onStaffClick = { staffId ->
                        navController.navigate("detail/$staffId")
                    }
                )
            }
            composable(
                route = "detail/{staffId}",
                arguments = listOf(navArgument("staffId") { type = NavType.StringType })
            ) { backStackEntry ->
                val staffId = backStackEntry.arguments?.getString("staffId")!!
                StaffDetailScreen(
                    staffId = staffId,
                    onBack = { navController.popBackStack() },
                    onPunched = {
                        navController.popBackStack(route = "home", inclusive = false)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    staff: List<Staff>,
    onStaffClick: (String) -> Unit
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

    Scaffold { inner ->
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
    staffId: String,
    onBack: () -> Unit,
    onPunched: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var punchDone by remember { mutableStateOf(false) }
    val staff = remember(staffId) { InMemoryRepository.findStaff(staffId) }
    val records = remember(staffId) { InMemoryRepository.getRecords(staffId) }
    var punchEnabled by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var pendingType by remember { mutableStateOf<PunchType?>(null) }

    fun requestPunch(type: PunchType) {
        if (!punchEnabled) return
        pendingType = type
        showDialog = true
    }

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
                    enabled = punchEnabled,
                    onClick = { requestPunch(PunchType.IN) }
                ) { Text("出勤") }

                Button(
                    modifier = Modifier.weight(1f),
                    enabled = punchEnabled,
                    onClick = { requestPunch(PunchType.BREAK_OUT) }
                ) { Text("外出") }
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
                    enabled = punchEnabled,
                    onClick = { requestPunch(PunchType.BREAK_IN) }
                ) { Text("戻り") }

                Button(
                    modifier = Modifier.weight(1f),
                    enabled = punchEnabled,
                    onClick = { requestPunch(PunchType.OUT) }
                ) { Text("退勤") }
            }


            Spacer(Modifier.height(12.dp))

            Text(
                "勤怠履歴",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.SemiBold
            )

            val dailyRecords = remember(records) { convertRecordsToDaily(records) }

            MonthlyAttendanceTable(dailyRecords = dailyRecords)

        }
    }

    if (showDialog && staff != null && pendingType != null) {
        val label = if (pendingType == PunchType.IN) "出勤" else "退勤"
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        punchEnabled = false
                        punchDone = true
                        InMemoryRepository.addRecord(
                            staffId,
                            AttendanceRecord(
                                timestamp = LocalDateTime.now(),
                                type = pendingType!!
                            )
                        )
                        showDialog = false
                        pendingType = null

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
                    pendingType = null
                }) { Text("キャンセル") }
            },
            title = { Text("確認") },
            text = { Text("${staff.name}さんの${label}を記録します。") }
        )
    }
}
