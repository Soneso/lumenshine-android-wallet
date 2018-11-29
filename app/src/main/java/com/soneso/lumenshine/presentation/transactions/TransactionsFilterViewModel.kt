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

class TransactionsFilterViewModel(private val transactionsUseCases: TransactionsUseCases) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val liveWallets: LiveData<Resource<List<WalletEntity>, ServerException>> = MutableLiveData()
    val liveTransactionsFilter: LiveData<TransactionsFilter> = MutableLiveData()

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

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}