package com.example.esportstracker.ui.matches

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esportstracker.data.remote.PlayerDto
import com.example.esportstracker.data.remote.TeamDetailDto
import com.example.esportstracker.ui.components.*
import com.example.esportstracker.ui.theme.AppColors

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
                        imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Yêu thích",
                        tint = AppColors.Primary
                    )
                }
            }
        }
    ) { t -> TeamDetailContent(t, isFav) }
}

@Composable
fun TeamDetailContent(t: TeamDetailDto, isFavorite: Boolean) {
    if (t.players.isEmpty()) {
        Column(Modifier.fillMaxSize()) {
            TeamHeader(t, isFavorite)
            Box(Modifier.weight(1f).fillMaxWidth()) {
                StateMessage(
                    icon = Icons.Outlined.Groups,
                    title = "Chưa có dữ liệu tuyển thủ",
                    message = "Đội hình sẽ được cập nhật khi có thông tin mới."
                )
            }
        }
    } else {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { TeamHeader(t, isFavorite) }
            items(t.players, key = { it.id }) {
                PlayerRow(it, Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
private fun TeamHeader(t: TeamDetailDto, isFavorite: Boolean) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TeamBadge(t.acronym ?: t.name, t.imageUrl, size = 96.dp)
        Spacer(Modifier.height(12.dp))
        Text(
            t.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        val sub = listOfNotNull(t.acronym, t.game?.name?.let { gameLabel(it) })
            .filter { it.isNotBlank() }
            .joinToString(" · ")
        if (sub.isNotBlank()) {
            Text(sub, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextMuted)
        }
        countryLabel(t.location)?.let {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = AppColors.TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = AppColors.TextMuted)
            }
        }
        if (isFavorite) {
            Spacer(Modifier.height(12.dp))
            AppChip("Đã yêu thích", AppColors.Primary, AppColors.PrimaryTint)
        }
        Spacer(Modifier.height(20.dp))
        SectionHeader("Tuyển thủ", trailing = "${t.players.size} thành viên")
    }
}

@Composable
fun PlayerRow(p: PlayerDto, modifier: Modifier = Modifier) {
    AppCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TeamBadge(p.name.take(1), p.imageUrl, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    p.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                val sub = listOfNotNull(roleLabel(p.role), countryLabel(p.nationality))
                    .joinToString(" · ")
                if (sub.isNotEmpty()) {
                    Text(sub, style = MaterialTheme.typography.bodySmall, color = AppColors.TextMuted)
                }
            }
        }
    }
}