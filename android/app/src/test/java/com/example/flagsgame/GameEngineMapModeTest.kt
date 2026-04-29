package com.example.flagsgame

import org.junit.Assert.*
import org.junit.Test

class GameEngineMapModeTest {

    @Test
    fun countries_have_continent_and_shapes_parsed() {
        val engine = GameEngine(total = 2)
        val countries = listOf(
            Country("DE", "🇩🇪", listOf("Niemcy"), continent = "europe", shapes = mapOf("europe" to "M10,10 L90,10 L90,60 L10,60 Z")),
            Country("FR", "🇫🇷", listOf("Francja"), continent = "europe", shapes = mapOf("europe" to "M20,30 L80,30 L80,80 L20,80 Z"))
        )
        engine.setCountries(countries)
        engine.startGame(seed = 123L)

        val pool = engine.getPoolReadOnly()
        assertEquals(2, pool.size)
        // ensure continent and shapes accessible
        assertEquals("europe", pool[0].continent)
        assertTrue(pool[0].shapes.containsKey("europe"))
    }
}
