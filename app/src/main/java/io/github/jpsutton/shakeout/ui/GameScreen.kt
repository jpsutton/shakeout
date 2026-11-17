package io.github.jpsutton.shakeout.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jpsutton.shakeout.game.Game
import io.github.jpsutton.shakeout.game.GameState
import io.github.jpsutton.shakeout.game.Mode
import io.github.jpsutton.shakeout.game.Placement

/** Share of a landscape screen's width taken by the dice and buttons; the scorecard gets the rest. */
private const val LANDSCAPE_CONTROLS_SHARE = 0.45f

/** Below this height the controls drop the "HELD" labels and the hint line, so the scorecard keeps its rows. */
private val CompactHeight = 640.dp

private val TrayShape = RoundedCornerShape(20.dp)

/**
 * The game screen. Nothing on it scrolls. In portrait the scorecard is on top and the dice and buttons sit at the
 * bottom, under the thumb; in landscape the controls are on the left and the scorecard fills the right.
 */
@Composable
fun GameScreen(vm: GameViewModel, onMenu: () -> Unit) {
    val state = vm.game ?: return
    var confirmLeave by rememberSaveable { mutableStateOf(false) }
    val selected = vm.selected
    val cardEnabled = vm.rolling.isEmpty()

    BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 12.dp, vertical = 8.dp)) {
        if (maxWidth > maxHeight) {
            val controlsWidth = maxWidth * LANDSCAPE_CONTROLS_SHARE
            val dieSize = ((controlsWidth - 56.dp) / 5).coerceAtMost(52.dp)
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(LANDSCAPE_CONTROLS_SHARE).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TopBar(state, compact = true) { confirmLeave = true }
                    Spacer(Modifier.weight(1f))
                    Tray(vm, state, dieSize, compact = true)
                    Buttons(vm, state, selected, compact = true)
                }
                ScoreCard(
                    state, selected, vm::select,
                    Modifier.weight(1f - LANDSCAPE_CONTROLS_SHARE).fillMaxHeight(),
                    enabled = cardEnabled,
                )
            }
        } else {
            val compact = maxHeight < CompactHeight
            // Dice scale with the height so a short screen leaves the scorecard enough rows, and with the width so
            // five fit in the tray.
            val dieSize = minOf((maxHeight * 0.085f).coerceIn(40.dp, 58.dp), (maxWidth - 56.dp) / 5)
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TopBar(state, compact) { confirmLeave = true }
                ScoreCard(state, selected, vm::select, Modifier.weight(1f).fillMaxWidth(), enabled = cardEnabled)
                Tray(vm, state, dieSize, compact)
                Buttons(vm, state, selected, compact)
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

/** Menu button, mode and turn, and the running total in brass. */
@Composable
private fun TopBar(state: GameState, compact: Boolean, onLeave: () -> Unit) {
    val turns = state.mode.columns * 13
    Row(
        Modifier.fillMaxWidth().height(if (compact) 44.dp else 52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Felt.Raised)
                .clickable(onClick = onLeave)
                .semantics {
                    contentDescription = "Menu"
                    role = Role.Button
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(18.dp)) {
                val w = 2.dp.toPx()
                for (y in listOf(0.2f, 0.5f, 0.8f)) {
                    drawLine(Felt.Text, Offset(0f, size.height * y), Offset(size.width, size.height * y), w, StrokeCap.Round)
                }
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                state.mode.title.uppercase(), color = Felt.Muted, fontSize = 11.sp, lineHeight = 14.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
            )
            val bonus = if (state.bonusPoints > 0) " · bonus +${state.bonusPoints}" else ""
            Text(
                "Turn ${minOf(state.turnsPlayed + 1, turns)} of $turns$bonus",
                fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 1,
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = "Score ${Game.total(state)}" },
        ) {
            Text(
                "${Game.total(state)}", color = Felt.Brass, fontWeight = FontWeight.ExtraBold,
                fontSize = if (compact) 26.sp else 32.sp, lineHeight = if (compact) 28.sp else 34.sp,
                modifier = Modifier.clearAndSetSemantics {},
            )
            if (!compact) {
                Text("points", color = Felt.Muted, fontSize = 11.sp, lineHeight = 13.sp, modifier = Modifier.clearAndSetSemantics {})
            }
        }
    }
}

