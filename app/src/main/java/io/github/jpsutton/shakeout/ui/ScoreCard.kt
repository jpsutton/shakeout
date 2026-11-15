package io.github.jpsutton.shakeout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jpsutton.shakeout.game.Category
import io.github.jpsutton.shakeout.game.Game
import io.github.jpsutton.shakeout.game.GameState
import io.github.jpsutton.shakeout.game.Mode
import io.github.jpsutton.shakeout.game.Placement

/** Sizes shared by every row of one scorecard. */
private class Metrics(val rowHeight: Dp, val textSize: TextUnit, val labelWidth: Dp)

private val LabelWidth = 112.dp
private val MaxRowHeight = 44.dp
private val DividerThickness = 1.dp

/** Rows of a card: 6 upper, the upper bonus, 7 lower; Triple adds the multiplier header and the column totals. */
fun scoreCardRows(mode: Mode): Int = 6 + 1 + 7 + if (mode == Mode.TRIPLE) 2 else 0

/**
 * The scorecard: one column per card, rows in card order. It never scrolls: the row height is the height it is
 * given divided by the number of rows (at most [MaxRowHeight]), and the text scales with it. Boxes the dice may go
 * in show the points they would score; tapping one selects it (confirmed with the Score button).
 */
@Composable
fun ScoreCard(
    state: GameState,
    selected: Placement?,
    onSelect: (Placement) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    // While the dice tumble there is nothing to score yet: no previews, nothing to tap.
    val legal = if (enabled) Game.legalPlacements(state).toSet() else emptySet()
    val columns = state.mode.columns
    BoxWithConstraints(modifier) {
        val rowHeight = ((maxHeight - DividerThickness * 2) / scoreCardRows(state.mode)).coerceAtMost(MaxRowHeight)
        // At most 14 sp: larger text outgrows the label column ("Three of a kind" clipped at 16 sp).
        val m = Metrics(rowHeight, (rowHeight.value * 0.5f).coerceIn(10f, 14f).sp, LabelWidth)
        Column(Modifier.fillMaxWidth()) {
            if (state.mode == Mode.TRIPLE) {
                Row(Modifier.fillMaxWidth().height(m.rowHeight), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(m.labelWidth))
                    repeat(columns) { Header("x${state.mode.multiplier(it)}", m) }
                }
            }
            Category.upper.forEach { CategoryRow(state, it, legal, selected, onSelect, m) }
            Summary("Upper bonus", columns, m) { col ->
                val sum = Game.upperSum(state, col)
                if (Game.upperBonus(state, col) > 0) "35" else "$sum/63"
            }
            HorizontalDivider(thickness = DividerThickness)
            Category.lower.forEach { CategoryRow(state, it, legal, selected, onSelect, m) }
            HorizontalDivider(thickness = DividerThickness)
            if (state.mode == Mode.TRIPLE) {
                Summary("Column total", columns, m) { col -> Game.columnTotal(state, col).toString() }
            }
        }
    }
}

@Composable
private fun RowScope.Header(text: String, m: Metrics) {
    Text(
        text, Modifier.weight(1f), textAlign = TextAlign.Center,
        fontSize = m.textSize, fontWeight = FontWeight.Bold, maxLines = 1,
    )
}

@Composable
private fun CategoryRow(
    state: GameState,
    category: Category,
    legal: Set<Placement>,
    selected: Placement?,
    onSelect: (Placement) -> Unit,
    m: Metrics,
) {
    Row(Modifier.fillMaxWidth().height(m.rowHeight), verticalAlignment = Alignment.CenterVertically) {
        Text(
            category.label, Modifier.width(m.labelWidth).padding(start = 4.dp),
            fontSize = m.textSize, maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
        )
        repeat(state.mode.columns) { col ->
            Cell(state, Placement(col, category), legal, selected, onSelect, m)
        }
    }
}

@Composable
private fun RowScope.Cell(
    state: GameState,
    p: Placement,
    legal: Set<Placement>,
    selected: Placement?,
    onSelect: (Placement) -> Unit,
    m: Metrics,
) {
    val scheme = MaterialTheme.colorScheme
    val filled = state.scores[p.column][p.category]
    val isLegal = p in legal
    val isSelected = p == selected
    val preview = if (isLegal) Game.scoreFor(state, p) else null
    val background = when {
        isSelected -> scheme.tertiaryContainer
        isLegal -> scheme.secondaryContainer
        else -> Color.Transparent
    }
    val description = when {
        filled != null -> "${p.category.label}, column ${p.column + 1}: $filled points"
        preview != null -> "${p.category.label}, column ${p.column + 1}: score $preview points"
        else -> "${p.category.label}, column ${p.column + 1}: open"
    }
    Box(
        Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(1.dp)
            .background(background, MaterialTheme.shapes.small)
            .then(
                if (isLegal) Modifier.clickable(role = Role.Button) { onSelect(p) } else Modifier,
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        when {
            filled != null -> Text(filled.toString(), fontSize = m.textSize, fontWeight = FontWeight.Bold, maxLines = 1)
            preview != null -> Text(
                preview.toString(),
                fontSize = m.textSize,
                maxLines = 1,
                color = if (isSelected) scheme.onTertiaryContainer else scheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun Summary(label: String, columns: Int, m: Metrics, value: (Int) -> String) {
    Row(Modifier.fillMaxWidth().height(m.rowHeight), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label, Modifier.width(m.labelWidth).padding(start = 4.dp),
            fontSize = m.textSize, maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
        )
        repeat(columns) { col ->
            Text(
                value(col), Modifier.weight(1f), textAlign = TextAlign.Center,
                fontSize = m.textSize, maxLines = 1,
            )
        }
    }
}
