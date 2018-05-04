package com.soneso.stellargate.networking.dto.auth

import com.fasterxml.jackson.annotation.JsonProperty

class LoginWithTfaStep2Response {

    var jwtToken = ""

    @JsonProperty("tfa_secret")
    var tfaSecret = ""

    @JsonProperty("mail_confirmed")
    var emailConfirmed = false

    @JsonProperty("mnemonic_confirmed")
    var mnemonicConfirmed = false

    @JsonProperty("tfa_confirmed")
    var tfaConfirmed = false
}