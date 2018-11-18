package com.soneso.lumenshine.domain.usecases

import com.soneso.lumenshine.domain.data.WalletCardData
import com.soneso.lumenshine.model.WalletRepository
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.util.Failure
import com.soneso.lumenshine.util.Resource
import com.soneso.lumenshine.util.Success
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class WalletsUseCase @Inject constructor(
        private val walletRepo: WalletRepository
) {

    fun provideAllWallets(): Flowable<Resource<WalletCardData, ServerException>> {

        return Flowable.create<Resource<WalletCardData, ServerException>>({ emitter ->
            val cd = CompositeDisposable()
            val d = walletRepo.loadAllWallets()
                    .flatMap {
                        when (it.state) {
                            Resource.SUCCESS -> Flowable.fromIterable(it.success())
                                    .map { entity -> Success<WalletCardData, ServerException>(WalletCardData(entity)) }

                            Resource.FAILURE -> Flowable.just(Failure<WalletCardData, ServerException>(it.failure()))
                            else -> Flowable.just(Resource(Resource.LOADING))
                        }
                    }
                    .doOnNext { resource ->
                        when (resource.state) {
                            Resource.SUCCESS -> {
                                val d = walletRepo.loadStellarWallet(resource.success().publicKey)
                                        .subscribeOn(Schedulers.io())
                                        .subscribe({ sw ->
                                            emitter.onNext(Success(resource.success().apply { setStellarWallet(sw) }))
                                        }, { throwable ->
                                            emitter.onNext(Failure(ServerException(throwable)))
                                        })
                                cd.add(d)
                            }
                        }
                    }
                    .subscribe { emitter.onNext(it) }
            cd.add(d)
            emitter.setCancellable { cd.dispose() }
        }, BackpressureStrategy.LATEST)
    }
}