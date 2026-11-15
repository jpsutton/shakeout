package io.github.jpsutton.shakeout.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Test

class DiceToRollTest {
    private val all = setOf(0, 1, 2, 3, 4)

    @Test fun firstRollChangesEveryDie() {
        assertEquals(all, Game.diceToRoll(GameState.new(Mode.STANDARD)))
    }

    @Test fun laterRollsSkipHeldDice() {
        var g = Game.roll(GameState.new(Mode.STANDARD), Random(1))
        g = Game.toggleHold(g, 0)
        g = Game.toggleHold(g, 3)
        assertEquals(setOf(1, 2, 4), Game.diceToRoll(g))
        g = Game.toggleHold(g, 3)
        assertEquals(setOf(1, 2, 3, 4), Game.diceToRoll(g))
    }

    @Test fun nothingRollsWhenNoRollIsLeft() {
        var g = GameState.new(Mode.STANDARD)
        repeat(GameState.ROLLS_PER_TURN) { g = Game.roll(g, Random(it)) }
        assertEquals(emptySet<Int>(), Game.diceToRoll(g))
    }

    @Test fun rollChangesOnlyTheDiceToRollAndKeepsTheRest() {
        var g = Game.roll(GameState.new(Mode.TRIPLE), Random(5))
        g = Game.toggleHold(g, 1)
        g = Game.toggleHold(g, 2)
        val before = g.dice
        val toRoll = Game.diceToRoll(g)
        val after = Game.roll(g, Random(6)).dice
        for (i in 0 until GameState.DICE) if (i !in toRoll) assertEquals(before[i], after[i])
        assertEquals(setOf(0, 3, 4), toRoll)
    }
}
