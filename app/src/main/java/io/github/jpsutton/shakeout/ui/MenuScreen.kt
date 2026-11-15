package io.github.jpsutton.shakeout.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jpsutton.shakeout.game.Game
import io.github.jpsutton.shakeout.game.Mode

@Composable
fun MenuScreen(vm: GameViewModel, onRules: () -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Shakeout", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        Text("Roll five dice, fill the card. Pick a mode to start.", style = MaterialTheme.typography.bodyLarge)

        vm.saved?.let { saved ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Game in progress", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${saved.mode.title}, turn ${saved.turnsPlayed + 1}, score ${Game.total(saved)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(onClick = vm::resume, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Resume") }
                }
            }
        }

        Mode.entries.forEach { mode ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f).padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(mode.title, style = MaterialTheme.typography.titleLarge)
                            Text(mode.blurb, style = MaterialTheme.typography.bodyMedium)
                        }
                        val best = vm.bests[mode] ?: 0
                        if (best > 0) BestScore(best)
                    }
                    OutlinedButton(
                        onClick = { vm.start(mode) },
                        Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) { Text(if (vm.saved?.mode == mode) "New ${mode.title} game" else "Play ${mode.title}") }
                }
            }
        }

        TextButton(onClick = onRules) { Text("How scoring works") }
    }
}

/** The best score of a mode, large, for the upper-right corner of its card. */
@Composable
private fun BestScore(best: Int) {
    Column(
        Modifier.semantics(mergeDescendants = true) { contentDescription = "Best score: $best" },
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            "BEST",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "$best",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 34.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
