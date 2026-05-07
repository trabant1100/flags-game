package com.example.flagsgame

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.dorukkangal.vectormaster.VectorMasterDrawable
import com.dorukkangal.vectormaster.models.PathModel
import androidx.core.graphics.withTranslation
import androidx.core.graphics.toColorInt

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

//    private val scaleDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isPanning = false
    private var zoomRect: RectF? = null

    // Caching and pre-calculation to avoid allocations in onDraw
    private var vectorMasterDrawable: VectorMasterDrawable? = null
    private val highlightColor = "#4A90E2".toColorInt()
    private val tempMatrix = Matrix()
    private val tempRect = RectF()
    private val internalZoomRect = RectF()

    private class StrokeUpdate(
        val model: PathModel,
        val baseWidth: Float,
        val isAutoRect: Boolean
    )
    private val updates = mutableListOf<StrokeUpdate>()

    init {
        gestureDetector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                // toggle zoom on double-tap: first zoom in, second zoom out
                val calcScaleFactor = {
                    // if we have a zoom rect, zoom to fit it; otherwise just zoom in centered on the tap
                    val currentZoomRect = zoomRect
                    if (currentZoomRect != null) {
                        val viewWidth = width.toFloat()
                        val viewHeight = height.toFloat()
                        val zoomWidth = currentZoomRect.width()
                        val zoomHeight = currentZoomRect.height()
                        val scaleX = viewWidth / zoomWidth
                        val scaleY = viewHeight / zoomHeight
                        (0.9f * minOf(scaleX, scaleY)).coerceIn(minScale, maxScale)
                    } else {
                        2.0f
                    }
                }
                val target = if (scaleFactor < 1.5f) calcScaleFactor() else 1.0f
                val prev = scaleFactor
                scaleFactor = target
                val fx = e.x
                val fy = e.y
                if (target == 1.0f) {
                    // if resetting zoom, also reset pan
                    offsetX = 0f
                    offsetY = 0f
                } else {
                    // adjust offsets so scaling centers on the double-tap point
                    offsetX = fx - (fx - offsetX) * (scaleFactor / prev)
                    offsetY = fy - (fy - offsetY) * (scaleFactor / prev)
                }
                invalidate()
                return true
            }
        })
    }

    private fun prepareMap() {
        val country = highlighted ?: return
        val countryOnMap = CountryOnMap(country)
        val continent = countryOnMap.continent

        // Recreate the drawable to ensure a clean state for each question.
        // Doing this once per question is acceptable.
        val map = VectorMasterDrawable(context, continent.drawable)
        vectorMasterDrawable = map
        map.setBounds(0, 0, width, height)

        updates.clear()
        val codes = countryOnMap.getCodes()
        var foundZoomRect = false

        for (code in codes) {
            val pathModel = map.getPathModelByName(code.lowercase())
            if (code.endsWith("-rect")) {
                pathModel.strokeAlpha = 1.0f
                updates.add(StrokeUpdate(pathModel, pathModel.strokeWidth, false))
                foundZoomRect = true
                pathModel.path.computeBounds(internalZoomRect, true)
            } else if (code.endsWith("-autorect")) {
                val orgPathModel = map.getPathModelByName(code.substringBefore("-autorect").lowercase())
                if (orgPathModel != null) {
                    orgPathModel.path.computeBounds(tempRect, true)
                    val rectModel = map.getPathModelByName("rect")
                    if (rectModel != null) {
                        tempMatrix.reset()
                        tempMatrix.setTranslate(tempRect.centerX() - 50, tempRect.centerY() - 50)
                        rectModel.transform(tempMatrix)
                        rectModel.strokeAlpha = 0.5f
                        updates.add(StrokeUpdate(rectModel, rectModel.strokeWidth, true))
                        foundZoomRect = true
                        rectModel.path.computeBounds(internalZoomRect, true)
                    }
                }
            } else {
                pathModel.fillColor = highlightColor
            }
        }

        zoomRect = if (foundZoomRect) RectF(internalZoomRect) else null
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        vectorMasterDrawable?.setBounds(0, 0, w, h)
        if (w > 0 && h > 0 && highlighted != null) {
            prepareMap()
        }
    }

    fun highlight(country: Country) {
        highlighted = country
        // reset pan/zoom when showing a new question
        scaleFactor = 1.0f
        offsetX = 0f
        offsetY = 0f
        lastTouchX = 0f
        lastTouchY = 0f

        if (width > 0 && height > 0) {
            prepareMap()
        } else {
            // will be prepared in onSizeChanged or onDraw
            vectorMasterDrawable = null
        }

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (highlighted == null) return

        if (vectorMasterDrawable == null && width > 0) {
            prepareMap()
        }

        val map = vectorMasterDrawable ?: return

        // update stroke widths based on current scaleFactor without allocations
        for (i in 0 until updates.size) {
            val u = updates[i]
            if (u.isAutoRect) {
                u.model.strokeWidth = u.baseWidth * ((-0.1f) * scaleFactor + 1.1f)
            } else {
                u.model.strokeWidth = u.baseWidth / scaleFactor
            }
        }

        canvas.withTranslation(offsetX, offsetY) {
            scale(scaleFactor, scaleFactor)
            map.draw(this)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // let detectors process first
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
                if (isPanning) {
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
