package com.soneso.lumenshine.model.wrapper

import com.soneso.lumenshine.model.entities.wallet.AssetType
import com.soneso.lumenshine.model.entities.wallet.WalletBalanceEntity
import com.soneso.lumenshine.model.entities.wallet.WalletDetailEntity
import com.soneso.lumenshine.model.entities.wallet.WalletEntity
import com.soneso.lumenshine.networking.dto.WalletDto
import org.stellar.sdk.responses.AccountResponse

fun WalletDto.toWallet(): WalletEntity {

    return WalletEntity(
            id,
            publicKey,
            name,
            federationAddress,
            showOnHomeScreen,
            publicKey
    )
}

fun AccountResponse.toStellarWallet(): WalletDetailEntity {

    val balanceList = balances.map { it.toWalletBalance() }
            // cristi.paval, 11/20/18 - put the native balance on the first position
            .sortedWith(Comparator { b1, b2 ->
                when {
                    b1.type == AssetType.NATIVE -> -1
                    b2.type == AssetType.NATIVE -> 1
                    else -> 0
                }
            })
    return WalletDetailEntity(
            balanceList,
            subentryCount ?: 0
    )
}

fun AccountResponse.Balance.toWalletBalance(): WalletBalanceEntity {
    return WalletBalanceEntity(balance.toDouble(), assetCode
            ?: "XLM", AssetType.fromStellarName(assetType), sellingLiabilities.toDouble())
}