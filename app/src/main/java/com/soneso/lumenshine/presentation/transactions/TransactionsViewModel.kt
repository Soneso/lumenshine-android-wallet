package com.soneso.lumenshine.presentation.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.usecases.TransactionsUseCases
import com.soneso.lumenshine.model.entities.operations.Operation
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.util.Resource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class TransactionsViewModel(private val transactionsUseCases: TransactionsUseCases) : ViewModel() {

    private val compoDisposable = CompositeDisposable()

    val liveOperations: LiveData<Resource<List<Operation>, ServerException>> = MutableLiveData()

    init {
        compoDisposable.add(fetchOperations())
    }

    private fun fetchOperations() = transactionsUseCases.getOperations()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                (liveOperations as MutableLiveData).value = it
            }

    override fun onCleared() {

        compoDisposable.dispose()
        super.onCleared()
    }
}