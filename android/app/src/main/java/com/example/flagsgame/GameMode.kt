package com.example.flagsgame

import android.content.Context
import androidx.annotation.StringRes

enum class GameMode(
    @param:StringRes val modeNameRes: Int,
    @param:StringRes val hintRes: Int,
    val isMapMode: Boolean = false
) {
    FLAG_TO_COUNTRY(
        R.string.mode_flag_to_country,
        R.string.guess_hint
    ),
    COUNTRY_TO_CAPITAL(
        R.string.mode_country_to_capital,
        R.string.guess_capital_hint
    ),
    MAP_TO_COUNTRY(
        R.string.mode_map_to_country,
        R.string.guess_hint,
        isMapMode = true
    );

    /**
     * Returns the string that should be displayed as the "correct answer"
     * for this mode.
     */
    fun getCorrectAnswer(country: Country): String {
        return when (this) {
            COUNTRY_TO_CAPITAL -> country.capitalDisplay.ifBlank { country.displayName }
            else -> country.displayName
        }
    }

    /**
     * Returns the title text to be displayed for the given country in this mode.
     */
    fun getTitle(context: Context, country: Country): String {
        return when (this) {
            COUNTRY_TO_CAPITAL -> country.displayName
            FLAG_TO_COUNTRY -> context.getString(R.string.app_name)
            MAP_TO_COUNTRY -> context.getString(R.string.map_mode)
        }
    }
}
