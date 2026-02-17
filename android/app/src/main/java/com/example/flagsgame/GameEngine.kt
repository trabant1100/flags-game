package com.example.flagsgame

import java.text.Normalizer
import kotlin.random.Random

class GameEngine(private val total: Int = 10) {
    private val _countries: MutableList<Country> = mutableListOf()
    lateinit var pool: MutableList<Country>
        private set
    var current = 0
        private set
    var score = 0
        private set

    val isFinished: Boolean
        get() = ::pool.isInitialized && current >= total

    fun setCountries(list: List<Country>) {
        _countries.clear()
        _countries.addAll(list)
    }

    fun startGame() {
        val copy = _countries.toMutableList().shuffled(Random(System.currentTimeMillis())).toMutableList()
        pool = copy.take(total).map {
            it.norms = it.names.map { n -> normalize(n) }
            it.displayName = it.names[0]
            it.userGuess = null
            it.correct = false
            it
        }.toMutableList()
        current = 0
        score = 0
    }

    fun getCurrent(): Country = pool[current]

    fun questionProgressText(): String = "Pytanie ${current + 1}/$total"

    fun checkAnswer(raw: String): Boolean {
        val g = normalize(raw)
        val target = pool[current]
        target.userGuess = raw
        val ok = target.norms.contains(g)
        target.correct = ok
        if (ok) score++
        return ok
    }

    fun markIDK(idkText: String) {
        val target = pool[current]
        target.userGuess = idkText
        target.correct = false
    }

    fun nextQuestion() {
        current++
    }

    fun goodList() = pool.filter { it.correct }
    fun badList() = pool.filter { !it.correct }

    private fun normalize(s: String): String {
        val n = Normalizer.normalize(s, Normalizer.Form.NFD)
        return n.replace(Regex("\\p{M}"), "").lowercase().replace(Regex("[^a-z0-9\\s]"), "")
    }
}
