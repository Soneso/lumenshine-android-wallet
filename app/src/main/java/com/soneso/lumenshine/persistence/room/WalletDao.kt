package com.soneso.lumenshine.persistence.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.soneso.lumenshine.model.entities.WalletEntity
import io.reactivex.Flowable

@Dao
interface WalletDao {

    @Query("SELECT * FROM ${DbNames.TABLE_WALLETS}")
    fun getAllWallets(): Flowable<List<WalletEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(walletEntities: List<WalletEntity>)
}