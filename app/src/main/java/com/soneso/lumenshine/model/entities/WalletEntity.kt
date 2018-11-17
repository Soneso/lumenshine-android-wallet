package com.soneso.lumenshine.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.soneso.lumenshine.persistence.room.DbNames

@Entity(tableName = DbNames.TABLE_WALLETS)
data class WalletEntity(

        @PrimaryKey
        @ColumnInfo(name = DbNames.COLUMN_ID)
        val id: Long,

        @ColumnInfo(name = DbNames.COLUMN_NAME)
        val name: String,

        @ColumnInfo(name = DbNames.COLUMN_FEDERATION_ADDRESS)
        val federationAddress: String,

        @ColumnInfo(name = DbNames.COLUMN_IN_HOME_SCREEN)
        val shownInHomeScreen: Boolean
)