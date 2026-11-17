package io.github.jpsutton.shakeout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jpsutton.shakeout.game.Category
import io.github.jpsutton.shakeout.game.Game
import io.github.jpsutton.shakeout.game.GameState
import io.github.jpsutton.shakeout.game.Mode

private val CardShape = RoundedCornerShape(20.dp)
private val ButtonShape = RoundedCornerShape(14.dp)

@Composable
fun MenuScreen(vm: GameViewModel, onRules: () -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Hero()

        vm.saved?.let { saved -> ResumeTile(saved, onResume = vm::resume, onAbandon = vm::abandonSaved) }

        SectionLabel("New game", Modifier.padding(top = 4.dp, start = 4.dp))
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Mode.entries.forEach { mode ->
                ModeCard(mode, vm.bests[mode] ?: 0, resumable = vm.saved?.mode == mode, Modifier.weight(1f).fillMaxHeight()) {
                    vm.start(mode)
                }
            }
        }

        TextButton(onClick = onRules, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text("How scoring works", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Three tumbled dice over the name. */
@Composable
private fun Hero() {
    Column(Modifier.padding(top = 12.dp, start = 4.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(
            Modifier.height(76.dp).clearAndSetSemantics {},
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            DieFace(5, 60.dp, Modifier.offset(y = (-4).dp).rotate(-12f), edge = 4.dp)
            DieFace(3, 60.dp, Modifier.offset(y = 4.dp).rotate(8f), edge = 4.dp)
            DieFace(6, 60.dp, Modifier.offset(y = (-12).dp).rotate(-4f), edge = 4.dp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Shakeout",
                fontSize = 52.sp, lineHeight = 54.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp,
                modifier = Modifier.semantics { heading() },
            )
            Text("Five dice. Three rolls. Thirteen boxes.", color = Felt.Muted, fontSize = 16.sp)
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(), modifier, color = Felt.Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
    )
}

@Composable
private fun ModeCard(mode: Mode, best: Int, resumable: Boolean, modifier: Modifier, onPlay: () -> Unit) {
    Column(
        modifier.clip(CardShape).background(Felt.Panel).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(mode.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
        Text(mode.blurb, color = Felt.Muted, fontSize = 14.sp, lineHeight = 19.sp)
        Spacer(Modifier.weight(1f))
        if (best > 0) {
            Column(Modifier.semantics(mergeDescendants = true) { contentDescription = "Best score: $best" }) {
                SectionLabel("Best", Modifier.clearAndSetSemantics {})
                Text(
                    "%,d".format(best), color = Felt.Brass, fontSize = 30.sp, lineHeight = 34.sp,
                    fontWeight = FontWeight.ExtraBold, modifier = Modifier.clearAndSetSemantics {},
                )
            }
        }
        Button(
            onClick = onPlay,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = ButtonShape,
            colors = ButtonDefaults.buttonColors(containerColor = Felt.Raised, contentColor = Felt.Text),
        ) {
            Text(if (resumable) "New game" else "Play", fontWeight = FontWeight.Bold, maxLines = 1)
        }
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
    val total = Game.total(saved)
    val summary = "${saved.mode.title}, turn ${saved.turnsPlayed + 1}, score $total"

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
                        .background(MaterialTheme.colorScheme.errorContainer, CardShape)
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
        Column(
            Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(Felt.Panel)
                .border(1.5.dp, Felt.Brass, CardShape)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().semantics(mergeDescendants = true) { contentDescription = "Game in progress: $summary" },
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f).clearAndSetSemantics {}, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionLabel("Game in progress")
                    Text(
                        "${saved.mode.title} · turn ${saved.turnsPlayed + 1}",
                        fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    )
                }
                Column(Modifier.clearAndSetSemantics {}, horizontalAlignment = Alignment.End) {
                    Text(
                        "%,d".format(total), color = Felt.Brass, fontSize = 30.sp, lineHeight = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text("points", color = Felt.Muted, fontSize = 12.sp)
                }
            }
            TurnProgress(saved)
            Button(
                onClick = onResume,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = ButtonShape,
            ) { Text("Resume", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
            Text(
                "Swipe the card sideways to abandon",
                Modifier.fillMaxWidth().clearAndSetSemantics {},
                color = Felt.Muted, fontSize = 12.sp, textAlign = TextAlign.Center,
            )
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

/** One segment per turn of the game, brass for the turns played. */
@Composable
private fun TurnProgress(saved: GameState) {
    val turns = saved.mode.columns * Category.entries.size
    Row(
        Modifier.fillMaxWidth().semantics { contentDescription = "${saved.turnsPlayed} of $turns turns played" },
        horizontalArrangement = Arrangement.spacedBy(if (turns > 20) 2.dp else 3.dp),
    ) {
        repeat(turns) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (i < saved.turnsPlayed) Felt.Brass else Felt.Line.copy(alpha = 0.16f)),
            )
        }
    }
}
