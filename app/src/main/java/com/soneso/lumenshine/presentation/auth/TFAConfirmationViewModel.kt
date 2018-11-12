package com.soneso.lumenshine.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.usecases.UserUseCases
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.presentation.util.putValue
import com.soneso.lumenshine.util.Resource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class TFAConfirmationViewModel(private val userUseCases: UserUseCases) : ViewModel() {

    val liveTfaSecret: LiveData<String> = MutableLiveData()

    val liveTfaConfirmation: LiveData<Resource<Boolean, ServerException>> = MutableLiveData()

    val liveTfaChangeConfirmation: LiveData<Resource<Boolean, ServerException>> = MutableLiveData()

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

        val d = userUseCases.confirmTfaRegistration(tfaCode.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    liveTfaConfirmation.putValue(it)
                }
        compositeDisposable.add(d)
    }

}