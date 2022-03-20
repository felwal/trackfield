package me.felwal.trackfield.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.graphics.drawable.DrawableWrapper
import androidx.appcompat.view.menu.MenuBuilder
import me.felwal.android.annotation.BumpFelwalAndRemove
import me.felwal.trackfield.R
import me.felwal.android.util.getColorByAttr
import me.felwal.android.util.getDrawableCompat
import me.felwal.android.util.getResIdByAttr
import me.felwal.android.util.setOptionalIconsVisible
import me.felwal.android.util.toColorStateList

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

@BumpFelwalAndRemove
operator fun IntArray.minus(element: Int): IntArray =
    (toMutableList() - element).toIntArray()

//

@BumpFelwalAndRemove
fun Context.getBitmap(@DrawableRes id: Int): Bitmap? = getDrawableCompat(id)?.toBitmap()

@BumpFelwalAndRemove
fun Context.getBitmapByAttr(@AttrRes attr: Int): Bitmap? = getBitmap(getResIdByAttr(attr))

@BumpFelwalAndRemove
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

//

/** Fixes checked state being ignored by injecting checked state directly into drawable */
@BumpFelwalAndRemove
@SuppressLint("RestrictedApi")
class CheckDrawableWrapper(val menuItem: MenuItem) : DrawableWrapper(menuItem.icon) {
    @SuppressLint("RestrictedApi")
    override fun setState(stateSet: IntArray) = super.setState(
        // inject checked state into drawable state set
        if (menuItem.isChecked) stateSet + android.R.attr.state_checked
        else stateSet
    )
}

/** Wrap icon drawable with [CheckDrawableWrapper]. */
@BumpFelwalAndRemove
fun MenuItem.fixIconCheckState() = apply { icon = CheckDrawableWrapper(this) }

@BumpFelwalAndRemove
fun Menu.createOptionalIcons() = setOptionalIconsVisible(true)

/**
 * Call this from [Activity.onPrepareOptionsMenu]; the colors need updating every time.
 */
@BumpFelwalAndRemove
fun Menu.prepareOptionalIcons(c: Context) = setOptionalIconsColor(c.getColorByAttr(R.attr.colorControlNormal));

@BumpFelwalAndRemove
val Menu.optionalItems
    @SuppressLint("RestrictedApi")
    get() = (this as? MenuBuilder)?.nonActionItems

@BumpFelwalAndRemove
@SuppressLint("RestrictedApi")
fun Menu.setOptionalIconsColor(@ColorInt color: Int) =
    optionalItems?.forEach { it.iconTintList = color.toColorStateList() }
