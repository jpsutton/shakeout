package io.github.jpsutton.shakeout.game

/** The 13 boxes of one scorecard column, in card order. */
enum class Category(val face: Int = 0) {
    ONES(1), TWOS(2), THREES(3), FOURS(4), FIVES(5), SIXES(6),
    THREE_OF_A_KIND, FOUR_OF_A_KIND, FULL_HOUSE, SMALL_STRAIGHT, LARGE_STRAIGHT, FIVE_KIND, CHANCE;

    val isUpper: Boolean get() = face != 0

    companion object {
        val upper: List<Category> = entries.filter { it.isUpper }
        val lower: List<Category> = entries.filter { !it.isUpper }

        fun upperFor(face: Int): Category = upper[face - 1]
    }
}

/** Game variants. [columns] is both the number of scorecard columns and the highest multiplier. */
enum class Mode(val columns: Int) {
    STANDARD(1),
    TRIPLE(3);

    fun multiplier(column: Int): Int = column + 1
}

/** One box on the card: a [category] in a [column] (0-based). */
data class Placement(val column: Int, val category: Category)
