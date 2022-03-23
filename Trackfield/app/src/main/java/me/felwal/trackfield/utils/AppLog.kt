package me.felwal.trackfield.utils

import android.util.Log

class AppLog {
    companion object {
        private const val LOG_TAG = "Trackfield"

        @JvmStatic
        fun v(msg: String) = Log.v(LOG_TAG, msg)

        @JvmStatic
        fun d(msg: String) = Log.d(LOG_TAG, msg)

        @JvmStatic
        fun i(msg: String) = Log.i(LOG_TAG, msg)

        @JvmStatic
        fun w(msg: String) = Log.w(LOG_TAG, msg)

        @JvmStatic
        fun e(msg: String, e: Exception? = null) =
            if (e != null) Log.e(LOG_TAG, msg, e)
            else Log.e(LOG_TAG, msg)
    }
}
