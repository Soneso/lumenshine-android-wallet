package com.soneso.lumenshine.domain.data

import com.soneso.lumenshine.model.entities.wallet.WalletEntity
import java.util.*

data class TransactionsFilter(
        //name and PK needed from the wallet
        val wallet: WalletEntity,
        val dateFrom: Date = Date(System.currentTimeMillis() - 7 * DAY_IN_MS),
        val dateTo: Date = Date()
) {
    companion object {
        const val DAY_IN_MS = 1000 * 60 * 60 * 24
    }
}