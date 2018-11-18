package com.soneso.lumenshine.model.entities.wallet

import androidx.room.Entity
import com.soneso.lumenshine.persistence.room.DbNames

@Entity(tableName = DbNames.TABLE_STELLAR_ACCOUNTS)
data class WalletDetailEntity(
        val balances: List<WalletBalanceEntity>
) {
    constructor() : this(emptyList<WalletBalanceEntity>())
}