package com.soneso.lumenshine.presentation.util

import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.*

/**
 * Extensions.
 * Created by cristi.paval on 3/20/18.
 */

fun TextView.setOnTextChangeListener(listener: ((View) -> Unit)) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            listener.invoke(this@setOnTextChangeListener)
        }
    })
}

fun TextView.setTextStyle(@StyleRes resId: Int) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        setTextAppearance(resId)
    } else {
        @Suppress("DEPRECATION")
        setTextAppearance(context, resId)
    }
}

fun <T> LiveData<T>.putValue(value: T) {
    (this as MutableLiveData).value = value
}

fun TextView.setDrawableEnd(drawable: Drawable?) {
    setCompoundDrawablesWithIntrinsicBounds(compoundDrawables[0], compoundDrawables[1], drawable, compoundDrawables[3])
}

fun TextView.setStyleCompat(@StyleRes resId: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        setTextAppearance(resId)
    } else {
        @Suppress("DEPRECATION")
        setTextAppearance(context, resId)
    }
}

fun DatePicker.getDate(): Date {
    val calendar: Calendar = Calendar.getInstance()
    calendar.set(year, month, dayOfMonth)
    return calendar.time
}

fun Calendar.startOfDay(): Date {
    add(Calendar.HOUR, -get(Calendar.HOUR))
    add(Calendar.MINUTE, -get(Calendar.MINUTE))
    add(Calendar.SECOND, -get(Calendar.SECOND))
    return time
}

fun Calendar.endOfDay(): Date {
    add(Calendar.HOUR, 23 - get(Calendar.HOUR))
    add(Calendar.MINUTE, 59 - get(Calendar.MINUTE))
    add(Calendar.SECOND, 59 - get(Calendar.SECOND))
    return time
}