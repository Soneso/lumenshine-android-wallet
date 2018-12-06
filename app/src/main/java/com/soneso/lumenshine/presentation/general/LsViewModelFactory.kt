package com.soneso.lumenshine.presentation.general

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.soneso.lumenshine.di.AppComponent
import com.soneso.lumenshine.presentation.SplashViewModel
import com.soneso.lumenshine.presentation.auth.AuthViewModel
import com.soneso.lumenshine.presentation.auth.login.FingerprintViewModel
import com.soneso.lumenshine.presentation.auth.login.LoginViewModel
import com.soneso.lumenshine.presentation.auth.login.PasswordViewModel
import com.soneso.lumenshine.presentation.auth.more.LostCredentialViewModel
import com.soneso.lumenshine.presentation.auth.registration.RegistrationViewModel
import com.soneso.lumenshine.presentation.auth.setup.*
import com.soneso.lumenshine.presentation.home.HomeViewModel
import com.soneso.lumenshine.presentation.settings.ChangePassViewModel
import com.soneso.lumenshine.presentation.settings.SettingsViewModel
import com.soneso.lumenshine.presentation.wallets.WalletsViewModel

class LsViewModelFactory(
        private val appComponent: AppComponent
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(RegistrationViewModel::class.java) -> RegistrationViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(TFAConfirmationViewModel::class.java) -> TFAConfirmationViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(MailConfirmationViewModel::class.java) -> MailConfirmationViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(NoteMnemonicViewModel::class.java) -> NoteMnemonicViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(ConfirmMnemonicViewModel::class.java) -> ConfirmMnemonicViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(LostCredentialViewModel::class.java) -> LostCredentialViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(PasswordViewModel::class.java) -> PasswordViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(FingerprintViewModel::class.java) -> FingerprintViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(FingerprintSetupViewModel::class.java) -> FingerprintSetupViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(ChangePassViewModel::class.java) -> ChangePassViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(WalletsViewModel::class.java) -> WalletsViewModel(appComponent.walletsUseCase) as T
            modelClass.isAssignableFrom(SplashViewModel::class.java) -> SplashViewModel(appComponent.userUseCases) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(appComponent.walletsUseCase) as T
            else -> throw IllegalArgumentException("View Model not found here not found")
        }
    }
}