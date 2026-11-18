package io.github.jpsutton.shakeout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jpsutton.shakeout.game.Category
import io.github.jpsutton.shakeout.game.Game
import io.github.jpsutton.shakeout.game.GameState
import io.github.jpsutton.shakeout.game.Mode
import io.github.jpsutton.shakeout.game.Placement
import io.github.jpsutton.shakeout.game.Scoring

/** Sizes shared by every row of one scorecard. */
private class Metrics(val rowHeight: Dp) {
    val textSize: TextUnit = (rowHeight.value * 0.5f).coerceIn(10f, 15f).sp
    val smallSize: TextUnit = (rowHeight.value * 0.4f).coerceIn(9f, 12f).sp
    val dieSize: Dp = (rowHeight * 0.55f).coerceAtMost(20.dp)
    val boxRadius: Dp = (rowHeight * 0.2f).coerceAtMost(8.dp)
}

private val MaxRowHeight = 44.dp
private val TripleLabelWidth = 112.dp
private val PanelShape = RoundedCornerShape(16.dp)

/**
 * Translucent overlay that sets the columns of a Triple card apart: strongest on x1, lighter on x2, none on x3.
 * Standard has no columns to tell apart.
 */
private fun columnTint(mode: Mode, column: Int): Color = when {
    mode != Mode.TRIPLE -> Color.Transparent
    column == 0 -> Color(0x1FFFFFFF)
    column == 1 -> Color(0x0FFFFFFF)
    else -> Color.Transparent
}

/**
 * Rows in the tallest part of a card. Regular: each half (Upper, Lower) has a header and 7 rows. Triple: the
 * multiplier header, 6 upper rows, the upper bonus, 7 lower rows and the column totals.
 */
fun scoreCardRows(mode: Mode): Int = if (mode == Mode.TRIPLE) 16 else 8

/**
 * The scorecard. It never scrolls: the row height is the height it is given divided by [scoreCardRows] (at most
 * [MaxRowHeight]), and the text scales with it. Regular shows the upper and lower halves side by side; Triple is a
 * table with one column per card. Boxes the dice may go in show the points they would score; tapping one selects it
 * (confirmed with the Score button).
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
    BoxWithConstraints(modifier) {
        val m = Metrics((maxHeight / scoreCardRows(state.mode)).coerceAtMost(MaxRowHeight))
        if (state.mode == Mode.TRIPLE) {
            TableCard(state, legal, selected, onSelect, m)
        } else {
            SplitCard(state, legal, selected, onSelect, m)
        }
    }
}

// Regular: two panels side by side.

@Composable
private fun SplitCard(
    state: GameState,
    legal: Set<Placement>,
    selected: Placement?,
    onSelect: (Placement) -> Unit,
    m: Metrics,
) {
    val upperSum = Game.upperSum(state, 0)
    val lowerSum = Category.lower.sumOf { state.scores[0][it] ?: 0 }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Panel(Modifier.weight(1f)) {
            PanelHeader("UPPER", "$upperSum", m)
            Category.upper.forEach { SplitRow(state, Placement(0, it), legal, selected, onSelect, m) }
            BonusRow(upperSum, m)
        }
        Panel(Modifier.weight(1f)) {
            PanelHeader("LOWER", "$lowerSum", m)
            Category.lower.forEach { SplitRow(state, Placement(0, it), legal, selected, onSelect, m) }
        }
    }
}

@Composable
private fun Panel(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.clip(PanelShape).background(Felt.Panel), content = content)
}

@Composable
private fun PanelHeader(title: String, value: String, m: Metrics) {
    Row(
        Modifier.fillMaxWidth().height(m.rowHeight).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Label(title, Modifier.weight(1f), m.smallSize, Felt.Muted, FontWeight.Bold, letterSpacing = 1.sp)
        Label(value, Modifier, m.smallSize, Felt.Muted, FontWeight.Bold)
    }
}

@Composable
private fun SplitRow(
    state: GameState,
    p: Placement,
    legal: Set<Placement>,
    selected: Placement?,
    onSelect: (Placement) -> Unit,
    m: Metrics,
) {
    val box = boxOf(state, p, legal, selected)
    Row(
        Modifier
            .fillMaxWidth()
            .height(m.rowHeight)
            .hairline()
            .background(if (box.look == Look.Selected) Felt.Brass.copy(alpha = 0.14f) else Color.Transparent)
            .then(if (box.legal) Modifier.clickable(role = Role.Button) { onSelect(p) } else Modifier)
            .semantics { contentDescription = box.description }
            .padding(start = 10.dp, end = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        if (p.category.isUpper) DieFace(p.category.face, m.dieSize, Modifier.clearAndSetSemantics {})
        AutoLabel(p.category.label, Modifier.weight(1f).clearAndSetSemantics {}, m.textSize, Felt.Text)
        ValueBox(box, m, Modifier.width(40.dp).fillMaxHeight().padding(vertical = m.rowHeight * 0.14f), showOpen = true)
    }
}

/** The upper bonus: progress towards 63, or the 35 once earned. */
@Composable
private fun BonusRow(upperSum: Int, m: Metrics) {
    val earned = upperSum >= Scoring.UPPER_BONUS_THRESHOLD
    Column(
        Modifier
            .fillMaxWidth()
            .height(m.rowHeight)
            .hairline()
            .background(Color(0x1F000000))
            .padding(horizontal = 10.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = if (earned) "Upper bonus earned, 35 points" else
                    "Upper bonus: $upperSum of ${Scoring.UPPER_BONUS_THRESHOLD}"
            },
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Row(Modifier.fillMaxWidth().clearAndSetSemantics {}) {
            Label("Bonus +${Scoring.UPPER_BONUS}", Modifier.weight(1f), m.smallSize, Felt.Muted)
            Label(
                if (earned) "earned" else "$upperSum / ${Scoring.UPPER_BONUS_THRESHOLD}",
                Modifier, m.smallSize, if (earned) Felt.Brass else Felt.Muted, FontWeight.Bold,
            )
        }
        val share = (upperSum.toFloat() / Scoring.UPPER_BONUS_THRESHOLD).coerceIn(0f, 1f)
        Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0x1AFFFFFF))) {
            if (share > 0f) Box(Modifier.fillMaxWidth(share).fillMaxHeight().background(Felt.Brass))
        }
    }
}

