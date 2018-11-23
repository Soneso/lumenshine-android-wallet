package com.soneso.lumenshine.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.soneso.lumenshine.persistence.room.DbNames

@Entity(tableName = DbNames.TABLE_REGISTRATION_STATUS)
data class RegistrationStatus(

        @PrimaryKey
        @ColumnInfo(name = DbNames.COLUMN_USERNAME)
        val username: String,

        @ColumnInfo(name = DbNames.COLUMN_MAIL_CONFIRMATION)
        val mailConfirmed: Boolean,

        @ColumnInfo(name = DbNames.COLUMN_TFA_CONFIRMATION)
        val tfaConfirmed: Boolean,

        @ColumnInfo(name = DbNames.COLUMN_MNEMONIC_CONFIRMATION)
        val mnemonicConfirmed: Boolean
) {
        fun isSetupCompleted() = mailConfirmed && tfaConfirmed && mnemonicConfirmed
}