package com.soneso.lumenshine.networking.dto.auth

import com.fasterxml.jackson.annotation.JsonProperty

data class RegistrationResponse(
        @JsonProperty("tfa_secret")
        val token2fa: String?
)