package com.example.esportstracker.ui.predictions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.esportstracker.data.predictions.PredictionEntity
import com.example.esportstracker.data.remote.MatchDto
import com.example.esportstracker.data.remote.TeamDto
import com.example.esportstracker.ui.components.*
import com.example.esportstracker.ui.matches.CardTitle
import com.example.esportstracker.ui.matches.matchTitle
import com.example.esportstracker.ui.theme.AppColors

// ====================== Logic chuỗi ván ======================

data class SeriesResult(
    val winsA: Int,
    val winsB: Int,
    val winnerId: Long?,
    val enabled: Int
)

/** Số ván tối đa của trận (BO3 = 3, BO5 = 5). */
fun gamesIn(match: MatchDto): Int = (match.numberOfGames ?: 1).coerceAtLeast(1)
/** Dự đoán trận BO1 (chỉ có 1 ván) không cần hiện tỉ số hay danh sách ván. */
fun PredictionEntity.isSingleGame(): Boolean =
    gamePicks.split(",").count { it.trim().toLongOrNull() != null } == 1

/** Tính tỉ số và đội thắng từ các lựa chọn từng ván; các ván sau khi đã phân thắng bại bị khóa. */
fun evaluateSeries(picks: List<Long?>, teamA: Long, teamB: Long, total: Int): SeriesResult {
    val need = total / 2 + 1
    var a = 0
    var b = 0
    var enabled = 0
    for (i in 0 until total) {
        if (a >= need || b >= need) break
        enabled = i + 1
        val p = picks.getOrNull(i) ?: break
        if (p == teamA) a++ else if (p == teamB) b++
    }
    val winner = when {
        a >= need -> teamA
        b >= need -> teamB
        else -> null
    }
    return SeriesResult(a, b, winner, enabled)
}

// ====================== Thẻ Dự đoán (màn Chi tiết trận) ======================

@Composable
fun PredictionSection(
    match: MatchDto,
    prediction: PredictionEntity?,
    onPredictClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val canPredict = match.status == "not_started" && match.opponents.size >= 2
    val total = gamesIn(match)

    AppCard {
        CardTitle(Icons.Outlined.Star, "Dự đoán của bạn")
        Spacer(Modifier.height(12.dp))

        if (prediction == null) {
            if (canPredict) {
                Text(
                    if (total > 1) "Chọn đội thắng từng ván của loạt BO$total."
                    else "Bạn nghĩ đội nào sẽ giành chiến thắng?",
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
                AppChip(
                    "${prediction.predictedWinnerName} thắng",
                    AppColors.Primary,
                    AppColors.PrimaryTint
                )
                if (prediction.predictedScore.isNotBlank() && !prediction.isSingleGame()) {
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
            GamePicksList(match, prediction)
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

@Composable
private fun GamePicksList(match: MatchDto, prediction: PredictionEntity) {
    val ids = prediction.gamePicks.split(",").mapNotNull { it.trim().toLongOrNull() }
    if (ids.size <= 1) return
    val teams = match.opponents.map { it.opponent }.associateBy { it.id }
    Spacer(Modifier.height(12.dp))
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ids.forEachIndexed { i, id ->
            val t = teams[id]
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Ván ${i + 1}",
                    modifier = Modifier.width(56.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.TextMuted
                )
                AppChip(
                    t?.acronym ?: t?.name ?: "?",
                    AppColors.Primary,
                    AppColors.PrimaryTint
                )
            }
        }
    }
}

// ====================== Hộp thoại Dự đoán ======================

@Composable
fun PredictionDialog(
    match: MatchDto,
    existing: PredictionEntity?,
    onDismiss: () -> Unit,
    onSave: (winnerId: Long, winnerName: String, score: String, gamePicks: String, note: String) -> Unit
) {
    val teams = match.opponents.map { it.opponent }
    val a = teams.getOrNull(0) ?: return
    val b = teams.getOrNull(1) ?: return
    val total = gamesIn(match)
    val need = total / 2 + 1

    val picks = remember {
        mutableStateListOf<Long?>().apply {
            repeat(total) { add(null) }
            existing?.gamePicks
                ?.split(",")
                ?.mapNotNull { it.trim().toLongOrNull() }
                ?.forEachIndexed { i, id -> if (i < total) this[i] = id }
        }
    }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var showError by remember { mutableStateOf(false) }
    val result = evaluateSeries(picks, a.id, b.id, total)

    AppDialog(
        title = "Dự đoán trận này",
        onDismiss = onDismiss,
        confirmText = "Lưu",
        onConfirm = {
            val w = result.winnerId
            if (w == null) {
                showError = true
            } else {
                val winnerTeam = if (w == a.id) a else b
                val played = picks.take(result.winsA + result.winsB).filterNotNull()
                onSave(
                    w,
                    winnerTeam.acronym ?: winnerTeam.name,
                    "${result.winsA}-${result.winsB}",
                    played.joinToString(","),
                    note
                )
            }
        }
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "${matchTitle(match)} · ${match.league?.name ?: ""} · BO$total",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextMuted
            )
            Text(
                if (total == 1) "Chọn đội thắng trận."
                else "Chọn đội thắng từng ván. Cần thắng $need ván để thắng trận.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextMuted
            )

            if (total == 1) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PickButton(
                        a, picks[0] == a.id, true,
                        { picks[0] = a.id; showError = false },
                        Modifier.weight(1f)
                    )
                    PickButton(
                        b, picks[0] == b.id, true,
                        { picks[0] = b.id; showError = false },
                        Modifier.weight(1f)
                    )
                }
            } else {
                repeat(total) { i ->
                    GameRow(
                        index = i,
                        a = a,
                        b = b,
                        picked = picks[i],
                        enabled = i < result.enabled,
                        onPick = { id ->
                            picks[i] = id
                            showError = false
                        }
                    )
                }
            }

            SeriesPreview(a, b, result, total, showError)

            AppTextField(
                value = note,
                onValueChange = { note = it },
                label = "Ghi chú",
                placeholder = "Ghi chú (không bắt buộc)",
                singleLine = false,
                minLines = 2
            )
        }
    }
}

