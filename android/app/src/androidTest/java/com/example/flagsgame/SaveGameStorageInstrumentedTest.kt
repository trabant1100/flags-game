package com.example.flagsgame

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONArray
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SaveGameStorageInstrumentedTest {

    private val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val file = File(ctx.filesDir, "saved_games.json")

    @After
    fun cleanup() {
        if (file.exists()) file.delete()
    }

    @Test
    fun saveRun_createsFileAndContainsExpectedStructure() {
        if (file.exists()) file.delete()

        val engine = GameEngine(3)
        // prepare deterministic pool
        val c1 = Country("PL", "🇵🇱", listOf("Polska"), "europe")
        val c2 = Country("DE", "🇩🇪", listOf("Niemcy"), "europe")
        val c3 = Country("FR", "🇫🇷", listOf("Francja"), "europe")
        engine.setCountries(listOf(c1, c2, c3))
        engine.startGame()

        // mark results: first correct, second idk, third wrong
        engine.getPoolReadOnly()[0].userGuess = "Polska"
        engine.getPoolReadOnly()[0].correct = true
        engine.getPoolReadOnly()[1].userGuess = ctx.getString(R.string.idk)
        engine.getPoolReadOnly()[1].correct = false
        engine.getPoolReadOnly()[2].userGuess = "WrongAnswer"
        engine.getPoolReadOnly()[2].correct = false
        val expectedScore = engine.getPoolReadOnly().count { it.correct }

        SaveGameStorage.saveRun(ctx, engine)

        assertTrue(file.exists())
        val arr = JSONArray(file.readText())
        assertEquals(1, arr.length())
        val run = arr.getJSONObject(0)
        assertEquals(expectedScore, run.getInt("score"))
        assertEquals(engine.getPoolReadOnly().size, run.getInt("total"))
        val items = run.getJSONArray("items")
        assertEquals(engine.getPoolReadOnly().size, items.length())

        val r0 = items.getJSONObject(0)
        assertEquals("correct", r0.getString("result"))
        val r1 = items.getJSONObject(1)
        assertEquals("idk", r1.getString("result"))
        val r2 = items.getJSONObject(2)
        assertEquals("wrong", r2.getString("result"))
    }

    @Test
    fun saveRun_appendsMultipleRuns() {
        if (file.exists()) file.delete()

        val engine = GameEngine(2)
        val c1 = Country("PL", "🇵🇱", listOf("Polska"), "europe")
        val c2 = Country("DE", "🇩🇪", listOf("Niemcy"), "europe")
        engine.setCountries(listOf(c1, c2))
        engine.startGame()

        engine.getPoolReadOnly()[0].userGuess = "Polska"
        engine.getPoolReadOnly()[0].correct = true
        engine.getPoolReadOnly()[1].userGuess = "Nie wiem"
        engine.getPoolReadOnly()[1].correct = false
        engine.getPoolReadOnly().count { it.correct }

        SaveGameStorage.saveRun(ctx, engine)
        SaveGameStorage.saveRun(ctx, engine)

        val arr = JSONArray(file.readText())
        assertEquals(2, arr.length())
    }
}
