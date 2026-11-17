package io.github.jpsutton.shakeout.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** How long dice tumble before a roll's result shows. The view model applies the roll after this long. */
const val ROLL_ANIMATION_MS = 700L

/** Pauses between the faces a tumbling die flashes; they lengthen so the die seems to slow down. Sum: [ROLL_ANIMATION_MS]. */
private val FacePauses = listOf(50L, 50L, 60L, 70L, 80L, 100L, 120L, 170L)

/** Pip positions of each face on a 3x3 grid, as (column, row). */
private val Pips = mapOf(
    1 to listOf(1 to 1),
    2 to listOf(0 to 0, 2 to 2),
    3 to listOf(0 to 0, 1 to 1, 2 to 2),
    4 to listOf(0 to 0, 2 to 0, 0 to 2, 2 to 2),
    5 to listOf(0 to 0, 2 to 0, 1 to 1, 0 to 2, 2 to 2),
    6 to listOf(0 to 0, 2 to 0, 0 to 1, 2 to 1, 0 to 2, 2 to 2),
)

/** How far a held die rises, and so the room the row keeps above the dice. */
private val HeldLift = 8.dp

/** Corner radius of a die, as a share of its width. */
private const val CORNER_SHARE = 0.24f

/**
 * Draws a die filling this scope: an ivory face showing [value] on a darker bottom edge [edge] tall, or an empty
 * green slot when [value] is 0 (not rolled yet). [ring] draws the brass ring of a held die.
 */
fun DrawScope.drawDie(value: Int, edge: Float, ring: Float = 0f) {
    val corner = CornerRadius(size.width * CORNER_SHARE)
    if (value == 0) {
        drawRoundRect(Felt.Raised, cornerRadius = corner)
        val line = 1.5.dp.toPx()
        drawRoundRect(
            Color(0x1FFFFFFF),
            topLeft = Offset(line / 2, line / 2),
            size = Size(size.width - line, size.height - line),
            cornerRadius = corner,
            style = Stroke(line),
        )
        return
    }
    val faceHeight = size.height - edge
    if (edge > 0f) drawRoundRect(Felt.DieEdge, cornerRadius = corner)
    drawRoundRect(Felt.Ivory, size = Size(size.width, faceHeight), cornerRadius = corner)
    val cellX = size.width / 4f
    val cellY = faceHeight / 4f
    val radius = size.width * 0.085f
    for ((cx, cy) in Pips[value].orEmpty()) {
        drawCircle(Felt.Pip, radius, Offset(cellX * (cx + 1), cellY * (cy + 1)))
    }
    if (ring > 0f) {
        drawRoundRect(
            Felt.Brass,
            topLeft = Offset(ring / 2, ring / 2),
            size = Size(size.width - ring, size.height - ring),
            cornerRadius = corner,
            style = Stroke(ring),
        )
    }
}

/** A die face with no behaviour, for decoration and the upper rows of the scorecard. */
@Composable
fun DieFace(value: Int, size: Dp, modifier: Modifier = Modifier, edge: Dp = 0.dp) {
    Canvas(modifier.size(size)) { drawDie(value, edge.toPx()) }
}

/**
 * The five dice, each [dieSize] square, spread across the width. Those in [rolling] tumble; the rest, held ones
 * included, stay still. A held die rises [HeldLift], wears a brass ring and, with [showHeldLabel], a "HELD" label
 * below it.
 */
@Composable
fun DiceRow(
    dice: List<Int>,
    held: Set<Int>,
    rolling: Set<Int>,
    canHold: Boolean,
    onToggle: (Int) -> Unit,
    dieSize: Dp,
    modifier: Modifier = Modifier,
    showHeldLabel: Boolean = true,
) {
    Row(modifier.fillMaxWidth().padding(top = HeldLift), horizontalArrangement = Arrangement.SpaceBetween) {
        dice.forEachIndexed { i, value ->
            Die(value, i in held, canHold, i in rolling, dieSize, showHeldLabel) { onToggle(i) }
        }
    }
}

@Composable
private fun Die(
    value: Int,
    held: Boolean,
    enabled: Boolean,
    rolling: Boolean,
    dieSize: Dp,
    showHeldLabel: Boolean,
    onClick: () -> Unit,
) {
    // While rolling, the die flashes random faces and spins once; the real value shows when it stops.
    var flashed by remember { mutableIntStateOf(value) }
    val spin = remember { Animatable(0f) }
    LaunchedEffect(rolling) {
        if (rolling) {
            launch {
                spin.snapTo(0f)
                spin.animateTo(360f, tween(ROLL_ANIMATION_MS.toInt(), easing = LinearOutSlowInEasing))
            }
            for (pause in FacePauses) {
                var face = Random.nextInt(1, 7)
                while (face == flashed) face = Random.nextInt(1, 7)
                flashed = face
                delay(pause)
            }
        } else {
            spin.snapTo(0f)
        }
    }
    val shown = if (rolling) flashed else value
    val lift by animateDpAsState(if (held) -HeldLift else 0.dp, tween(150), label = "lift")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(dieSize)
                .offset { IntOffset(0, lift.roundToPx()) }
                .graphicsLayer {
                    rotationZ = spin.value
                    val scale = if (rolling) 1.08f else 1f
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(dieSize * CORNER_SHARE))
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
                .semantics {
                    contentDescription = when {
                        rolling -> "Die, rolling"
                        value == 0 -> "Die, not rolled"
                        else -> "Die showing $value"
                    }
                    stateDescription = if (held) "Held" else "Not held"
                },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawDie(shown, edge = 3.dp.toPx(), ring = if (held && shown != 0) 3.dp.toPx() else 0f)
            }
        }
        if (showHeldLabel) {
            Text(
                "HELD",
                Modifier.padding(top = 4.dp).alpha(if (held) 1f else 0f).clearAndSetSemantics {},
                color = Felt.Brass,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
        }
    }
}
