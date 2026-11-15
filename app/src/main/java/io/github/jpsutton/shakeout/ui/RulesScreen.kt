package io.github.jpsutton.shakeout.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val Sections = listOf(
    "Your turn" to "Roll five dice up to three times. After a roll, tap dice to hold them; held dice stay put on the " +
        "next roll. Then score the turn in one open box. Every box is used once, so some turns you must score 0.",
    "Upper boxes" to "Ones to Sixes score the sum of the dice showing that number. Reach 63 in the upper boxes " +
        "(three of each number) to earn a 35 point bonus.",
    "Lower boxes" to "Three and four of a kind score the sum of all dice. Full house is 25, small straight (four in " +
        "a row) 30, large straight (five in a row) 40, five of a kind 50, and chance is the sum of all dice.",
    "Bonus and joker" to "If your five of a kind box holds 50 and you roll five of a kind again, add 100. The joker " +
        "rule then applies: use the matching upper box if open, else any open lower box (full house and " +
        "straights score full value), else any open upper box.",
    "Triple mode" to "Three cards, scored x1, x2 and x3, and you pick any open box on any card each turn. Each card " +
        "has its own 63 point upper bonus. The bonus for repeat fives of a kind needs all three five of a kind boxes " +
        "at 50 and is a flat 100. The joker rule applies once all three are filled.",
)

@Composable
fun RulesScreen(onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("How scoring works", style = MaterialTheme.typography.headlineSmall)
        Sections.forEach { (title, body) ->
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            "Shakeout is an independent dice game and is not affiliated with or endorsed by any publisher.",
            style = MaterialTheme.typography.bodySmall,
        )
        TextButton(onClick = onBack) { Text("Back") }
    }
}
