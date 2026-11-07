package com.example.esportstracker.ui.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esportstracker.data.favorites.FavoriteTeamEntity
import com.example.esportstracker.ui.components.*
import com.example.esportstracker.ui.theme.AppColors

@Composable
fun FavoritesScreen(
    onTeamClick: (Long) -> Unit,
    vm: FavoritesViewModel = viewModel()
) {
    val list by vm.favorites.collectAsState()
    var editing by remember { mutableStateOf<FavoriteTeamEntity?>(null) }
    var deleting by remember { mutableStateOf<FavoriteTeamEntity?>(null) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Đội yêu thích")
        SectionHeader(
            title = "Đội của bạn",
            trailing = "${list.size} đội tuyển",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (list.isEmpty()) {
                StateMessage(
                    icon = Icons.Outlined.FavoriteBorder,
                    title = "Chưa có đội yêu thích",
                    message = "Mở trang đội tuyển và chạm vào trái tim để theo dõi đội bạn yêu thích."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list, key = { it.teamId }) { f ->
                        FavoriteCard(
                            f = f,
                            onClick = { onTeamClick(f.teamId) },
                            onEdit = { editing = f },
                            onDelete = { deleting = f }
                        )
                    }
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
        AppDialog(
            title = "Xóa khỏi yêu thích?",
            onDismiss = { deleting = null },
            confirmText = "Xóa",
            onConfirm = { vm.remove(f); deleting = null }
        ) {
            Text("Bạn muốn xóa ${f.name} khỏi danh sách?", color = AppColors.TextMuted)
        }
    }
}

@Composable
private fun FavoriteCard(
    f: FavoriteTeamEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AppCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TeamBadge(f.name.substringBefore(" ").take(4), f.logoUrl, size = 48.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    f.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                if (f.game.isNotBlank()) {
                    Text(
                        gameLabel(f.game),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextMuted
                    )
                }
            }
        }
        if (f.note.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                f.note,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextMuted,
                maxLines = 3
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Đã yêu thích",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.Primary
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Sửa ghi chú", tint = AppColors.Primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Xóa", tint = AppColors.TextMuted)
                }
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
    val maxLength = 200
    var note by remember(item.teamId) { mutableStateOf(item.note) }

    AppDialog(
        title = "Ghi chú về ${item.name}",
        onDismiss = onDismiss,
        confirmText = "Lưu",
        onConfirm = { onSave(note) }
    ) {
        Text(
            "Lưu điều bạn muốn nhớ về đội tuyển.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextMuted
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = note,
            onValueChange = { if (it.length <= maxLength) note = it },
            label = "Ghi chú",
            singleLine = false,
            minLines = 3
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "${note.length} / $maxLength ký tự",
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.TextMuted
        )
    }
}