package com.soneso.lumenshine.presentation.auth

import com.soneso.lumenshine.presentation.general.LsFragment

/**
 * Fragment.
 * Created by cristi.paval on 3/21/18.
 */
open class AuthFragment : LsFragment() {

    protected val authViewModel: AuthViewModel
        get() = authActivity.authViewModel

    protected val authActivity: BaseAuthActivity
        get() = activity as BaseAuthActivity

    fun replaceFragment(fragment: AuthFragment, tag: String) {

//        authActivity.replaceFragment(fragment, tag)
    }

    fun showLoadingView() {
        authActivity.showLoading(true)
    }

    fun hideLoadingView() {
        authActivity.showLoading(false)
    }
}