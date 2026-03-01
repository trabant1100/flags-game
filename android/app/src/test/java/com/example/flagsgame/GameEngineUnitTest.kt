package com.example.flagsgame

import org.junit.Assert.*
import org.junit.Test

class GameEngineUnitTest {

    @Test
    fun startGame_avoidsPreviouslyAnswered_whenEnoughCandidates() {
        val engine = GameEngine(2)
        val countries = listOf(
            Country("A", "🏳️", listOf("Aland")),
            Country("B", "🏴", listOf("Bland")),
            Country("C", "🏁", listOf("Cland"))
        )
        engine.setCountries(countries)

        val avoid = setOf("A")
        engine.startGame(avoidCodes = avoid, seed = 42L)

        val poolCodes = engine.getPoolReadOnly().map { it.code }.toSet()
        assertFalse("Pool should not contain avoided code A", poolCodes.contains("A"))
        assertEquals(2, poolCodes.size)
    }

    @Test
    fun startGame_prefersLessAsked_whenSelectingPool() {
        val engine = GameEngine(3)
        val countries = listOf(
            Country("A", "🇦", listOf("Aland")),
            Country("B", "🇧", listOf("Bland")),
            Country("C", "🇨", listOf("Cland")),
            Country("D", "🇩", listOf("Dland")),
            Country("E", "🇪", listOf("Eland"))
        )
        engine.setCountries(countries)

        // asked counts: A most asked, C least
        val counts = mapOf("A" to 10, "B" to 5, "C" to 0, "D" to 1, "E" to 3)

        engine.startGame(avoidCodes = emptySet(), askedCounts = counts, seed = 123L)

        val selected = engine.getPoolReadOnly().map { it.code }
        // expect the three least-asked codes: C (0), D (1), E (3)
        assertTrue(selected.containsAll(listOf("C", "D", "E")))
        assertEquals(3, selected.size)
    }
}
