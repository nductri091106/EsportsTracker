package com.example.esportstracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.esportstracker.data.remote.MatchDto
import com.example.esportstracker.data.remote.PlayerDto
import com.example.esportstracker.data.remote.TeamDetailDto
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.esportstracker.data.predictions.PredictionEntity
import com.example.esportstracker.data.reminders.ReminderEntity
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DetailScaffold(
    title: String,
    onBack: () -> Unit,
    state: DetailState<T>,
    onRetry: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (T) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = actions
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (state) {
                DetailState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is DetailState.Error -> Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.message)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onRetry) { Text("Thử lại") }
                }
                is DetailState.Success -> content(state.data)
            }
        }
    }
}

// ---------- Chi tiết trận ----------

@Composable
fun MatchDetailScreen(
    onBack: () -> Unit,
    onTeamClick: (Long) -> Unit,
    vm: MatchDetailViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val prediction by vm.prediction.collectAsState()
    val reminder by vm.reminder.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var showReminderForm by remember { mutableStateOf(false) }
    var showReminderDelete by remember { mutableStateOf(false) }

    DetailScaffold(
        title = "Chi tiết trận",
        onBack = onBack,
        state = state,
        onRetry = { vm.load() }
    ) { m ->
        MatchDetailContent(
            m = m,
            prediction = prediction,
            reminder = reminder,
            onTeamClick = onTeamClick,
            onPredictClick = { showForm = true },
            onDeleteClick = { showDelete = true },
            onReminderClick = { showReminderForm = true },
            onReminderDeleteClick = { showReminderDelete = true }
        )

        if (showForm) {
            PredictionDialog(
                match = m,
                existing = prediction,
                onDismiss = { showForm = false },
                onSave = { winnerId, winnerName, score, note ->
                    vm.savePrediction(m, winnerId, winnerName, score, note)
                    showForm = false
                }
            )
        }

        if (showDelete) {
            AlertDialog(
                onDismissRequest = { showDelete = false },
                title = { Text("Xóa dự đoán?") },
                text = { Text("Bạn muốn xóa dự đoán cho trận này?") },
                confirmButton = {
                    TextButton(onClick = { vm.deletePrediction(); showDelete = false }) {
                        Text("Xóa")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDelete = false }) { Text("Hủy") }
                }
            )
        }

        if (showReminderForm) {
            ReminderDialog(
                match = m,
                existing = reminder,
                onDismiss = { showReminderForm = false },
                onSave = { minutes ->
                    vm.saveReminder(m, minutes)
                    showReminderForm = false
                }
            )
        }

        if (showReminderDelete) {
            AlertDialog(
                onDismissRequest = { showReminderDelete = false },
                title = { Text("Xóa nhắc lịch?") },
                text = { Text("Bạn muốn xóa nhắc lịch cho trận này?") },
                confirmButton = {
                    TextButton(onClick = { vm.deleteReminder(); showReminderDelete = false }) {
                        Text("Xóa")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReminderDelete = false }) { Text("Hủy") }
                }
            )
        }
    }
}

@Composable
fun MatchDetailContent(
    m: MatchDto,
    prediction: PredictionEntity?,
    reminder: ReminderEntity?,
    onTeamClick: (Long) -> Unit,
    onPredictClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onReminderClick: () -> Unit,
    onReminderDeleteClick: () -> Unit
) {
    val teamA = m.opponents.getOrNull(0)?.opponent
    val teamB = m.opponents.getOrNull(1)?.opponent
    val scoreA = m.results.firstOrNull { it.teamId == teamA?.id }?.score
    val scoreB = m.results.firstOrNull { it.teamId == teamB?.id }?.score

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(m.league?.name ?: "", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(m.name, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            if (m.status == "running") "● LIVE" else formatTime(m.beginAt),
            style = MaterialTheme.typography.labelLarge
        )
        Text("Trạng thái: ${statusLabel(m.status)}", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(32.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.clickable(enabled = teamA != null) { teamA?.let { onTeamClick(it.id) } }) {
                TeamColumn(teamA)
            }
            Text(
                if (scoreA != null && scoreB != null) "$scoreA - $scoreB" else "VS",
                style = MaterialTheme.typography.headlineMedium
            )
            Box(Modifier.clickable(enabled = teamB != null) { teamB?.let { onTeamClick(it.id) } }) {
                TeamColumn(teamB)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Bấm vào đội để xem chi tiết", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(24.dp))

        PredictionSection(m, prediction, onPredictClick, onDeleteClick)
        Spacer(Modifier.height(12.dp))
        ReminderSection(m, reminder, onReminderClick, onReminderDeleteClick)
    }
}

fun statusLabel(status: String): String = when (status) {
    "not_started" -> "Chưa bắt đầu"
    "running" -> "Đang diễn ra"
    "finished" -> "Đã kết thúc"
    else -> status
}

// ---------- Chi tiết đội ----------

@Composable
fun TeamDetailScreen(
    onBack: () -> Unit,
    vm: TeamDetailViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val isFav by vm.isFavorite.collectAsState()

    DetailScaffold(
        title = "Đội tuyển",
        onBack = onBack,
        state = state,
        onRetry = { vm.load() },
        actions = {
            val s = state
            if (s is DetailState.Success) {
                IconButton(onClick = { vm.toggleFavorite(s.data) }) {
                    Icon(
                        imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Yêu thích",
                        tint = if (isFav) MaterialTheme.colorScheme.error
                        else LocalContentColor.current
                    )
                }
            }
        }
    ) { t -> TeamDetailContent(t) }
}

@Composable
fun TeamDetailContent(t: TeamDetailDto) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = t.imageUrl,
                    contentDescription = t.name,
                    modifier = Modifier.size(96.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(t.name, style = MaterialTheme.typography.headlineSmall)
                t.acronym?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                t.location?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                Spacer(Modifier.height(16.dp))
                Text("Tuyển thủ", style = MaterialTheme.typography.titleMedium)
            }
        }
        if (t.players.isEmpty()) {
            item { Text("Chưa có dữ liệu tuyển thủ", style = MaterialTheme.typography.bodyMedium) }
        } else {
            items(t.players, key = { it.id }) { PlayerRow(it) }
        }
    }
}

