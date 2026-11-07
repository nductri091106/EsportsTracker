package com.example.esportstracker.ui.matches

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.esportstracker.data.predictions.PredictionEntity
import com.example.esportstracker.data.reminders.ReminderEntity
import com.example.esportstracker.data.remote.MatchDto
import com.example.esportstracker.data.remote.PlayerDto
import com.example.esportstracker.data.remote.TeamDetailDto
import com.example.esportstracker.data.remote.TeamDto
import com.example.esportstracker.ui.components.*
import com.example.esportstracker.ui.theme.AppColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ====================== Khung chung ======================

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
        containerColor = AppColors.Background,
        topBar = { AppTopBar(title = title, onBack = onBack, actions = actions) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (state) {
                DetailState.Loading -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center),
                    color = AppColors.Primary
                )
                is DetailState.Error -> StateMessage(
                    icon = Icons.Outlined.WifiOff,
                    title = "Không thể tải dữ liệu",
                    message = "Kiểm tra kết nối mạng của bạn rồi thử lại.",
                    iconTint = AppColors.Danger,
                    actionText = "Thử lại",
                    actionIcon = Icons.Filled.Refresh,
                    onAction = onRetry
                )
                is DetailState.Success -> content(state.data)
            }
        }
    }
}

// ====================== Chi tiết trận ======================

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
            AppDialog(
                title = "Xóa dự đoán?",
                onDismiss = { showDelete = false },
                confirmText = "Xóa",
                onConfirm = { vm.deletePrediction(); showDelete = false }
            ) {
                Text("Bạn muốn xóa dự đoán cho trận này?", color = AppColors.TextMuted)
            }
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
            AppDialog(
                title = "Xóa nhắc lịch?",
                onDismiss = { showReminderDelete = false },
                confirmText = "Xóa",
                onConfirm = { vm.deleteReminder(); showReminderDelete = false }
            ) {
                Text("Bạn muốn xóa nhắc lịch cho trận này?", color = AppColors.TextMuted)
            }
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
    val scoreLabel = matchScoreText(m.status, scoreA, scoreB)
    val hasScore = scoreLabel.contains(" - ")
    val finished = m.status == "finished"

    fun loser(team: TeamDto?) =
        finished && team != null && m.winnerId != null && m.winnerId != team.id

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.EmojiEvents,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                matchHeaderLine(m),
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextMuted
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            matchTitle(m),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.height(12.dp))
        when (m.status) {
            "running" -> LiveChip()
            "finished" -> FinishedChip()
            else -> TimeChip(formatTime(m.beginAt))
        }
        Spacer(Modifier.height(24.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamHero(teamA, loser(teamA), onTeamClick)
            Text(
                if (hasScore) "$scoreA - $scoreB" else "VS",
                style = if (hasScore) MaterialTheme.typography.headlineMedium
                else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (hasScore) AppColors.TextPrimary else AppColors.TextMuted
            )
            TeamHero(teamB, loser(teamB), onTeamClick)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            timezoneLabel(),
            style = MaterialTheme.typography.labelMedium,
            color = AppColors.TextMuted
        )
        Spacer(Modifier.height(24.dp))

        PredictionSection(m, prediction, onPredictClick, onDeleteClick)
        Spacer(Modifier.height(12.dp))
        ReminderSection(m, reminder, onReminderClick, onReminderDeleteClick)
        Spacer(Modifier.height(16.dp))
        Text(
            "Lịch thi đấu có thể thay đổi.",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextMuted
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun TeamHero(team: TeamDto?, dimmed: Boolean, onClick: (Long) -> Unit) {
    val label = team?.acronym ?: team?.name ?: "TBD"
    Column(
        Modifier
            .width(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = team != null) { team?.let { onClick(it.id) } }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TeamBadge(label, team?.imageUrl, size = 96.dp, dimmed = dimmed)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            if (team != null) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CardTitle(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextPrimary
        )
    }
}

// ====================== Thẻ Dự đoán ======================

@Composable
fun PredictionSection(
    match: MatchDto,
    prediction: PredictionEntity?,
    onPredictClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val canPredict = match.status == "not_started" && match.opponents.size >= 2

    AppCard {
        CardTitle(Icons.Outlined.Star, "Dự đoán của bạn")
        Spacer(Modifier.height(12.dp))

        if (prediction == null) {
            if (canPredict) {
                Text(
                    "Bạn nghĩ đội nào sẽ giành chiến thắng?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextMuted
                )
                Spacer(Modifier.height(12.dp))
                AppPrimaryButton("Dự đoán trận này", onPredictClick)
            } else {
                Text(
                    if (match.status != "not_started") "Trận đã bắt đầu, không thể dự đoán."
                    else "Chưa xác định đủ 2 đội.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextMuted
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppChip("${prediction.predictedWinnerName} thắng", AppColors.Primary, AppColors.PrimaryTint)
                if (prediction.predictedScore.isNotBlank()) {
                    Spacer(Modifier.width(12.dp))
                    Text(
                        prediction.predictedScore.replace(Regex("""\s*-\s*"""), " - "),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                }
                if (match.status == "finished" && match.winnerId != null) {
                    Spacer(Modifier.width(8.dp))
                    if (match.winnerId == prediction.predictedWinnerId) {
                        AppChip("Đúng", AppColors.Success, AppColors.SuccessTint)
                    } else {
                        AppChip("Sai", AppColors.Danger, AppColors.DangerTint)
                    }
                }
            }
            if (prediction.note.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    prediction.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextMuted
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (canPredict) AppTextAction("Sửa", onPredictClick, Icons.Outlined.Edit)
                AppTextAction("Xóa", onDeleteClick, Icons.Outlined.Delete, AppColors.Danger)
            }
        }
    }
}

// ====================== Thẻ Nhắc lịch ======================

@Composable
fun ReminderSection(
    match: MatchDto,
    reminder: ReminderEntity?,
    onSetClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val canRemind = match.status == "not_started" && match.beginAt != null

    AppCard {
        CardTitle(Icons.Outlined.Notifications, "Nhắc lịch")
        Spacer(Modifier.height(12.dp))

        if (reminder == null) {
            if (canRemind) {
                Text(
                    "Nhận thông báo trước khi trận bắt đầu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextMuted
                )
                Spacer(Modifier.height(12.dp))
                AppOutlinedButton("Nhắc tôi trước giờ đấu", onSetClick)
            } else {
                Text(
                    "Không thể đặt nhắc cho trận này.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextMuted
                )
            }
        } else {
            Text(
                "Nhắc trước ${reminder.minutesBefore} phút" +
                        if (!reminder.enabled) " (đang tắt)" else "",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Thông báo lúc ${formatEpoch(reminder.matchTimeMillis - reminder.minutesBefore * 60_000L)}",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextMuted
            )
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (canRemind) AppTextAction("Sửa", onSetClick, Icons.Outlined.Edit)
                AppTextAction("Xóa", onDeleteClick, Icons.Outlined.Delete, AppColors.Danger)
            }
        }
    }
}

// ====================== Hộp thoại ======================

private val scoreRegex = Regex("""\d+\s*-\s*\d+""")

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = AppColors.Primary,
                unselectedColor = AppColors.TextMuted
            )
        )
        Text(label, style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
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
    var winnerError by remember { mutableStateOf(false) }
    var scoreError by remember { mutableStateOf(false) }

    AppDialog(
        title = "Dự đoán trận này",
        onDismiss = onDismiss,
        confirmText = "Lưu",
        onConfirm = {
            val w = teams.firstOrNull { it.id == winnerId }
            winnerError = w == null
            scoreError = score.isNotBlank() && !scoreRegex.matches(score.trim())
            if (w != null && !scoreError) {
                onSave(w.id, w.acronym ?: w.name, score, note)
            }
        }
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "${matchTitle(match)} · ${match.league?.name ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextMuted
            )
            Text(
                "Đội chiến thắng",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.TextMuted
            )
            teams.forEach { t ->
                RadioRow(label = t.acronym ?: t.name, selected = winnerId == t.id) {
                    winnerId = t.id
                    winnerError = false
                }
            }
            if (winnerError) {
                Text(
                    "Hãy chọn đội thắng",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.Danger
                )
            }
            AppTextField(
                value = score,
                onValueChange = { score = it; scoreError = false },
                label = "Tỉ số dự đoán",
                placeholder = "vd 2-1",
                error = scoreError,
                helper = if (scoreError) "Tỉ số phải có dạng 2-1" else "Nhập tỉ số, vd 2-1"
            )
            AppTextField(
                value = note,
                onValueChange = { note = it },
                label = "Ghi chú",
                placeholder = "Ghi chú (không bắt buộc)",
                singleLine = false,
                minLines = 3
            )
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
    val beginMs = remember(match.beginAt) { match.beginAt?.let { Instant.parse(it).toEpochMilli() } }
    val triggerMs = beginMs?.minus(minutes * 60_000L)
    val tooLate = triggerMs == null || triggerMs <= System.currentTimeMillis()

    AppDialog(
        title = "Nhắc lịch",
        onDismiss = onDismiss,
        confirmText = "Lưu",
        onConfirm = { if (!tooLate) onSave(minutes) }
    ) {
        Text(
            "Nhắc tôi trước giờ ${matchTitle(match)} bắt đầu:",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextMuted
        )
        Spacer(Modifier.height(8.dp))
        listOf(5, 15, 30, 60).forEach { m ->
            RadioRow(label = "$m phút", selected = minutes == m) { minutes = m }
        }
        Spacer(Modifier.height(8.dp))
        if (tooLate) {
            Text(
                "Quá sát giờ đấu, hãy chọn mốc ngắn hơn.",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Danger
            )
        } else if (triggerMs != null) {
            Text(
                "Bạn sẽ được nhắc lúc ${formatEpoch(triggerMs)}.",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Cyan
            )
        }
    }
}

// ====================== Hàm hỗ trợ ======================

fun matchTitle(m: MatchDto): String {
    val a = m.opponents.getOrNull(0)?.opponent
    val b = m.opponents.getOrNull(1)?.opponent
    return if (a != null && b != null) "${a.acronym ?: a.name} — ${b.acronym ?: b.name}" else m.name
}

private fun matchHeaderLine(m: MatchDto): String {
    val parts = mutableListOf<String>()
    m.league?.name?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
    m.tournament?.name?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
    m.numberOfGames?.let { parts.add("BO$it") }
    return parts.joinToString(" · ")
}

private fun formatEpoch(ms: Long): String =
    Instant.ofEpochMilli(ms)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm dd/MM"))

private fun timezoneLabel(): String {
    val offset = ZoneId.systemDefault().rules.getOffset(Instant.now()).totalSeconds
    return if (offset == 7 * 3600) "Giờ Việt Nam (UTC+7)" else "Theo múi giờ của thiết bị"
}

