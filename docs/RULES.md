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
- At the end, column 2 totals are doubled and column 3 totals are tripled.
- Bonus: once all three five-of-a-kind boxes hold 50, a later five of a kind earns +100 if the box you place it in
  scores more than 0. The bonus is flat: it is added after the columns are multiplied, not multiplied itself.
- Joker: usable once all three five-of-a-kind boxes are filled (50 or 0). The dice go in the upper box of that face
  in a column where it is open. If that face is filled in all columns, any open lower box in any column (full value).
  If no lower box is open anywhere, scratch any open upper box.

## What is verified and what is a design choice

Only one thing about Triple was checked against a source: an online copy of the 1978 rules page. It says Triple is
three games played at once on a three-column card, with column 2 doubled and column 3 tripled. Its quoted
end-of-game steps multiply the columns first and then add bonus chips, which is why the bonus here is flat; that
part is an inference from those steps, not an explicit statement. The page's link to the full rulebook PDF redirects
to an HTML page, so the full text was never read, and no other rule sets were consulted.

Everything else in the Triple section (per-column 35 upper bonus, bonus eligibility, joker placement order,
scratching an upper box as a last resort) is the app author's choice. Published variants may differ; some online
implementations may multiply the bonus by its column, which I have not verified either way. The rules live in `Game`
and `Scoring` (`app/src/main/java/io/github/jpsutton/shakeout/game/`); change them and this file together.
