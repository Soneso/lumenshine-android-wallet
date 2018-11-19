package com.soneso.lumenshine.model.entities.wallet

data class WalletDetailEntity(
        val balances: List<WalletBalanceEntity>,
        val subentryCount: Int
) {
    constructor() : this(emptyList<WalletBalanceEntity>(), 0)
}