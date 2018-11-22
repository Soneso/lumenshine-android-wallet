package com.soneso.lumenshine.domain.usecases

import com.soneso.lumenshine.domain.data.OperationFilter
import com.soneso.lumenshine.model.TransactionRepository
import com.soneso.lumenshine.model.WalletRepository
import com.soneso.lumenshine.model.entities.operations.Operation
import com.soneso.lumenshine.model.entities.wallet.WalletEntity
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.util.Resource
import com.soneso.lumenshine.util.mapResource
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import javax.inject.Inject

class TransactionsUseCases @Inject constructor(
        private val transactionRepo: TransactionRepository,
        private val walletRepo: WalletRepository
) {

    val operationFilter = BehaviorProcessor.create<OperationFilter>()

    fun getWallet(): Flowable<Resource<WalletEntity, ServerException>> = walletRepo.loadAllWallets().mapResource({ it -> it[0] }, { it })

    fun getWallets(): Flowable<Resource<List<WalletEntity>, ServerException>> = walletRepo.loadAllWallets()

    fun getOperations(): Flowable<Resource<List<Operation>, ServerException>> =
            operationFilter.flatMap {
                transactionRepo.loadOperations(it.wallet.publicKey, it.dateFrom, it.dateTo)
            }

    fun setPrimaryWallet(wallet: WalletEntity) {
        val filter = OperationFilter(wallet)
        operationFilter.onNext(filter)
    }
}
