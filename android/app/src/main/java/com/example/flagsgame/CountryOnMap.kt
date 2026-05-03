package com.example.flagsgame

class CountryOnMap(country: Country) {
    private val country: Country = country.copy(code = country.code.lowercase())
    fun getCodes(): List<String> {
        return when (country.continent) {
            "europe" -> getEuropeCodes(country.code)
            else -> throw NotImplementedError("Continent ${country.continent} not supported yet")
            // TODO add more continent-specific codes if needed, e.g. for US states
        } ?: listOf(country.code.lowercase())
    }

    private fun getEuropeCodes(countryCode: String): List<String>? {
        return when (countryCode) {
            "ru" -> listOf("ru-main", "ru-kaliningrad")
            "mt" -> listOf("mt-malta", "mt-gozo")
            else -> null
        }
    }
}