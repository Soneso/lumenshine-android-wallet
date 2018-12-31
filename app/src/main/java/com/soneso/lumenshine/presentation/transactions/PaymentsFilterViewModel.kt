package com.soneso.lumenshine.presentation.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.data.TransactionsFilter
import com.soneso.lumenshine.domain.usecases.TransactionsUseCases
import com.soneso.lumenshine.model.entities.wallet.WalletEntity
import com.soneso.lumenshine.presentation.util.putValue
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class PaymentsFilterViewModel(private val transactionsUseCases: TransactionsUseCases) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val liveTransactionsFilter: LiveData<TransactionsFilter> = MutableLiveData()
    val liveWalletCurrencies: LiveData<List<String>> = MutableLiveData()

    init {
        val d = transactionsUseCases.observeTransactionsFilter()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    liveTransactionsFilter.putValue(it)
                }
        compositeDisposable.add(d)
    }

    fun loadWalletDetails(wallet: WalletEntity) {
        val d = transactionsUseCases.getWalletDetails(wallet)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    (liveWalletCurrencies as MutableLiveData).value = it.balances.map { details -> details.code }
                }, {})

        compositeDisposable.add(d)
    }


    fun updateReceivedFilter(filterReceived: Boolean, receivedFrom: Int?, receivedTo: Int?) {
        if (filterReceived) transactionsUseCases.operationFilter.paymentsFilter.active = true
        transactionsUseCases.operationFilter.paymentsFilter.filterReceived = filterReceived
        transactionsUseCases.operationFilter.paymentsFilter.receivedFrom = receivedFrom
        transactionsUseCases.operationFilter.paymentsFilter.receivedTo = receivedTo
    }

    fun updateSentFilter(filterSent: Boolean, sentFrom: Int?, sentTo: Int?) {
        if (filterSent) transactionsUseCases.operationFilter.paymentsFilter.active = true
        transactionsUseCases.operationFilter.paymentsFilter.filterSent = filterSent
        transactionsUseCases.operationFilter.paymentsFilter.sentFrom = sentFrom
        transactionsUseCases.operationFilter.paymentsFilter.sentTo = sentTo
    }

    fun updateCurrencyFiler(filterCurrency: Boolean, currency: String?) {
        if (filterCurrency) transactionsUseCases.operationFilter.paymentsFilter.active = true
        transactionsUseCases.operationFilter.paymentsFilter.filterCurrency = filterCurrency
        transactionsUseCases.operationFilter.paymentsFilter.currency = currency
    }

    fun getPaymentFilterData() = transactionsUseCases.operationFilter.paymentsFilter
}
