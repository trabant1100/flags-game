package com.example.flagsgame

import android.content.Context
import org.json.JSONArray

object CountriesLoader {
    fun loadFromAssets(context: Context, assetName: String = "countries.json"): MutableList<Country> {
        val list = mutableListOf<Country>()
        val json = context.assets.open(assetName).bufferedReader().use { it.readText() }
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
            // optional continent and shapes
            val continent = if (o.has("continent")) o.optString("continent", "") else ""
            val shapesMap = mutableMapOf<String, String>()
            if (o.has("shapes") && !o.isNull("shapes")) {
                val shapesObj = o.getJSONObject("shapes")
                val keys = shapesObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    shapesMap[k] = shapesObj.optString(k, "")
                }
            }
            val country = Country(code, flag, names)
            country.continent = continent
            country.shapes = shapesMap
            country.capitals = capitals
            list.add(country)
        }
        return list
    }
}
