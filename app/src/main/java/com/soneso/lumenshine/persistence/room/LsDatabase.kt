package com.soneso.lumenshine.persistence.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.model.entities.UserSecurity
import com.soneso.lumenshine.model.entities.wallet.WalletEntity

@Database(
        entities = [UserSecurity::class, WalletEntity::class, RegistrationStatus::class],
        version = 1
)
abstract class LsDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun walletDao(): WalletDao
}