/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.extensions

import android.app.Activity
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.view.KeyEvent
import android.view.View
import android.view.Window
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

/**
 * This extension changes keyboard to numberPassword but without
 * password conversion into dots if the [isPassword] is false.
 * The password keyboard is more cool the the number one.
 */
fun AppCompatEditText.applyCustomNumberKeyboardOption(isPassword: Boolean = false) {
    inputType = InputType.TYPE_NUMBER_VARIATION_PASSWORD or InputType.TYPE_CLASS_NUMBER
    if (!isPassword) {
        transformationMethod = HideReturnsTransformationMethod.getInstance()
    }
}

/**
 * Block a keyboard closing after done action of the edit text
 * @param listener - the done action listener
 */
fun AppCompatEditText.setOnKeyBlockedListener(listener: () -> Unit) {
    setOnKeyListener { _, keyCode, event ->
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
            listener.invoke()
            return@setOnKeyListener true
        }
        return@setOnKeyListener false
    }
}

// Show/Hide keyboard methods for different sources.

fun AppCompatEditText.showKeyboard(activity: Activity) {
    showKeyboard(activity.window)
}

fun AppCompatEditText.showKeyboard(window: Window) {
    requestFocus()
    WindowCompat.getInsetsController(window, window.decorView).show(WindowInsetsCompat.Type.ime())
}

fun Activity.hideKeyboard() {
    WindowCompat.getInsetsController(window, window.decorView).hide(WindowInsetsCompat.Type.ime())
}

fun Fragment.hideKeyboard() {
    activity?.hideKeyboard()
}

fun View.hideKeyboard(window: Window) {
    WindowCompat.getInsetsController(window, this).hide(WindowInsetsCompat.Type.ime())
}