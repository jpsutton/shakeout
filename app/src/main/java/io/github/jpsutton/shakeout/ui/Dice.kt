package io.github.jpsutton.shakeout.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Pip positions of each face on a 3x3 grid, as (column, row). */
private val Pips = mapOf(
    1 to listOf(1 to 1),
    2 to listOf(0 to 0, 2 to 2),
    3 to listOf(0 to 0, 1 to 1, 2 to 2),
    4 to listOf(0 to 0, 2 to 0, 0 to 2, 2 to 2),
    5 to listOf(0 to 0, 2 to 0, 1 to 1, 0 to 2, 2 to 2),
    6 to listOf(0 to 0, 2 to 0, 0 to 1, 2 to 1, 0 to 2, 2 to 2),
)

@Composable
fun DiceRow(dice: List<Int>, held: Set<Int>, canHold: Boolean, onToggle: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        dice.forEachIndexed { i, value ->
            Die(value, i in held, canHold) { onToggle(i) }
        }
    }
}

@Composable
private fun RowScope.Die(value: Int, held: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier
            .weight(1f)
            .aspectRatio(1f)
            .offset(y = if (held) (-10).dp else 0.dp)
            .clip(shape)
            .border(if (held) 4.dp else 1.dp, if (held) scheme.primary else scheme.outline, shape)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics {
                contentDescription = if (value == 0) "Die, not rolled" else "Die showing $value"
                stateDescription = if (held) "Held" else "Not held"
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxWidth().aspectRatio(1f)) {
            drawRect(if (value == 0) Color(0xFFB8C4BC) else Color(0xFFFDFDF8))
            val pips = Pips[value].orEmpty()
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
