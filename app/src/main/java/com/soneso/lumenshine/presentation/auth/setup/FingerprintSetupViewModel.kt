package com.soneso.lumenshine.presentation.auth.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.usecases.UserUseCases
import com.soneso.lumenshine.presentation.util.putValue
import com.soneso.lumenshine.util.Failure
import com.soneso.lumenshine.util.LsException
import com.soneso.lumenshine.util.Resource
import com.soneso.lumenshine.util.Success
import io.reactivex.disposables.CompositeDisposable

class FingerprintSetupViewModel(private val useCase: UserUseCases) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val liveLogin: LiveData<Resource<Unit, LsException>> = MutableLiveData()

    fun loginAndSetFingerPrint(password: CharSequence) {
        liveLogin.putValue(Resource(Resource.LOADING))
        val d = useCase.login(password)
                .flatMapCompletable { useCase.savePassword(password) }
                .subscribe({
                    liveLogin.putValue(Success(Unit))
                }, {
                    liveLogin.putValue(Failure(it as LsException))
                })
        compositeDisposable.add(d)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}