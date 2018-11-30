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

class SplashViewModel(useCase: UserUseCases) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val liveIsUserLoggedIn: LiveData<Boolean> = MutableLiveData<Boolean>()

    init {
        val d = useCase.isUserLoggedIn()
                .delay(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { it -> liveIsUserLoggedIn.putValue(it) }
        compositeDisposable.add(d)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}