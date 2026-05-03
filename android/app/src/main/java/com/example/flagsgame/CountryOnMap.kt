package com.example.flagsgame

class CountryOnMap(country: Country) {
    private val country: Country = country.copy(code = country.code.lowercase())
    val continent: Continent = Continent.fromName(country.continent)
    fun getCodes(): List<String> {
        return continent.getCodes(country.code)
    }
}