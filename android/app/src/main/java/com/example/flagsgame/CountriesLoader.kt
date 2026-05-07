package com.example.flagsgame

import android.content.Context
import org.json.JSONArray

open class CountriesLoader {
    open fun loadFromAssets(context: Context, assetName: String = "countries.json"): MutableList<Country> {
        val json = context.assets.open(assetName).bufferedReader().use { it.readText() }
        return loadFromJsonString(json)
    }

    // Public helper to parse countries from a JSON string (useful in JVM tests)
    fun loadFromJsonString(json: String): MutableList<Country> {
        val list = mutableListOf<Country>()
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val code = o.getString("code")
            val flag = o.getString("flag")
            val namesArr = o.getJSONArray("names")
            val names = mutableListOf<String>()
            for (j in 0 until namesArr.length()) names.add(namesArr.getString(j))
            // optional capitals array
            val capitals = mutableListOf<String>()
            if (o.has("capitals") && !o.isNull("capitals")) {
                val caps = o.getJSONArray("capitals")
                for (j in 0 until caps.length()) capitals.add(caps.getString(j))
            }
            // optional continent
            val continent = if (o.has("continent")) o.getString("continent") else null
            val country = Country(code, flag, names, continent)
            country.capitals = capitals
            list.add(country)
        }
        return list
    }
}
