package com.soneso.lumenshine.networking.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class OperationDto(

        @JsonProperty("op_id")
        var id: Long,

        @JsonProperty("op_type")
        var type: Int,

        @JsonProperty("op_application_order")
        var order: Int,

        @JsonProperty("tx_created_at")
        var created: Date,

        @JsonProperty("tx_fee_paid")
        var transactionFee: Int,

        @JsonProperty("tx_source_account")
        var transactionSource: String,

        @JsonProperty("tx_operation_count")
        var transactionOperationCount: Int,

        @JsonProperty("tx_transaction_hash")
        var transactionHash: String,

        @JsonProperty("tx_memo_type")
        var transactionMemoType: String,

        @JsonProperty("tx_memo")
        var transactionMemo: String,

        @JsonProperty("op_details")
        var operationDetails: String
) {

    inner class CreateAccountDetails(

            @JsonProperty("funder")
            var funder: String,

            @JsonProperty("account")
            var account: String,

            @JsonProperty("starting_balance")
            var startingBalance: String
    )

    inner class PaymentDetails(

            @JsonProperty("to")
            var to: String,

            @JsonProperty("from")
            var from: String,

            @JsonProperty("amount")
            var amount: String,

            @JsonProperty("asset_type")
            var currency: String
    )
}