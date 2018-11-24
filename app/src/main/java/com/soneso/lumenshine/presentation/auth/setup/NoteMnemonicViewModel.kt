package com.soneso.lumenshine.presentation.auth.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.soneso.lumenshine.domain.usecases.UserUseCases

class NoteMnemonicViewModel(useCases: UserUseCases) : ViewModel() {

    val liveMnemonic: LiveData<String> = MutableLiveData<String>().apply { value = useCases.getMnemonic() }
}