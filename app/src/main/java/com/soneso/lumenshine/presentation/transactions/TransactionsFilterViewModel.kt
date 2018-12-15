package com.soneso.lumenshine.presentation.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.data.TransactionsFilter
import com.soneso.lumenshine.domain.usecases.TransactionsUseCases
import com.soneso.lumenshine.model.entities.wallet.WalletEntity
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.presentation.util.putValue
import com.soneso.lumenshine.util.Resource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.HashSet

class TransactionsFilterViewModel(private val transactionsUseCases: TransactionsUseCases) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val liveWallets: LiveData<Resource<List<WalletEntity>, ServerException>> = MutableLiveData()
    val liveTransactionsFilter: LiveData<TransactionsFilter> = MutableLiveData()
    lateinit var initialTransactionFilterState: TransactionsFilter

    init {
        compositeDisposable.add(fetchAllWallets())
        val d = transactionsUseCases.observeTransactionsFilter()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    liveTransactionsFilter.putValue(it)
                }
        compositeDisposable.add(d)
    }

    private fun fetchAllWallets() = transactionsUseCases.getWallets()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                (liveWallets as MutableLiveData).value = it
            }

    fun updateFilters(wallet: WalletEntity, dateFrom: Date, dateTo: Date) {
        val transactionsFilter = TransactionsFilter(wallet, dateFrom, dateTo)
        transactionsUseCases.updateTransactionsFilter(transactionsFilter)
    }

    fun updateOperationFilter(memo: String, paymentsActive: Boolean, offersActive: Boolean, otherActive: Boolean) {
        transactionsUseCases.operationFilter.memo = memo
        transactionsUseCases.operationFilter.paymentsFilter.active = paymentsActive
        transactionsUseCases.operationFilter.offersFilter.active = offersActive
        transactionsUseCases.operationFilter.othersFilter.active = otherActive
    }

    fun updatePaymentOperationFilter(paymentsActive: Boolean) {
        transactionsUseCases.operationFilter.paymentsFilter.active = paymentsActive
        if (!paymentsActive) transactionsUseCases.clearPaymentsFilters()

    }

    fun updateOfferOperationFilter(offerActive: Boolean) {
        transactionsUseCases.operationFilter.offersFilter.active = offerActive
        if (!offerActive) transactionsUseCases.clearOffersFilters()
    }

    fun updateOtherOperationFilter(otherActive: Boolean) {
        transactionsUseCases.operationFilter.othersFilter.active = otherActive
        if (!otherActive) transactionsUseCases.operationFilter.othersFilter.otherOperations = HashSet()
    }

    fun resetOperationFilter() {
        transactionsUseCases.operationFilter.resetFilter()
    }

    fun getOperationFilter() = transactionsUseCases.operationFilter

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}