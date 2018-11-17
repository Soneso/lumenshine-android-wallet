package com.soneso.lumenshine.domain.data

import com.soneso.lumenshine.model.entities.StellarWallet
import com.soneso.lumenshine.model.entities.WalletEntity

class WalletCardData(entity: WalletEntity, stellar: StellarWallet? = null) {

    val id = entity.id
    val name = entity.name
    val federationAddress = entity.federationAddress
    val shownInHomeScreen = entity.shownInHomeScreen
    var publicKey0 = stellar?.publicKey0
        private set

    fun setStellarWallet(wallet: StellarWallet) {
        publicKey0 = wallet.publicKey0
    }

    fun hasBalances() = publicKey0 != null
}