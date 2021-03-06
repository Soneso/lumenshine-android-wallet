package com.soneso.lumenshine.presentation.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.usecases.UserUseCases
import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.presentation.util.putValue
import com.soneso.lumenshine.util.Failure
import com.soneso.lumenshine.util.LsException
import com.soneso.lumenshine.util.Resource
import com.soneso.lumenshine.util.Success
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class PasswordViewModel(private val useCases: UserUseCases) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val liveLogin: LiveData<Resource<RegistrationStatus, LsException>> = MutableLiveData()

    fun login(password: CharSequence, tfaCode: CharSequence) {

        liveLogin.putValue(Resource(Resource.LOADING))
        val d = useCases.login(password, tfaCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveLogin.putValue(Success(it))
                }, {
                    liveLogin.putValue(Failure(it as LsException))
                })
        compositeDisposable.add(d)
    }

    fun login(password: CharSequence) {

        liveLogin.putValue(Resource(Resource.LOADING))
        val d = useCases.login(password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveLogin.putValue(Success(it))
                }, {
                    liveLogin.putValue(Failure(it as LsException))
                })
        compositeDisposable.add(d)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun login() {
        liveLogin.putValue(Resource(Resource.LOADING))
        val d = useCases.login()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveLogin.putValue(Success(it))
                }, {
                    liveLogin.putValue(Failure(it as LsException))
                })
        compositeDisposable.add(d)
    }
}