/** The dice in their tray, with a hint and the rolls left on taller screens. */
@Composable
private fun Tray(vm: GameViewModel, state: GameState, dieSize: Dp, compact: Boolean) {
    val canHold = state.hasRolled && state.rollsLeft > 0 && !state.isOver && vm.rolling.isEmpty()
    Column(
        Modifier
            .fillMaxWidth()
            .clip(TrayShape)
            .background(Felt.Tray)
            .padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = if (compact) 10.dp else 8.dp),
    ) {
        DiceRow(
            dice = state.dice,
            held = state.held,
            rolling = vm.rolling,
            canHold = canHold,
            onToggle = vm::toggleHold,
            dieSize = dieSize,
            showHeldLabel = !compact,
        )
        if (!compact) {
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    when {
                        vm.rolling.isNotEmpty() -> "Rolling…"
                        !state.hasRolled -> "Roll to start the turn"
                        canHold -> "Tap dice to hold them"
                        else -> "Pick a box to score"
                    },
                    color = Felt.Muted, fontSize = 13.sp, modifier = Modifier.weight(1f),
                )
                RollsLeft(state.rollsLeft)
            }
        }
    }
}

/** One dot per roll in a turn, brass while still to use. */
@Composable
private fun RollsLeft(left: Int) {
    Row(
        Modifier.semantics(mergeDescendants = true) {
            contentDescription = if (left == 1) "1 roll left" else "$left rolls left"
        },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(GameState.ROLLS_PER_TURN) { i ->
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .then(if (i < left) Modifier.background(Felt.Brass) else Modifier.border(1.5.dp, Felt.Dim, CircleShape)),
            )
        }
    }
}

/** Roll (green) and Score (brass). Each says what it will do. */
@Composable
private fun Buttons(vm: GameViewModel, state: GameState, selected: Placement?, compact: Boolean) {
    val idle = vm.rolling.isEmpty()
    val left = state.rollsLeft
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ActionButton(
            title = when {
                !state.hasRolled -> "Roll"
                left > 0 -> "Roll again"
                else -> "No rolls left"
            },
            subtitle = if (left == 1) "1 roll left" else "$left rolls left",
            primary = false,
            enabled = left > 0 && !state.isOver && idle,
            compact = compact,
            onClick = vm::roll,
            modifier = Modifier.weight(1f),
        )
        val points = selected?.let { Game.scoreFor(state, it) }
        val multiplier = selected?.let { state.mode.multiplier(it.column) } ?: 1
        ActionButton(
            title = if (points == null) "Score" else "Score ${points * multiplier}",
            subtitle = when {
                selected == null -> "Tap a box first"
                state.mode == Mode.TRIPLE -> "${selected.category.label}, $points × $multiplier"
                else -> selected.category.label
            },
            primary = true,
            enabled = selected != null,
            compact = compact,
            onClick = vm::confirm,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ActionButton(
    title: String,
    subtitle: String,
    primary: Boolean,
    enabled: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val container = if (primary) Felt.Brass else Felt.Raised
    val content = if (primary) Felt.BrassInk else Felt.Text
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = if (compact) 48.dp else 56.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            // A disabled button sinks back into the table rather than turning a muddy brass.
            disabledContainerColor = Felt.Raised.copy(alpha = 0.5f),
            disabledContentColor = Felt.Muted,
        ),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 16.sp, lineHeight = 19.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(subtitle, fontSize = 11.sp, lineHeight = 13.sp, maxLines = 1)
        }
    }
}

@Composable
private fun FinishedDialog(state: GameState, newBest: Boolean, onMenu: () -> Unit) {
    AlertDialog(
        onDismissRequest = onMenu,
        title = { Text("Game over") },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "${Game.total(state)}", color = Felt.Brass, fontSize = 56.sp, lineHeight = 58.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.semantics { contentDescription = "Final score: ${Game.total(state)}" },
                )
                if (newBest) {
                    Text("New best for ${state.mode.title}!", color = Felt.Brass, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = { TextButton(onClick = onMenu) { Text("Menu") } },
    )
}
