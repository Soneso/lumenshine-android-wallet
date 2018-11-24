package com.soneso.lumenshine.model.entities

import java.io.Serializable

data class RegistrationStatus(
        val mailConfirmed: Boolean = false,
        val tfaConfirmed: Boolean = false,
        val mnemonicConfirmed: Boolean = false
) : Serializable {


    fun isSetupCompleted() = mailConfirmed && tfaConfirmed && mnemonicConfirmed
}