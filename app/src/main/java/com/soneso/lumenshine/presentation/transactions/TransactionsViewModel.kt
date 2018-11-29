package com.soneso.lumenshine.presentation.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.data.TransactionsFilter
import com.soneso.lumenshine.domain.usecases.TransactionsUseCases
import com.soneso.lumenshine.model.entities.operations.Operation
import com.soneso.lumenshine.model.entities.wallet.WalletEntity
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.util.Resource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class TransactionsViewModel(private val transactionsUseCases: TransactionsUseCases) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val liveWallet: LiveData<Resource<WalletEntity, ServerException>> = MutableLiveData()
    val liveFilters: LiveData<TransactionsFilter> = MutableLiveData()
    val liveOperations: LiveData<Resource<List<Operation>, ServerException>> = MutableLiveData()

    init {
        compositeDisposable.add(fetchWallet())
        compositeDisposable.add(fetchOperations())
        observeFilters()
    }

    fun selectPrimaryWallet(wallet: WalletEntity) = transactionsUseCases.setPrimaryWallet(wallet)

    private fun fetchWallet() = transactionsUseCases.getWallet()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { (liveWallet as MutableLiveData).value = it }

    private fun fetchOperations() = transactionsUseCases.getOperations()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                (liveOperations as MutableLiveData).value = it
            }

    private fun observeFilters() = transactionsUseCases.transactionsFilter
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { (liveFilters as MutableLiveData).value = it }

    override fun onCleared() {

        compositeDisposable.dispose()
        super.onCleared()
    }
}