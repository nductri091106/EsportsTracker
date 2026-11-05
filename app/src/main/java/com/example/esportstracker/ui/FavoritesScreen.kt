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
import coil.compose.AsyncImage
import com.example.esportstracker.data.favorites.FavoriteTeamEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBack: () -> Unit,
    onTeamClick: (Long) -> Unit,
    vm: FavoritesViewModel = viewModel()
) {
    val list by vm.favorites.collectAsState()
    var editing by remember { mutableStateOf<FavoriteTeamEntity?>(null) }
    var deleting by remember { mutableStateOf<FavoriteTeamEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đội yêu thích") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        if (list.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), Alignment.Center) {
                Text("Chưa có đội yêu thích.\nVào trang đội và bấm ♥ để thêm.")
            }
        } else {
            LazyColumn(
                Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(list, key = { it.teamId }) { f ->
                    FavoriteCard(
                        f,
                        onClick = { onTeamClick(f.teamId) },
                        onEdit = { editing = f },
                        onDelete = { deleting = f }
                    )
                }
            }
        }
    }

    editing?.let { f ->
        NoteDialog(
            item = f,
            onDismiss = { editing = null },
            onSave = { note -> vm.updateNote(f, note); editing = null }
        )
    }

    deleting?.let { f ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Xóa khỏi yêu thích?") },
            text = { Text("Bạn muốn xóa ${f.name} khỏi danh sách?") },
            confirmButton = {
                TextButton(onClick = { vm.remove(f); deleting = null }) { Text("Xóa") }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text("Hủy") }
            }
        )
    }
}

@Composable
private fun FavoriteCard(
    f: FavoriteTeamEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = f.logoUrl,
                contentDescription = f.name,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(f.name, style = MaterialTheme.typography.titleMedium)
                if (f.game.isNotBlank()) {
                    Text(f.game, style = MaterialTheme.typography.bodySmall)
                }
                if (f.note.isNotBlank()) {
                    Text("Ghi chú: ${f.note}", style = MaterialTheme.typography.bodySmall, maxLines = 2)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Sửa ghi chú")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Xóa")
            }
        }
    }
}

@Composable
private fun NoteDialog(
    item: FavoriteTeamEntity,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var note by remember(item.teamId) { mutableStateOf(item.note) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ghi chú về ${item.name}") },
        text = {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Ghi chú") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { TextButton(onClick = { onSave(note) }) { Text("Lưu") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}