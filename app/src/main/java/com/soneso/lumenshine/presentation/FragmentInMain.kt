package com.soneso.lumenshine.presentation

import com.soneso.lumenshine.presentation.general.LsFragment

open class FragmentInMain : LsFragment() {

    private val mainActivity: MainActivity
        get() = activity as MainActivity

    fun showLoadingView(show: Boolean) {
        mainActivity.showLoadingView(show)
    }
}