package com.example.flagsgame

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.Normalizer
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

data class Country(
    val code: String,
    val flag: String,
    val names: List<String>,
    var norms: List<String> = listOf(),
    var displayName: String = "",
    var userGuess: String? = null,
    var correct: Boolean = false
)

class MainActivity : AppCompatActivity() {
    private val TOTAL = 10
    private lateinit var countries: MutableList<Country>
    private lateinit var pool: MutableList<Country>
    private var current = 0
    private var score = 0
    private var answered = false

    private lateinit var progressTv: TextView
    private lateinit var flagTv: TextView
    private lateinit var input: EditText
    private lateinit var submitBtn: Button
    private lateinit var idkBtn: Button
    private lateinit var feedbackTv: TextView
    private lateinit var resultContainer: LinearLayout
    private lateinit var resultHeader: TextView
    private lateinit var summaryList: LinearLayout
    private lateinit var restartBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressTv = findViewById(R.id.progress)
        flagTv = findViewById(R.id.flag)
        input = findViewById(R.id.guessInput)
        submitBtn = findViewById(R.id.submitBtn)
        idkBtn = findViewById(R.id.idkBtn)
        feedbackTv = findViewById(R.id.feedback)
        resultContainer = findViewById(R.id.resultContainer)
        resultHeader = findViewById(R.id.resultHeader)
        summaryList = findViewById(R.id.summaryList)
        restartBtn = findViewById(R.id.restartBtn)

        buildCountries()
        startGame()

        submitBtn.setOnClickListener {
            if (!answered) checkAnswer()
            else nextQuestion()
        }

        // Pressing Enter / IME action should submit the answer when the field is filled
        input.setOnEditorActionListener { _, actionId, event ->
            val isEnter = (actionId == EditorInfo.IME_ACTION_DONE) ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            if (isEnter) {
                if (!answered) {
                    // only check if input is not empty
                    if (!TextUtils.isEmpty(input.text.toString().trim())) checkAnswer()
                } else {
                    nextQuestion()
                }
                true
            } else false
        }

        idkBtn.setOnClickListener {
            if (!answered) {
                val target = pool[current]
                target.userGuess = "Nie wiem"
                target.correct = false
                showFeedback(false, getString(R.string.wrong_format, target.displayName))
                input.isEnabled = false
                answered = true
                Handler(Looper.getMainLooper()).postDelayed({ nextQuestion() }, 900)
            }
        }

