package com.soneso.lumenshine.model

import com.soneso.lumenshine.model.entities.operations.Operation
import com.soneso.lumenshine.model.wrapper.toOperation
import com.soneso.lumenshine.networking.NetworkStateObserver
import com.soneso.lumenshine.networking.api.TransactionApi
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.util.Resource
import com.soneso.lumenshine.util.asHttpResourceLoader
import com.soneso.lumenshine.util.mapResource
import io.reactivex.Flowable
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class TransactionRepository @Inject constructor(
        private val networkStateObserver: NetworkStateObserver,
        r: Retrofit
) {

    private val transactionApi = r.create(TransactionApi::class.java)

    fun loadOperations(walletPK: String, start: Date, end: Date): Flowable<Resource<List<Operation>, ServerException>> {

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        return transactionApi.getTransactions(
                walletPK,
                dateFormat.format(start),
                dateFormat.format(end))
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({ dto ->
                    dto.map { it.toOperation(walletPK) }
                }, { it })


    }

}