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
            name,
            federationAddress,
            showOnHomeScreen,
            publicKey
    )
}

fun AccountResponse.toStellarWallet(): WalletDetailEntity {

    return WalletDetailEntity(
            balances.map { it.toWalletBalance() }
    )
}

fun AccountResponse.Balance.toWalletBalance(): WalletBalanceEntity {
    return WalletBalanceEntity(balance.toDouble(), assetCode
            ?: "XLM", AssetType.fromStellarName(assetType))
}