package com.example.esportstracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esportstracker.data.PredictionEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionsScreen(
    onBack: () -> Unit,
    onMatchClick: (Long) -> Unit,
    vm: PredictionsViewModel = viewModel()
) {
    val items by vm.items.collectAsState()
    var deleting by remember { mutableStateOf<PredictionEntity?>(null) }

    val decided = items.filter { it.outcome == Outcome.CORRECT || it.outcome == Outcome.WRONG }
    val correct = decided.count { it.outcome == Outcome.CORRECT }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dự đoán của tôi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = { TextButton(onClick = { vm.refresh() }) { Text("Làm mới") } }
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), Alignment.Center) {
                Text("Chưa có dự đoán nào.\nMở một trận chưa bắt đầu để dự đoán.")
            }
        } else {
            LazyColumn(
                Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Đúng $correct / ${decided.size} trận đã có kết quả",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(items, key = { it.prediction.matchId }) { item ->
                    PredictionCard(
                        item = item,
                        onClick = { onMatchClick(item.prediction.matchId) },
                        onDelete = { deleting = item.prediction }
                    )
                }
            }
        }
    }

    deleting?.let { p ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Xóa dự đoán?") },
            text = { Text("Xóa dự đoán cho trận ${p.matchName}?") },
            confirmButton = {
                TextButton(onClick = { vm.remove(p); deleting = null }) { Text("Xóa") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Hủy") } }
        )
    }
}

@Composable
private fun PredictionCard(item: PredictionItem, onClick: () -> Unit, onDelete: () -> Unit) {
    val p = item.prediction
    val (label, color) = when (item.outcome) {
        Outcome.CORRECT -> "✔ Đúng" to MaterialTheme.colorScheme.primary
        Outcome.WRONG -> "✘ Sai" to MaterialTheme.colorScheme.error
        Outcome.PENDING -> "Chưa có kết quả" to MaterialTheme.colorScheme.onSurfaceVariant
        Outcome.CHECKING -> "Đang kiểm tra..." to MaterialTheme.colorScheme.onSurfaceVariant
        Outcome.UNKNOWN -> "Không xác định" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(p.leagueName, style = MaterialTheme.typography.labelMedium)
                Text(p.matchName, style = MaterialTheme.typography.titleMedium)
                Text(
                    buildString {
                        append("Bạn chọn: ${p.predictedWinnerName}")
                        if (p.predictedScore.isNotBlank()) append(" (${p.predictedScore})")
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                if (p.note.isNotBlank()) {
                    Text("Ghi chú: ${p.note}", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(4.dp))
                Text(label, color = color, style = MaterialTheme.typography.labelLarge)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Xóa")
            }
        }
    }
}