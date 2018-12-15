package com.soneso.lumenshine.domain.usecases

import com.google.authenticator.OtpProvider
import com.soneso.lumenshine.domain.data.CredentialStatus
import com.soneso.lumenshine.domain.data.ErrorCodes
import com.soneso.lumenshine.domain.util.toCharArray
import com.soneso.lumenshine.model.UserRepository
import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.model.entities.UserSecurity
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.presentation.util.decodeBase32
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager.
 * Created by cristi.paval on 3/22/18.
 */
@Singleton
class UserUseCases
@Inject constructor(private val userRepo: UserRepository) {

    private var mnemonic = ""

    fun registerAccount(foreName: CharSequence, lastName: CharSequence, email: CharSequence, password: CharSequence): Completable {

        val forenameString = foreName.toString()
        val lastNameString = lastName.toString()
        val emailString = email.toString()

        val helper = UserSecurityHelper(password.toCharArray())
        return Single
                .create<UserSecurity> {
                    it.onSuccess(helper.generateUserSecurity())
                    mnemonic = String(helper.mnemonicChars)
                }
                .flatMapCompletable { userRepo.createUserAccount(forenameString, lastNameString, emailString, it) }
    }

    fun confirmTfaRegistration(tfaCode: String) = userRepo.confirmTfaRegistration(tfaCode)

    fun login(): Single<RegistrationStatus> =
            userRepo.loadUserCredentials()
                    .flatMap {
                        val tfaCode = OtpProvider.currentTotpCode(it.tfaSecret.decodeBase32()) ?: ""
                        login(it.username, it.password, tfaCode)
                    }

    fun login(password: CharSequence): Single<RegistrationStatus> =
            userRepo.loadUserCredentials()
                    .flatMap {
                        val tfaCode = OtpProvider.currentTotpCode(it.tfaSecret.decodeBase32()) ?: ""
                        login(it.username, password.toString(), tfaCode)
                    }

    fun loginAndSavePass(password: CharSequence): Completable =
            login(password).flatMapCompletable { userRepo.savePassword(password.toString()) }

    fun login(password: CharSequence, tfaCode: CharSequence): Single<RegistrationStatus> =
            userRepo.loadUserCredentials()
                    .flatMap { login(it.username, password.toString(), tfaCode.toString()) }

    fun login(email: CharSequence, password: CharSequence, tfaCode: CharSequence?): Single<RegistrationStatus> {
        return userRepo.loginStep1(email.toString(), tfaCode?.toString())
                .map {
                    val sep10Challenge = it.sep10Challenge
                    val helper = UserSecurityHelper(password.toCharArray())
                    val keyPair = helper.decipherUserSecurity(it)
                            ?: throw ServerException(ErrorCodes.LOGIN_WRONG_PASSWORD)
                    mnemonic = String(helper.mnemonicChars)
                    Pair(sep10Challenge, keyPair)
                }
                .flatMap { userRepo.loginStep2(it.first, it.second) }
                .flatMap { status ->
                    userRepo.loadUserCredentials()
                            .flatMapCompletable {
                                if (it.password.isNotEmpty()) {
                                    // cristi.paval, 12/5/18 - that means the user has configured biometric login, so we update the password
                                    userRepo.savePassword(password.toString())
                                } else {
                                    Completable.complete()
                                }
                            }
                            .toSingle { status }

                }
    }

    fun getMnemonic() = mnemonic

    fun confirmMnemonic() = userRepo.confirmMnemonic()

    fun resendConfirmationMail() = userRepo.resendConfirmationMail()

    fun refreshRegistrationStatus() = userRepo.loadRegistrationStatus()

    fun provideTfaSecret() = userRepo.loadTfaSecret()

    fun requestPasswordReset(email: String): Completable = userRepo.requestEmailForPasswordReset(email)

    fun requestTfaReset(email: String): Completable = userRepo.requestEmailForTfaReset(email)

    fun provideUsername(): Single<String> = userRepo.loadUsername()

    fun loginCredentialStatus(): Single<CredentialStatus> {
        return userRepo.loadUserCredentials()
                .map {
                    when {
                        it.password.isNotBlank() && it.tfaSecret.isNotBlank() -> CredentialStatus.TFA_AND_PASS
                        it.tfaSecret.isNotBlank() -> CredentialStatus.TFA
                        else -> CredentialStatus.NONE
                    }
                }
    }

    fun changeUserPassword(oldPass: CharSequence, newPass: CharSequence): Completable =
            userRepo.loadUserAuthData()
                    .flatMapCompletable { us ->
                        val helper = UserSecurityHelper(oldPass.toCharArray())
                        val keyPair = helper.decipherUserSecurity(us)
                        if (keyPair == null) {
                            throw ServerException(ErrorCodes.LOGIN_WRONG_PASSWORD)
                        } else {
                            val newUs = helper.changePassword(us, newPass.toCharArray())
                            userRepo.loadAndSignSep10Challenge(keyPair)
                                    .flatMapCompletable {
                                        newUs.sep10Challenge = it
                                        userRepo.changeUserPassword(newUs)
                                                .andThen(userRepo.loadUserCredentials())
                                                .flatMapCompletable { creds ->
                                                    if (creds.password.isNotEmpty()) {
                                                        // cristi.paval, 12/6/18 - if the user has password saved locally, update it
                                                        userRepo.savePassword(newPass.toString())
                                                    } else {
                                                        Completable.complete()
                                                    }
                                                }
                                    }
                        }
                    }

    fun changeTfaPassword(pass: CharSequence): Single<String> =
            userRepo.loadUserAuthData()
                    .flatMap { us ->
                        val helper = UserSecurityHelper(pass.toCharArray())
                        val keyPair = helper.decipherUserSecurity(us)
                        if (keyPair == null) {
                            throw ServerException(ErrorCodes.LOGIN_WRONG_PASSWORD)
                        } else {
                            userRepo.loadAndSignSep10Challenge(keyPair)
                        }
                    }
                    .flatMap { userRepo.changeTfaSecret(it) }

    fun confirmTfaSecretChange(tfaSecret: String, tfaCode: CharSequence) = userRepo.confirmTfaSecretChange(tfaSecret, tfaCode.toString())

    fun logout(): Completable = userRepo.logout().doOnComplete { mnemonic = "" }

    companion object {

        const val TAG = "UserUseCases"
    }
}