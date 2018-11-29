package com.soneso.lumenshine.presentation.general

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.soneso.lumenshine.di.AppComponent
import com.soneso.lumenshine.presentation.MainViewModel
import com.soneso.lumenshine.presentation.SplashViewModel
import com.soneso.lumenshine.presentation.auth.AuthViewModel
import com.soneso.lumenshine.presentation.auth.LostCredentialViewModel
import com.soneso.lumenshine.presentation.auth.TFAConfirmationViewModel
import com.soneso.lumenshine.presentation.home.HomeViewModel
import com.soneso.lumenshine.presentation.settings.SettingsViewModel
import com.soneso.lumenshine.presentation.transactions.*
import com.soneso.lumenshine.presentation.wallets.WalletsViewModel

class LsViewModelFactory(
        private val appComponent: AppComponent
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(WalletsViewModel::class.java) -> WalletsViewModel(appComponent.walletsUseCase) as T
            modelClass.isAssignableFrom(TransactionsViewModel::class.java) -> TransactionsViewModel(appComponent.transactionsUseCase) as T
            modelClass.isAssignableFrom(TransactionsFilterViewModel::class.java) -> TransactionsFilterViewModel(appComponent.transactionsUseCase) as T
            modelClass.isAssignableFrom(PaymentsFilterViewModel::class.java) -> PaymentsFilterViewModel(appComponent.transactionsUseCase) as T
            modelClass.isAssignableFrom(OffersFilterViewModel::class.java) -> OffersFilterViewModel(appComponent.transactionsUseCase) as T
            modelClass.isAssignableFrom(OtherFilterViewModel::class.java) -> OtherFilterViewModel(appComponent.transactionsUseCase) as T
            modelClass.isAssignableFrom(SplashViewModel::class.java) -> SplashViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel() as T
            modelClass.isAssignableFrom(LostCredentialViewModel::class.java) -> LostCredentialViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(TFAConfirmationViewModel::class.java) -> TFAConfirmationViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(appComponent.walletsUseCase) as T
            else -> throw IllegalArgumentException("View Model not found here not found")
        }
    }
}