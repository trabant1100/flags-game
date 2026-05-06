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

    fun setCountries(list: List<Country>) {
        _countries.clear()
        _countries.addAll(list)
    }


    fun startGame(avoidCodes: Set<String> = emptySet(), askedCounts: Map<String, Int> = emptyMap(), seed: Long? = null) {
        val rng = if (seed != null) Random(seed) else Random(System.currentTimeMillis())

        val shuffled = _countries.toMutableList().shuffled(rng)

        // TODO tmp for display
//            .filter { listOf("fk").contains(it.code.lowercase()) }

        // prefer countries not in avoidCodes, and among them prefer lower askedCounts
        val candidates = shuffled
            .filter { it.code !in avoidCodes }
            .sortedWith(compareBy({ askedCounts[it.code] ?: 0 }, { rng.nextDouble() }))
            .toMutableList()

        val selected = mutableListOf<Country>()
        selected.addAll(candidates.take(total))

        if (selected.size < total) {
            // fill remaining from remaining (including previously answered), preferring lower askedCounts
            val remainingNeeded = total - selected.size
            val fallback = shuffled
                .filter { c -> selected.none { it.code == c.code } }
                .sortedWith(compareBy({ askedCounts[it.code] ?: 0 }, { rng.nextDouble() }))
                .take(remainingNeeded)
            selected.addAll(fallback)
        }

        pool = selected.map {
            it.norms = it.names.map { n -> normalize(n) }
            it.capNorms = it.capitals.map { c -> normalize(c) }
            it.displayName = if (it.names.isNotEmpty()) it.names[0] else it.displayName
            it.capitalDisplay = if (it.capitals.isNotEmpty()) it.capitals[0] else ""
            it.userGuess = null
            it.correct = false
            it
        }.toMutableList()
        current = 0
        score = 0
    }

    fun getCurrent(): Country = pool[current]

    fun checkAnswer(raw: String, mode: GameMode): Boolean {
        val g = normalize(raw)
        val target = pool[current]
        target.userGuess = raw
        val ok = when (mode) {
            GameMode.COUNTRY_TO_CAPITAL -> target.capNorms.contains(g)
            else -> target.norms.contains(g)
        }
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
