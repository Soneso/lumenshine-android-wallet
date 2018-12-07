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

class ChangePassViewModel(private val useCase: UserUseCases) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val livePassChange: LiveData<Resource<Unit, LsException>> = MutableLiveData()

    fun changePassword(oldPass: CharSequence, newPass: CharSequence) {
        livePassChange.putValue(Resource(Resource.LOADING))
        val d = useCase.changeUserPassword(oldPass, newPass)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    livePassChange.putValue(Success(Unit))
                }, {
                    livePassChange.putValue(Failure(it as LsException))
                })
        compositeDisposable.add(d)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}