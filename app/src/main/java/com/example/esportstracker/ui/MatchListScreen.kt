package com.example.esportstracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esportstracker.data.matches.Game
import com.example.esportstracker.data.remote.MatchDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.example.esportstracker.data.remote.TeamDto
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.font.FontWeight

@Composable
fun MatchListScreen(
    onMatchClick: (Long) -> Unit,
    vm: MatchListViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val game by vm.game.collectAsState()

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "ESPORTS TRACKER",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
        TabRow(selectedTabIndex = game.ordinal) {
            Game.values().forEach { g ->
                Tab(
                    selected = g == game,
                    onClick = { vm.selectGame(g) },
                    text = { Text(g.label) }
                )
            }
        }

        when (val s = state) {
            UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }

            is UiState.Error -> Column(
                Modifier.fillMaxSize(),
                Arrangement.Center,
                Alignment.CenterHorizontally
            ) {
                Text(s.message)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { vm.load() }) { Text("Thử lại") }
            }

            is UiState.Success -> LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(s.matches, key = { it.id }) { m ->
                    MatchCard(m, onClick = { onMatchClick(m.id) })
                }
            }
        }
    }
}

@Composable
fun MatchCard(m: MatchDto, onClick: () -> Unit) {
    val teamA = m.opponents.getOrNull(0)?.opponent
    val teamB = m.opponents.getOrNull(1)?.opponent
    val scoreA = m.results.firstOrNull { it.teamId == teamA?.id }?.score
    val scoreB = m.results.firstOrNull { it.teamId == teamB?.id }?.score

    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(m.league?.name ?: "", style = MaterialTheme.typography.labelMedium)
                if (m.status == "running") {
                    Text(
                        "● LIVE",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                } else {
                    Text(formatTime(m.beginAt), style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamColumn(teamA)
                Text(
                    if (scoreA != null && scoreB != null) "$scoreA - $scoreB" else "VS",
                    style = MaterialTheme.typography.titleLarge
                )
                TeamColumn(teamB)
            }
        }
    }
}

@Composable
fun TeamColumn(team: TeamDto?) {
    Column(Modifier.width(100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = team?.imageUrl,
            contentDescription = team?.name,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            team?.acronym ?: team?.name ?: "TBD",
            maxLines = 1,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

fun formatTime(iso: String?): String =
    iso?.let {
        Instant.parse(it)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm dd/MM"))
    } ?: "Chưa có lịch"