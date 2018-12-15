package com.soneso.lumenshine.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.usecases.UserUseCases
import com.soneso.lumenshine.presentation.util.putValue
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * View model.
 * Created by cristi.paval on 3/22/18.
 */
class AuthViewModel(private val userUseCases: UserUseCases) : ViewModel() {

    val liveUsername: LiveData<String> = MutableLiveData()

    val liveLogout: LiveData<Unit> = MutableLiveData()

    private val compositeDisposable = CompositeDisposable()
    val liveFingerprintActive: LiveData<Boolean> = MutableLiveData()

    fun logout() {
        val d = userUseCases.logout()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    liveLogout.putValue(Unit)
                }
        compositeDisposable.add(d)
    }

    fun setFingerprintActive(boolean: Boolean) {
        liveFingerprintActive.putValue(boolean)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}