package com.soneso.lumenshine.model.wrapper

import com.soneso.lumenshine.model.entities.StellarWallet
import com.soneso.lumenshine.model.entities.Wallet
import com.soneso.lumenshine.networking.dto.WalletDto
import org.stellar.sdk.responses.AccountResponse

fun WalletDto.toWallet(): Wallet {

    return Wallet(
            id,
            publicKey,
            name,
            federationAddress,
            showOnHomeScreen
    )
}

fun AccountResponse.toStellarWallet(): StellarWallet {

    return StellarWallet(
            String(keypair.publicKey)
    )
}