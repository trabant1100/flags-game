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


    fun startGame(avoidCodes: Set<String> = emptySet(), askedCounts: Map<String, Int> = emptyMap(), seed: Long? = null) {
        val rng = if (seed != null) Random(seed) else Random(System.currentTimeMillis())
        val shuffled = _countries.toMutableList().shuffled(rng)

        // prefer countries not in avoidCodes, and among them prefer lower askedCounts
        val candidates = shuffled
            .filter { it.code !in avoidCodes }
            .sortedWith(compareBy({ askedCounts[it.code] ?: 0 }, { it.code }))
            .toMutableList()

        val selected = mutableListOf<Country>()
        selected.addAll(candidates.take(total))

        if (selected.size < total) {
            // fill remaining from remaining (including previously answered), preferring lower askedCounts
            val remainingNeeded = total - selected.size
            val fallback = shuffled
                .filter { c -> selected.none { it.code == c.code } }
                .sortedWith(compareBy({ askedCounts[it.code] ?: 0 }, { it.code }))
                .take(remainingNeeded)
            selected.addAll(fallback)
        }

        pool = selected.map {
            it.norms = it.names.map { n -> normalize(n) }
            it.displayName = if (it.names.isNotEmpty()) it.names[0] else it.displayName
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

    // Public accessors for read-only external access
    fun getTotal(): Int = total
    fun getPoolReadOnly(): List<Country> = if (::pool.isInitialized) pool else emptyList()
    
    /** Restore engine state from an external pool snapshot. */
    fun restoreState(poolList: List<Country>, currentIndex: Int, scoreVal: Int) {
        pool = poolList.toMutableList()
        current = currentIndex
        score = scoreVal
    }
    private fun normalize(s: String): String {
        val n = Normalizer.normalize(s, Normalizer.Form.NFD)
        return n.replace(Regex("\\p{M}"), "").lowercase().replace(Regex("[^a-z0-9\\s]"), "")
    }
}
