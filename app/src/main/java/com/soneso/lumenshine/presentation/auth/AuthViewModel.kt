package com.soneso.lumenshine.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.usecases.UserUseCases
import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.presentation.util.putValue
import com.soneso.lumenshine.util.Failure
import com.soneso.lumenshine.util.LsException
import com.soneso.lumenshine.util.Resource
import com.soneso.lumenshine.util.Success
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * View model.
 * Created by cristi.paval on 3/22/18.
 */
class AuthViewModel(private val userUseCases: UserUseCases) : ViewModel() {

    val liveLastUsername: LiveData<String> = MutableLiveData()

    val liveTfaChangeConfirmation: LiveData<Resource<Boolean, ServerException>> = MutableLiveData()

    val liveMnemonicConfirmation: LiveData<Resource<Boolean, LsException>> = MutableLiveData()

    val liveMnemonic: LiveData<String> = MutableLiveData()

    val liveRegistrationStatus: LiveData<RegistrationStatus> = MutableLiveData()

    val liveLogin: LiveData<Resource<RegistrationStatus, LsException>> = MutableLiveData()

    val liveLogout: LiveData<Unit> = MutableLiveData()

    var isFingerprintFlow = false

    private val compositeDisposable = CompositeDisposable()

    init {
        initRegistrationStatus()
    }

    fun initRegistrationStatus() {

//        val d = userUseCases.provideRegistrationStatus()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe { it ->
//                    Timber.d("Registration status just published.")
//                    liveRegistrationStatus.putValue(it)
//                }
//        compositeDisposable.add(d)
    }

    fun login(email: CharSequence, password: CharSequence, tfa: CharSequence? = null) {

        liveLogin.putValue(Resource(Resource.LOADING))
        val d = userUseCases.login(email, password, tfa)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveRegistrationStatus.putValue(it)
                    liveLogin.putValue(Success(it))
                }, {
                    liveLogin.putValue(Failure(it as LsException))
                })
        compositeDisposable.add(d)
    }

    fun confirmMnemonic() {

        val d = userUseCases.confirmMnemonic()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    liveMnemonicConfirmation.putValue(it)
                }
        compositeDisposable.add(d)
    }

    fun confirmTfaSecretChange(tfaCode: CharSequence) {

        val d = userUseCases.confirmTfaSecretChange(tfaCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    liveTfaChangeConfirmation.putValue(it)
                }
        compositeDisposable.add(d)
    }

    fun logout() {
        val d = userUseCases.logout()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    liveLogout.putValue(Unit)
                }
        compositeDisposable.add(d)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}