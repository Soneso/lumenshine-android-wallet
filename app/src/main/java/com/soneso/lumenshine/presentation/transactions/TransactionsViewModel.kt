package com.soneso.lumenshine.presentation.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.usecases.TransactionsUseCases
import com.soneso.lumenshine.model.entities.Wallet
import com.soneso.lumenshine.model.entities.operations.Operation
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.util.Resource
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class TransactionsViewModel(private val transactionsUseCases: TransactionsUseCases) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val liveOperations: LiveData<Resource<List<Operation>, ServerException>> = MutableLiveData()
    val liveWallets: LiveData<Resource<List<Wallet>, ServerException>> = MutableLiveData()

    init {
        compositeDisposable.add(fetchAllWallets())
        //TODO maybe replace with 1 call as subsribe ( a bigger object containing all the parameters)
        compositeDisposable.add(transactionsUseCases.selectedWallet.subscribe { fetchOperations() })
        compositeDisposable.add(transactionsUseCases.selectedDateFrom.subscribe { fetchOperations() })
        compositeDisposable.add(transactionsUseCases.selectedDateTo.subscribe { fetchOperations() })
    }

    fun selectWallet(wallet: Wallet) = transactionsUseCases.setWallet(wallet)

    fun selectDateFrom(dateFrom: Date) = transactionsUseCases.setDateFrom(dateFrom)

    fun selectDateTo(dateTo: Date) = transactionsUseCases.setDateTo(dateTo)

    private fun fetchAllWallets() = transactionsUseCases.getWallets()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                (liveWallets as MutableLiveData).value = it
            }

    private fun fetchOperations() = transactionsUseCases.getOperations()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                (liveOperations as MutableLiveData).value = it
            }

    fun observeWallet(): Flowable<Wallet> = transactionsUseCases.selectedWallet
    fun observeDateFrom(): Flowable<Date> = transactionsUseCases.selectedDateFrom
    fun observeDateTo(): Flowable<Date> = transactionsUseCases.selectedDateTo

    override fun onCleared() {

        compositeDisposable.dispose()
        super.onCleared()
    }
}