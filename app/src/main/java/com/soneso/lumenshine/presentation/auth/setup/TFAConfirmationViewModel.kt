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

class TFAConfirmationViewModel(private val userUseCases: UserUseCases) : ViewModel() {

    val liveTfaSecret: LiveData<String> = MutableLiveData()

    val liveTfaConfirmation: LiveData<Resource<RegistrationStatus, LsException>> = MutableLiveData()

    private val compositeDisposable = CompositeDisposable()

    fun fetchTfaSecret() {
        val d = userUseCases.provideTfaSecret()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { it ->
                    liveTfaSecret.putValue(it)
                }
        compositeDisposable.add(d)
    }

    fun confirmTfaRegistration(tfaCode: CharSequence) {

        liveTfaConfirmation.putValue(Resource(Resource.LOADING))
        val d = userUseCases.confirmTfaRegistration(tfaCode.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveTfaConfirmation.putValue(Success(it))
                }, {
                    liveTfaConfirmation.putValue(Failure(it as LsException))
                })
        compositeDisposable.add(d)
    }

}