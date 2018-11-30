package com.soneso.lumenshine.domain.usecases

import com.soneso.lumenshine.domain.data.OperationsFilter
import com.soneso.lumenshine.domain.data.TransactionsFilter
import com.soneso.lumenshine.model.TransactionRepository
import com.soneso.lumenshine.model.WalletRepository
import com.soneso.lumenshine.model.entities.operations.Operation
import com.soneso.lumenshine.model.entities.wallet.WalletEntity
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.util.Resource
import com.soneso.lumenshine.util.mapResource
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionsUseCases @Inject constructor(
        private val transactionRepo: TransactionRepository,
        private val walletRepo: WalletRepository
) {

    //   TODO make private
    val transactionsFilter = BehaviorProcessor.create<TransactionsFilter>()
    // not a stream
    val operationsFilter = BehaviorProcessor.create<OperationsFilter>()

    val wallets = BehaviorProcessor.create<Resource<List<WalletEntity>, ServerException>>()

    init {
        walletRepo.loadAllWallets()
                .doOnComplete {
                    Timber.e("TUC - ON COMPLETE")
                }
                .subscribeOn(Schedulers.io())
                .subscribe(wallets)
    }

    fun getWallet(): Flowable<Resource<WalletEntity, ServerException>> =
            wallets
                    .doOnNext {
                        Timber.d("TUC - Resource state: ${it.state}")
                    }
                    .mapResource({ it -> it[0] }, { it })

    fun getWallets(): Flowable<Resource<List<WalletEntity>, ServerException>> = wallets

    fun getOperations(): Flowable<Resource<List<Operation>, ServerException>> =
            transactionsFilter
                    .observeOn(Schedulers.io())
                    .flatMap {
                        transactionRepo.loadOperations(it.wallet.publicKey, it.dateFrom, it.dateTo)
                    }
//                    .filter{
//                        //TODO
//                    }

    fun setPrimaryWallet(wallet: WalletEntity) {
        val filter = TransactionsFilter(wallet)
        transactionsFilter.onNext(filter)
    }

    fun updateTransactionsFilter(newFilter: TransactionsFilter) {
        transactionsFilter.onNext(newFilter)
    }

    fun updateOperationsFilter(newFilter: OperationsFilter) {
        operationsFilter.onNext(newFilter)
    }

    fun observeTransactionsFilter(): Flowable<TransactionsFilter> = transactionsFilter

    fun observeOperationsFilter(): Flowable<OperationsFilter> = operationsFilter
}
