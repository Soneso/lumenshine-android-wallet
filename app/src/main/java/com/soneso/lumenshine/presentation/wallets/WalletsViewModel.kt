package com.soneso.lumenshine.presentation.wallets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.data.WalletCardData
import com.soneso.lumenshine.domain.usecases.WalletsUseCase
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.util.Resource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class WalletsViewModel(private val walletsUseCase: WalletsUseCase) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val liveWallets: LiveData<Resource<WalletCardData, ServerException>> = MutableLiveData()

    init {
        compositeDisposable.add(fetchAllWallets())
    }

    private fun fetchAllWallets() = walletsUseCase.provideAllWallets()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                (liveWallets as MutableLiveData).value = it
            }

    override fun onCleared() {

        compositeDisposable.dispose()
        super.onCleared()
    }
}