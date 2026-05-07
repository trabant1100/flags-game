package com.example.flagsgame

class CountryOnMap(country: Country) {
    private val country: Country = country.copy(code = country.code.lowercase())
    val continent = country.continent?.let { Continent.fromName(country.continent) }

    fun getCodes(): List<String> {
        return continent?.getCodes(country.code) ?: listOf(country.code)
    }
}