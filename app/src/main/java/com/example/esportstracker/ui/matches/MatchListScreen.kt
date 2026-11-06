package com.example.esportstracker.ui.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esportstracker.data.matches.Game
import com.example.esportstracker.data.remote.MatchDto
import com.example.esportstracker.data.remote.TeamDto
import com.example.esportstracker.ui.components.*
import com.example.esportstracker.ui.theme.AppColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MatchListScreen(
    onMatchClick: (Long) -> Unit,
    vm: MatchListViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val game by vm.game.collectAsState()
    val filter by vm.filter.collectAsState()

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Text(
            "ESPORTS TRACKER",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
        )

        GameTabs(selected = game, onSelect = { vm.selectGame(it) })

        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MatchFilter.values().forEach { f ->
                AppFilterChip(text = f.label, selected = f == filter, onClick = { vm.selectFilter(f) })
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (val s = state) {
                UiState.Loading -> MatchListSkeleton()

                is UiState.Error -> StateMessage(
                    icon = Icons.Outlined.WifiOff,
                    title = "Không thể tải trận đấu",
                    message = "Kiểm tra kết nối mạng của bạn rồi thử lại.",
                    iconTint = AppColors.Danger,
                    actionText = "Thử lại",
                    actionIcon = Icons.Filled.Refresh,
                    onAction = { vm.load() }
                )

                is UiState.Success ->
                    if (s.matches.isEmpty()) {
                        StateMessage(
                            icon = Icons.Outlined.EventBusy,
                            title = "Không có trận nào.",
                            message = "Lịch thi đấu sẽ được cập nhật sớm. Hãy quay lại sau nhé."
                        )
                    } else {
                        MatchListContent(s.matches, filter, onMatchClick)
                    }
            }
        }
    }
}

@Composable
private fun GameTabs(selected: Game, onSelect: (Game) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Game.values().forEach { g ->
                val isSelected = g == selected
                Column(
                    Modifier
                        .weight(1f)
                        .clickable { onSelect(g) }
                        .padding(top = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        g.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) AppColors.Primary else AppColors.TextMuted
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier
                            .fillMaxWidth(0.6f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isSelected) AppColors.Primary else Color.Transparent)
                    )
                }
            }
        }
        HorizontalDivider(color = AppColors.Border)
    }
}

@Composable
private fun MatchListContent(
    matches: List<MatchDto>,
    filter: MatchFilter,
    onMatchClick: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = if (filter == MatchFilter.UPCOMING) "Hôm nay & sắp tới" else "Kết quả gần đây",
                trailing = "${matches.size} trận",
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(matches, key = { it.id }) { m ->
            MatchCard(m, onClick = { onMatchClick(m.id) })
        }
        if (filter == MatchFilter.UPCOMING) {
            item { ReminderHint() }
        }
    }
}

@Composable
private fun MatchListSkeleton() {
    Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Đang tải lịch thi đấu...",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextMuted
        )
        repeat(3) {
            AppCard {
                SkeletonBox(Modifier.width(110.dp).height(14.dp))
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonBox(Modifier.size(44.dp), CircleShape)
                    SkeletonBox(Modifier.width(48.dp).height(20.dp))
                    SkeletonBox(Modifier.size(44.dp), CircleShape)
                }
            }
        }
    }
}

@Composable
private fun ReminderHint() {
    Row(Modifier.padding(start = 4.dp, top = 4.dp), verticalAlignment = Alignment.Top) {
        Icon(
            Icons.Outlined.Notifications,
            contentDescription = null,
            tint = AppColors.Cyan,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            "Mở trận đấu để đặt lời nhắc và không bỏ lỡ giờ thi đấu.",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextMuted
        )
    }
}

@Composable
fun MatchCard(m: MatchDto, onClick: () -> Unit) {
    val teamA = m.opponents.getOrNull(0)?.opponent
    val teamB = m.opponents.getOrNull(1)?.opponent
    val scoreA = m.results.firstOrNull { it.teamId == teamA?.id }?.score
    val scoreB = m.results.firstOrNull { it.teamId == teamB?.id }?.score
    val live = m.status == "running"
    val finished = m.status == "finished"
    val hasScore = scoreA != null && scoreB != null

    fun loser(team: TeamDto?) =
        finished && team != null && m.winnerId != null && m.winnerId != team.id

    AppCard(
        onClick = onClick,
        borderColor = if (live) AppColors.Danger else AppColors.Border
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.SurfaceHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.EmojiEvents,
                        contentDescription = null,
                        tint = AppColors.TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    m.league?.name ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            when {
                live -> LiveChip()
                finished -> FinishedChip()
                else -> TimeChip(formatTime(m.beginAt))
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamSide(teamA, loser(teamA), logoFirst = true, modifier = Modifier.weight(1f))
            Text(
                if (hasScore) "$scoreA - $scoreB" else "VS",
                modifier = Modifier.padding(horizontal = 8.dp),
                fontSize = if (hasScore) 24.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (hasScore) AppColors.TextPrimary else AppColors.TextMuted
            )
            TeamSide(teamB, loser(teamB), logoFirst = false, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                matchSubtitle(m, scoreA, scoreB),
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.TextMuted
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AppColors.TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TeamSide(
    team: TeamDto?,
    dimmed: Boolean,
    logoFirst: Boolean,
    modifier: Modifier = Modifier
) {
    val label = team?.acronym ?: team?.name ?: "TBD"
    val name: @Composable () -> Unit = {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (dimmed) AppColors.TextMuted.copy(alpha = 0.5f) else AppColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    Row(
        modifier,
        horizontalArrangement = if (logoFirst) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (logoFirst) {
            TeamBadge(label, team?.imageUrl, size = 44.dp, dimmed = dimmed)
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f, fill = false)) { name() }
        } else {
            Box(Modifier.weight(1f, fill = false)) { name() }
            Spacer(Modifier.width(8.dp))
            TeamBadge(label, team?.imageUrl, size = 44.dp, dimmed = dimmed)
        }
    }
}
fun matchSubtitle(m: MatchDto, scoreA: Int?, scoreB: Int?): String {
    val parts = mutableListOf<String>()
    m.tournament?.name?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
    if (m.status == "running" && scoreA != null && scoreB != null) {
        parts.add("Ván ${scoreA + scoreB + 1}")
    }
    if (m.status == "finished") formatDate(m.beginAt)?.let { parts.add(it) }
    m.numberOfGames?.let { parts.add("BO$it") }
    return parts.joinToString(" · ")
}

fun formatTime(iso: String?): String =
    iso?.let {
        Instant.parse(it)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm dd/MM"))
    } ?: "Chưa có lịch"

fun formatDate(iso: String?): String? =
    iso?.let {
        Instant.parse(it)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("dd/MM"))
    }