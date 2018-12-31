package com.soneso.lumenshine.presentation.general

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.soneso.lumenshine.BuildConfig
import com.soneso.lumenshine.LsApp
import com.soneso.lumenshine.R
import com.soneso.lumenshine.util.LsException
import kotlinx.android.synthetic.main.activity_ls.*


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
        if (!BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_ls)

        viewModelFactory = LsViewModelFactory(lsApp.appComponent)
    }

    private fun hideStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    fun showSnackbar(text: CharSequence) {

        val view = findViewById<ViewGroup>(android.R.id.content).getChildAt(0) ?: return
        com.google.android.material.snackbar.Snackbar.make(view, text, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                .setAction(R.string.ok, null)
                .show()
    }

    fun showErrorSnackbar(e: LsException?) {

        val message = e?.cause?.message ?: getString(R.string.unknown_error)
        showSnackbar(message)
    }

    fun showLoading(loading: Boolean) {
        if (loading) {
            loadingView.visibility = View.VISIBLE
        } else {
            loadingView.visibility = View.GONE
        }
    }

    override fun setContentView(layoutResID: Int) {
        val layout = LayoutInflater.from(this).inflate(layoutResID, baseActivityView, false)
        baseActivityView.addView(layout, 0)
    }
}