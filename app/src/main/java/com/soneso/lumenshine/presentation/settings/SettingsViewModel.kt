package com.soneso.lumenshine.presentation.settings

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

class SettingsViewModel(private val userUseCases: UserUseCases) : ViewModel() {

    val livePasswordChange: LiveData<Resource<Boolean, ServerException>> = MutableLiveData()

    val liveTfaSecret: LiveData<Resource<String, ServerException>> = MutableLiveData()

    val liveTfaChangeConfirmation: LiveData<Resource<Boolean, ServerException>> = MutableLiveData()

    private val compositeDisposable = CompositeDisposable()

    fun changePassword(oldPass: CharSequence, newPass: CharSequence) {

//        userUseCases.changeUserPassword(oldPass, newPass)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe {
//                    livePasswordChange.putValue(it)
//                }
    }

    fun changeTfaSecret(pass: CharSequence) {

//        val d = userUseCases.changeTfaPassword(pass)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe {
//                    liveTfaSecret.putValue(it)
//                }
//        compositeDisposable.add(d)
    }

    fun confirmTfaSecretChange(tfaCode: CharSequence) {

        val d = userUseCases.confirmTfaSecretChange(tfaCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    liveTfaChangeConfirmation.putValue(it)
                }
        compositeDisposable.add(d)
    }

    override fun onCleared() {

        compositeDisposable.dispose()
        super.onCleared()
    }
}