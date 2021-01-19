package com.arul.camerax.utils

import android.content.Context
import android.widget.Toast

val Any.TAG: String
    get() = this.javaClass.simpleName

fun Context.toast(resourceId: Int) = toast(getString(resourceId))

fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_LONG) = Toast.makeText(this, message, duration).show()