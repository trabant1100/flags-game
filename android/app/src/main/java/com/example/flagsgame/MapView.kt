package com.example.flagsgame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.dorukkangal.vectormaster.VectorMasterDrawable

/**
 * Simple view that draws SVG pathData strings for a continent map and highlights one country.
 * Expect paths supplied as map of country code -> pathData. Path data should be in viewport coordinates
 * normalized to a similar viewbox for all countries (user is responsible for providing compatible data).
 */
class MapView @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null
) : View(ctx, attrs) {
    private var highlighted: Country? = null
    // transform state for pan & zoom
    private var scaleFactor = 1.0f
    private var minScale = 0.5f
    private var maxScale = 6.0f
    private var offsetX = 0f
    private var offsetY = 0f

    private val scaleDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isPanning = false

    init {
        scaleDetector = ScaleGestureDetector(ctx, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val prev = scaleFactor
                scaleFactor *= detector.scaleFactor
                scaleFactor = scaleFactor.coerceIn(minScale, maxScale)
                // adjust offsets so scaling centers on the gesture focal point
                val focusX = detector.focusX
                val focusY = detector.focusY
                offsetX = focusX - (focusX - offsetX) * (scaleFactor / prev)
                offsetY = focusY - (focusY - offsetY) * (scaleFactor / prev)
                invalidate()
                return true
            }
        })

        gestureDetector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                // toggle zoom on double-tap: first zoom in, second zoom out
                val target = if (scaleFactor < 1.5f) 2.0f else 1.0f
                val prev = scaleFactor
                scaleFactor = target.coerceIn(minScale, maxScale)
                val fx = e.x
                val fy = e.y
                offsetX = fx - (fx - offsetX) * (scaleFactor / prev)
                offsetY = fy - (fy - offsetY) * (scaleFactor / prev)
                invalidate()
                return true
            }
        })
    }

    fun highlight(country: Country) {
        highlighted = country
        // reset pan/zoom when showing a new question
        scaleFactor = 1.0f
        offsetX = 0f
        offsetY = 0f
        lastTouchX = 0f
        lastTouchY = 0f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val map = VectorMasterDrawable(context, R.drawable.europe)
        // draw with pan/zoom transforms
        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scaleFactor, scaleFactor)

        map.setBounds(0, 0, width, height)

        if (highlighted != null) {
            val codes = CountryOnMap(highlighted!!).getCodes()
            for (code in codes) {
                val pathModel = map.getPathModelByName(code.lowercase())
                assert(pathModel != null) { "No path model found for country code $code" }
                pathModel.fillColor = Color.parseColor("#4A90E2")
            }
        }
        map.draw(canvas)
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // let detectors process first
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                // only allow panning when zoomed in
                isPanning = scaleFactor > 1.0f
            }
            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress && isPanning) {
                    val x = event.x
                    val y = event.y
                    val dx = x - lastTouchX
                    val dy = y - lastTouchY
                    offsetX += dx
                    offsetY += dy
                    lastTouchX = x
                    lastTouchY = y
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isPanning = false
            }
        }
        return true
    }
}