@Composable
private fun GameRow(
    index: Int,
    a: TeamDto,
    b: TeamDto,
    picked: Long?,
    enabled: Boolean,
    onPick: (Long) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Ván ${index + 1}",
            modifier = Modifier.width(52.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) AppColors.TextPrimary else AppColors.TextMuted
        )
        PickButton(a, picked == a.id && enabled, enabled, { onPick(a.id) }, Modifier.weight(1f))
        PickButton(b, picked == b.id && enabled, enabled, { onPick(b.id) }, Modifier.weight(1f))
    }
}

@Composable
private fun PickButton(
    team: TeamDto,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) AppColors.PrimaryTint else AppColors.Surface,
        border = BorderStroke(1.dp, if (selected) AppColors.Primary else AppColors.Border)
    ) {
        Box(
            Modifier.fillMaxSize().alpha(if (enabled) 1f else 0.4f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                team.acronym ?: team.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) AppColors.Primary else AppColors.TextPrimary
            )
        }
    }
}

@Composable
private fun SeriesPreview(
    a: TeamDto,
    b: TeamDto,
    result: SeriesResult,
    total: Int,
    showError: Boolean
) {
    val winner = when (result.winnerId) {
        a.id -> a
        b.id -> b
        else -> null
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = AppColors.Surface,
        border = BorderStroke(1.dp, AppColors.Border)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "Kết quả dự đoán",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.TextMuted
            )
            Spacer(Modifier.height(6.dp))
            if (winner != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppChip(
                        "${winner.acronym ?: winner.name} thắng",
                        AppColors.Primary,
                        AppColors.PrimaryTint
                    )
                    if (total > 1) {
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "${result.winsA} - ${result.winsB}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    }
                }
                if (result.enabled < total) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Các ván còn lại không cần thi đấu.",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextMuted
                    )
                }
            } else {
                Text(
                    when {
                        showError && total == 1 -> "Hãy chọn đội thắng trận."
                        showError -> "Hãy chọn đủ các ván để xác định đội thắng."
                        total == 1 -> "Chọn đội thắng trận."
                        else -> "Chọn kết quả từng ván để xác định đội thắng."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (showError) AppColors.Danger else AppColors.TextMuted
                )
            }
        }
    }
}