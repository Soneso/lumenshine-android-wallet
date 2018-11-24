package com.soneso.lumenshine.presentation.auth.setup

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

class MailConfirmationViewModel(private val useCases: UserUseCases) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val liveUsername: LiveData<String> = MutableLiveData()
    val liveRegistrationStatus: LiveData<Resource<RegistrationStatus, LsException>> = MutableLiveData()
    val liveMailResend: LiveData<Resource<Unit, LsException>> = MutableLiveData()

    init {
        val d = useCases.provideUsername()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { it -> liveUsername.putValue(it) }
        compositeDisposable.add(d)
    }

    fun refreshRegistrationStatus() {

        liveRegistrationStatus.putValue(Resource(Resource.LOADING))
        val d = useCases.refreshRegistrationStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveRegistrationStatus.putValue(Success(it))
                }, {
                    liveRegistrationStatus.putValue(Failure(it as LsException))
                })

        compositeDisposable.add(d)
    }

    fun resendConfirmationMail() {

        liveMailResend.putValue(Resource(Resource.LOADING))
        val d = useCases.resendConfirmationMail()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveMailResend.putValue(Success(Unit))
                }, {
                    liveMailResend.putValue(Failure(it as LsException))
                })
        compositeDisposable.add(d)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}