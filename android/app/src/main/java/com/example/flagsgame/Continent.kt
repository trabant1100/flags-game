package com.example.flagsgame

enum class Continent(val drawable: Int) {
    EUROPE(R.drawable.europe) {
        override fun getCodes(countryCode: String): List<String> {
            return when (countryCode) {
                "ru" -> listOf("ru-main", "ru-kaliningrad")
                "mt" -> listOf("mt-malta", "mt-gozo")
                else -> super.getCodes(countryCode)
            }
        }
    },
    ASIA(R.drawable.asia),
    AFRICA(R.drawable.africa),
    NORTH_AMERICA(R.drawable.north_america),
    SOUTH_AMERICA(R.drawable.south_america)
    ;

    private val rects = listOf(
        "ae", "bd", "lb", "hk", "lk", "il", "sg", // rects in svg
    )

    private val autoRects = listOf(
        // generate rects
        "bi", "cv", "dj", "gm", "gw", "gq", "km", "ls", "mu", "rw", "sc", "st", "sz",
        "ad", "va", "mc", "li", "lu",
    )

    open fun getCodes(countryCode: String) : List<String> {
        if (countryCode in rects) {
            return listOf(countryCode, "$countryCode-rect")
        } else if (countryCode in autoRects) {
            return listOf(countryCode, "$countryCode-autorect")
        }
        return listOf(countryCode)
    }

    companion object {
        fun fromName(name: String): Continent {
            val enumName = name.replace("-", "_")
            return entries.find { it.name.equals(enumName, ignoreCase = true) }!!
        }
    }
}