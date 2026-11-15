package io.github.jpsutton.shakeout.ui

import io.github.jpsutton.shakeout.game.Category
import io.github.jpsutton.shakeout.game.Mode

val Category.label: String
    get() = when (this) {
        Category.ONES -> "Ones"
        Category.TWOS -> "Twos"
        Category.THREES -> "Threes"
        Category.FOURS -> "Fours"
        Category.FIVES -> "Fives"
        Category.SIXES -> "Sixes"
        Category.THREE_OF_A_KIND -> "Three of a kind"
        Category.FOUR_OF_A_KIND -> "Four of a kind"
        Category.FULL_HOUSE -> "Full house"
        Category.SMALL_STRAIGHT -> "Small straight"
        Category.LARGE_STRAIGHT -> "Large straight"
        Category.FIVE_KIND -> "Five of a kind"
        Category.CHANCE -> "Chance"
    }

val Mode.title: String
    get() = when (this) {
        Mode.STANDARD -> "Regular"
        Mode.TRIPLE -> "Triple"
    }

val Mode.blurb: String
    get() = when (this) {
        Mode.STANDARD -> "One scorecard, 13 turns."
        Mode.TRIPLE -> "Three scorecards worth x1, x2 and x3, 39 turns."
    }
