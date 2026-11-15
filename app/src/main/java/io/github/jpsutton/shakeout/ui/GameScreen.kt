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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jpsutton.shakeout.game.Game
import io.github.jpsutton.shakeout.game.GameState

@Composable
fun GameScreen(vm: GameViewModel, onMenu: () -> Unit) {
    val state = vm.game ?: return
    var confirmLeave by rememberSaveable { mutableStateOf(false) }
    val selected = vm.selected
    val total = Game.total(state)

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(state.mode.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Turn ${minOf(state.turnsPlayed + 1, state.mode.columns * 13)} of ${state.mode.columns * 13}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text("$total", style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = { confirmLeave = true }) { Text("Menu") }
        }

        DiceRow(
            dice = state.dice,
            held = state.held,
            rolling = vm.rolling,
            canHold = state.hasRolled && state.rollsLeft > 0 && !state.isOver && vm.rolling.isEmpty(),
            onToggle = vm::toggleHold,
            modifier = Modifier.padding(top = 10.dp),
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = vm::roll,
                enabled = state.rollsLeft > 0 && !state.isOver && vm.rolling.isEmpty(),
                modifier = Modifier.weight(1f).heightIn(min = 52.dp),
            ) {
                Text(
                    when {
                        !state.hasRolled -> "Roll"
                        state.rollsLeft > 0 -> "Roll again (${state.rollsLeft} left)"
                        else -> "Pick a box"
                    },
                )
            }
            OutlinedButton(
                onClick = vm::confirm,
                enabled = selected != null,
                modifier = Modifier.weight(1f).heightIn(min = 52.dp),
            ) {
                Text(
                    if (selected == null) "Score" else
                        "Score ${Game.scoreFor(state, selected)} in ${selected.category.label}",
                )
            }
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            ScoreCard(state, selected, vm::select, enabled = vm.rolling.isEmpty())
        }
    }

    if (state.isOver) FinishedDialog(state, vm.newBest, onMenu)
    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { Text("Back to menu?") },
            text = { Text("Your game is saved and can be resumed.") },
            confirmButton = { TextButton(onClick = { confirmLeave = false; onMenu() }) { Text("Menu") } },
            dismissButton = { TextButton(onClick = { confirmLeave = false }) { Text("Keep playing") } },
        )
    }
}

@Composable
private fun FinishedDialog(state: GameState, newBest: Boolean, onMenu: () -> Unit) {
    AlertDialog(
        onDismissRequest = onMenu,
        title = { Text("Game over") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Final score: ${Game.total(state)}", style = MaterialTheme.typography.titleLarge)
                if (newBest) Text("New best for ${state.mode.title}!")
            }
        },
        confirmButton = { TextButton(onClick = onMenu) { Text("Menu") } },
    )
}
