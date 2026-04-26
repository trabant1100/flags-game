package com.example.flagsgame

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SaveGameStorage {
    private const val FILE_NAME = "saved_games.json"

    fun saveRun(context: Context, engine: GameEngine, mode: GameMode = GameMode.FLAG_TO_COUNTRY) {
        try {
            val file = File(context.filesDir, FILE_NAME)
            val arr = if (file.exists()) {
                val text = file.readText()
                if (text.isBlank()) JSONArray() else JSONArray(text)
            } else JSONArray()

            val run = JSONObject()
            val timestamp = System.currentTimeMillis()
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            run.put("timestamp", timestamp)
            run.put("time", sdf.format(Date(timestamp)))
            // compute score from pool to avoid relying on mutable internal counters
            val computedScore = engine.getPoolReadOnly().count { it.correct }
            run.put("score", computedScore)
            run.put("mode", mode.name)
            run.put("total", engineTotal(engine))

            val items = JSONArray()
            for (c in engine.getPoolReadOnly()) {
                val it = JSONObject()
                it.put("code", c.code)
                it.put("flag", c.flag)
                it.put("displayName", c.displayName)
                it.put("userGuess", c.userGuess ?: JSONObject.NULL)
                val result = when {
                    c.userGuess == null -> "unanswered"
                    c.userGuess == context.getString(R.string.idk) -> "idk"
                    c.correct -> "correct"
                    else -> "wrong"
                }
                it.put("result", result)
                items.put(it)
            }
            run.put("items", items)

            arr.put(run)
            file.writeText(arr.toString(2))
        } catch (_: Exception) {
        }
    }

    private fun engineTotal(engine: GameEngine): Int {
        return try { engine.getPoolReadOnly().size } catch (_: Exception) { 10 }
    }

    fun readAll(context: Context): JSONArray {
        return try {
            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) JSONArray()
            else {
                val text = file.readText()
                if (text.isBlank()) JSONArray() else JSONArray(text)
            }
        } catch (_: Exception) {
            JSONArray()
        }
    }

    /**
     * Return set of country codes that have been answered in previous saved runs.
     * An answered item is any item whose "result" is not "unanswered".
     */
    fun getAnsweredCodes(context: Context): Set<String> = getAnsweredCodes(context, GameMode.FLAG_TO_COUNTRY)

    fun getAnsweredCodes(context: Context, mode: GameMode): Set<String> {
        val codes = mutableSetOf<String>()
        try {
            val arr = readAll(context)
            for (i in 0 until arr.length()) {
                val run = arr.getJSONObject(i)
                val runMode = try { GameMode.valueOf(run.optString("mode", GameMode.FLAG_TO_COUNTRY.name)) } catch (_: Exception) { GameMode.FLAG_TO_COUNTRY }
                if (runMode != mode) continue
                val items = run.optJSONArray("items") ?: continue
                for (j in 0 until items.length()) {
                    val it = items.getJSONObject(j)
                    val result = it.optString("result", "unanswered")
                    if (result != "unanswered") {
                        val code = it.optString("code", "")
                        if (code.isNotEmpty()) codes.add(code)
                    }
                }
            }
        } catch (_: Exception) {
        }
        return codes
    }

    /**
     * Return a map of country code -> number of times it appears in saved runs.
     * Counts all occurrences in saved runs (asked count), regardless of result.
     */
    fun getAskedCounts(context: Context): Map<String, Int> = getAskedCounts(context, GameMode.FLAG_TO_COUNTRY)

    fun getAskedCounts(context: Context, mode: GameMode): Map<String, Int> {
        val counts = mutableMapOf<String, Int>()
        try {
            val arr = readAll(context)
            for (i in 0 until arr.length()) {
                val run = arr.getJSONObject(i)
                val runMode = try { GameMode.valueOf(run.optString("mode", GameMode.FLAG_TO_COUNTRY.name)) } catch (_: Exception) { GameMode.FLAG_TO_COUNTRY }
                if (runMode != mode) continue
                val items = run.optJSONArray("items") ?: continue
                for (j in 0 until items.length()) {
                    val it = items.getJSONObject(j)
                    val code = it.optString("code", "")
                    if (code.isNotEmpty()) {
                        val prev = counts[code] ?: 0
                        counts[code] = prev + 1
                    }
                }
            }
        } catch (_: Exception) {
        }
        return counts
    }
}
