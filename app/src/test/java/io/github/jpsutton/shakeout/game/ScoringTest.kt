package io.github.jpsutton.shakeout.game

import io.github.jpsutton.shakeout.game.Category.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoringTest {
    private fun s(c: Category, vararg d: Int, joker: Boolean = false) = Scoring.score(c, d.toList(), joker)

    @Test fun upperBoxesSumMatchingFaces() {
        assertEquals(3, s(ONES, 1, 3, 1, 1, 4))
        assertEquals(0, s(SIXES, 1, 2, 3, 4, 5))
        assertEquals(20, s(FOURS, 4, 4, 4, 4, 4))
    }

    @Test fun kinds() {
        assertEquals(16, s(THREE_OF_A_KIND, 3, 3, 3, 4, 3))
        assertEquals(0, s(THREE_OF_A_KIND, 3, 3, 4, 4, 5))
        assertEquals(22, s(FOUR_OF_A_KIND, 5, 5, 5, 5, 2))
        assertEquals(0, s(FOUR_OF_A_KIND, 5, 5, 5, 2, 2))
        assertEquals(10, s(FOUR_OF_A_KIND, 2, 2, 2, 2, 2))
    }

    @Test fun fullHouse() {
        assertEquals(25, s(FULL_HOUSE, 2, 2, 3, 3, 3))
        assertEquals(0, s(FULL_HOUSE, 2, 2, 3, 3, 4))
        assertEquals(0, s(FULL_HOUSE, 4, 4, 4, 4, 4))
        assertEquals(25, s(FULL_HOUSE, 4, 4, 4, 4, 4, joker = true))
    }

    @Test fun straights() {
        assertEquals(30, s(SMALL_STRAIGHT, 1, 2, 3, 4, 6))
        assertEquals(30, s(SMALL_STRAIGHT, 3, 4, 5, 6, 6))
        assertEquals(30, s(SMALL_STRAIGHT, 2, 3, 3, 4, 5))
        assertEquals(0, s(SMALL_STRAIGHT, 1, 2, 3, 5, 6))
        assertEquals(40, s(LARGE_STRAIGHT, 2, 3, 4, 5, 6))
        assertEquals(40, s(LARGE_STRAIGHT, 1, 2, 3, 4, 5))
        assertEquals(0, s(LARGE_STRAIGHT, 1, 2, 3, 4, 6))
        assertEquals(30, s(SMALL_STRAIGHT, 5, 5, 5, 5, 5, joker = true))
        assertEquals(40, s(LARGE_STRAIGHT, 5, 5, 5, 5, 5, joker = true))
    }

    @Test fun yahtzeeAndChance() {
        assertEquals(50, s(YAHTZEE, 6, 6, 6, 6, 6))
        assertEquals(0, s(YAHTZEE, 6, 6, 6, 6, 5))
        assertEquals(15, s(CHANCE, 1, 2, 3, 4, 5))
        assertTrue(Scoring.isYahtzee(listOf(1, 1, 1, 1, 1)))
        assertFalse(Scoring.isYahtzee(listOf(0, 0, 0, 0, 0)))
    }
}
