package com.example.flagsgame

import android.view.View
import android.widget.LinearLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityUiTest {

    @Test
    fun restart_hides_results_and_clears_summary() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // choose default mode from startup dialog so tests can continue
            var modeText = ""
            scenario.onActivity { modeText = it.getString(R.string.mode_flag_to_country) }
            onView(withText(modeText)).perform(click())

            scenario.onActivity { activity ->
                // simulate showing results
                val resultContainer = activity.findViewById<View>(R.id.resultContainer)
                val restartBtn = activity.findViewById<View>(R.id.restartBtn)
                val summaryList = activity.findViewById<LinearLayout>(R.id.summaryList)
                resultContainer.visibility = View.VISIBLE
                restartBtn.visibility = View.VISIBLE
                // add a fake child to summary to simulate previous results
                summaryList.addView(android.widget.TextView(activity).apply { text = "old" })
            }

            // click restart
            onView(withId(R.id.restartBtn)).perform(click())

            // select mode on the restart dialog so restart proceeds
            onView(withText(modeText)).perform(click())

            // resultContainer should be gone
            onView(withId(R.id.resultContainer)).check(matches(withEffectiveVisibility(Visibility.GONE)))

            // summaryList should be empty
            scenario.onActivity { activity ->
                val summaryList = activity.findViewById<LinearLayout>(R.id.summaryList)
                assert(summaryList.childCount == 0)
            }
        }
    }

    @Test
    fun restart_restores_gameplay_controls() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // choose default mode from startup dialog so tests can continue
            var modeText = ""
            scenario.onActivity { modeText = it.getString(R.string.mode_flag_to_country) }
            onView(withText(modeText)).perform(click())

            scenario.onActivity { activity ->
                // simulate results state
                activity.findViewById<View>(R.id.resultContainer).visibility = View.VISIBLE
                activity.findViewById<View>(R.id.restartBtn).visibility = View.VISIBLE
                activity.findViewById<View>(R.id.flag).visibility = View.GONE
                activity.findViewById<View>(R.id.guessInput).visibility = View.GONE
                activity.findViewById<View>(R.id.submitBtn).visibility = View.GONE
                activity.findViewById<View>(R.id.idkBtn).visibility = View.GONE
            }

            onView(withId(R.id.restartBtn)).perform(click())

            // select mode on the restart dialog so restart proceeds
            onView(withText(modeText)).perform(click())

            // after restart, gameplay controls should be visible
            onView(withId(R.id.flag)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            onView(withId(R.id.guessInput)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            onView(withId(R.id.submitBtn)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            onView(withId(R.id.idkBtn)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        }
    }

    @Test
    fun start_map_to_country_for_all_continents() {
        val originalLoader = MainActivity.defaultLoader
        var countryUnderTest = 0
        var countriesCount = 1
        try {
            MainActivity.defaultLoader = object : CountriesLoader() {
                override fun loadFromAssets(
                    context: android.content.Context,
                    assetName: String
                ): MutableList<Country> {
                    val countries = originalLoader.loadFromAssets(context, assetName).filter { it.continent != null }
                    countriesCount = countries.size
                    val c = countries[countryUnderTest]
                    return mutableListOf(c)
                }
            }

            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                // choose default mode from startup dialog so tests can continue
                while (countryUnderTest < countriesCount) {
                    var modeText = ""
                    scenario.onActivity { modeText = it.getString(R.string.mode_map_to_country) }
                    onView(withText(modeText)).perform(click())
                    onView(withText("Wszystkie kontynenty")).perform(click())

                    onView(withId(R.id.cancelBtn)).perform(click())
                    countryUnderTest++
                }
            }
        } finally {
            MainActivity.defaultLoader = originalLoader
        }
    }
}
