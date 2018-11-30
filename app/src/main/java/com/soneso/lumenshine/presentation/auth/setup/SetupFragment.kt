package com.soneso.lumenshine.presentation.auth.setup

import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.presentation.auth.AuthFragment
import com.soneso.lumenshine.presentation.auth.AuthSetupActivity

open class SetupFragment : AuthFragment() {

    fun renderRegistrationStatus(status: RegistrationStatus) {
        (authActivity as AuthSetupActivity).renderRegistrationStatus(status)
    }
}