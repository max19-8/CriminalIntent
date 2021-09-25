package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point


fun getScaledBitmap(path: String, activity: Activity) : Bitmap {
    val size = Point()
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        val display = activity.display
        display?.getRealSize(size)
    } else {
        @Suppress("DEPRECATION")
        activity.windowManager.defaultDisplay.getSize(size)
    }
    return getScaledBitmap(path, size.x, size.y)
}



fun getScaledBitmap(path: String, destWidth: Int , destHeight: Int): Bitmap{
    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path,options)

    val scrWidth = options.outWidth.toFloat()
    val scrHeight = options.outHeight.toFloat()

    var inSampleSize = 1

    if (scrHeight > destHeight || scrWidth > destWidth) {
        val heightScale = scrHeight / destHeight
        val widthScale = scrWidth / destWidth

        val sampleScale = if (heightScale > widthScale) {
            heightScale
        }else{
            widthScale
        }
        inSampleSize = Math.round(sampleScale)
    }
    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize

    return BitmapFactory.decodeFile(path,options)
}

