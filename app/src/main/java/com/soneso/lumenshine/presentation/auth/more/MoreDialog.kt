package com.soneso.lumenshine.presentation.auth.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.general.LsDialog
import kotlinx.android.synthetic.main.dialog_more.*

class MoreDialog : LsDialog() {
    override fun isWithHeader(): Boolean {
        return false
    }

    private var listener: View.OnClickListener? = null

    override fun getContentLayout(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.dialog_more, container, false).apply {
                minimumHeight = resources.displayMetrics.heightPixels / 2
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lostPassTab.setOnClickListener(listener)
        lostTfaTab.setOnClickListener(listener)
        importMnemonicTab.setOnClickListener(listener)
        aboutTab.setOnClickListener(listener)
        helpTab.setOnClickListener(listener)
    }

    companion object {
        const val TAG = "MoreDialog"

        fun showInstance(fm: FragmentManager, tabClickListener: View.OnClickListener) {
            MoreDialog().apply {
                listener = View.OnClickListener {
                    dismiss()
                    tabClickListener.onClick(it)
                }
            }.show(fm, TAG)
        }
    }
}