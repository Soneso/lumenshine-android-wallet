package com.soneso.lumenshine.persistence.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.soneso.lumenshine.model.entities.UserSecurity
import io.reactivex.Single

@Dao
interface UserDao {

    @Query("SELECT * FROM ${DbNames.TABLE_USER_DATA} WHERE ${DbNames.COLUMN_USERNAME} = :username")
    fun getUserDataById(username: String): Single<UserSecurity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveUserData(userData: UserSecurity)
}