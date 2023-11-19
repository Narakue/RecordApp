package com.zero.app.util

import android.annotation.SuppressLint
import android.content.Context

class Util {
    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
    }
}