package com.example.esportstracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esportstracker.data.ReminderEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatMillis(ms: Long): String =
    Instant.ofEpochMilli(ms)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm dd/MM"))

@Composable
fun MinutesPicker(selected: Int, onSelect: (Int) -> Unit) {
    Column {
        listOf(5, 15, 30, 60).forEach { m ->
            Row(
                Modifier.fillMaxWidth().clickable { onSelect(m) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selected == m, onClick = { onSelect(m) })
                Text("$m phút")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onBack: () -> Unit,
    onMatchClick: (Long) -> Unit,
    vm: RemindersViewModel = viewModel()
) {
    val list by vm.reminders.collectAsState()
    var editing by remember { mutableStateOf<ReminderEntity?>(null) }
    var deleting by remember { mutableStateOf<ReminderEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nhắc lịch") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = { TextButton(onClick = { vm.sendTest() }) { Text("Gửi thử") } }
            )
        }
    ) { padding ->
        if (list.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), Alignment.Center) {
                Text("Chưa có nhắc lịch nào.\nMở một trận chưa bắt đầu để đặt nhắc.")
            }
        } else {
            LazyColumn(
                Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(list, key = { it.id }) { r ->
                    ReminderCard(
                        r = r,
                        onClick = { onMatchClick(r.matchId) },
                        onToggle = { vm.setEnabled(r, it) },
                        onEdit = { editing = r },
                        onDelete = { deleting = r }
                    )
                }
            }
        }
    }

    editing?.let { r ->
        MinutesDialog(
            initial = r.minutesBefore,
            onDismiss = { editing = null },
            onSave = { vm.setMinutes(r, it); editing = null }
        )
    }

    deleting?.let { r ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Xóa nhắc lịch?") },
            text = { Text("Xóa nhắc cho trận ${r.title}?") },
            confirmButton = {
                TextButton(onClick = { vm.remove(r); deleting = null }) { Text("Xóa") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Hủy") } }
        )
    }
}

@Composable
private fun MinutesDialog(initial: Int, onDismiss: () -> Unit, onSave: (Int) -> Unit) {
    var minutes by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đổi thời gian nhắc") },
        text = { MinutesPicker(minutes) { minutes = it } },
        confirmButton = { TextButton(onClick = { onSave(minutes) }) { Text("Lưu") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
private fun ReminderCard(
    r: ReminderEntity,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val passed = r.matchTimeMillis - r.minutesBefore * 60_000L <= System.currentTimeMillis()

    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(r.title, style = MaterialTheme.typography.titleMedium)
                Text("Giờ đấu: ${formatMillis(r.matchTimeMillis)}", style = MaterialTheme.typography.bodySmall)
                Text(
                    if (passed) "Đã qua" else "Nhắc trước ${r.minutesBefore} phút",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(checked = r.enabled, onCheckedChange = onToggle, enabled = !passed)
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Sửa") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Xóa") }
        }
    }
}