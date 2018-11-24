package com.soneso.lumenshine.persistence.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.soneso.lumenshine.model.entities.wallet.WalletEntity

@Database(
        entities = [WalletEntity::class],
        version = 1
)
abstract class LsDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao
}