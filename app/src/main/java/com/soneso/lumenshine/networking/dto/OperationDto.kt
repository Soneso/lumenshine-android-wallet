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

    data class CreateAccountDetails(
            @JsonProperty("funder")
            var funder: String,

            @JsonProperty("account")
            var account: String,

            @JsonProperty("starting_balance")
            var startingBalance: Float
    )

    data class PaymentDetails(
            @JsonProperty("to")
            var destination: String,

            @JsonProperty("from")
            var from: String,

            @JsonProperty("amount")
            var amount: Float,

            @JsonProperty("asset_type")
            var assetType: String
    )

    data class PathPaymentDetails(
            @JsonProperty("to")
            var destination: String,

            @JsonProperty("from")
            var from: String,

            @JsonProperty("amount")
            var amount: Float,

            @JsonProperty("asset_type")
            var assetType: String,

            @JsonProperty("path")
            var path: List<String>,

            @JsonProperty("source_max")
            var sourceMax: Float,

            @JsonProperty("source_amount")
            var sourceAmount: Float,

            @JsonProperty("source_asset_type")
            var sourceAssetType: String
    )

    data class ChangeTrustDetails(
            @JsonProperty("limit")
            var limit: String,

            @JsonProperty("asset_code")
            var assetCode: String,

            @JsonProperty("asset_issuer")
            var assetIssuer: String
    )

    data class OfferDetails(
            @JsonProperty("offer_id")
            var offerId: Long,

            @JsonProperty("amount")
            var amount: Float,

            @JsonProperty("price")
            var price: Float,

            @JsonProperty("buying_asset_code")
            var buyingAssetCode: String,

            @JsonProperty("selling_asset_type")
            var sellingAssetType: String
    )

    data class ManageDataDetails(
            @JsonProperty("name")
            var name: String,

            @JsonProperty("value")
            var value: String
    )

    data class BumpSequenceDetails(
            @JsonProperty("bump_to")
            var bumpTo: Long
    )

    data class AccountMergeDetails(
            @JsonProperty("account")
            var mergedAccount: String
    )

    data class SetOptionDetails(
            @JsonProperty("inflation_dest")
            var inflationDestination: String?,

            @JsonProperty("set_flags_s")
            var setFlags: Set<String>?,

            @JsonProperty("clear_flags_s")
            var clearFlags: Set<String>?,

            @JsonProperty("master_key_weight")
            var masterWeight: Int?,

            @JsonProperty("low_threshold")
            var lowThreshold: Int?,

            @JsonProperty("med_threshold")
            var mediumThreshold: Int?,

            @JsonProperty("high_threshold")
            var highThreshold: Int?,

            @JsonProperty("signer_key")
            var signerKey: String?,

            @JsonProperty("signer_weight")
            var signerWeight: Int?,

            @JsonProperty("home_domain")
            var homeDomain: String?
    )
}