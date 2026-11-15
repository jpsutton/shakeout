package io.github.jpsutton.shakeout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.jpsutton.shakeout.game.Category
import io.github.jpsutton.shakeout.game.Game
import io.github.jpsutton.shakeout.game.GameState
import io.github.jpsutton.shakeout.game.Mode
import io.github.jpsutton.shakeout.game.Placement

private val LabelWidth = 118.dp

/**
 * The scorecard: one column per card, rows in card order. Boxes the dice may go in show the points they would
 * score; tapping one selects it (confirmed with the Score button).
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
    Column(modifier) {
        if (state.mode == Mode.TRIPLE) {
            Row(Modifier.fillMaxWidth()) {
                Spacer(LabelWidth)
                repeat(columns) { Header("x${state.mode.multiplier(it)}") }
            }
        }
        Category.upper.forEach { CategoryRow(state, it, legal, selected, onSelect) }
        Summary("Upper bonus", columns) { col ->
            val sum = Game.upperSum(state, col)
            if (Game.upperBonus(state, col) > 0) "35" else "$sum/63"
        }
        HorizontalDivider()
        Category.lower.forEach { CategoryRow(state, it, legal, selected, onSelect) }
        HorizontalDivider()
        if (state.mode == Mode.TRIPLE) {
            Summary("Column total", columns) { col -> Game.columnTotal(state, col).toString() }
        }
        if (state.bonusPoints > 0) Summary("Bonus", 1) { "+${state.bonusPoints}" }
    }
}

@Composable
private fun Spacer(width: androidx.compose.ui.unit.Dp) = Box(Modifier.width(width))

@Composable
private fun RowScope.Header(text: String) {
    Text(
        text, Modifier.weight(1f), textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun CategoryRow(
    state: GameState,
    category: Category,
    legal: Set<Placement>,
    selected: Placement?,
    onSelect: (Placement) -> Unit,
) {
    Row(Modifier.fillMaxWidth().heightIn(min = 40.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            category.label, Modifier.width(LabelWidth).padding(start = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        repeat(state.mode.columns) { col ->
            Cell(state, Placement(col, category), legal, selected, onSelect)
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
) {
    val scheme = MaterialTheme.colorScheme
    val filled = state.scores[p.column][p.category]
    val isLegal = p in legal
    val isSelected = p == selected
    val preview = if (isLegal) Game.scoreFor(state, p) else null
    val background = when {
        isSelected -> scheme.tertiaryContainer
        isLegal -> scheme.secondaryContainer
        else -> androidx.compose.ui.graphics.Color.Transparent
    }
    val description = when {
        filled != null -> "${p.category.label}, column ${p.column + 1}: $filled points"
        preview != null -> "${p.category.label}, column ${p.column + 1}: score $preview points"
        else -> "${p.category.label}, column ${p.column + 1}: open"
    }
    Box(
        Modifier
            .weight(1f)
            .heightIn(min = 44.dp)
            .padding(2.dp)
            .background(background, MaterialTheme.shapes.small)
            .then(
                if (isLegal) Modifier.clickable(role = Role.Button) { onSelect(p) } else Modifier,
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        when {
            filled != null -> Text(filled.toString(), fontWeight = FontWeight.Bold)
            preview != null -> Text(
                preview.toString(),
                color = if (isSelected) scheme.onTertiaryContainer else scheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun Summary(label: String, columns: Int, value: (Int) -> String) {
    Row(Modifier.fillMaxWidth().heightIn(min = 32.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label, Modifier.width(LabelWidth).padding(start = 4.dp),
            style = MaterialTheme.typography.bodySmall,
        )
        repeat(columns) { col ->
            Text(
                value(col), Modifier.weight(1f), textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
