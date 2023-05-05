package com.ys_production.unsplashapi

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import eightbitlab.com.blurview.BlurAlgorithm
import eightbitlab.com.blurview.RenderScriptBlur
import kotlin.math.roundToInt

private const val TAG = "BlurShadowProvider"
class BlurShadowProvider(context: Context) {

    private val blurEffect: BlurAlgorithm = RenderScriptBlur(context)

    fun getBlurShadow(input: Bitmap, radius: Float): Bitmap? = BitmapUtils.copyToSoftware(input).let {
        try {
            val downscalingRadius: Float
            val downscaledRadius: Float
            if (radius > 25f) {
                downscalingRadius = radius / 25
                downscaledRadius = 25f
            } else {
                downscalingRadius = 1f
                downscaledRadius = radius
            }

            val outputBitmapWidth = it.width + (radius * 2).roundToInt()
            val outputBitmapHeight = it.height + (radius * 2).roundToInt()

            // Downscale input bitmap to multiply actual blur radius and performance
            val downscaledInput = Bitmap.createScaledBitmap(
                it,
                (it.width / (downscalingRadius)).roundToInt(),
                (it.height / (downscalingRadius)).roundToInt(),
                true
            )

            // Draw the downscaled input into another bitmap for blurring
            val blurBitmap = Bitmap.createBitmap(
                (outputBitmapWidth / (downscalingRadius)).roundToInt(),
                (outputBitmapHeight / (downscalingRadius)).roundToInt(),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(blurBitmap)
            canvas.drawBitmap(downscaledInput, downscaledRadius, downscaledRadius, null)

            // Blur, yeah
            blurEffect.blur(blurBitmap, downscaledRadius)

            // Upscale the blurred bitmap again
            val upscaledBlurBitmap = Bitmap.createScaledBitmap(
                blurBitmap,
                outputBitmapWidth,
                outputBitmapHeight,
                true
            )
            downscaledInput.recycle()
            blurBitmap.recycle()

            return upscaledBlurBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "getBlurShadow: ${e.message}")

            return null
        }
    }
}