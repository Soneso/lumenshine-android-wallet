package com.soneso.lumenshine.domain.data

import com.soneso.lumenshine.model.entities.wallet.WalletDetailEntity
import com.soneso.lumenshine.model.entities.wallet.WalletEntity

class Wallet(entity: WalletEntity, details: WalletDetailEntity? = null) {

    val id = entity.id
    val name = entity.name
    val federationAddress = entity.federationAddress
    val shownInHomeScreen = entity.shownInHomeScreen
    val publicKey = entity.publicKey
    var balances = details?.balances
        private set

    fun setDetails(details: WalletDetailEntity) {
        balances = details.balances
    }

    fun hasBalances() = balances?.isNotEmpty() == true

    fun isLoaded() = balances != null
}