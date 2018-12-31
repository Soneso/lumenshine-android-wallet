package com.soneso.lumenshine.model

import com.soneso.lumenshine.model.entities.wallet.WalletDetailEntity
import com.soneso.lumenshine.model.entities.wallet.WalletEntity
import com.soneso.lumenshine.model.wrapper.toStellarWallet
import com.soneso.lumenshine.model.wrapper.toWallet
import com.soneso.lumenshine.networking.NetworkStateObserver
import com.soneso.lumenshine.networking.api.WalletApi
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.persistence.room.LsDatabase
import com.soneso.lumenshine.util.*
import io.reactivex.Flowable
import io.reactivex.Single
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
import org.stellar.sdk.requests.ErrorResponse
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor(
        private val networkStateObserver: NetworkStateObserver,
        r: Retrofit,
        private val stellarServer: Server,
        db: LsDatabase
) {

    private val walletApi = r.create(WalletApi::class.java)
    private val walletDao = db.walletDao()

    fun loadAllWallets(): Flowable<Resource<List<WalletEntity>, ServerException>> {

        val refresher: Flowable<Resource<List<WalletEntity>, ServerException>> = walletApi.getAllWallets()
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({ dto ->
                    dto.map { it.toWallet() }
                }, { it })

        return walletDao.getAllWallets()
                .map { Success<List<WalletEntity>, ServerException>(it) as Resource<List<WalletEntity>, ServerException> }
                .refreshWith(refresher) { walletDao.insertAll(it) }
    }

    fun loadWalletDetails(publicKey: String): Single<WalletDetailEntity> {

        return Single.create {
            try {
                val ar = stellarServer.accounts().account(KeyPair.fromAccountId(publicKey))
                it.onSuccess(ar.toStellarWallet())
            } catch (e: ErrorResponse) {
                when (e.code) {
                    404 -> it.onSuccess(WalletDetailEntity())
                    else -> it.onError(e)
                }
            }
        }
    }
}