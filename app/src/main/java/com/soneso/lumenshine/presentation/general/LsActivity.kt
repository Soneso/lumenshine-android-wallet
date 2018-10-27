package com.soneso.lumenshine.presentation.general

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.soneso.lumenshine.LsApp
import com.soneso.lumenshine.R
import com.soneso.lumenshine.util.LsException


/**
 * Base activity for class.
 * Created by cristi.paval on 3/12/18.
 */
@SuppressLint("Registered")
open class LsActivity : AppCompatActivity() {

    val lsApp: LsApp
        get() = application as LsApp

    lateinit var viewModelFactory: LsViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        hideStatusBar()
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        super.onCreate(savedInstanceState)
        viewModelFactory = LsViewModelFactory(lsApp.appComponent)
    }

    fun hideStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    fun showSnackbar(text: CharSequence) {

        val view = findViewById<ViewGroup>(android.R.id.content).getChildAt(0) ?: return
        com.google.android.material.snackbar.Snackbar.make(view, text, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                .setAction(R.string.ok, null)
                .show()
    }

    fun showErrorSnackbar(e: LsException?) {

        val message = e?.throwable?.message ?: getString(R.string.unknown_error)
        showSnackbar(message)
    }
}