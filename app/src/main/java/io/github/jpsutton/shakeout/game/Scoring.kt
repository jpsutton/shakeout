package io.github.jpsutton.shakeout.game

/** Score of five dice in a category. Pure rules, no game state. */
object Scoring {
    const val UPPER_BONUS_THRESHOLD = 63
    const val UPPER_BONUS = 35
    const val FULL_HOUSE = 25
    const val SMALL_STRAIGHT = 30
    const val LARGE_STRAIGHT = 40
    const val YAHTZEE = 50
    const val YAHTZEE_BONUS = 100

    fun counts(dice: List<Int>): IntArray {
        val counts = IntArray(7)
        for (d in dice) counts[d]++
        return counts
    }

    fun isYahtzee(dice: List<Int>): Boolean = dice.size == 5 && dice.all { it == dice[0] } && dice[0] in 1..6

    /**
     * Score [dice] in [category]. With [joker] set (a Yahtzee played under the joker rule), full house and
     * the straights score their full value even though the dice do not form them.
     */
    fun score(category: Category, dice: List<Int>, joker: Boolean = false): Int {
        val counts = counts(dice)
        val sum = dice.sum()
        return when (category) {
            Category.ONES, Category.TWOS, Category.THREES,
            Category.FOURS, Category.FIVES, Category.SIXES -> counts[category.face] * category.face
            Category.THREE_OF_A_KIND -> if (counts.any { it >= 3 }) sum else 0
            Category.FOUR_OF_A_KIND -> if (counts.any { it >= 4 }) sum else 0
            Category.FULL_HOUSE ->
                if (joker || (counts.any { it == 3 } && counts.any { it == 2 })) FULL_HOUSE else 0
            Category.SMALL_STRAIGHT -> if (joker || hasRun(counts, 4)) SMALL_STRAIGHT else 0
            Category.LARGE_STRAIGHT -> if (joker || hasRun(counts, 5)) LARGE_STRAIGHT else 0
            Category.YAHTZEE -> if (isYahtzee(dice)) YAHTZEE else 0
            Category.CHANCE -> sum
        }
    }

    /** Whether [counts] holds [length] consecutive faces. */
    private fun hasRun(counts: IntArray, length: Int): Boolean {
        var run = 0
        for (face in 1..6) {
            run = if (counts[face] > 0) run + 1 else 0
            if (run >= length) return true
        }
        return false
    }
}
