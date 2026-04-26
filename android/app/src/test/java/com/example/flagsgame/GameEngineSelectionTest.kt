package com.example.flagsgame

import org.junit.Assert.*
import org.junit.Test

class GameEngineSelectionTest {

    @Test
    fun startGame_respectsAvoidAndAskedCounts_and_isDeterministicWithSeed() {
        val engine = GameEngine(total = 3)
        val countries = listOf(
            Country("A", "flagA", listOf("Aland")),
            Country("B", "flagB", listOf("Bland")),
            Country("C", "flagC", listOf("Cland")),
            Country("D", "flagD", listOf("Dland"))
        )
        engine.setCountries(countries)

        val avoid = setOf("A")
        val asked = mapOf("A" to 10, "B" to 1, "C" to 2, "D" to 0)

        engine.startGame(avoidCodes = avoid, askedCounts = asked, seed = 12345L)

        val poolCodes = engine.getPoolReadOnly().map { it.code }

        // Avoided code should not be present
        assertFalse(poolCodes.contains("A"))

        // With asked counts 0,1,2 (D,B,C) and total=3 we expect D,B,C in that order
        assertEquals(listOf("D", "B", "C"), poolCodes)
    }
}
