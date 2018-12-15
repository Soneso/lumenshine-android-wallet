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
}