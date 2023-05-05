package com.ys_production.unsplashapi

import android.graphics.Bitmap
import android.os.Build
import java.io.OutputStream

object BitmapUtils {

    /**
     * Copy hardware bitmap to software.
     * It will simply return the input bitmap if it's not a hardware bitmap
     */
    fun copyToSoftware(
        hwBitmap: Bitmap,
        recycleHwBitmap: Boolean = false
    ): Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hwBitmap.config == Bitmap.Config.HARDWARE) {
        val bitmapCopy = hwBitmap.copy(Bitmap.Config.ARGB_8888, false)
        if (recycleHwBitmap) {
            hwBitmap.recycle()
        }
        bitmapCopy
    } else {
        hwBitmap
    }
}

fun Bitmap.compress(quality: Int, outputStream: OutputStream): Boolean {
    val bitmapCompressFormat = Bitmap.CompressFormat.PNG
    return compress(bitmapCompressFormat, quality, outputStream)
}