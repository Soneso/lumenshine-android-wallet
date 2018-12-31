package com.soneso.lumenshine.networking.api

import com.soneso.lumenshine.networking.dto.OperationDto
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TransactionApi {

    @GET("/portal/user/dashboard/get_stellar_transactions")
    fun getTransactions(
            @Query("stellar_account_pk") accountPK: String,
            @Query("start_timestamp") startTimestamp: String,
            @Query("end_timestamp") endTimestamp: String
    ): Single<Response<List<OperationDto>?>>
}
