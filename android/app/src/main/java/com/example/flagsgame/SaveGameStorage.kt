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

    fun saveRun(context: Context, engine: GameEngine) {
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
            run.put("score", engine.score)
            run.put("total", engineTotal(engine))

            val items = JSONArray()
            for (c in engine.pool) {
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
        return try { engine.pool.size } catch (_: Exception) { 10 }
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
}
