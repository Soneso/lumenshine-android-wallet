package com.soneso.lumenshine.networking.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class WalletDto(

        @JsonProperty("id")
        var id: Long?,

        @JsonProperty("public_key")
        var publicKey: String?,

        @JsonProperty("wallet_name")
        var name: String?,

        @JsonProperty("federation_address")
        var federationAddress: String?,

        @JsonProperty("show_on_homescreen")
        var showOnHomeScreen: Boolean?
)