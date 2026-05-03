package com.example.flagsgame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
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
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG)
    private var highlighted: Country? = null

    init {
        paint.style = Paint.Style.FILL
        paint.color = Color.LTGRAY
        stroke.style = Paint.Style.STROKE
        stroke.strokeWidth = 2f
        stroke.color = Color.DKGRAY
    }

    fun highlight(country: Country) {
        highlighted = country
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//            val map = context.getDrawable(R.drawable.europe)
//            map?.setBounds(0, 0, width, height)
//            map?.draw(canvas)

        val map = VectorMasterDrawable(context, R.drawable.europe)
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
        // draw a simple demo triangle so the view isn't blank during development
        /*val demo = Path()
        val left = width * 0.2f
        val right = width * 0.8f
        val top = height * 0.2f
        val bottom = height * 0.75f
        demo.moveTo(left, top)
        demo.lineTo(right, top)
        demo.lineTo((left + right) / 2f, bottom)
        demo.close()
        paint.color = Color.LTGRAY
        canvas.drawPath(demo, paint)
        canvas.drawPath(demo, stroke)
        return */
    }

    // simple scale to fit
//        val bounds = android.graphics.RectF()
//        for (p in countryPaths.values) p.computeBounds(bounds, true)
//        val scaleX = width / bounds.width()
//        val scaleY = height / bounds.height()
//        val scale = minOf(scaleX, scaleY) * 0.95f
//        val tx = -bounds.left * scale + (width - bounds.width() * scale) / 2f
//        val ty = -bounds.top * scale + (height - bounds.height() * scale) / 2f
//
//        canvas.save()
//        canvas.translate(tx, ty)
//        canvas.scale(scale, scale)
//
//        for ((code, path) in countryPaths) {
//            if (code == highlighted) {
//                paint.color = Color.parseColor("#4A90E2")
//            } else {
//                paint.color = Color.LTGRAY
//            }
//            canvas.drawPath(path, paint)
//            canvas.drawPath(path, stroke)
//        }
//
//        canvas.restore()
}
