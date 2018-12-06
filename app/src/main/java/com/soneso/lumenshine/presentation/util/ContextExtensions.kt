package com.soneso.lumenshine.presentation.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.soneso.lumenshine.R


/**
 * Util class.
 * Created by cristi.paval on 3/16/18.
 */

fun Context.forwardToBrowser(url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    if (isIntentSafe(intent)) {
        startActivity(intent)
    }
}

fun Context.isIntentSafe(intent: Intent): Boolean {
    val activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    return activities.size > 0
}

fun FragmentActivity.showInfoDialog() {
    val fragmentTransaction = this.supportFragmentManager.beginTransaction()
    val infoDialog = InfoDialog.newInstance(R.string.info_dialog, R.drawable.ic_error_outline)
    infoDialog.setViewBuilder(object : InfoDialog.ViewBuilder {
        override fun createView(context: Context, inflater: LayoutInflater): View {
            return inflater.inflate(R.layout.info, null)
        }
    })
    infoDialog.show(fragmentTransaction, InfoDialog.TAG)
}

/**
 * show an fullscreen information dialog
 *
 * @param titleResId the dialog title resource id
 * @param contentResId hte content layout resource id
 * @param (optional) the dialog icon id
 */
fun FragmentActivity.showInfoDialog(titleResId: Int, contentResId: Int, iconResId: Int = 0) {
    val fragmentTransaction = this.supportFragmentManager.beginTransaction()
    val infoDialog = InfoDialog.newInstance(titleResId, iconResId)
    infoDialog.setViewBuilder(object : InfoDialog.ViewBuilder {
        override fun createView(context: Context, inflater: LayoutInflater): View {
            return inflater.inflate(contentResId, null)
        }
    })
    infoDialog.show(fragmentTransaction, InfoDialog.TAG)
}

@ColorInt
fun Context.color(@ColorRes colorId: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        getColor(colorId)
    } else {
        ContextCompat.getColor(this, colorId)
    }
}

fun Context.showAlert(titleResId: Int, messageResId: Int) {
    AlertDialog.Builder(this)
            .setTitle(titleResId)
            .setMessage(messageResId)
            .setNeutralButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
}