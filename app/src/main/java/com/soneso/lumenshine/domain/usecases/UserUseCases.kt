package com.soneso.lumenshine.domain.usecases

import com.google.authenticator.OtpProvider
import com.soneso.lumenshine.domain.util.toCharArray
import com.soneso.lumenshine.model.UserRepository
import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.model.entities.UserSecurity
import com.soneso.lumenshine.presentation.util.decodeBase32
import com.soneso.lumenshine.util.LsException
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
    private var tfaSecret = ""

    fun registerAccount(foreName: CharSequence, lastName: CharSequence, email: CharSequence, password: CharSequence): Completable {

        val forenameString = foreName.toString()
        val lastNameString = lastName.toString()
        val emailString = email.toString()

        val helper = UserSecurityHelper(password.toCharArray())
        return Single
                .create<UserSecurity> {
                    it.onSuccess(helper.generateUserSecurity(emailString))
                    mnemonic = String(helper.mnemonicChars)
                }
                .flatMapCompletable { userRepo.createUserAccount(forenameString, lastNameString, emailString, it) }
    }

    fun confirmTfaRegistration(tfaCode: String) = userRepo.confirmTfaRegistration(tfaCode)

    fun login(password: CharSequence): Single<RegistrationStatus> {
        val tfaCode = OtpProvider.currentTotpCode(tfaSecret.decodeBase32()) ?: ""
        return userRepo.loadUsername()
                .flatMap { login(it, password.toString(), tfaCode) }
    }

    fun login(email: CharSequence, password: CharSequence, tfaCode: CharSequence?): Single<RegistrationStatus> {
        return userRepo.loginStep1(email.toString(), tfaCode?.toString())
                .map {
                    val sep10Challenge = it.sep10Challenge
                    val helper = UserSecurityHelper(password.toCharArray())
                    val keyPair = helper.decipherUserSecurity(it)
                            ?: throw LsException("Wrong password")
                    mnemonic = String(helper.mnemonicChars)
                    Pair(sep10Challenge, keyPair)
                }
                .flatMap { userRepo.loginStep2(it.first, it.second) }
                .flatMap { pair ->
                    val status = pair.second
                    if (status.isSetupCompleted()) {
                        userRepo.loadTfaSecret(pair.first).doOnSuccess { tfaSecret = it }.flatMap { Single.just(status) }
                    } else {
                        Single.just(status)
                    }
                }
    }

    fun getMnemonic() = mnemonic

    fun confirmMnemonic() = userRepo.confirmMnemonic()

    fun resendConfirmationMail() = userRepo.resendConfirmationMail()

    fun refreshRegistrationStatus() = userRepo.loadRegistrationStatus()

    fun provideTfaSecret() = userRepo.loadTfaSecret()

    fun requestPasswordReset(email: String) = userRepo.requestEmailForPasswordReset(email)

    fun requestTfaReset(email: String) = userRepo.requestEmailForTfaReset(email)

    fun provideUsername() = userRepo.loadUsername()

    fun isUserLoggedIn() = tfaSecret.isNotEmpty()

//    fun changeUserPassword(currentPass: CharSequence, newPass: CharSequence): Flowable<Resource<Boolean, ServerException>> {
//
//        return userRepo.getUserData()
//                .toFlowable()
//                .flatMap {
//                    val helper = UserSecurityHelper(currentPass.toCharArray())
//                    val us = helper.changePassword(it, newPass.toCharArray())
//                    userRepo.changeUserPassword(us)
//                }
//    }
//
//    fun changeTfaPassword(pass: CharSequence): Flowable<Resource<String, ServerException>> {
//
//        return userRepo.getUserData()
//                .toFlowable()
//                .flatMap {
//                    val helper = UserSecurityHelper(pass.toCharArray())
//                    val publicKey188 = helper.decipherUserSecurityOld(it)
//                    if (publicKey188 != null) {
//                        userRepo.changeTfaSecret(publicKey188)
//                    } else {
//                        Flowable.just(Failure<String, ServerException>(ServerException(ErrorCodes.UNKNOWN)))
//                    }
//                }
//    }

    fun confirmTfaSecretChange(tfaCode: CharSequence) = userRepo.confirmTfaSecretChange(tfaCode.toString())

    fun logout(): Completable {
        return userRepo.logout()
                .doOnComplete {
                    mnemonic = ""
                    tfaSecret = ""
                }
    }

    companion object {

        const val TAG = "UserUseCases"
    }
}