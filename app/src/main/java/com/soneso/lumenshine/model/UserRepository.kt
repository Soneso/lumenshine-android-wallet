package com.soneso.lumenshine.model

import com.soneso.lumenshine.domain.data.Country
import com.soneso.lumenshine.domain.data.UserProfile
import com.soneso.lumenshine.domain.util.base64String
import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.model.entities.UserSecurity
import com.soneso.lumenshine.model.wrapper.toRegistrationStatus
import com.soneso.lumenshine.networking.NetworkStateObserver
import com.soneso.lumenshine.networking.api.SgApi
import com.soneso.lumenshine.networking.api.UserApi
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.persistence.SgPrefs
import com.soneso.lumenshine.persistence.room.LsDatabase
import com.soneso.lumenshine.util.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * Class used to user operations to server.
 * Created by cristi.paval on 3/26/18.
 */
class UserRepository @Inject constructor(
        private val networkStateObserver: NetworkStateObserver,
        db: LsDatabase,
        r: Retrofit
) {

    private val userApi = r.create(UserApi::class.java)
    private val userDao = db.userDao()

    fun createUserAccount(userProfile: UserProfile, userSecurity: UserSecurity): Flowable<Resource<Boolean, ServerException>> {

        return userApi.registerUser(
                userProfile.email,
                userSecurity.passwordKdfSalt.base64String(),
                userSecurity.encryptedMnemonicMasterKey.base64String(),
                userSecurity.mnemonicMasterKeyEncryptionIv.base64String(),
                userSecurity.encryptedMnemonic.base64String(),
                userSecurity.mnemonicEncryptionIv.base64String(),
                userSecurity.encryptedWordListMasterKey.base64String(),
                userSecurity.wordListMasterKeyEncryptionIv.base64String(),
                userSecurity.encryptedWordList.base64String(),
                userSecurity.wordListEncryptionIv.base64String(),
                userSecurity.publicKeyIndex0,
                userSecurity.publicKeyIndex188,
                userProfile.country?.code
        )
                .doOnSuccess {
                    if (it.isSuccessful) {
                        SgPrefs.jwtToken = it.headers()[SgApi.HEADER_NAME_AUTHORIZATION] ?: return@doOnSuccess
                    }
                }
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    SgPrefs.tfaSecret = it.token2fa
                    userDao.saveRegistrationStatus(RegistrationStatus(userProfile.email, false, false, false))
                    true
                }, { it })
    }

    fun confirmTfaRegistration(tfaCode: String): Flowable<Resource<Boolean, ServerException>> {

        return userApi.confirmTfaRegistration(tfaCode)
                .doOnSuccess {
                    if (it.isSuccessful) {
                        SgPrefs.jwtToken = it.headers()[SgApi.HEADER_NAME_AUTHORIZATION] ?: return@doOnSuccess
                    }
                }
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    userDao.saveRegistrationStatus(it.toRegistrationStatus(SgPrefs.username))
                    true
                }, { it })
    }

    fun getSalutations(): Flowable<Resource<List<String>, LsException>> {

        return userApi.getSalutationList()
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    it.salutations
                }, { it })
    }

    fun getCountries(): Flowable<Resource<List<Country>, LsException>> {

        return userApi.getCountryList()
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({ it.countries }, { it })
    }

    fun loginStep1(email: String, tfaCode: String? = null): Flowable<Resource<Boolean, ServerException>> {

        return userApi.loginStep1(email, tfaCode)
                .doOnSuccess {
                    if (it.isSuccessful) {
                        SgPrefs.jwtToken = it.headers()[SgApi.HEADER_NAME_AUTHORIZATION] ?: return@doOnSuccess
                    }
                }
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    val us = UserSecurity(
                            email,
                            it.publicKeyIndex0,
                            "",
                            it.passwordKdfSalt(),
                            it.encryptedMnemonicMasterKey(),
                            it.mnemonicMasterKeyEncryptionIv(),
                            it.encryptedMnemonic(),
                            it.mnemonicEncryptionIv(),
                            it.encryptedWordListMasterKey(),
                            it.wordListMasterKeyEncryptionIv(),
                            it.encryptedWordList(),
                            it.wordListEncryptionIv()
                    )
                    userDao.saveUserData(us)
                    true
                }, { it })
    }

    fun loginStep2(username: String, publicKey188: String): Flowable<Resource<Boolean, ServerException>> {

        return userApi.loginStep2(publicKey188)
                .doOnSuccess {
                    if (it.isSuccessful) {
                        SgPrefs.jwtToken = it.headers()[SgApi.HEADER_NAME_AUTHORIZATION] ?: return@doOnSuccess
                    }
                }
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    SgPrefs.username = username
                    SgPrefs.tfaSecret = it.tfaSecret
                    userDao.saveRegistrationStatus(it.toRegistrationStatus(username))
                    it.tfaConfirmed && it.emailConfirmed && it.mnemonicConfirmed
                }, { it })
                .flatMap {
                    return@flatMap if (it.isSuccessful && it.success()) {
                        refreshTfaSecret(publicKey188)
                    } else {
                        Flowable.just(it)
                    }
                }
    }

    fun getUserData(username: String = SgPrefs.username) = userDao.getUserDataById(username)

    fun confirmMnemonic(): Flowable<Resource<Boolean, LsException>> {

        return userApi.confirmMnemonic()
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    userDao.saveRegistrationStatus(RegistrationStatus(SgPrefs.username, true, true, true))
                    true
                }, { it })
    }

    fun resendConfirmationMail(): Flowable<Resource<Boolean, ServerException>> =
            SgPrefs.observeUsername()
                    .flatMap { username ->
                        userApi.resendConfirmationMail(username)
                                .asHttpResourceLoader(networkStateObserver)
                                .mapResource({ true }, { it })
                    }


    fun refreshRegistrationStatus(): Flowable<Resource<Boolean, ServerException>> {

        return userApi.getRegistrationStatus()
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    userDao.saveRegistrationStatus(it.toRegistrationStatus(SgPrefs.username))
                    true
                }, { it })
    }

    fun loadTfaSecret(): Flowable<Resource<String, ServerException>> {

        return Flowable.create({ emitter ->
            emitter.onNext(Success(SgPrefs.tfaSecret))
            emitter.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    fun requestEmailForPasswordReset(email: String): Flowable<Resource<Boolean, LsException>> {

        return userApi.requestResetPasswordEmail(email)
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({ true }, { it })
    }

    fun requestEmailForTfaReset(email: String): Flowable<Resource<Boolean, LsException>> {

        return userApi.requestResetTfaEmail(email)
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({ true }, { it })
    }

    private fun refreshTfaSecret(publicKey188: String): Flowable<Resource<Boolean, ServerException>> {

        return userApi.getTfaSecret(publicKey188)
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    SgPrefs.tfaSecret = it.tfaSecret
                    true
                }, { it })
    }

    fun getLastUsername(): Single<String> {

        return Single.create<String> {
            it.onSuccess(SgPrefs.username)
        }
    }

    fun changeUserPassword(userSecurity: UserSecurity): Flowable<Resource<Boolean, ServerException>> {

        return userApi.changePassword(
                userSecurity.passwordKdfSalt.base64String(),
                userSecurity.encryptedMnemonicMasterKey.base64String(),
                userSecurity.mnemonicMasterKeyEncryptionIv.base64String(),
                userSecurity.encryptedWordListMasterKey.base64String(),
                userSecurity.wordListMasterKeyEncryptionIv.base64String(),
                userSecurity.publicKeyIndex188
        )
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({ true }, { it })
    }

    fun changeTfaSecret(publicKey188: String): Flowable<Resource<String, ServerException>> {

        return userApi.changeTfaSecret(publicKey188)
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    SgPrefs.tfaSecret = it.tfaSecret
                    it.tfaSecret
                }, { it })
    }

    fun confirmTfaSecretChange(tfaCode: String): Flowable<Resource<Boolean, ServerException>> {

        return userApi.confirmTfaSecretChange(tfaCode)
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    userDao.saveRegistrationStatus(it.toRegistrationStatus(SgPrefs.username))
                    true
                }, { it })
    }

    fun getRegistrationStatus(): Flowable<RegistrationStatus> {

        return SgPrefs.observeUsername().filter { it.isNotBlank() }
                .flatMap { userDao.getRegistrationStatus(it) }
    }

    fun logout(): Completable {
        return Completable.create {
            val username = SgPrefs.username
            SgPrefs.username = ""
            userDao.removeRegistrationStatus(username)
            it.onComplete()
        }
    }

    companion object {

        const val TAG = "UserRepository"
    }
}