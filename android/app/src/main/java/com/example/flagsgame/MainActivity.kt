package com.example.flagsgame

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
// animations removed per user request
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val engine = GameEngine(10)
    private var answered = false

    private lateinit var progressTv: TextView
    private lateinit var titleTv: TextView
    private lateinit var flagTv: TextView
    private lateinit var input: EditText
    private lateinit var submitBtn: Button
    private lateinit var idkBtn: Button
    private lateinit var feedbackTv: TextView
    private lateinit var resultContainer: LinearLayout
    private lateinit var resultHeader: TextView
    private lateinit var summaryList: LinearLayout
    private lateinit var restartBtn: Button

    companion object {
        private const val KEY_GAME_JSON = "game_state_json"
        private const val KEY_GAME_CURRENT = "game_current"
        private const val KEY_GAME_SCORE = "game_score"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressTv = findViewById(R.id.progress)
        titleTv = findViewById(R.id.title)
        flagTv = findViewById(R.id.flag)
        input = findViewById(R.id.guessInput)
        submitBtn = findViewById(R.id.submitBtn)
        idkBtn = findViewById(R.id.idkBtn)
        feedbackTv = findViewById(R.id.feedback)
        resultContainer = findViewById(R.id.resultContainer)
        resultHeader = findViewById(R.id.resultHeader)
        summaryList = findViewById(R.id.summaryList)
        restartBtn = findViewById(R.id.restartBtn)

        // Load countries from assets and start or restore the game
        try {
            val list = CountriesLoader.loadFromAssets(this)
            engine.setCountries(list)
            // if we have saved instance state, restore engine from it
            val savedJson = savedInstanceState?.getString(KEY_GAME_JSON)
            if (!savedJson.isNullOrEmpty()) {
                val restored = mutableListOf<Country>()
                val arr = org.json.JSONArray(savedJson)
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val namesArr = o.getJSONArray("names")
                    val names = mutableListOf<String>()
                    for (j in 0 until namesArr.length()) names.add(namesArr.getString(j))
                    val c = Country(o.getString("code"), o.getString("flag"), names)
                    c.displayName = o.optString("displayName", if (names.isNotEmpty()) names[0] else "")
                    c.userGuess = if (o.isNull("userGuess")) null else o.optString("userGuess", null)
                    c.correct = o.optBoolean("correct", false)
                    // norms may be absent; leave it for GameEngine.normalize if needed
                    restored.add(c)
                }
                val cur = savedInstanceState?.getInt(KEY_GAME_CURRENT, 0) ?: 0
                val sc = savedInstanceState?.getInt(KEY_GAME_SCORE, 0) ?: 0
                engine.restoreState(restored, cur, sc)
            } else {
                engine.startGame()
            }
        } catch (e: Exception) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Failed to load countries: ${e.message}")
                .setPositiveButton("OK") { d, _ -> d.dismiss() }
                .show()
            // set a minimal fallback so app still runs
            engine.setCountries(listOf(Country("PL", "🇵🇱", listOf("Polska"))))
            engine.startGame()
        }

        showQuestionUI()

        submitBtn.setOnClickListener {
            if (!answered) doCheck()
            else doNext()
        }

        input.setOnEditorActionListener { _, actionId, event ->
            val isEnter = (actionId == EditorInfo.IME_ACTION_DONE) ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            if (isEnter) {
                if (!answered) {
                    if (!TextUtils.isEmpty(input.text.toString().trim())) doCheck()
                } else {
                    doNext()
                }
                true
            } else false
        }

        idkBtn.setOnClickListener {
            if (!answered) {
                engine.markIDK(getString(R.string.idk))
                showFeedback(false, getString(R.string.wrong_format, engine.getCurrent().displayName))
                input.requestFocus()
                showKeyboard()
                answered = true
                Handler(Looper.getMainLooper()).postDelayed({ doNext() }, 900)
            }
        }

        restartBtn.setOnClickListener {
            engine.startGame()
            showQuestionUI()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try {
            val arr = org.json.JSONArray()
            for (c in engine.getPoolReadOnly()) {
                val o = org.json.JSONObject()
                o.put("code", c.code)
                o.put("flag", c.flag)
                val names = org.json.JSONArray()
                for (n in c.names) names.put(n)
                o.put("names", names)
                o.put("displayName", c.displayName)
                o.put("userGuess", c.userGuess ?: org.json.JSONObject.NULL)
                o.put("correct", c.correct)
                arr.put(o)
            }
            outState.putString(KEY_GAME_JSON, arr.toString())
            outState.putInt(KEY_GAME_CURRENT, engine.current)
            outState.putInt(KEY_GAME_SCORE, engine.score)
        } catch (_: Exception) {
        }
    }

    private fun showQuestionUI() {
        val c = engine.getCurrent()
        progressTv.text = getString(R.string.question, engine.current + 1, engineTotal())
        flagTv.text = c.flag
        feedbackTv.text = ""
        input.setText("")
        input.isEnabled = true
        // ensure gameplay controls are visible when showing a question
        flagTv.visibility = View.VISIBLE
        input.visibility = View.VISIBLE
        submitBtn.visibility = View.VISIBLE
        idkBtn.visibility = View.VISIBLE
        feedbackTv.visibility = View.VISIBLE
        // also show title and progress during gameplay
        titleTv.visibility = View.VISIBLE
        progressTv.visibility = View.VISIBLE
        input.requestFocus()
        submitBtn.text = getString(R.string.check)
        // hide previous results when showing a new question
        resultContainer.visibility = View.GONE
        restartBtn.visibility = View.GONE
        summaryList.removeAllViews()
        answered = false
    }

    private fun doCheck() {
        val raw = input.text.toString().trim()
        if (TextUtils.isEmpty(raw)) {
            feedbackTv.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            feedbackTv.text = getString(R.string.empty_prompt)
            input.requestFocus()
            return
        }
        val ok = engine.checkAnswer(raw)
        if (ok) {
            showFeedback(true, getString(R.string.correct))
            input.requestFocus()
            showKeyboard()
            answered = true
            Handler(Looper.getMainLooper()).postDelayed({ doNext() }, 900)
        } else {
            showFeedback(false, getString(R.string.wrong_format, engine.getCurrent().displayName))
            input.requestFocus()
            input.selectAll()
            showKeyboard()
            submitBtn.text = if (engine.current + 1 == engineTotal()) getString(R.string.see_result) else getString(R.string.next)
            answered = true
        }
    }

    private fun doNext() {
        engine.nextQuestion()
        if (engine.current >= engineTotal()) showResultUI() else showQuestionUI()
    }

    private fun showResultUI() {
        // hide gameplay UI when showing results
        flagTv.visibility = View.GONE
        input.visibility = View.GONE
        submitBtn.visibility = View.GONE
        idkBtn.visibility = View.GONE
        feedbackTv.visibility = View.GONE

        // hide title and progress when showing results
        titleTv.visibility = View.GONE
        progressTv.visibility = View.GONE

        resultContainer.visibility = View.VISIBLE
        restartBtn.visibility = View.VISIBLE
        resultHeader.text = getString(R.string.result_format, engine.score, engineTotal())
        summaryList.removeAllViews()

        // persist this completed run
        SaveGameStorage.saveRun(this, engine)

        val good = engine.goodList()
        val bad = engine.badList()
        val inflater = LayoutInflater.from(this)

        if (good.isNotEmpty()) {
            val header = TextView(this)
            header.text = getString(R.string.correct_header)
            header.textSize = 16f
            header.setPadding(0, 12, 0, 6)
            summaryList.addView(header)
            for (g in good) {
                val item = inflater.inflate(R.layout.summary_list_item, summaryList, false)
                val flag = item.findViewById<TextView>(R.id.flag)
                val correctAnswer = item.findViewById<TextView>(R.id.correct_answer)
                val userAnswer = item.findViewById<TextView>(R.id.user_answer)

                flag.text = g.flag
                correctAnswer.text = g.displayName
                userAnswer.visibility = View.GONE

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
                val item = inflater.inflate(R.layout.summary_list_item, summaryList, false)
                val flag = item.findViewById<TextView>(R.id.flag)
                val correctAnswer = item.findViewById<TextView>(R.id.correct_answer)
                val userAnswer = item.findViewById<TextView>(R.id.user_answer)

                flag.text = b.flag
                correctAnswer.text = getString(R.string.correct_label, b.displayName)
                val user = if (!b.userGuess.isNullOrBlank()) b.userGuess else "<brak>"
                userAnswer.text = getString(R.string.your_label, user)
                userAnswer.visibility = View.VISIBLE

                summaryList.addView(item)
            }
        }
    }

    private fun engineTotal() = try {
        engine.getPoolReadOnly().size
    } catch (_: Exception) {
        10
    }

    private fun showFeedback(ok: Boolean, text: String) {
        val color = if (ok) android.R.color.holo_green_dark else android.R.color.holo_red_dark
        feedbackTv.setTextColor(ContextCompat.getColor(this, color))
        feedbackTv.text = text
    }

    private fun showKeyboard() {
        try {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
        } catch (_: Exception) {
        }
    }

    // animations removed per user request
}
