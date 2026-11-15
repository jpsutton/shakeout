package io.github.jpsutton.shakeout.game

import io.github.jpsutton.shakeout.game.Category.*
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameTest {
    /** A state mid-turn: [scores] per column, dice already rolled. */
    private fun state(
        mode: Mode,
        dice: List<Int>,
        vararg columns: Map<Category, Int>,
        bonus: Int = 0,
    ) = GameState(mode, columns.toList(), bonusPoints = bonus, dice = dice, rollsLeft = 2)

    private val allBut = { open: Set<Category>, yahtzee: Int ->
        Category.entries.filter { it !in open }.associateWith { if (it == YAHTZEE) yahtzee else 0 }
    }

    @Test fun rollKeepsHeldDiceAndCountsRolls() {
        var g = GameState.new(Mode.STANDARD)
        assertTrue(Game.legalPlacements(g).isEmpty())
        g = Game.toggleHold(g, 0)
        assertTrue("cannot hold before first roll", g.held.isEmpty())
        g = Game.roll(g, Random(1))
        assertEquals(2, g.rollsLeft)
        val first = g.dice
        g = Game.toggleHold(g, 0)
        g = Game.toggleHold(g, 3)
        g = Game.roll(g, Random(2))
        assertEquals(first[0], g.dice[0])
        assertEquals(first[3], g.dice[3])
        g = Game.roll(g, Random(3))
        assertEquals(0, g.rollsLeft)
        assertEquals("no fourth roll", g, Game.roll(g, Random(4)))
        assertEquals("no hold after last roll", g, Game.toggleHold(g, 1))
    }

    @Test fun placingScoresAndStartsNextTurn() {
        val g = state(Mode.STANDARD, listOf(2, 2, 2, 5, 5), emptyMap())
        val next = Game.place(g, Placement(0, FULL_HOUSE))
        assertEquals(25, next.scores[0][FULL_HOUSE])
        assertFalse(next.hasRolled)
        assertTrue(next.held.isEmpty())
        assertEquals(g, Game.place(g, Placement(0, FULL_HOUSE)).let { g })
        assertEquals("filled box is illegal", next.copy(dice = g.dice, rollsLeft = 1),
            Game.place(next.copy(dice = g.dice, rollsLeft = 1), Placement(0, FULL_HOUSE)))
    }

    @Test fun upperBonusAt63() {
        val upper = mapOf(ONES to 3, TWOS to 6, THREES to 9, FOURS to 12, FIVES to 15, SIXES to 18)
        var g = GameState(Mode.STANDARD, listOf(upper))
        assertEquals(63, Game.upperSum(g, 0))
        assertEquals(35, Game.upperBonus(g, 0))
        assertEquals(63 + 35, Game.total(g))
        g = GameState(Mode.STANDARD, listOf(upper + (SIXES to 12)))
        assertEquals(0, Game.upperBonus(g, 0))
    }

    @Test fun standardYahtzeeBonusNeedsFiftyInBox() {
        val open = setOf(CHANCE, SIXES)
        val dice = List(5) { 6 }
        val withFifty = state(Mode.STANDARD, dice, allBut(open, 50))
        val next = Game.place(withFifty, Placement(0, SIXES))
        assertEquals(100, next.bonusPoints)
        assertEquals(30, next.scores[0][SIXES])
        val scratched = state(Mode.STANDARD, dice, allBut(open, 0))
        assertEquals(0, Game.place(scratched, Placement(0, SIXES)).bonusPoints)
    }

    @Test fun firstYahtzeeIsNotABonus() {
        val g = state(Mode.STANDARD, List(5) { 3 }, emptyMap())
        val next = Game.place(g, Placement(0, YAHTZEE))
        assertEquals(50, next.scores[0][YAHTZEE])
        assertEquals(0, next.bonusPoints)
        assertFalse(Game.isJoker(g))
        assertEquals(13, Game.legalPlacements(g).size)
    }

    @Test fun jokerForcesMatchingUpperBoxFirst() {
        val open = setOf(FOURS, FULL_HOUSE, CHANCE)
        val g = state(Mode.STANDARD, List(5) { 4 }, allBut(open, 50))
        assertTrue(Game.isJoker(g))
        assertEquals(listOf(Placement(0, FOURS)), Game.legalPlacements(g))
    }

    @Test fun jokerUsesLowerBoxAtFullValueWhenUpperIsFilled() {
        val open = setOf(FULL_HOUSE, LARGE_STRAIGHT, TWOS)
        val g = state(Mode.STANDARD, List(5) { 4 }, allBut(open, 50))
        assertEquals(setOf(Placement(0, FULL_HOUSE), Placement(0, LARGE_STRAIGHT)), Game.legalPlacements(g).toSet())
        assertEquals(25, Game.scoreFor(g, Placement(0, FULL_HOUSE)))
        assertEquals(40, Game.scoreFor(g, Placement(0, LARGE_STRAIGHT)))
    }

    @Test fun jokerScratchesUpperBoxWhenNoLowerIsOpen() {
        val open = setOf(TWOS, THREES)
        val g = state(Mode.STANDARD, List(5) { 4 }, allBut(open, 50))
        assertEquals(setOf(Placement(0, TWOS), Placement(0, THREES)), Game.legalPlacements(g).toSet())
        assertEquals(0, Game.scoreFor(g, Placement(0, TWOS)))
    }

    @Test fun jokerAppliesWithScratchedYahtzeeBoxToo() {
        val open = setOf(SIXES, CHANCE)
        val g = state(Mode.STANDARD, List(5) { 6 }, allBut(open, 0))
        assertTrue(Game.isJoker(g))
        assertEquals(listOf(Placement(0, SIXES)), Game.legalPlacements(g))
    }

    @Test fun tripleStartsWithAnyBoxInAnyColumn() {
        val g = state(Mode.TRIPLE, listOf(1, 2, 3, 4, 6), emptyMap(), emptyMap(), emptyMap())
        assertEquals(39, Game.legalPlacements(g).size)
    }

    @Test fun tripleTotalsMultiplyColumns() {
        val g = GameState(
            Mode.TRIPLE,
            listOf(mapOf(CHANCE to 20), mapOf(CHANCE to 20), mapOf(CHANCE to 20)),
        )
        assertEquals(20 + 40 + 60, Game.total(g))
    }

    @Test fun tripleUpperBonusIsPerColumnThenMultiplied() {
        val upper = mapOf(ONES to 3, TWOS to 6, THREES to 9, FOURS to 12, FIVES to 15, SIXES to 18)
        val g = GameState(Mode.TRIPLE, listOf(emptyMap(), emptyMap(), upper))
        assertEquals((63 + 35) * 3, Game.total(g))
    }

    @Test fun tripleJokerNeedsAllThreeYahtzeeBoxesFilled() {
        val open = setOf(CHANCE, SIXES)
        val dice = List(5) { 6 }
        val two = state(Mode.TRIPLE, dice, allBut(open, 50), allBut(open, 50), allBut(open + YAHTZEE, 0))
        assertFalse(Game.isJoker(two))
        assertEquals(7, Game.legalPlacements(two).size)
        val three = state(Mode.TRIPLE, dice, allBut(open, 50), allBut(open, 50), allBut(open, 50))
        assertTrue(Game.isJoker(three))
        assertEquals(
            setOf(Placement(0, SIXES), Placement(1, SIXES), Placement(2, SIXES)),
            Game.legalPlacements(three).toSet(),
        )
    }

    @Test fun tripleJokerMovesToLowerBoxesWhenFaceIsFilledEverywhere() {
        val open = setOf(CHANCE, FULL_HOUSE)
        val g = state(Mode.TRIPLE, List(5) { 2 }, allBut(open, 50), allBut(open, 50), allBut(open, 50))
        assertEquals(6, Game.legalPlacements(g).size)
        assertTrue(Game.legalPlacements(g).none { it.category.isUpper })
    }

    @Test fun tripleBonusIsMultipliedByColumn() {
        val open = setOf(CHANCE, SIXES)
        val dice = List(5) { 6 }
        val g = state(Mode.TRIPLE, dice, allBut(open, 50), allBut(open, 50), allBut(open, 50))
        assertEquals(100, Game.place(g, Placement(0, SIXES)).bonusPoints)
        assertEquals(200, Game.place(g, Placement(1, SIXES)).bonusPoints)
        val placed = Game.place(g, Placement(2, SIXES))
        assertEquals(300, placed.bonusPoints)
        assertEquals(30, placed.scores[2][SIXES])
    }

    @Test fun tripleBonusNeedsAllThreeYahtzeeBoxesAtFifty() {
        val open = setOf(CHANCE, SIXES)
        val dice = List(5) { 6 }
        val g = state(Mode.TRIPLE, dice, allBut(open, 50), allBut(open, 50), allBut(open, 0))
        assertEquals(0, Game.place(g, Placement(0, SIXES)).bonusPoints)
    }

    @Test fun tripleBonusNeedsPointsInTheBox() {
        val open = setOf(TWOS, THREES)
        val dice = List(5) { 6 }
        val g = state(Mode.TRIPLE, dice, allBut(open, 50), allBut(open, 50), allBut(open, 50))
        assertEquals("only upper boxes are open, so the joker scratches", 0, Game.scoreFor(g, Placement(0, TWOS)))
        assertEquals(0, Game.place(g, Placement(0, TWOS)).bonusPoints)
    }

    @Test fun gameEndsAfterAllBoxesAreFilled() {
        var g = GameState.new(Mode.STANDARD)
        val rng = Random(42)
        var turns = 0
        while (!g.isOver) {
            g = Game.roll(g, rng)
            g = Game.place(g, Game.legalPlacements(g).first())
            turns++
        }
        assertEquals(13, turns)
        assertTrue(Game.legalPlacements(g).isEmpty())

        var t = GameState.new(Mode.TRIPLE)
        turns = 0
        while (!t.isOver) {
            t = Game.roll(t, rng)
            t = Game.place(t, Game.legalPlacements(t).first())
            turns++
        }
        assertEquals(39, turns)
        assertTrue(Game.total(t) >= 0)
    }
}
