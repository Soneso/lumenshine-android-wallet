package com.soneso.lumenshine.domain.usecases

import com.soneso.lumenshine.model.TransactionRepository
import com.soneso.lumenshine.model.WalletRepository
import com.soneso.lumenshine.model.entities.Wallet
import com.soneso.lumenshine.model.entities.operations.Operation
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.util.Resource
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import java.util.*
import javax.inject.Inject

class TransactionsUseCases @Inject constructor(
        private val transactionRepo: TransactionRepository,
        private val walletRepo: WalletRepository
) {

    private val defaultStartDate: Date = Date(System.currentTimeMillis() - 7 * DAY_IN_MS)
    private val defaultEndDate: Date = Date()

    val selectedWallet = BehaviorProcessor.create<Wallet>()
    val selectedDateFrom = BehaviorProcessor.create<Date>()
    val selectedDateTo = BehaviorProcessor.create<Date>()

    fun getWallets(): Flowable<Resource<List<Wallet>, ServerException>> = walletRepo.loadAllWallets()

    fun getOperations(): Flowable<Resource<List<Operation>, ServerException>> =
            transactionRepo.loadOperations(
                    selectedWallet.value!!.publicKey,
                    if (selectedDateFrom.value != null) selectedDateFrom.value!! else defaultStartDate,
                    if (selectedDateTo.value != null) selectedDateTo.value!! else defaultEndDate)

    fun setWallet(wallet: Wallet) {
        selectedWallet.onNext(wallet)
    }

    fun setDateFrom(dateFrom: Date) {
        selectedDateFrom.onNext(dateFrom)
    }

    fun setDateTo(dateTo: Date) {
        selectedDateTo.onNext(dateTo)
    }

    companion object {
        const val DAY_IN_MS = 1000 * 60 * 60 * 24
    }
}
