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

class OffersFilterViewModel(private val transactionsUseCases: TransactionsUseCases) : ViewModel() {

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

    fun updateSellingCurrency(filterSellingCurrency: Boolean, sellingCurrency: String?) {
        if (filterSellingCurrency) transactionsUseCases.operationFilter.offersFilter.active
        transactionsUseCases.operationFilter.offersFilter.filterSellingCurrency = filterSellingCurrency
        transactionsUseCases.operationFilter.offersFilter.sellingCurrency = sellingCurrency
    }

    fun updateBuyingCurrency(filterBuyingCurrency: Boolean, buyingCurrency: String?) {
        if (filterBuyingCurrency) transactionsUseCases.operationFilter.offersFilter.active
        transactionsUseCases.operationFilter.offersFilter.filterBuyingCurrency = filterBuyingCurrency
        transactionsUseCases.operationFilter.offersFilter.buyingCurrency = buyingCurrency
    }

    fun getOfferFilterData() = transactionsUseCases.operationFilter.offersFilter
}