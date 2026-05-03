package com.example.flagsgame

enum class Continent(val drawable: Int) {
    EUROPE(R.drawable.europe) {
        override fun getCodes(countryCode: String): List<String> {
            return when (countryCode) {
                "ru" -> listOf("ru-main", "ru-kaliningrad")
                "mt" -> listOf("mt-malta", "mt-gozo")
                else -> listOf(countryCode)
            }
        }
    },
    ASIA(R.drawable.asia),
    AFRICA(R.drawable.africa),
    NORTH_AMERICA(R.drawable.north_america),;

    open fun getCodes(countryCode: String) : List<String> {
        return listOf(countryCode)
    }

//    fun getDrawable(): Int {
//        return drawable
//    }

    companion object {
        fun fromName(name: String): Continent {
            val enumName = name.replace("-", "_")
            return entries.find { it.name.equals(enumName, ignoreCase = true) }!!
        }
    }
}