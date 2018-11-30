package com.soneso.lumenshine.model.wrapper

import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.networking.dto.auth.ConfirmTfaResponse
import com.soneso.lumenshine.networking.dto.auth.ConfirmTfaSecretChangeResponse
import com.soneso.lumenshine.networking.dto.auth.GetRegistrationStatusResponse
import com.soneso.lumenshine.networking.dto.auth.LoginStep2Response

fun GetRegistrationStatusResponse.toRegistrationStatus(): RegistrationStatus {

    return RegistrationStatus(mailConfirmed, tfaConfirmed, mnemonicConfirmed)
}

fun ConfirmTfaResponse.toRegistrationStatus(): RegistrationStatus {

    return RegistrationStatus(emailConfirmed, tfaConfirmed, mnemonicConfirmed)
}

fun ConfirmTfaSecretChangeResponse.toRegistrationStatus(): RegistrationStatus {

    return RegistrationStatus(mailConfirmed, tfaConfirmed, mnemonicConfirmed)
}

fun LoginStep2Response.toRegistrationStatus(): RegistrationStatus {

    return RegistrationStatus(emailConfirmed, tfaConfirmed, mnemonicConfirmed)
}