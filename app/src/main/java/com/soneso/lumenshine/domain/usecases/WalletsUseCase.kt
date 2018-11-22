package com.soneso.lumenshine.domain.usecases

import com.soneso.lumenshine.domain.data.Wallet
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

    /**
     * This method returns a flowable stream on which is emitted all the wallets one by one. Every wallet will be sent 2 times,
     * first time it contains data from horizon server, then second time it contains all details fetched from stellar sdk.
     */
    fun provideWallets(forHomeScreenOnly: Boolean = false): Flowable<Resource<Wallet, ServerException>> {

        return Flowable.create<Resource<Wallet, ServerException>>({ emitter ->
            val cd = CompositeDisposable()
            val d = walletRepo.loadAllWallets()
                    .flatMap {
                        when (it.state) {
                            Resource.SUCCESS -> Flowable.fromIterable(it.success().filter { w -> w.shownInHomeScreen || !forHomeScreenOnly })
                                    .map { entity -> Success<Wallet, ServerException>(Wallet(entity)) }

                            Resource.FAILURE -> Flowable.just(Failure<Wallet, ServerException>(it.failure()))
                            else -> Flowable.just(Resource(Resource.LOADING))
                        }
                    }
                    .doOnNext { resource ->
                        when (resource.state) {
                            Resource.SUCCESS -> {
                                val d = walletRepo.loadWalletDetails(resource.success().publicKey)
                                        .subscribeOn(Schedulers.io())
                                        .subscribe({ sw ->
                                            emitter.onNext(Success(resource.success().apply { setDetails(sw) }))
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