package io.github.jpsutton.shakeout.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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

/** The five dice. Those in [rolling] tumble; the rest, held ones included, stay still. */
@Composable
fun DiceRow(
    dice: List<Int>,
    held: Set<Int>,
    rolling: Set<Int>,
    canHold: Boolean,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        dice.forEachIndexed { i, value ->
            Die(value, i in held, canHold, i in rolling) { onToggle(i) }
        }
    }
}

@Composable
private fun RowScope.Die(value: Int, held: Boolean, enabled: Boolean, rolling: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)

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

    Box(
        Modifier
            .weight(1f)
            .aspectRatio(1f)
            .offset(y = if (held) (-10).dp else 0.dp)
            .graphicsLayer {
                rotationZ = spin.value
                val scale = if (rolling) 1.08f else 1f
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .border(if (held) 4.dp else 1.dp, if (held) scheme.primary else scheme.outline, shape)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics {
                contentDescription = when {
                    rolling -> "Die, rolling"
                    value == 0 -> "Die, not rolled"
                    else -> "Die showing $value"
                }
                stateDescription = if (held) "Held" else "Not held"
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxWidth().aspectRatio(1f)) {
            drawRect(if (shown == 0) Color(0xFFB8C4BC) else Color(0xFFFDFDF8))
            val pips = Pips[shown].orEmpty()
            val cell = size.width / 4f
            val radius = size.width * 0.085f
            for ((cx, cy) in pips) {
                drawCircle(Color(0xFF1A1A1A), radius, Offset(cell * (cx + 1), cell * (cy + 1)))
            }
        }
        if (held) {
            Text(
                "HELD",
                Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp),
                color = scheme.primary,
                fontSize = 10.sp,
            )
        }
    }
}
