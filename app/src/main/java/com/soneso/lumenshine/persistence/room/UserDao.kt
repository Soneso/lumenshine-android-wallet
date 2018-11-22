package com.soneso.lumenshine.persistence.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.model.entities.UserSecurity
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface UserDao {

    @Query("SELECT * FROM ${DbNames.TABLE_USER_DATA} WHERE ${DbNames.COLUMN_USERNAME} = :username")
    fun getUserDataById(username: String): Single<UserSecurity>

    @Query("SELECT * FROM ${DbNames.TABLE_REGISTRATION_STATUS} WHERE ${DbNames.COLUMN_USERNAME} = :username")
    fun getRegistrationStatus(username: String): Flowable<RegistrationStatus>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRegistrationStatus(status: RegistrationStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveUserData(userData: UserSecurity)

    @Query("DELETE FROM ${DbNames.TABLE_REGISTRATION_STATUS} WHERE ${DbNames.COLUMN_USERNAME} = :username")
    fun removeRegistrationStatus(username: String)
}