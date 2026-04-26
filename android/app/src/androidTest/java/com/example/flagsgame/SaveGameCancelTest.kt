package com.example.flagsgame

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SaveGameCancelTest {

    @Test
    fun cancel_showsModeDialog_and_startsNewGame() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // choose startup mode
            var modeText = ""
            scenario.onActivity { modeText = it.getString(R.string.mode_flag_to_country) }
            onView(withText(modeText)).perform(click())

            // ensure gameplay controls visible
            onView(withId(R.id.flag)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

            // click cancel
            onView(withId(R.id.cancelBtn)).perform(click())

            // choose capital mode on the restart dialog
            var capModeText = ""
            scenario.onActivity { capModeText = it.getString(R.string.mode_country_to_capital) }
            onView(withText(capModeText)).perform(click())

            // after choosing mode, gameplay UI should be visible again
            onView(withId(R.id.flag)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            onView(withId(R.id.guessInput)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        }
    }

    @Test
    fun cancel_doesNotPersistRun() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        // ensure saved file removed before test
        ctx.deleteFile("saved_games.json")

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // select a mode to start
            var modeText = ""
            scenario.onActivity { modeText = it.getString(R.string.mode_flag_to_country) }
            onView(withText(modeText)).perform(click())

            // cancel the run
            onView(withId(R.id.cancelBtn)).perform(click())

            // select same mode to restart
            onView(withText(modeText)).perform(click())

            // verify no runs were saved
            val arr = SaveGameStorage.readAll(ctx)
            assertEquals(0, arr.length())
        }
    }
}
