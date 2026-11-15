package io.github.jpsutton.shakeout.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameCodecTest {
    @Test fun roundTripsEveryStageOfBothModes() {
        val rng = Random(7)
        for (mode in Mode.entries) {
            var g = GameState.new(mode)
            while (true) {
                assertEquals(g, GameCodec.decode(GameCodec.encode(g)))
                if (g.isOver) break
                g = Game.roll(g, rng)
                assertEquals(g, GameCodec.decode(GameCodec.encode(g)))
                g = Game.toggleHold(g, 1)
                assertEquals(g, GameCodec.decode(GameCodec.encode(g)))
                g = Game.place(g, Game.legalPlacements(g).first())
            }
        }
    }

    @Test fun rejectsGarbage() {
        assertNull(GameCodec.decode(""))
        assertNull(GameCodec.decode("STANDARD|0|1,2,3|0|3|"))
        assertNull(GameCodec.decode("QUAD|0|0,0,0,0,0||3|"))
        assertNull(GameCodec.decode("TRIPLE|0|0,0,0,0,0||3|;"))
        assertNull(GameCodec.decode("STANDARD|0|0,0,0,0,9||3|"))
        assertNull(GameCodec.decode("STANDARD|0|0,0,0,0,0||7|"))
        assertNull(GameCodec.decode("STANDARD|0|0,0,0,0,0||3|ONES"))
    }
}
