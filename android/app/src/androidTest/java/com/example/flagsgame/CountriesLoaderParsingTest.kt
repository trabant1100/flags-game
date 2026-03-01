package com.example.flagsgame

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CountriesLoaderParsingTest {

    @Test
    fun loadFromAssets_parsesCapitals_for_PL() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val list = CountriesLoader.loadFromAssets(ctx)
        val pl = list.find { it.code == "PL" }
        assertNotNull("PL should be present in countries.json", pl)
        assertTrue("PL should have at least one capital", pl!!.capitals.isNotEmpty())
        assertTrue("PL capitals should contain Warszawa", pl.capitals.contains("Warszawa"))
    }
}
