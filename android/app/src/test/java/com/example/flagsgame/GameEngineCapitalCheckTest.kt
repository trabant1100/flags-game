package com.example.flagsgame

import org.junit.Assert.*
import org.junit.Test

class GameEngineCapitalCheckTest {

    @Test
    fun checkCapitalAnswer_acceptsNormalizedVariants_and_updatesScore() {
        val engine = GameEngine(total = 1)
        // capitals include a diacritic variant and an ascii fallback
        val country = Country("PL", "flagPL", listOf("Polska"))
        country.capitals = listOf("Łódź", "Lodz")
        engine.setCountries(listOf(country))

        engine.startGame(seed = 42L)

        // normalized input without diacritics should match
        val ok1 = engine.checkCapitalAnswer("lodz")
        assertTrue(ok1)

        // score should be incremented
        assertEquals(1, engine.score)
    }
}
