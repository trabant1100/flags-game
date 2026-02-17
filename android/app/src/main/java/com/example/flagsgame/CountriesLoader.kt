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
            list.add(Country(code, flag, names))
        }
        return list
    }
}
