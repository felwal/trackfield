package me.felwal.trackfield.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun Context.isGranted(perm: String) =
    ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED

fun Context.shouldAskPermissions(): Boolean {
    return !hasPermissionToStorage() || !hasPermissionToLocation()
}

@TargetApi(23)
fun Activity.askPermissions() {
    val storagePermissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        else arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val requestCode = 200
    ActivityCompat.requestPermissions(this, storagePermissions, requestCode)
    ActivityCompat.requestPermissions(this, locationPermissions, requestCode);
}

fun Context.hasPermissionToStorage(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        isGranted(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
    }
    else {
        isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) && isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

fun Context.hasPermissionToLocation(): Boolean {
    return isGranted(Manifest.permission.ACCESS_FINE_LOCATION) && isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
}
