package com.soneso.lumenshine.presentation.auth.login

import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.usecases.UserUseCases
import io.reactivex.disposables.CompositeDisposable

class FingerprintViewModel(private val useCase: UserUseCases) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()


    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}