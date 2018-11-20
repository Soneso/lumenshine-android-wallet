package com.soneso.lumenshine.model.wrapper

import android.util.Base64
import com.soneso.lumenshine.model.entities.operations.*
import com.soneso.lumenshine.networking.dto.OperationDto
import com.soneso.lumenshine.networking.dto.Parse
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet

fun OperationDto.toOperation(walletPK: String): Operation {

    val dateFormat = SimpleDateFormat("dd.MM.yyyy 'at' hh:mm:ssa", Locale.getDefault())

    val fee: String = "%.2f".format(if (transactionSource == walletPK) (transactionFee / transactionOperationCount).toFloat() else 0F)
    val byCurrentWallet: Boolean = transactionSource == walletPK

    val mapper = Parse.createMapper()

    when (type) {
        Operation.Type.CREATE_ACCOUNT.value -> {
            val details = mapper.readValue(operationDetails, OperationDto.CreateAccountDetails::class.java)


            return Payment(id, fee, order, dateFormat.format(created), byCurrentWallet,
                    transactionSource, transactionHash, transactionMemoType, transactionMemo,
                    details.funder,
                    details.account,
                    "%.2f".format(details.startingBalance),
                    "XLM",
                    true)
        }

        Operation.Type.PAYMENT.value -> {
            val details = mapper.readValue(operationDetails, OperationDto.PaymentDetails::class.java)

            return Payment(id, fee, order, dateFormat.format(created), byCurrentWallet,
                    transactionSource, transactionHash, transactionMemoType, transactionMemo,
                    details.from,
                    details.destination,
                    "%.2f".format(details.amount),
                    getAssetText(details.assetType),
                    details.destination == walletPK)
        }

        Operation.Type.CHANGE_TRUST.value -> {
            val details = mapper.readValue(operationDetails, OperationDto.ChangeTrustDetails::class.java)

            return ChangeTrust(id, fee, order, dateFormat.format(created), byCurrentWallet,
                    transactionSource, transactionHash, transactionMemoType, transactionMemo,
                    details.limit,
                    details.assetCode,
                    details.assetIssuer)
        }

        Operation.Type.OFFER.value -> {
            val details = mapper.readValue(operationDetails, OperationDto.OfferDetails::class.java)

            return Offer(
                    id, fee, Operation.Type.OFFER, order, dateFormat.format(created), byCurrentWallet,
                    transactionSource, transactionHash, transactionMemoType, transactionMemo,
                    details.offerId,
                    details.amount.toString(),
                    details.price.toString(),
                    getAssetText(details.buyingAssetCode),
                    getAssetText(details.sellingAssetType))
        }

        Operation.Type.PASSIVE_OFFER.value -> {
            val details = mapper.readValue(operationDetails, OperationDto.OfferDetails::class.java)

            return Offer(
                    id, fee, Operation.Type.PASSIVE_OFFER, order, dateFormat.format(created), byCurrentWallet,
                    transactionSource, transactionHash, transactionMemoType, transactionMemo,
                    details.offerId,
                    details.amount.toString(),
                    details.price.toString(),
                    getAssetText(details.buyingAssetCode),
                    getAssetText(details.sellingAssetType))
        }

        Operation.Type.MANAGE_DATA.value -> {
            val details = mapper.readValue(operationDetails, OperationDto.ManageDataDetails::class.java)

            val decodedBytes = Base64.decode(details.value, Base64.DEFAULT)
            val value = String(decodedBytes, StandardCharsets.US_ASCII)
            //TODO the case where the content is binary

            return ManageData(
                    id, fee, order, dateFormat.format(created), byCurrentWallet,
                    transactionSource, transactionHash, transactionMemoType, transactionMemo,
                    details.name,
                    value)
        }

        Operation.Type.PATH_PAYMENT.value -> {
            val details = mapper.readValue(operationDetails, OperationDto.PathPaymentDetails::class.java)

            val isReceived: Boolean = details.destination == walletPK
            val amount = if (isReceived) details.amount else details.sourceAmount
            val currency = if (isReceived) details.assetType else details.sourceAssetType

            return Payment(
                    id, fee, order, dateFormat.format(created), byCurrentWallet,
                    transactionSource, transactionHash, transactionMemoType, transactionMemo,
                    details.from,
                    details.destination,
                    "%.2f".format(amount),
                    getAssetText(currency),
                    isReceived)
        }

        Operation.Type.BUMP_SEQUENCE.value -> {
            val details = mapper.readValue(operationDetails, OperationDto.BumpSequenceDetails::class.java)

            return BumpSequence(
                    id, fee, order, dateFormat.format(created), byCurrentWallet,
                    transactionSource, transactionHash, transactionMemoType, transactionMemo,
                    details.bumpTo)
        }

        Operation.Type.ACCOUNT_MERGE.value -> {
            val details = mapper.readValue(operationDetails, OperationDto.AccountMergeDetails::class.java)

            return AccountMerge(
                    id, fee, order, dateFormat.format(created), byCurrentWallet,
                    transactionSource, transactionHash, transactionMemoType, transactionMemo,
                    details.mergedAccount)
        }

        Operation.Type.SET_OPTIONS.value -> {
            val details = mapper.readValue(operationDetails, OperationDto.SetOptionDetails::class.java)

            val setOptionOperation = SetOptions(
                    id, fee, order, dateFormat.format(created), byCurrentWallet,
                    transactionSource, transactionHash, transactionMemoType, transactionMemo)

            setOptionOperation.inflationDestination = details.inflationDestination
            setOptionOperation.setFlags = getFlagTexts(details.setFlags)
            setOptionOperation.clearedFlags = getFlagTexts(details.clearFlags)
            setOptionOperation.masterWeight = details.masterWeight
            setOptionOperation.lowThreshold = details.lowThreshold
            setOptionOperation.mediumThreshold = details.mediumThreshold
            setOptionOperation.highThreshold = details.highThreshold
            setOptionOperation.signerKey = details.signerKey
            setOptionOperation.signerType = getSignerType(details.signerKey)
            setOptionOperation.signerWeight = details.signerWeight
            setOptionOperation.homeDomain = details.homeDomain

            return setOptionOperation
        }

        else -> {
            throw Error("Unknown type")
        }
    }
}

private fun getAssetText(asset: String): String = if (asset == "native") "XLM" else asset

private fun getFlagTexts(flags: Set<String>?): Set<String>? {
    if (flags == null) return null

    val texts: HashSet<String> = HashSet()
    flags.forEach {
        when (it) {
            "auth_required" -> texts.add("Authorisation required")
            "auth_revocable" -> texts.add("Authorization revocable")
            "auth_immutable" -> texts.add("Authorization immutable")
        }
    }
    return if (texts.isEmpty()) null else texts
}

private fun getSignerType(signerKey: String?): String? {
    if (signerKey == null) return null

    return when (signerKey[0]) {
        'G' -> "Ed25519 Public Key"
        'X' -> "sha256 Hash"
        'T' -> "Pre-authorized Transaction Hash"
        else -> null
    }
}