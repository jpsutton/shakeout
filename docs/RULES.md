# Shakeout rules

Five dice, up to three rolls per turn (keep any dice between rolls), then score the turn in one open box.
13 turns per column; every box is filled exactly once.

## Boxes

| Box | Score |
| --- | --- |
| Ones to Sixes | Sum of dice showing that face |
| Upper bonus | 35 if the upper section totals 63 or more |
| Three of a kind | Sum of all dice, if 3+ match |
| Four of a kind | Sum of all dice, if 4+ match |
| Full house | 25 for three of one face and two of another |
| Small straight | 30 for four in sequence |
| Large straight | 40 for five in sequence |
| Yahtzee (five of a kind) | 50 |
| Chance | Sum of all dice |

A box that does not match scores 0 (a "scratch").

## Standard mode (one column)

- Bonus: a later five of a kind earns +100 if the Yahtzee box holds 50. A Yahtzee box scratched to 0 earns no bonuses.
- Joker: with the Yahtzee box filled (50 or 0), a later five of a kind must go in the matching upper box if open.
  If that is filled, use any open lower box (full house, straights score full value).
  If all lower boxes are filled too, scratch any open upper box.

## Triple mode (three columns, x1 / x2 / x3)

Each turn you choose any open box in any column.

- Every column scores like a standard card, with its own 63 -> 35 upper bonus.
- At the end, column 2 totals are doubled and column 3 totals are tripled.
- Yahtzee bonus: once all three Yahtzee boxes hold 50, a later five of a kind earns +100 if the box you place it in
  scores more than 0. The chip is multiplied by the column it is placed in (x1/x2/x3).
- Joker: usable once all three Yahtzee boxes are filled (50 or 0). The dice go in the upper box of that face in a
  column where it is open. If that face is filled in all columns, any open lower box in any column (full value).
  If no lower box is open anywhere, scratch any open upper box.

## Sources and design choices (Triple)

Verified: the 1978 rules page on yahtzeemanifesto.com says Triple is three simultaneous games on a three-column
card, scores in column 2 are doubled and column 3 tripled, and bonus chips are added after the columns are
multiplied. The page's PDF link redirects to an HTML page, so the full 1978 text was not read.

Everything else in the Triple section above (per-column 35 upper bonus, bonus eligibility, bonus multiplied by its
column, joker placement order, upper-box scratch as last resort) is a design choice by the author of this app,
not checked against a published rulebook. Published variants differ on these points; if one matters to you,
change `TripleRules` in the game engine (to be added) and this file together.
