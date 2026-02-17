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

        // Load countries from assets and start the game
        try {
            val list = CountriesLoader.loadFromAssets(this)
            engine.setCountries(list)
            engine.startGame()
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

    private fun showQuestionUI() {
        val c = engine.getCurrent()
        progressTv.text = getString(R.string.question, engine.current + 1, engineTotal())
        flagTv.text = c.flag
        feedbackTv.text = ""
        input.setText("")
        input.isEnabled = true
        input.requestFocus()
        submitBtn.text = getString(R.string.check)
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
        resultContainer.visibility = View.VISIBLE
        restartBtn.visibility = View.VISIBLE
        resultHeader.text = getString(R.string.result_format, engine.score, engineTotal())
        summaryList.removeAllViews()

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

    private fun engineTotal() = engine.run { /* total */
        // Use pool size if initialized, otherwise default
        try {
            pool.size
        } catch (_: Exception) {
            10
        }
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
