package com.soneso.lumenshine.presentation.auth.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.general.DialogWithHeader

class TermsOfServiceDialog : DialogWithHeader() {
    override fun getTitle(): String {
        return context!!.getString(R.string.terms_of_service_title)
    }

    override fun getContentLayout(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.dialog_terms_of_service, container, false).apply { minimumHeight = resources.displayMetrics.heightPixels }

    companion object {
        const val TAG = "TermsOfServiceDialog"

        fun showInstance(fm: FragmentManager) {
            TermsOfServiceDialog().show(fm, TAG)
        }
    }
}