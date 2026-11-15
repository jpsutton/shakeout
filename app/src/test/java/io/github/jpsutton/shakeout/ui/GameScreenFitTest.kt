package io.github.jpsutton.shakeout.ui

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import io.github.jpsutton.shakeout.game.Mode
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Lays the game screen out at common phone sizes, in both modes, and checks that the whole scorecard fits on screen:
 * nothing scrolls, so its last row must end inside the screen. Writes a report and one screenshot per case to
 * app/build/layout-report/ for a look at the result.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w1000dp-h1000dp-xxhdpi")
class GameScreenFitTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private class Screen(val width: Int, val height: Int, val topInset: Int, val bottomInset: Int) {
        val name get() = "${width}x$height"
    }

    // Window sizes in dp. Portrait screens lose the status bar (24 dp) and a 3-button navigation bar (48 dp).
    private val screens = listOf(
        Screen(320, 568, 24, 48), Screen(360, 640, 24, 48), Screen(360, 720, 24, 48), Screen(360, 780, 24, 48),
        Screen(393, 851, 24, 48), Screen(412, 915, 24, 48), Screen(384, 854, 24, 48), Screen(411, 731, 24, 48),
        Screen(480, 1000, 24, 48),
        // Landscape.
        Screen(640, 360, 24, 24), Screen(851, 393, 24, 24),
    )

    private var current by mutableStateOf(screens[0])

    @Test fun scorecardFitsWithoutScrollingOnCommonScreens() {
        val vm = ViewModelProvider(rule.activity)[GameViewModel::class.java]
        rule.setContent {
            val s = current
            Box(
                Modifier.size(s.width.dp, s.height.dp).testTag("screen").padding(top = s.topInset.dp, bottom = s.bottomInset.dp),
            ) {
                ShakeoutTheme { Surface { GameScreen(vm) {} } }
            }
        }
        val out = File("build/layout-report").apply { mkdirs() }
        val report = StringBuilder("screen  mode      row height (dp)  last row bottom / screen bottom (dp)\n")
        val problems = mutableListOf<String>()
        for (mode in Mode.entries) for (screen in screens) {
            vm.start(mode)
            current = screen
            rule.waitForIdle()

            val density = rule.activity.resources.displayMetrics.density
            fun bottomDp(node: androidx.compose.ui.test.SemanticsNodeInteraction) =
                node.fetchSemanticsNode().boundsInRoot.bottom / density
            val screenBottom = (screen.height - screen.bottomInset).toFloat()
            val last = if (mode == Mode.TRIPLE) rule.onNodeWithText("Column total") else rule.onNodeWithContentDescription("Chance, column 1: open")
            val lastBottom = bottomDp(last)
            val firstCell = rule.onNodeWithContentDescription("Ones, column 1: open").fetchSemanticsNode().boundsInRoot
            val rowHeight = (firstCell.bottom - firstCell.top) / density + 2 // the cell has 1 dp padding each side
            report.append("%-8s%-10s%-17.1f%.1f / %.1f%n".format(screen.name, mode.title, rowHeight, lastBottom, screenBottom))
            if (lastBottom > screenBottom + 0.5f) problems += "${mode.title} ${screen.name}: scorecard ends at $lastBottom dp, screen ends at $screenBottom dp"
            if (rowHeight < MIN_ROW_DP) problems += "${mode.title} ${screen.name}: rows are only $rowHeight dp tall"

            val image = rule.onNodeWithTag("screen").captureToImage().asAndroidBitmap()
            File(out, "${mode.name.lowercase()}-${screen.name}.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        File(out, "report.txt").writeText(report.toString())
        assertTrue("Layout problems:\n" + problems.joinToString("\n"), problems.isEmpty())
    }

    private companion object {
        /**
         * Smallest row height accepted. Fitting the card without scrolling takes about 18 dp rows on the tightest
         * cases (Triple on a 640x360 landscape phone, Triple on a 320x568 phone); well below Android's 48 dp touch
         * guideline, but each cell is wide (about 75 dp) and the alternative is a scrolling card.
         */
        const val MIN_ROW_DP = 18f
    }
}
