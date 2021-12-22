package com.felwal.trackfield.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import com.felwal.android.util.getDrawableCompat
import com.felwal.android.util.getResIdByAttr
import com.google.android.gms.maps.model.LatLng

fun Context.getBitmap(@DrawableRes id: Int): Bitmap? = getDrawableCompat(id)?.toBitmap()

fun Context.getBitmapByAttr(@AttrRes attr: Int): Bitmap? = getBitmap(getResIdByAttr(attr))

fun Drawable.toBitmap(): Bitmap? {
    (this as? BitmapDrawable)?.bitmap?.let { return it }

    val bitmap =
        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            // Single color bitmap will be created of 1x1 pixel
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }
        else {
            Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        }

    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)

    return bitmap
}

fun Float.addValueToAverage(newValue: Float, newValueIndex: Int): Float =
    (this * newValueIndex + newValue) / (newValueIndex + 1)

fun Double.addValueToAverage(newValue: Double, newValueIndex: Int): Double =
    (this * newValueIndex + newValue) / (newValueIndex + 1)

fun dynamicAverage(prevAvg: Float, newValue: Float, newValueIndex: Int): Float =
    (prevAvg * newValueIndex + newValue) / (newValueIndex + 1)

fun <E> List<E>.split(count: Int): List<List<E>> {
    val sublists = mutableListOf<List<E>>()
    val indexStep = size / count

    for (i in 0 until count) {
        sublists.add(
            if (i == count - 1) subList(i * indexStep, size)
            else subList(i * indexStep, (i + 1) * indexStep)
        )
    }

    return sublists
}
