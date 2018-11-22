package com.soneso.lumenshine.presentation.general;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.soneso.lumenshine.R
import kotlinx.android.synthetic.main.header_dialog.*
import kotlinx.android.synthetic.main.header_dialog.view.*

abstract class DialogWithHeader : LsDialog() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.header_dialog, container, false).apply {
            minimumWidth = resources.displayMetrics.widthPixels
        }

        val contentLayout = getContentLayout(inflater, container, savedInstanceState)
        rootView.dialogRootView.addView(contentLayout)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialogHeaderTitle.text = getTitle()

        ivCancel.setOnClickListener {
            dismissDialog()
        }
    }

    private fun dismissDialog() {
        dismiss()
    }

    override fun isWithHeader(): Boolean {
        return true
    }

    abstract fun getTitle(): String
}