// Triple: one table, a column per card.

@Composable
private fun TableCard(
    state: GameState,
    legal: Set<Placement>,
    selected: Placement?,
    onSelect: (Placement) -> Unit,
    m: Metrics,
) {
    val columns = state.mode.columns
    Panel(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(m.rowHeight), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(TripleLabelWidth))
            repeat(columns) {
                Box(
                    Modifier.weight(1f).fillMaxHeight().background(columnTint(state.mode, it)),
                    contentAlignment = Alignment.Center,
                ) {
                    Label(
                        "×${state.mode.multiplier(it)}", Modifier, m.textSize, Felt.Brass, FontWeight.ExtraBold,
                        align = TextAlign.Center,
                    )
                }
            }
        }
        Category.upper.forEach { TableRow(state, it, legal, selected, onSelect, m) }
        SummaryRow("Upper bonus", state.mode, m) { col ->
            if (Game.upperBonus(state, col) > 0) "35" else "${Game.upperSum(state, col)}/${Scoring.UPPER_BONUS_THRESHOLD}"
        }
        Category.lower.forEach { TableRow(state, it, legal, selected, onSelect, m) }
        SummaryRow("Column total", state.mode, m) { col -> Game.columnTotal(state, col).toString() }
    }
}

@Composable
private fun TableRow(
    state: GameState,
    category: Category,
    legal: Set<Placement>,
    selected: Placement?,
    onSelect: (Placement) -> Unit,
    m: Metrics,
) {
    Row(Modifier.fillMaxWidth().height(m.rowHeight).hairline(), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.width(TripleLabelWidth).padding(start = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (category.isUpper) DieFace(category.face, m.dieSize)
            AutoLabel(category.label, Modifier.weight(1f), m.textSize, Felt.Text)
        }
        repeat(state.mode.columns) { col -> TableCell(state, Placement(col, category), legal, selected, onSelect, m) }
    }
}

@Composable
private fun RowScope.TableCell(
    state: GameState,
    p: Placement,
    legal: Set<Placement>,
    selected: Placement?,
    onSelect: (Placement) -> Unit,
    m: Metrics,
) {
    val box = boxOf(state, p, legal, selected)
    Box(
        Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(columnTint(state.mode, p.column))
            .then(if (box.legal) Modifier.clickable(role = Role.Button) { onSelect(p) } else Modifier)
            .semantics { contentDescription = box.description }
            .padding(horizontal = 3.dp, vertical = m.rowHeight * 0.1f),
    ) {
        ValueBox(box, m, Modifier.fillMaxSize(), showOpen = false)
    }
}

