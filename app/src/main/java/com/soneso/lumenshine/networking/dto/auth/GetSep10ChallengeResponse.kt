package com.soneso.lumenshine.networking.dto.auth

import com.fasterxml.jackson.annotation.JsonProperty

data class GetSep10ChallengeResponse(
        @JsonProperty("sep10_transaction")
        val sep10TransactionChallenge: String?
)