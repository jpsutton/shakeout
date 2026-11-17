package io.github.jpsutton.shakeout.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * The "Felt & Brass" palette: a green card table, ivory dice and brass for whatever needs the player's eye (the
 * score, held dice, the box picked, the main button). The table is the same in light and dark system themes.
 */
object Felt {
    /** The table: screen background. */
    val Table = Color(0xFF0F3B2B)

    /** The dice tray, sunk into the table. */
    val Tray = Color(0xFF0A2C20)

    /** Raised panels: the scorecard, menu cards, dialogs. */
    val Panel = Color(0xFF134733)

    /** Secondary buttons and dice not rolled yet. */
    val Raised = Color(0xFF1E5A42)

    val Ivory = Color(0xFFFBF8F0)
    val DieEdge = Color(0xFFC9C2AE)
    val Pip = Color(0xFF1C1C1A)

    val Text = Color(0xFFEEF3EF)

    /** Labels and secondary text; 6.9:1 on [Table]. */
    val Muted = Color(0xFFA9C2B4)

    /** Placeholders for open boxes; 5:1 on [Panel]. */
    val Dim = Color(0xFF8FB0A0)

    val Brass = Color(0xFFE7B54E)
    val BrassInk = Color(0xFF241A04)

    /** Brass for a box that would score 0. */
    val BrassDim = Color(0xFFC9A659)

    /** Hairlines between scorecard rows. */
    val Line = Color(0x12FFFFFF)
}

private val Scheme = darkColorScheme(
    primary = Felt.Brass,
    onPrimary = Felt.BrassInk,
    secondary = Felt.Raised,
    onSecondary = Felt.Text,
    secondaryContainer = Felt.Raised,
    onSecondaryContainer = Felt.Text,
    background = Felt.Table,
    onBackground = Felt.Text,
    surface = Felt.Table,
    onSurface = Felt.Text,
    onSurfaceVariant = Felt.Muted,
    surfaceContainer = Felt.Panel,
    surfaceContainerHigh = Felt.Panel,
    surfaceContainerHighest = Felt.Panel,
    outline = Felt.Dim,
    outlineVariant = Felt.Line,
)

@Composable
fun ShakeoutTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
