package io.github.jpsutton.shakeout.data

import android.content.Context
import io.github.jpsutton.shakeout.game.GameCodec
import io.github.jpsutton.shakeout.game.GameState
import io.github.jpsutton.shakeout.game.Mode

/** What survives between launches: the unfinished game and the best score of each mode. */
class Store(context: Context) {
    private val prefs = context.getSharedPreferences("shakeout", Context.MODE_PRIVATE)

    fun loadGame(): GameState? = prefs.getString(GAME, null)?.let(GameCodec::decode)

    fun saveGame(state: GameState?) {
        prefs.edit().apply { if (state == null) remove(GAME) else putString(GAME, GameCodec.encode(state)) }.apply()
    }

    fun best(mode: Mode): Int = prefs.getInt(BEST + mode.name, 0)

    /** Record [score] if it beats the best; returns whether it did. */
    fun submit(mode: Mode, score: Int): Boolean {
        if (score <= best(mode)) return false
        prefs.edit().putInt(BEST + mode.name, score).apply()
        return true
    }

    private companion object {
        const val GAME = "game"
        const val BEST = "best_"
    }
}
