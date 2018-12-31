package com.soneso.lumenshine.domain.usecases

import com.google.common.collect.ComparisonChain
import com.soneso.lumenshine.domain.data.OperationsFilter
import com.soneso.lumenshine.domain.data.OperationsSort
import com.soneso.lumenshine.domain.data.TransactionsFilter
import com.soneso.lumenshine.domain.util.SortType
import com.soneso.lumenshine.model.TransactionRepository
import com.soneso.lumenshine.model.WalletRepository
import com.soneso.lumenshine.model.entities.operations.Offer
import com.soneso.lumenshine.model.entities.operations.Operation
import com.soneso.lumenshine.model.entities.operations.Payment
import com.soneso.lumenshine.model.entities.wallet.WalletDetailEntity
import com.soneso.lumenshine.model.entities.wallet.WalletEntity
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.util.Resource
import com.soneso.lumenshine.util.mapResource
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionsUseCases @Inject constructor(
        private val transactionRepo: TransactionRepository,
        private val walletRepo: WalletRepository
) {

    private val transactionsFilter = BehaviorProcessor.create<TransactionsFilter>()
    private var operationSort = BehaviorProcessor.create<OperationsSort>()

    var operationsSort = OperationsSort()
    // #Zica replace the object with 1 Processor / Operations Filter Fragment and unify them in 1
    val operationFilter = OperationsFilter()

    val wallets = BehaviorProcessor.create<Resource<List<WalletEntity>, ServerException>>()

    init {
        walletRepo.loadAllWallets()
                .subscribeOn(Schedulers.io())
                .subscribe(wallets)
    }

    fun getWallet(): Flowable<Resource<WalletEntity, ServerException>> =
            wallets.mapResource({ it -> it[0] }, { it })

    fun getWalletDetails(wallet: WalletEntity): Single<WalletDetailEntity> =
            walletRepo.loadWalletDetails(wallet.publicKey)


    fun getWallets(): Flowable<Resource<List<WalletEntity>, ServerException>> = wallets

    fun getOperations(): Flowable<Resource<List<Operation>, ServerException>> =
            transactionsFilter
                    .observeOn(Schedulers.io())
                    .flatMap {
                        transactionRepo.loadOperations(it.wallet.publicKey, it.dateFrom, it.dateTo)
                    }
                    .map {
                        when (it.state) {
                            Resource.SUCCESS -> {
                                var filteredOperations: List<Operation> = getFilteredList(it.success())
                                Collections.sort(filteredOperations, OperationComparator(operationsSort))
                                Resource(it.state, filteredOperations)
                            }
                            else -> it
                        }
                    }

    private fun getFilteredList(allOperations: List<Operation>): List<Operation> {

        if (!operationFilter.isActive()) return allOperations

        val filteredOperations: ArrayList<Operation> = ArrayList()

        allOperations.forEach { operation ->
            if (operationFilter.getOperationTypes().contains(operation.type) && !operationMemoFiltered(operation)) {

                if (Operation.Type.PAYMENT == operation.type || Operation.Type.PATH_PAYMENT == operation.type) {
                    if (!paymentOperationFiltered(operation as Payment)) filteredOperations.add(operation)

                } else if (Operation.Type.OFFER == operation.type || Operation.Type.PASSIVE_OFFER == operation.type) {
                    if (!offerOperationFiltered(operation as Offer)) filteredOperations.add(operation)

                } else {
                    filteredOperations.add(operation)
                }
            }
        }

        return filteredOperations
    }

    private fun operationMemoFiltered(operation: Operation): Boolean = !operationFilter.memo.isEmpty() && !operation.transactionMemo.contains(operationFilter.memo, true)


    private fun paymentOperationFiltered(payment: Payment): Boolean {
        if (payment.isReceived) {
            if (operationFilter.paymentsFilter.minimumOneSelected() && !operationFilter.paymentsFilter.filterReceived)
                return true

            if (operationFilter.paymentsFilter.receivedFrom != null && payment.amount.toDouble() < operationFilter.paymentsFilter.receivedFrom!!
                    || operationFilter.paymentsFilter.receivedTo != null && payment.amount.toDouble() > operationFilter.paymentsFilter.receivedTo!!)
                return true
        }

        if (!payment.isReceived) {
            if (operationFilter.paymentsFilter.minimumOneSelected() && !operationFilter.paymentsFilter.filterSent)
                return true
            if (operationFilter.paymentsFilter.sentFrom != null && payment.amount.toDouble() < operationFilter.paymentsFilter.sentFrom!!
                    || operationFilter.paymentsFilter.sentTo != null && payment.amount.toDouble() > operationFilter.paymentsFilter.sentTo!!)
                return true
        }

        if (operationFilter.paymentsFilter.filterCurrency
                && operationFilter.paymentsFilter.currency != null && payment.currency != operationFilter.paymentsFilter.currency)
            return true

        return false
    }

    private fun offerOperationFiltered(offer: Offer): Boolean {
        if (operationFilter.offersFilter.filterSellingCurrency
                && operationFilter.offersFilter.sellingCurrency != null && offer.sellingAssetType != operationFilter.offersFilter.sellingCurrency)
            return true

        if (operationFilter.offersFilter.filterBuyingCurrency
                && operationFilter.offersFilter.buyingCurrency != null && offer.buyingAssetCode != operationFilter.offersFilter.buyingCurrency)
            return true

        return false
    }

    fun setPrimaryWallet(wallet: WalletEntity) {
        val filter = TransactionsFilter(wallet)
        transactionsFilter.onNext(filter)
    }

    fun updateTransactionsFilter(newFilter: TransactionsFilter) {
        transactionsFilter.onNext(newFilter)
    }

    fun observeTransactionsFilter(): Flowable<TransactionsFilter> = transactionsFilter

    fun clearPaymentsFilters() {
        operationFilter.paymentsFilter.filterReceived = false
        operationFilter.paymentsFilter.receivedFrom = null
        operationFilter.paymentsFilter.receivedTo = null
        operationFilter.paymentsFilter.filterSent = false
        operationFilter.paymentsFilter.sentFrom = null
        operationFilter.paymentsFilter.sentTo = null
        operationFilter.paymentsFilter.filterCurrency = false
        operationFilter.paymentsFilter.currency = null
    }

    fun clearOffersFilters() {
        operationFilter.offersFilter.filterSellingCurrency = false
        operationFilter.offersFilter.sellingCurrency = null
        operationFilter.offersFilter.filterBuyingCurrency = false
        operationFilter.offersFilter.buyingCurrency = null
    }

    class OperationComparator(private val operationsSort: OperationsSort) : Comparator<Operation> {

        override fun compare(o1: Operation?, o2: Operation?): Int {

            val dateFormat = SimpleDateFormat("dd.MM.yyyy 'at' hh:mm:ssa", Locale.getDefault())
            var chainStart = ComparisonChain.start()

            if (operationsSort.sortByDate)
                chainStart =
                        if (SortType.ASC == operationsSort.dateSortType)
                            chainStart.compare(dateFormat.parse(o1!!.date), dateFormat.parse(o2!!.date))
                        else
                            chainStart.compare(dateFormat.parse(o2!!.date), dateFormat.parse(o1!!.date))

            if (operationsSort.sortByType)
                chainStart =
                        if (SortType.ASC == operationsSort.typeSortType)
                            chainStart.compare(o1!!.type.name, o2!!.type.name)
                        else
                            chainStart.compare(o2!!.type.name, o1!!.type.name)

            if (operationsSort.sortByAmount)
                chainStart =
                        if (SortType.ASC == operationsSort.amountSortType)
                            chainStart.compare(o1!!.getSortAmount(), o2!!.getSortAmount(), nullsLast())
                        else
                            chainStart.compare(o2!!.getSortAmount(), o1!!.getSortAmount(), nullsLast())

            if (operationsSort.sortByCurrency)
                chainStart =
                        if (SortType.ASC == operationsSort.currencySortType)
                            chainStart.compare(o1!!.getSortCurrency(), o2!!.getSortCurrency(), nullsLast())
                        else
                            chainStart.compare(o2!!.getSortCurrency(), o1!!.getSortCurrency(), nullsLast())

            return chainStart.result()
        }
    }
}