        restartBtn.setOnClickListener { startGame() }
    }

    private fun normalize(s: String): String {
        val n = Normalizer.normalize(s, Normalizer.Form.NFD)
        return n.replace(Regex("\\p{M}"), "").lowercase().replace(Regex("[^a-z0-9\\s]"), "")
    }

    private fun buildCountries() {
        // Load countries from assets/countries.json to make the list configurable
        countries = try {
            loadCountriesFromAssets()
        } catch (e: Exception) {
            // fallback to a minimal hardcoded list if loading fails
            mutableListOf(
                Country("PL", "🇵🇱", listOf("Polska")),
                Country("DE", "🇩🇪", listOf("Niemcy")),
                Country("FR", "🇫🇷", listOf("Francja"))
            )
        }
    }

    private fun loadCountriesFromAssets(): MutableList<Country> {
        val list = mutableListOf<Country>()
        val json = assets.open("countries.json").bufferedReader().use { it.readText() }
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

    private fun startGame() {
        val copy = countries.toMutableList().shuffled(Random(System.currentTimeMillis())).toMutableList()
        pool = copy.take(TOTAL).map {
            it.norms = it.names.map { n -> normalize(n) }
            it.displayName = it.names[0]
            it.userGuess = null
            it.correct = false
            it
        }.toMutableList()
        current = 0
        score = 0
        answered = false
        resultContainer.visibility = View.GONE
        restartBtn.visibility = View.GONE
        summaryList.removeAllViews()
        showQuestion()
    }

    private fun showQuestion() {
        val c = pool[current]
        progressTv.text = "Pytanie ${current + 1}/$TOTAL"
        flagTv.text = c.flag
        feedbackTv.text = ""
        input.setText("")
        input.isEnabled = true
        input.requestFocus()
        submitBtn.text = "Sprawdź"
        answered = false
    }

    private fun checkAnswer() {
        val raw = input.text.toString().trim()
        if (TextUtils.isEmpty(raw)) {
            feedbackTv.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            feedbackTv.text = getString(R.string.empty_prompt)
            input.requestFocus()
            return
        }
        val g = normalize(raw)
        val target = pool[current]
        target.userGuess = raw
        val ok = target.norms.contains(g)
        target.correct = ok
        if (ok) {
            score++
            showFeedback(true, getString(R.string.correct))
            input.isEnabled = false
            answered = true
            Handler(Looper.getMainLooper()).postDelayed({ nextQuestion() }, 900)
        } else {
            showFeedback(false, getString(R.string.wrong_format, target.displayName))
            input.isEnabled = false
            submitBtn.text = if (current + 1 == TOTAL) "Zobacz wynik" else "Następne"
            answered = true
        }
    }

    private fun showFeedback(ok: Boolean, text: String) {
        val color = if (ok) android.R.color.holo_green_dark else android.R.color.holo_red_dark
        feedbackTv.setTextColor(resources.getColor(color))
        feedbackTv.text = text
    }

    private fun nextQuestion() {
        current++
        if (current >= TOTAL) showResult() else showQuestion()
    }

    private fun showResult() {
        resultContainer.visibility = View.VISIBLE
        restartBtn.visibility = View.VISIBLE
        resultHeader.text = getString(R.string.result_format, score, TOTAL)
        summaryList.removeAllViews()

        val good = pool.filter { it.correct }
        val bad = pool.filter { !it.correct }

        // correct answers
        if (good.isNotEmpty()) {
            val header = TextView(this)
            header.text = getString(R.string.correct_header)
            header.textSize = 16f
            header.setPadding(0, 12, 0, 6)
            summaryList.addView(header)
            for (g in good) {
                val item = LinearLayout(this)
                item.orientation = LinearLayout.HORIZONTAL
                item.setPadding(8, 8, 8, 8)
                val flag = TextView(this)
                flag.text = g.flag
                flag.textSize = 36f
                flag.width = (64 * resources.displayMetrics.density).toInt()
                item.addView(flag)
                val txt = TextView(this)
                txt.text = g.displayName
                txt.textSize = 15f
                item.addView(txt)
                summaryList.addView(item)
            }
        }

        if (bad.isNotEmpty()) {
            val header = TextView(this)
            header.text = getString(R.string.wrong_header)
            header.textSize = 16f
            header.setPadding(0, 12, 0, 6)
            summaryList.addView(header)
            for (b in bad) {
                val item = LinearLayout(this)
                item.orientation = LinearLayout.HORIZONTAL
                item.setPadding(8, 8, 8, 8)
                val flag = TextView(this)
                flag.text = b.flag
                flag.textSize = 36f
                flag.width = (64 * resources.displayMetrics.density).toInt()
                item.addView(flag)
                val txt = LinearLayout(this)
                txt.orientation = LinearLayout.VERTICAL
                val correctTv = TextView(this)
                correctTv.text = getString(R.string.correct_label, b.displayName)
                val yourTv = TextView(this)
                val user = if (!b.userGuess.isNullOrBlank()) b.userGuess else getString(R.string.your_label, "<brak>")
                yourTv.text = getString(R.string.your_label, user)
                yourTv.setTextColor(resources.getColor(android.R.color.darker_gray))
                txt.addView(correctTv)
                txt.addView(yourTv)
                item.addView(txt)
                summaryList.addView(item)
            }
        }
    }

}
