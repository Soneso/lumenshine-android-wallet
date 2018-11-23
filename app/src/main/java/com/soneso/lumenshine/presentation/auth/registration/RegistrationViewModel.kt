package com.soneso.lumenshine.presentation.auth.registration

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

class RegistrationViewModel(private val useCases: UserUseCases) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val liveRegistration: LiveData<Resource<Unit, LsException>> = MutableLiveData()

    fun register(foreName: CharSequence, lastName: CharSequence, email: CharSequence, password: CharSequence) {

        liveRegistration.putValue(Resource(Resource.LOADING))
        val d = useCases.registerAccount(foreName, lastName, email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveRegistration.putValue(Success(Unit))
                }, {
                    liveRegistration.putValue(Failure(it as LsException))
                })
        compositeDisposable.add(d)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}