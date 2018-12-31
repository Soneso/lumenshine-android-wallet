package com.soneso.lumenshine.presentation.settings

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

class ChangeTfaViewModel(private val useCase: UserUseCases) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val liveTfaSecret: LiveData<Resource<String, LsException>> = MutableLiveData()
    val liveTfaConfirmation: LiveData<Resource<Unit, LsException>> = MutableLiveData()

    fun changeTfa(pass: CharSequence) {
        liveTfaSecret.putValue(Resource(Resource.LOADING))
        val d = useCase.changeTfaPassword(pass)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { liveTfaSecret.putValue(Success(it)) },
                        { liveTfaSecret.putValue(Failure(it as LsException)) }
                )
        compositeDisposable.add(d)
    }

    fun confirmTfa(tfaSecret: String, tfaCode: CharSequence) {
        liveTfaConfirmation.putValue(Resource(Resource.LOADING))
        val d = useCase.confirmTfaSecretChange(tfaSecret, tfaCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { liveTfaConfirmation.putValue(Success(Unit)) },
                        { liveTfaConfirmation.putValue(Failure(it as LsException)) }
                )
        compositeDisposable.add(d)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}