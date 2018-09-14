package com.gk.sgutil.bus.view

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.widget.TextView
import com.gk.sgutil.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory


/**
 * Utility class to create colored map marker with text on it
 */
class MapMarkerMaker {
    companion object {

        private const val TEXT_SIZE = 16f
        // The percent in the image to position the text
        private const val TEXT_POS_X_FACTOR = 0.51f
        private const val TEXT_POS_Y_FACTOR = 0.44f

        private fun computeColor(start: Int, end: Int, percent: Float, shift: Int): Int {
            val s = (start shr shift) and 0xFF
            val e = (end shr shift) and 0xFF
            return (s + (percent * (e - s)).toInt()) shl shift
        }

        /**
         * Compute the gradient color between start and end.
         * @param startColor
         *      The starting color
         * @param endColor
         *      The ending color
         * @param total
         *      The total number of records
         * @param curr
         *      The current item index
         */
        fun computeColor(startColor: Int, endColor: Int, total: Int, curr: Int) : Int {
            val percent = curr.toFloat() / (total - 1).toFloat()
            return 0xFF000000.toInt() +
                    computeColor(startColor, endColor, percent, 16) + // R
                    computeColor(startColor, endColor, percent, 8) + // G
                    computeColor(startColor, endColor, percent, 0) // B
        }
    }

    private var mMarker: Bitmap? = null
    private var mCircle: Bitmap? = null

    // Create an editable image as the base image
    private fun createMarker(context: Context) : Bitmap {
        if (mMarker == null) {
            mMarker = BitmapFactory.decodeResource(context.resources, R.mipmap.map_marker)
        }
        return mMarker!!.copy(Bitmap.Config.ARGB_8888, true)
    }

    // Create the colored image
    private fun createColoredBoard(context: Context, color: Int) : Bitmap {
        if (mCircle == null) {
            mCircle = BitmapFactory.decodeResource(context.resources, R.mipmap.map_marker_circle)
        }
        val bitmap = Bitmap.createBitmap(mCircle!!.width, mCircle!!.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
        canvas.drawBitmap(mCircle!!, 0f, 0f, paint)
        return bitmap
    }

    /**
     * Create a colored map marker image with text.
     * @context
     *      Must be Activity context to get the theme
     * @text
     *      The text to show on the marker
     * @color
     *      The color of the marker image
     */
    fun createTextMarker(context: Context, text: String, color: Int): BitmapDescriptor {
        // Set text style
        val textView = TextView(context)
        textView.text = text
        textView.textSize = TEXT_SIZE
        textView.setTypeface(null, Typeface.BOLD)
        // Set text paint style
        val textPaint = textView.paint
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = ContextCompat.getColor(context, R.color.route_marker_text)
        val bitmap = createMarker(context) // Base background
        val colorCircle = createColoredBoard(context, color) // Color image to cover the background
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(colorCircle, 0f, 0f, Paint()) // Draw the colored image on the background
        canvas.drawText(text, canvas.width * TEXT_POS_X_FACTOR, canvas.height * TEXT_POS_Y_FACTOR, textPaint)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