@Composable
fun PlayerRow(p: PlayerDto) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = p.imageUrl,
                contentDescription = p.name,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(p.name, style = MaterialTheme.typography.titleSmall)
                val sub = listOfNotNull(p.role, p.nationality).joinToString(" • ")
                if (sub.isNotEmpty()) Text(sub, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun PredictionSection(
    match: MatchDto,
    prediction: PredictionEntity?,
    onPredictClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val canPredict = match.status == "not_started" && match.opponents.size >= 2

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Dự đoán của bạn", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (prediction == null) {
                if (canPredict) {
                    Button(onClick = onPredictClick) { Text("Dự đoán trận này") }
                } else {
                    Text(
                        if (match.status != "not_started") "Trận đã bắt đầu, không thể dự đoán."
                        else "Chưa xác định đủ 2 đội.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Text("Đội thắng: ${prediction.predictedWinnerName}", style = MaterialTheme.typography.bodyLarge)
                if (prediction.predictedScore.isNotBlank()) {
                    Text("Tỉ số: ${prediction.predictedScore}", style = MaterialTheme.typography.bodyMedium)
                }
                if (prediction.note.isNotBlank()) {
                    Text("Ghi chú: ${prediction.note}", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canPredict) {
                        OutlinedButton(onClick = onPredictClick) { Text("Sửa") }
                    }
                    OutlinedButton(onClick = onDeleteClick) { Text("Xóa") }
                }
            }
        }
    }
}

@Composable
fun PredictionDialog(
    match: MatchDto,
    existing: PredictionEntity?,
    onDismiss: () -> Unit,
    onSave: (winnerId: Long, winnerName: String, score: String, note: String) -> Unit
) {
    val teams = match.opponents.map { it.opponent }
    var winnerId by remember { mutableStateOf(existing?.predictedWinnerId) }
    var score by remember { mutableStateOf(existing?.predictedScore ?: "") }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Dự đoán trận" else "Sửa dự đoán") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Đội thắng", style = MaterialTheme.typography.labelLarge)
                teams.forEach { t ->
                    Row(
                        Modifier.fillMaxWidth().clickable { winnerId = t.id },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = winnerId == t.id, onClick = { winnerId = t.id })
                        Text(t.name)
                    }
                }
                OutlinedTextField(
                    value = score,
                    onValueChange = { score = it },
                    label = { Text("Tỉ số (vd 2-1, không bắt buộc)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú (không bắt buộc)") }
                )
                errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val w = teams.firstOrNull { it.id == winnerId }
                when {
                    w == null -> errorMsg = "Hãy chọn đội thắng"
                    score.isNotBlank() && !Regex("""\d+\s*-\s*\d+""").matches(score.trim()) ->
                        errorMsg = "Tỉ số phải có dạng 2-1"
                    else -> onSave(w.id, w.name, score, note)
                }
            }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}
@Composable
fun ReminderSection(
    match: MatchDto,
    reminder: ReminderEntity?,
    onSetClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val canRemind = match.status == "not_started" && match.beginAt != null

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Nhắc lịch", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (reminder == null) {
                if (canRemind) {
                    Button(onClick = onSetClick) { Text("Nhắc tôi trước giờ đấu") }
                } else {
                    Text("Không thể đặt nhắc cho trận này.", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Text(
                    "Nhắc trước ${reminder.minutesBefore} phút" +
                            if (!reminder.enabled) " (đang tắt)" else "",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canRemind) {
                        OutlinedButton(onClick = onSetClick) { Text("Sửa") }
                    }
                    OutlinedButton(onClick = onDeleteClick) { Text("Xóa") }
                }
            }
        }
    }
}

@Composable
fun ReminderDialog(
    match: MatchDto,
    existing: ReminderEntity?,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var minutes by remember { mutableStateOf(existing?.minutesBefore ?: 15) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Đặt nhắc lịch" else "Sửa nhắc lịch") },
        text = {
            Column {
                Text("Nhắc trước giờ đấu:", style = MaterialTheme.typography.labelLarge)
                MinutesPicker(minutes) { minutes = it; errorMsg = null }
                errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val begin = match.beginAt?.let { Instant.parse(it).toEpochMilli() }
                if (begin == null || begin - minutes * 60_000L <= System.currentTimeMillis()) {
                    errorMsg = "Quá sát giờ đấu, hãy chọn mốc ngắn hơn"
                } else {
                    onSave(minutes)
                }
            }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}