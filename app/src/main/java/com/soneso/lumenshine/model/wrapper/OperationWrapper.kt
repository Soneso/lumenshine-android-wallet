package com.soneso.lumenshine.model.wrapper

import com.google.gson.JsonParser
import com.soneso.lumenshine.model.entities.operations.Operation
import com.soneso.lumenshine.model.entities.operations.Payment
import com.soneso.lumenshine.networking.dto.OperationDto
import java.text.SimpleDateFormat
import java.util.*

fun OperationDto.toOperation(walletPK: String): Operation {

    val dateFormat = SimpleDateFormat("dd.MM.yyyy 'at' hh:mm:ssa", Locale.getDefault())
    val jsonDetails = JsonParser().parse(operationDetails).asJsonObject

    val fee: String = "%.2f".format(if (transactionSource == walletPK) (transactionFee / transactionOperationCount).toFloat() else 0F)

    when (type) {
        Operation.Type.PAYMENT.value -> {
            val destination = jsonDetails.get("to").asString
            val assetType = jsonDetails.get("asset_type").asString
            val currency: String = if (assetType != "native") assetType else "XLM"

            return Payment(
                    id,
                    fee,
                    order,
                    dateFormat.format(created),
                    transactionHash,
                    transactionMemoType,
                    transactionMemo,
                    destination,
                    "%.2f".format(jsonDetails.get("amount").asFloat),
                    currency,
                    destination == walletPK)
        }

        Operation.Type.CREATE_ACCOUNT.value -> {
            return Payment(
                    id,
                    fee,
                    order,
                    dateFormat.format(created),
                    transactionHash,
                    transactionMemoType,
                    transactionMemo,
                    jsonDetails.get("account").asString,
                    "%.2f".format(jsonDetails.get("starting_balance").asFloat),
                    "XLM",
                    true)
        }
        else -> {
            throw Error("Unknown type")
        }
    }
}