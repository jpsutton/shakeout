package io.github.jpsutton.shakeout.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.jpsutton.shakeout.game.Game
import io.github.jpsutton.shakeout.game.GameState
import io.github.jpsutton.shakeout.game.Placement

/** Share of a landscape screen's width taken by the dice and buttons; the scorecard gets the rest. */
private const val LANDSCAPE_CONTROLS_SHARE = 0.45f

/**
 * The game screen. Nothing on it scrolls: in portrait the controls sit on top and the scorecard takes the rest of the
 * height; in landscape the controls are on the left and the scorecard fills the right.
 */
@Composable
fun GameScreen(vm: GameViewModel, onMenu: () -> Unit) {
    val state = vm.game ?: return
    var confirmLeave by rememberSaveable { mutableStateOf(false) }
    val selected = vm.selected
    val cardEnabled = vm.rolling.isEmpty()

    BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().padding(8.dp)) {
        if (maxWidth > maxHeight) {
            val dieSize = ((maxWidth * LANDSCAPE_CONTROLS_SHARE - 16.dp) / 5).coerceAtMost(52.dp)
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Controls(vm, state, selected, dieSize, { confirmLeave = true }, Modifier.weight(LANDSCAPE_CONTROLS_SHARE))
                ScoreCard(
                    state, selected, vm::select,
                    Modifier.weight(1f - LANDSCAPE_CONTROLS_SHARE).fillMaxHeight(),
                    enabled = cardEnabled,
                )
            }
        } else {
            // Dice scale with the height so a short screen leaves the scorecard enough rows.
            val dieSize = (maxHeight * 0.085f).coerceIn(40.dp, 60.dp)
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Controls(vm, state, selected, dieSize, { confirmLeave = true }, Modifier.fillMaxWidth())
                ScoreCard(state, selected, vm::select, Modifier.weight(1f).fillMaxWidth(), enabled = cardEnabled)
            }
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

/** Title bar, dice and the Roll / Score buttons. */
@Composable
private fun Controls(
    vm: GameViewModel,
    state: GameState,
    selected: Placement?,
    dieSize: Dp,
    onLeave: () -> Unit,
    modifier: Modifier,
) {
    val turns = state.mode.columns * 13
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(state.mode.title, style = MaterialTheme.typography.titleMedium)
                val bonus = if (state.bonusPoints > 0) " · bonus +${state.bonusPoints}" else ""
                Text(
                    "Turn ${minOf(state.turnsPlayed + 1, turns)} of $turns$bonus",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text("${Game.total(state)}", style = MaterialTheme.typography.headlineSmall)
            TextButton(
                onClick = onLeave,
                modifier = Modifier.heightIn(min = 36.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) { Text("Menu") }
        }

        DiceRow(
            dice = state.dice,
            held = state.held,
            rolling = vm.rolling,
            canHold = state.hasRolled && state.rollsLeft > 0 && !state.isOver && vm.rolling.isEmpty(),
            onToggle = vm::toggleHold,
            dieSize = dieSize,
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = vm::roll,
                enabled = state.rollsLeft > 0 && !state.isOver && vm.rolling.isEmpty(),
                modifier = Modifier.weight(1f).heightIn(min = 44.dp),
            ) {
                Text(
                    when {
                        !state.hasRolled -> "Roll"
                        state.rollsLeft > 0 -> "Roll again (${state.rollsLeft} left)"
                        else -> "Pick a box"
                    },
                    maxLines = 1,
                )
            }
            OutlinedButton(
                onClick = vm::confirm,
                enabled = selected != null,
                modifier = Modifier.weight(1f).heightIn(min = 44.dp),
            ) {
                Text(
                    if (selected == null) "Score" else
                        "Score ${Game.scoreFor(state, selected)} in ${selected.category.label}",
                    maxLines = 1,
                )
            }
        }
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
