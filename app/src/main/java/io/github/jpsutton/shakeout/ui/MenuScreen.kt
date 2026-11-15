package io.github.jpsutton.shakeout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jpsutton.shakeout.game.Game
import io.github.jpsutton.shakeout.game.GameState
import io.github.jpsutton.shakeout.game.Mode

@Composable
fun MenuScreen(vm: GameViewModel, onRules: () -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Shakeout", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        Text("Roll five dice, fill the card. Pick a mode to start.", style = MaterialTheme.typography.bodyLarge)

        vm.saved?.let { saved -> ResumeTile(saved, onResume = vm::resume, onAbandon = vm::abandonSaved) }

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

/**
 * The unfinished game. Swipe it either way to abandon it; the swipe only asks, and the game is discarded once the
 * player confirms (cancelling snaps the tile back). Screen readers get the same through a custom action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResumeTile(saved: GameState, onResume: () -> Unit, onAbandon: () -> Unit) {
    var confirming by rememberSaveable { mutableStateOf(false) }
    val swipe = rememberSwipeToDismissBoxState()
    // A finished swipe only asks: open the dialog and slide the tile back; the dialog decides.
    LaunchedEffect(swipe.currentValue) {
        if (swipe.currentValue != SwipeToDismissBoxValue.Settled) {
            confirming = true
            swipe.reset()
        }
    }
    val summary = "${saved.mode.title}, turn ${saved.turnsPlayed + 1}, score ${Game.total(saved)}"

    SwipeToDismissBox(
        state = swipe,
        modifier = Modifier.semantics {
            customActions = listOf(CustomAccessibilityAction("Abandon game") { confirming = true; true })
        },
        backgroundContent = {
            val direction = swipe.dismissDirection
            if (direction != SwipeToDismissBoxValue.Settled) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium)
                        .padding(horizontal = 20.dp),
                    contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) {
                        Alignment.CenterStart
                    } else {
                        Alignment.CenterEnd
                    },
                ) {
                    Text(
                        "Abandon",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        },
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Game in progress", style = MaterialTheme.typography.titleMedium)
                Text(summary, style = MaterialTheme.typography.bodyMedium)
                Button(onClick = onResume, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Resume") }
            }
        }
    }

    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text("Abandon this game?") },
            text = { Text("$summary will be discarded. This can't be undone.") },
            confirmButton = { TextButton(onClick = { confirming = false; onAbandon() }) { Text("Abandon") } },
            dismissButton = { TextButton(onClick = { confirming = false }) { Text("Keep playing") } },
        )
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
