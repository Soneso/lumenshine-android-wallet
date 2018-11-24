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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class ConfirmMnemonicViewModel(private val useCases: UserUseCases) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val liveMnemonic: LiveData<String> = MutableLiveData<String>().apply { value = useCases.getMnemonic() }
    val liveMnemonicConfirmation: LiveData<Resource<Unit, LsException>> = MutableLiveData()

    fun confirmMnemonic() {

        liveMnemonicConfirmation.putValue(Resource(Resource.LOADING))
        val d = useCases.confirmMnemonic()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveMnemonicConfirmation.putValue(Success(Unit))
                }, {
                    liveMnemonicConfirmation.putValue(Failure(it as LsException))
                })
        compositeDisposable.add(d)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}