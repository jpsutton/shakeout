package io.github.jpsutton.shakeout.game

/**
 * One-line text form of a [GameState], for saving an unfinished game:
 * `mode|bonus|dice|held|rollsLeft|column;column;...` with a column as `CATEGORY=score,CATEGORY=score`.
 */
object GameCodec {
    fun encode(state: GameState): String = listOf(
        state.mode.name,
        state.bonusPoints.toString(),
        state.dice.joinToString(","),
        state.held.sorted().joinToString(","),
        state.rollsLeft.toString(),
        state.scores.joinToString(";") { col -> col.entries.joinToString(",") { "${it.key.name}=${it.value}" } },
    ).joinToString("|")

    /** The decoded state, or null if [text] is not a valid, consistent saved game. */
    fun decode(text: String): GameState? = try {
        val parts = text.split("|", limit = 6)
        require(parts.size == 6)
        val mode = Mode.valueOf(parts[0])
        val dice = parts[2].split(",").map { it.toInt() }
        val held = if (parts[3].isEmpty()) emptySet() else parts[3].split(",").map { it.toInt() }.toSet()
        val rollsLeft = parts[4].toInt()
        val scores = parts[5].split(";", limit = mode.columns + 1).map { col ->
            if (col.isEmpty()) emptyMap() else col.split(",").associate {
                val (name, score) = it.split("=")
                Category.valueOf(name) to score.toInt()
            }
        }
        require(scores.size == mode.columns)
        require(dice.size == GameState.DICE && dice.all { it in 0..6 })
        require(held.all { it in 0 until GameState.DICE })
        require(rollsLeft in 0..GameState.ROLLS_PER_TURN)
        GameState(mode, scores, parts[1].toInt(), dice, held, rollsLeft)
    } catch (_: IllegalArgumentException) {
        null
    } catch (_: IndexOutOfBoundsException) {
        null
    }
}
