package io.github.jpsutton.shakeout.ui

import android.app.Application
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.jpsutton.shakeout.data.Store
import io.github.jpsutton.shakeout.game.Game
import io.github.jpsutton.shakeout.game.GameState
import io.github.jpsutton.shakeout.game.Mode
import io.github.jpsutton.shakeout.game.Placement
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel(app: Application) : AndroidViewModel(app) {
    private val store = Store(app)

    /** The game in progress, or null on the menu. */
    var game: GameState? by mutableStateOf(null)
        private set

    /** The box picked on the card, waiting for the player to confirm. */
    var selected: Placement? by mutableStateOf(null)
        private set

    var saved: GameState? by mutableStateOf(store.loadGame())
        private set

    /** Set when the finished game beat the stored best. */
    var newBest: Boolean by mutableStateOf(false)
        private set

    var bests: Map<Mode, Int> by mutableStateOf(Mode.entries.associateWith(store::best))
        private set

    fun start(mode: Mode) {
        newBest = false
        selected = null
        rolling = emptySet()
        game = GameState.new(mode).also(store::saveGame)
        saved = game
    }

    fun resume() {
        newBest = false
        selected = null
        rolling = emptySet()
        game = saved
    }

    fun toMenu() {
        game = null
        selected = null
        rolling = emptySet()
    }

    /** Dice tumbling right now. The roll itself is applied when they stop, so no result shows early. */
    var rolling: Set<Int> by mutableStateOf(emptySet())
        private set

    fun roll() {
        val g = game ?: return
        if (rolling.isNotEmpty()) return
        val toRoll = Game.diceToRoll(g)
        if (toRoll.isEmpty()) return
        selected = null
        if (!animationsEnabled()) {
            update(Game.roll(g))
            return
        }
        rolling = toRoll
        viewModelScope.launch {
            delay(ROLL_ANIMATION_MS)
            rolling = emptySet()
            // Skip the roll if the player left this game while the dice were tumbling.
            if (game === g) update(Game.roll(g))
        }
    }

    /** False when the system animation scale is 0 (the "remove animations" setting): dice then change at once. */
    private fun animationsEnabled(): Boolean =
        Settings.Global.getFloat(getApplication<Application>().contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f

    fun toggleHold(index: Int) {
        val g = game ?: return
        if (rolling.isNotEmpty()) return
        update(Game.toggleHold(g, index))
    }

    fun select(p: Placement) {
        val g = game ?: return
        if (rolling.isNotEmpty()) return
        if (p in Game.legalPlacements(g)) selected = if (selected == p) null else p
    }

    fun confirm() {
        val g = game ?: return
        val p = selected ?: return
        selected = null
        val next = Game.place(g, p)
        update(next)
        if (next.isOver) {
            newBest = store.submit(next.mode, Game.total(next))
            bests = Mode.entries.associateWith(store::best)
            store.saveGame(null)
            saved = null
        }
    }

    private fun update(next: GameState) {
        game = next
        if (!next.isOver) {
            store.saveGame(next)
            saved = next
        }
    }
}
