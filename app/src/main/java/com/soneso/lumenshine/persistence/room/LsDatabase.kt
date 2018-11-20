package com.soneso.lumenshine.persistence.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.model.entities.UserSecurity
import com.soneso.lumenshine.model.entities.Wallet

@Database(
        entities = [UserSecurity::class, Wallet::class, RegistrationStatus::class],
        version = 2
)
abstract class LsDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun walletDao(): WalletDao
}