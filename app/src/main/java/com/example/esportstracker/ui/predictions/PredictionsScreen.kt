package com.example.esportstracker.ui.predictions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esportstracker.data.predictions.PredictionEntity
import com.example.esportstracker.ui.components.*
import com.example.esportstracker.ui.theme.AppColors

private const val PAGE_SIZE = 3

@Composable
fun PredictionsScreen(
    onMatchClick: (Long) -> Unit,
    vm: PredictionsViewModel = viewModel()
) {
    val items by vm.items.collectAsState()
    var deleting by remember { mutableStateOf<PredictionEntity?>(null) }
    var page by remember { mutableIntStateOf(0) }

    val pageCount = maxOf(1, (items.size + PAGE_SIZE - 1) / PAGE_SIZE)
    val currentPage = minOf(page, pageCount - 1)
    val from = currentPage * PAGE_SIZE
    val pageItems = items.drop(from).take(PAGE_SIZE)
    val nextItems = items.drop(from + PAGE_SIZE).take(PAGE_SIZE)

    val correct = items.count { it.outcome == Outcome.CORRECT }
    val wrong = items.count { it.outcome == Outcome.WRONG }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Dự đoán của tôi", actionText = "Làm mới", onAction = { vm.refresh() })

        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (items.isEmpty()) {
                StateMessage(
                    icon = Icons.Outlined.Star,
                    title = "Chưa có dự đoán nào",
                    message = "Mở một trận chưa bắt đầu để dự đoán đội thắng."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { SummaryCard(correct, wrong) }
                    item {
                        SectionHeader(
                            title = if (currentPage == 0) "Dự đoán gần đây" else "Các dự đoán tiếp theo",
                            trailing = "${from + 1}–${from + pageItems.size} / ${items.size}"
                        )
                    }
                    items(pageItems, key = { it.prediction.matchId }) { item ->
                        PredictionCard(
                            item = item,
                            onClick = { onMatchClick(item.prediction.matchId) },
                            onDelete = { deleting = item.prediction }
                        )
                    }
                    if (nextItems.isNotEmpty()) {
                        item {
                            val decided = nextItems.count {
                                it.outcome == Outcome.CORRECT || it.outcome == Outcome.WRONG
                            }
                            val pending = nextItems.size - decided
                            val parts = listOfNotNull(
                                decided.takeIf { it > 0 }?.let { "$it kết quả" },
                                pending.takeIf { it > 0 }?.let { "$it trận sắp tới" }
                            )
                            PageLink(
                                text = "Xem tiếp: " + parts.joinToString(" và "),
                                forward = true
                            ) { page = currentPage + 1 }
                        }
                    } else if (currentPage > 0) {
                        item {
                            PageLink(text = "Về các dự đoán mới nhất", forward = false) { page = 0 }
                        }
                    }
                }
            }
        }
    }

    deleting?.let { p ->
        AppDialog(
            title = "Xóa dự đoán?",
            onDismiss = { deleting = null },
            confirmText = "Xóa",
            onConfirm = { vm.remove(p); deleting = null }
        ) {
            Text("Xóa dự đoán cho trận ${p.matchName}?", color = AppColors.TextMuted)
        }
    }
}

@Composable
private fun SummaryCard(correct: Int, wrong: Int) {
    val total = correct + wrong
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.PrimaryTint,
        border = BorderStroke(1.dp, AppColors.Primary.copy(alpha = 0.35f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                if (total == 0) "Chưa có trận nào có kết quả"
                else "Đúng $correct / $total trận đã có kết quả",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            if (total > 0) {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(correct) { Segment(AppColors.Success, Modifier.weight(1f)) }
                    repeat(wrong) { Segment(AppColors.Danger, Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun Segment(color: Color, modifier: Modifier) {
    Box(modifier.height(4.dp).clip(RoundedCornerShape(2.dp)).background(color))
}

@Composable
private fun PredictionCard(item: PredictionItem, onClick: () -> Unit, onDelete: () -> Unit) {
    val p = item.prediction
    val info = item.info
    val header = listOfNotNull(info?.league ?: p.leagueName, info?.date)
        .filter { it.isNotBlank() }
        .joinToString(" · ")
    val decided = item.outcome == Outcome.CORRECT || item.outcome == Outcome.WRONG
    val rightText = info?.score ?: if (decided) "—" else "VS"

    AppCard(onClick = onClick) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                header,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.TextMuted,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Xóa",
                    tint = AppColors.TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                info?.title ?: p.matchName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                rightText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (info?.score != null) AppColors.TextPrimary else AppColors.Cyan
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                buildString {
                    append("Bạn chọn: ${p.predictedWinnerName}")
                    if (p.predictedScore.isNotBlank()) append(" (${p.predictedScore})")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextMuted
            )
            when (item.outcome) {
                Outcome.CORRECT -> AppChip("Đúng", AppColors.Success, AppColors.SuccessTint)
                Outcome.WRONG -> AppChip("Sai", AppColors.Danger, AppColors.DangerTint)
                else -> AppChip("Chưa có kết quả", AppColors.TextMuted, AppColors.SurfaceHigh)
            }
        }
    }
}

@Composable
private fun PageLink(text: String, forward: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!forward) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.Primary
        )
        if (forward) {
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}