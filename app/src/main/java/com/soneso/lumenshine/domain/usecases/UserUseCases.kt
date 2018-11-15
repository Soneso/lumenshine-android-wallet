package com.soneso.lumenshine.domain.usecases

import com.soneso.lumenshine.domain.data.Country
import com.soneso.lumenshine.domain.data.ErrorCodes
import com.soneso.lumenshine.domain.data.UserProfile
import com.soneso.lumenshine.domain.util.toCharArray
import com.soneso.lumenshine.model.UserRepository
import com.soneso.lumenshine.model.entities.UserSecurity
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.util.Failure
import com.soneso.lumenshine.util.Resource
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.processors.BehaviorProcessor
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager.
 * Created by cristi.paval on 3/22/18.
 */
@Singleton
class UserUseCases
@Inject constructor(private val userRepo: UserRepository) {

    private val passSubject = BehaviorProcessor.create<String>()

    fun registerAccount(foreName: CharSequence, lastName: CharSequence, email: CharSequence, password: CharSequence,
                        country: Country?): Flowable<Resource<Boolean, ServerException>> {

        val userProfile = UserProfile()
        userProfile.forename = foreName.toString()
        userProfile.lastname = lastName.toString()
        userProfile.email = email.toString()
        userProfile.country = country

        val helper = UserSecurityHelper(password.toCharArray())
        return Single
                .create<UserSecurity> {
                    it.onSuccess(helper.generateUserSecurity(userProfile.email))
                }
                .doOnSuccess { passSubject.onNext(password.toString()) }
                .toFlowable()
                .flatMap { userRepo.createUserAccount(userProfile, it) }
    }

    fun confirmTfaRegistration(tfaCode: String) = userRepo.confirmTfaRegistration(tfaCode)

    fun provideSalutations() = userRepo.getSalutations()

    fun provideCountries() = userRepo.getCountries()

    fun login(email: CharSequence, password: CharSequence, tfaCode: CharSequence?): Flowable<Resource<Boolean, ServerException>> {

        passSubject.onNext(password.toString())
        val username = email.toString()
        val tfaFlow = if (tfaCode != null) Flowable.just(tfaCode.toString()) else userRepo.loadTfaCode().toFlowable()
        return tfaFlow.flatMap { tfa ->
            userRepo.loginStep1(username, tfa).flatMap {
                if (it.isSuccessful) {
                    userRepo.getUserData(username).toFlowable().flatMap { userData ->
                        val helper = UserSecurityHelper(password.toCharArray())
                        val publicKeyIndex188 = helper.decipherUserSecurity(userData)
                        if (publicKeyIndex188 == null) {
                            Flowable.just(Failure<Boolean, ServerException>(ServerException(ErrorCodes.LOGIN_WRONG_PASSWORD)))
                        } else {
                            userRepo.loginStep2(username, publicKeyIndex188)
                        }
                    }
                } else {
                    Flowable.just(it)
                }
            }
        }
    }

    fun provideMnemonic(): Single<String> {

        return Single.zip(passSubject
                .doOnNext { Timber.d("Pass: $it") }
                .filter { it.isNotBlank() }.firstOrError(),
                userRepo.getUserData().doOnSuccess { Timber.d("UserData for: ${it.username}") },
                BiFunction<String, UserSecurity, String> { pass, userSecurity ->
                    val helper = UserSecurityHelper(pass.toCharArray())
                    helper.decipherUserSecurity(userSecurity)
                    String(helper.mnemonicChars)
                }
        )
    }

    fun confirmMnemonic() = userRepo.confirmMnemonic()

    fun resendConfirmationMail() = userRepo.resendConfirmationMail()

    fun refreshRegistrationStatus() = userRepo.refreshRegistrationStatus()

    fun provideTfaSecret() = userRepo.loadTfaSecret()

    fun requestPasswordReset(email: String) = userRepo.requestEmailForPasswordReset(email)

    fun requestTfaReset(email: String) = userRepo.requestEmailForTfaReset(email)

    fun provideLastUsername() = userRepo.getLastUsername()

    fun isUserLoggedIn(): Single<Boolean> =
            userRepo.getLastUsername()
                    .flatMap { username ->
                        if (username.isNotBlank()) {
                            userRepo.getRegistrationStatus()
                                    .firstOrError()
                                    .map { it.mailConfirmed && it.tfaConfirmed && it.mnemonicConfirmed }
                        } else {
                            Single.just(false)
                        }
                    }

    fun changeUserPassword(currentPass: CharSequence, newPass: CharSequence): Flowable<Resource<Boolean, ServerException>> {

        return userRepo.getUserData()
                .toFlowable()
                .flatMap {
                    val helper = UserSecurityHelper(currentPass.toCharArray())
                    val us = helper.changePassword(it, newPass.toCharArray())
                    userRepo.changeUserPassword(us)
                }
    }

    fun changeTfaPassword(pass: CharSequence): Flowable<Resource<String, ServerException>> {

        return userRepo.getUserData()
                .toFlowable()
                .flatMap {
                    val helper = UserSecurityHelper(pass.toCharArray())
                    val publicKey188 = helper.decipherUserSecurity(it)
                    if (publicKey188 != null) {
                        userRepo.changeTfaSecret(publicKey188)
                    } else {
                        Flowable.just(Failure<String, ServerException>(ServerException(ErrorCodes.UNKNOWN)))
                    }
                }
    }

    fun confirmTfaSecretChange(tfaCode: CharSequence) = userRepo.confirmTfaSecretChange(tfaCode.toString())

    fun provideRegistrationStatus() = userRepo.getRegistrationStatus()

    fun logout() = userRepo.logout()

    fun setNewSession() {
        passSubject.onNext("")
    }

    companion object {

        const val TAG = "UserUseCases"
    }
}