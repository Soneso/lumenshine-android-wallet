package com.soneso.lumenshine.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.usecases.UserUseCases

class SplashViewModel(userUseCase: UserUseCases) : ViewModel() {

    val liveIsUserLoggedIn: LiveData<Boolean> = MutableLiveData<Boolean>().apply { value = userUseCase.isUserLoggedIn() }
}