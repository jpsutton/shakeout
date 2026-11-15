package io.github.jpsutton.shakeout.game

import kotlin.random.Random

/**
 * Immutable state of one game. [scores] holds one map per column; a category absent from it is open.
 * [bonusPoints] is the total of five-of-a-kind bonus chips. Each bonus is worth 100 times the multiplier of the column it was placed in.
 * [dice] is five zeros until the first roll of a turn.
 */
data class GameState(
    val mode: Mode,
    val scores: List<Map<Category, Int>>,
    val bonusPoints: Int = 0,
    val dice: List<Int> = List(DICE) { 0 },
    val held: Set<Int> = emptySet(),
    val rollsLeft: Int = ROLLS_PER_TURN,
) {
    val hasRolled: Boolean get() = rollsLeft < ROLLS_PER_TURN
    val turnsPlayed: Int get() = scores.sumOf { it.size }
    val isOver: Boolean get() = turnsPlayed == mode.columns * Category.entries.size

    companion object {
        const val DICE = 5
        const val ROLLS_PER_TURN = 3

        fun new(mode: Mode): GameState = GameState(mode, List(mode.columns) { emptyMap() })
    }
}

/** Game rules over a [GameState]: rolling, legal placements, scoring and totals. */
object Game {
    fun roll(state: GameState, random: Random = Random.Default): GameState {
        if (state.rollsLeft == 0 || state.isOver) return state
        val dice = state.dice.mapIndexed { i, d ->
            if (i in state.held && state.hasRolled) d else random.nextInt(1, 7)
        }
        return state.copy(dice = dice, rollsLeft = state.rollsLeft - 1)
    }

    /** Toggle holding die [index]; only between rolls of a turn. */
    fun toggleHold(state: GameState, index: Int): GameState {
        if (!state.hasRolled || state.rollsLeft == 0 || index !in 0 until GameState.DICE) return state
        return state.copy(held = if (index in state.held) state.held - index else state.held + index)
    }

    fun isOpen(state: GameState, p: Placement): Boolean = p.category !in state.scores[p.column]

    private fun openBoxes(state: GameState): List<Placement> =
        (0 until state.mode.columns).flatMap { col ->
            Category.entries.filter { it !in state.scores[col] }.map { Placement(col, it) }
        }

    /**
     * The joker rule applies: a five of a kind rolled after every five-of-a-kind box has been filled. In Triple
     * mode it also needs the matching upper box filled in every column; until then the roll is placed like any other.
     */
    fun isJoker(state: GameState): Boolean {
        if (!state.hasRolled || !Scoring.isFiveOfAKind(state.dice)) return false
        if (!state.scores.all { Category.FIVE_KIND in it }) return false
        return state.mode == Mode.STANDARD || Category.upperFor(state.dice[0]).let { upper -> state.scores.all { upper in it } }
    }

    /** Boxes the current dice may be scored in. Empty before the first roll. */
    fun legalPlacements(state: GameState): List<Placement> {
        if (!state.hasRolled || state.isOver) return emptyList()
        val open = openBoxes(state)
        if (!isJoker(state)) return open
        val matching = Category.upperFor(state.dice[0])
        open.filter { it.category == matching }.takeIf { it.isNotEmpty() }?.let { return it }
        open.filter { !it.category.isUpper }.takeIf { it.isNotEmpty() }?.let { return it }
        return open
    }

    /** Points the current dice would score at [p] (joker rule applied). */
    fun scoreFor(state: GameState, p: Placement): Int =
        Scoring.score(p.category, state.dice, joker = isJoker(state))

    /** Whether scoring the current dice at [p] also earns the five-of-a-kind bonus. */
    fun earnsBonus(state: GameState, p: Placement): Boolean {
        if (!state.hasRolled || !Scoring.isFiveOfAKind(state.dice)) return false
        return when (state.mode) {
            Mode.STANDARD -> state.scores[0][Category.FIVE_KIND] == Scoring.FIVE_KIND
            // All three five-of-a-kind boxes hold 50, and the placement scores something (docs/RULES.md).
            Mode.TRIPLE -> state.scores.all { it[Category.FIVE_KIND] == Scoring.FIVE_KIND } &&
                scoreFor(state, p) > 0
        }
    }

    /** Score the current dice at [p] and start the next turn. Returns [state] if [p] is not legal. */
    fun place(state: GameState, p: Placement): GameState {
        if (p !in legalPlacements(state)) return state
        val points = scoreFor(state, p)
        val bonus = if (earnsBonus(state, p)) Scoring.FIVE_KIND_BONUS * state.mode.multiplier(p.column) else 0
        val scores = state.scores.mapIndexed { col, m -> if (col == p.column) m + (p.category to points) else m }
        return GameState(
            mode = state.mode,
            scores = scores,
            bonusPoints = state.bonusPoints + bonus,
        )
    }

    fun upperSum(state: GameState, column: Int): Int =
        Category.upper.sumOf { state.scores[column][it] ?: 0 }

    fun upperBonus(state: GameState, column: Int): Int =
        if (upperSum(state, column) >= Scoring.UPPER_BONUS_THRESHOLD) Scoring.UPPER_BONUS else 0

    /** One column's total, upper bonus included, before its multiplier and without five-of-a-kind bonuses. */
    fun columnTotal(state: GameState, column: Int): Int =
        state.scores[column].values.sum() + upperBonus(state, column)

    fun total(state: GameState): Int =
        (0 until state.mode.columns).sumOf { columnTotal(state, it) * state.mode.multiplier(it) } +
            state.bonusPoints
}
