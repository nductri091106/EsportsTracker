package com.example.esportstracker.ui.reminders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esportstracker.data.reminders.ReminderEntity
import com.example.esportstracker.ui.components.*
import com.example.esportstracker.ui.theme.AppColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun formatMillis(ms: Long): String =
    Instant.ofEpochMilli(ms)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm dd/MM"))

private fun triggerAt(r: ReminderEntity, minutes: Int = r.minutesBefore): Long =
    r.matchTimeMillis - minutes * 60_000L

@Composable
fun RemindersScreen(
    onMatchClick: (Long) -> Unit,
    vm: RemindersViewModel = viewModel()
) {
    val list by vm.reminders.collectAsState()
    var editing by remember { mutableStateOf<ReminderEntity?>(null) }
    var deleting by remember { mutableStateOf<ReminderEntity?>(null) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Nhắc lịch", actionText = "Gửi thử", onAction = { vm.sendTest() })

        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (list.isEmpty()) {
                StateMessage(
                    icon = Icons.Outlined.Notifications,
                    title = "Chưa có nhắc lịch nào",
                    message = "Mở một trận chưa bắt đầu để đặt lời nhắc."
                )
            } else {
                val enabledCount = list.count { it.enabled && triggerAt(it) > System.currentTimeMillis() }
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        SectionHeader(
                            title = "Lời nhắc của bạn",
                            trailing = "$enabledCount đang bật",
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(list, key = { it.id }) { r ->
                        ReminderCard(
                            r = r,
                            onClick = { onMatchClick(r.matchId) },
                            onToggle = { vm.setEnabled(r, it) },
                            onEdit = { editing = r },
                            onDelete = { deleting = r }
                        )
                    }
                    item { NotificationHint() }
                }
            }
        }
    }

    editing?.let { r ->
        MinutesDialog(
            reminder = r,
            onDismiss = { editing = null },
            onSave = { vm.setMinutes(r, it); editing = null }
        )
    }

    deleting?.let { r ->
        AppDialog(
            title = "Xóa nhắc lịch?",
            onDismiss = { deleting = null },
            confirmText = "Xóa",
            onConfirm = { vm.remove(r); deleting = null }
        ) {
            Text("Xóa nhắc cho trận ${r.title}?", color = AppColors.TextMuted)
        }
    }
}

@Composable
private fun ReminderCard(
    r: ReminderEntity,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val passed = triggerAt(r) <= System.currentTimeMillis()
    val active = r.enabled && !passed

    AppCard(onClick = onClick) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    r.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Giờ đấu: ${formatMillis(r.matchTimeMillis)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (active) AppColors.Cyan else AppColors.TextMuted
                )
            }
            Switch(
                checked = r.enabled && !passed,
                onCheckedChange = onToggle,
                enabled = !passed,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AppColors.Primary,
                    uncheckedThumbColor = AppColors.TextMuted,
                    uncheckedTrackColor = AppColors.SurfaceHigh,
                    uncheckedBorderColor = AppColors.Border
                )
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Nhắc trước ${r.minutesBefore} phút",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextPrimary
                )
                Text(
                    when {
                        passed -> "Đã qua"
                        r.enabled -> "Đang bật"
                        else -> "Đã tắt"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextMuted
                )
            }
            Row {
                IconButton(onClick = onEdit, enabled = !passed) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = "Sửa",
                        tint = if (passed) AppColors.TextMuted else AppColors.Primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Xóa", tint = AppColors.TextMuted)
                }
            }
        }
    }
}

@Composable
private fun NotificationHint() {
    Row(Modifier.padding(start = 4.dp, top = 4.dp), verticalAlignment = Alignment.Top) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = null,
            tint = AppColors.TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            "Cho phép thông báo trên thiết bị để nhận lời nhắc đúng giờ.",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextMuted
        )
    }
}

@Composable
private fun MinutesDialog(
    reminder: ReminderEntity,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var minutes by remember(reminder.id) { mutableStateOf(reminder.minutesBefore) }
    val trigger = triggerAt(reminder, minutes)
    val tooLate = trigger <= System.currentTimeMillis()

    AppDialog(
        title = "Đổi thời gian nhắc",
        onDismiss = onDismiss,
        confirmText = "Lưu",
        onConfirm = { if (!tooLate) onSave(minutes) }
    ) {
        Text(
            "Nhắc tôi trước giờ ${reminder.title} bắt đầu:",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextMuted
        )
        Spacer(Modifier.height(8.dp))
        listOf(5, 15, 30, 60).forEach { m ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { minutes = m },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = minutes == m,
                    onClick = { minutes = m },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = AppColors.Primary,
                        unselectedColor = AppColors.TextMuted
                    )
                )
                Text("$m phút", style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
            }
        }
        Spacer(Modifier.height(8.dp))
        if (tooLate) {
            Text(
                "Quá sát giờ đấu, hãy chọn mốc ngắn hơn.",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Danger
            )
        } else {
            Text(
                "Bạn sẽ được nhắc lúc ${formatMillis(trigger)}.",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Cyan
            )
        }
    }
}