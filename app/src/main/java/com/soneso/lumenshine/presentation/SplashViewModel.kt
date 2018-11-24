package com.soneso.lumenshine.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.usecases.UserUseCases
import com.soneso.lumenshine.presentation.util.putValue
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SplashViewModel(userUseCase: UserUseCases) : ViewModel() {

    val liveIsUserLoggedIn: LiveData<Boolean> = MutableLiveData()
    private val compositeDisposable = CompositeDisposable()

    init {
        val d = userUseCase.isUserLoggedIn()
                .subscribeOn(Schedulers.io())
                .delay(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { it ->
                    liveIsUserLoggedIn.putValue(it)
                }
        compositeDisposable.add(d)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}