@Composable
private fun SummaryRow(label: String, mode: Mode, m: Metrics, value: (Int) -> String) {
    Row(
        Modifier.fillMaxWidth().height(m.rowHeight).hairline().background(Color(0x1F000000)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(TripleLabelWidth).padding(start = 10.dp)) {
            Label(label, Modifier, m.smallSize, Felt.Muted)
        }
        repeat(mode.columns) { col ->
            Box(
                Modifier.weight(1f).fillMaxHeight().background(columnTint(mode, col)),
                contentAlignment = Alignment.Center,
            ) {
                Label(value(col), Modifier, m.smallSize, Felt.Muted, FontWeight.Bold, align = TextAlign.Center)
            }
        }
    }
}

// Boxes.

private enum class Look { Filled, Open, Preview, PreviewZero, Selected }

private class BoxInfo(val text: String, val look: Look, val description: String, val legal: Boolean)

private fun boxOf(state: GameState, p: Placement, legal: Set<Placement>, selected: Placement?): BoxInfo {
    val filled = state.scores[p.column][p.category]
    val name = "${p.category.label}, column ${p.column + 1}"
    return when {
        filled != null -> BoxInfo("$filled", Look.Filled, "$name: $filled points", false)
        p in legal -> {
            val points = Game.scoreFor(state, p)
            val look = when {
                p == selected -> Look.Selected
                points == 0 -> Look.PreviewZero
                else -> Look.Preview
            }
            BoxInfo("$points", look, "$name: score $points points", true)
        }
        else -> BoxInfo("–", Look.Open, "$name: open", false)
    }
}

/** A box's number: plain when filled, in a dashed brass outline as a preview, on brass when selected. */
@Composable
private fun ValueBox(box: BoxInfo, m: Metrics, modifier: Modifier, showOpen: Boolean) {
    val shape = RoundedCornerShape(m.boxRadius)
    val styled = when (box.look) {
        Look.Selected -> modifier.clip(shape).background(Felt.Brass)
        Look.Preview -> modifier.dashedOutline(Felt.Brass, m.boxRadius)
        Look.PreviewZero -> modifier.dashedOutline(Felt.Brass.copy(alpha = 0.45f), m.boxRadius)
        else -> modifier
    }
    val color = when (box.look) {
        Look.Filled -> Felt.Text
        Look.Open -> Felt.Dim
        Look.Preview -> Felt.Brass
        Look.PreviewZero -> Felt.BrassDim
        Look.Selected -> Felt.BrassInk
    }
    Box(styled.clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
        if (box.look != Look.Open || showOpen) {
            Label(
                box.text, Modifier, m.textSize, color,
                if (box.look == Look.Open) FontWeight.Normal else FontWeight.Bold,
            )
        }
    }
}

// Helpers.

/** One line of text that never wraps. */
@Composable
private fun Label(
    text: String,
    modifier: Modifier,
    size: TextUnit,
    color: Color,
    weight: FontWeight = FontWeight.Normal,
    align: TextAlign = TextAlign.Start,
    letterSpacing: TextUnit = TextUnit.Unspecified,
) {
    BasicText(
        text, modifier,
        style = TextStyle(
            color = color, fontSize = size, fontWeight = weight, textAlign = align, letterSpacing = letterSpacing,
            fontFeatureSettings = "tnum",
        ),
        maxLines = 1, softWrap = false,
    )
}

/** A category name that shrinks, down to 9 sp, to fit a narrow column ("Three of a kind" on a small phone). */
@Composable
private fun AutoLabel(text: String, modifier: Modifier, size: TextUnit, color: Color) {
    BasicText(
        text, modifier,
        style = TextStyle(color = color, fontSize = size),
        maxLines = 1, softWrap = false,
        autoSize = TextAutoSize.StepBased(minFontSize = 9.sp, maxFontSize = size, stepSize = 0.5.sp),
    )
}

/** A hairline along the top edge, between rows. */
private fun Modifier.hairline(): Modifier = drawBehind {
    drawRect(Felt.Line, size = Size(size.width, 1.dp.toPx()))
}

private fun Modifier.dashedOutline(color: Color, radius: Dp): Modifier = drawBehind {
    val w = 1.5.dp.toPx()
    drawRoundRect(
        color,
        topLeft = Offset(w / 2, w / 2),
        size = Size(size.width - w, size.height - w),
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))),
    )
}
