package com.soneso.lumenshine.presentation.general

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.util.showInfoDialog
import com.soneso.lumenshine.util.LsException

/**
 * Base Fragment for Lumenshine App.
 * Created by cristi.paval on 3/8/18.
 */
open class LsFragment : Fragment() {

    lateinit var viewModelFactory: LsViewModelFactory

    private val lsActivity: LsActivity
        get() = activity as LsActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModelFactory = LsViewModelFactory(lsActivity.lsApp.appComponent)
    }

    fun showSnackbar(text: CharSequence) {
        val view = view ?: return
        Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                .setAction(R.string.ok, null)
                .show()
    }

    fun showSnackbar(@StringRes textId: Int) {
        val view = view ?: return
        Snackbar.make(view, textId, Snackbar.LENGTH_LONG)
                .setAction(R.string.ok, null)
                .show()
    }

    fun showErrorSnackbar(e: LsException?) {

        val view = view ?: return
        val message = e?.message ?: e?.cause?.message ?: getString(R.string.unknown_error)
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.ok, null)
                .show()
    }

    fun showLoadingView(show: Boolean) {
        lsActivity.showLoading(show)
    }

    fun showInfoDialog(titleResId: Int, contentResId: Int, iconResId: Int = 0) {
        lsActivity.showInfoDialog(titleResId, contentResId, iconResId)
    }
}