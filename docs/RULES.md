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
| Five of a kind | 50 |
| Chance | Sum of all dice |

A box that does not match scores 0 (a "scratch").

## Regular mode (one column)

- Bonus: a later five of a kind earns +100 if the five-of-a-kind box holds 50. A box scratched to 0 earns no bonuses.
- Joker: with the five-of-a-kind box filled (50 or 0), a later five of a kind must go in the matching upper box if open.
  If that is filled, use any open lower box (full house and straights score full value).
  If all lower boxes are filled too, scratch any open upper box.

## Triple mode (three columns, x1 / x2 / x3)

Each turn you choose any open box in any column. 39 turns.

- Every column scores like a regular card, with its own 63 -> 35 upper bonus.
- At the end, column 2 totals are doubled and column 3 totals are tripled; the upper bonus is part of the column total.
- Bonus: once all three five-of-a-kind boxes hold 50, a later five of a kind earns +100 if the box you place it in
  scores more than 0. The bonus is worth 100 x the column it is placed in (100, 200 or 300).
- Joker: applies only when all three five-of-a-kind boxes are filled (50 or 0) and the matching upper box is filled
  in all three columns. The dice then go in any open lower box in any column at full value (full house 25,
  straights 30/40), never in an upper box. If no lower box is open anywhere, scratch any open upper box.
- Until the joker applies, a five of a kind is placed like any other roll: any open box, scored as rolled.

## Sources and what is a design choice

The regular rules are the standard published ones. For Triple, the only source read directly was an online copy of
the 1978 rules page: three games played at once on a three-column card, column 2 doubled, column 3 tripled, bonus
chips added at the end.

The rest of the Triple section (the bonus being worth 100 x its column, the joker waiting until every five-of-a-kind
box and the matching upper box are filled, strict bonus eligibility, the upper-box scratch as a last resort) was set
from a rules search whose results could not be re-inspected when this was written. Treat those points as the app
author's choices rather than checked rulebook text. Published variants differ. The rules live in `Game` and `Scoring`
(`app/src/main/java/io/github/jpsutton/shakeout/game/`); change them and this file together.
