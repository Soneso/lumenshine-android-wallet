package com.soneso.lumenshine.model.wrapper

import com.soneso.lumenshine.model.entities.StellarWallet
import com.soneso.lumenshine.model.entities.WalletBalanceEntity
import com.soneso.lumenshine.model.entities.WalletEntity
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

fun AccountResponse.toStellarWallet(): StellarWallet {

    return StellarWallet(
            balances.map { it.toWalletBalance() }
    )
}

fun AccountResponse.Balance.toWalletBalance(): WalletBalanceEntity {
    return WalletBalanceEntity(balance.toDouble(), assetType)
}