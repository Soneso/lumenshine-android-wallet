package com.soneso.lumenshine.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.usecases.UserUseCases
import com.soneso.lumenshine.presentation.util.putValue
import com.soneso.lumenshine.util.Failure
import com.soneso.lumenshine.util.LsException
import com.soneso.lumenshine.util.Resource
import com.soneso.lumenshine.util.Success
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class LostCredentialViewModel(private val userUseCases: UserUseCases) : ViewModel() {

    val liveCredentialResetEmail: LiveData<Resource<Unit, LsException>> = MutableLiveData()

    private val compositeDisposable = CompositeDisposable()

    fun requestPasswordResetEmail(email: CharSequence) {

        liveCredentialResetEmail.putValue(Resource(Resource.LOADING))
        val d = userUseCases.requestPasswordReset(email.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveCredentialResetEmail.putValue(Success(Unit))
                }, { liveCredentialResetEmail.putValue(Failure(it as LsException)) })
        compositeDisposable.add(d)
    }

    fun requestTfaResetEmail(email: CharSequence) {

        liveCredentialResetEmail.putValue(Resource(Resource.LOADING))
        val d = userUseCases.requestTfaReset(email.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveCredentialResetEmail.putValue(Success(Unit))
                }, { liveCredentialResetEmail.putValue(Failure(it as LsException)) })
        compositeDisposable.add(d)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}