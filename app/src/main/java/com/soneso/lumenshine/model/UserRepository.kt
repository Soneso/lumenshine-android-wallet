package com.soneso.lumenshine.model

import com.google.authenticator.OtpProvider
import com.moandjiezana.toml.Toml
import com.soneso.lumenshine.domain.data.Country
import com.soneso.lumenshine.domain.data.UserProfile
import com.soneso.lumenshine.domain.util.base64String
import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.model.entities.UserSecurity
import com.soneso.lumenshine.model.wrapper.toRegistrationStatus
import com.soneso.lumenshine.networking.NetworkStateObserver
import com.soneso.lumenshine.networking.api.LsApi
import com.soneso.lumenshine.networking.api.UserApi
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.persistence.LsPrefs
import com.soneso.lumenshine.persistence.room.LsDatabase
import com.soneso.lumenshine.presentation.util.decodeBase32
import com.soneso.lumenshine.util.LsException
import com.soneso.lumenshine.util.Resource
import com.soneso.lumenshine.util.asHttpResourceLoader
import com.soneso.lumenshine.util.mapResource
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.stellar.sdk.KeyPair
import org.stellar.sdk.ManageDataOperation
import org.stellar.sdk.Transaction
import retrofit2.Retrofit
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Class used to user operations to server.
 * Created by cristi.paval on 3/26/18.
 */
@Singleton
class UserRepository @Inject constructor(
        private val networkStateObserver: NetworkStateObserver,
        db: LsDatabase,
        r: Retrofit
) {

    private val userApi = r.create(UserApi::class.java)
    private val userDao = db.userDao()

    fun createUserAccount(userProfile: UserProfile, userSecurity: UserSecurity): Flowable<Resource<Boolean, ServerException>> {

        return userApi.registerUser(
                userProfile.forename,
                userProfile.lastname,
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
                KeyPair.random().accountId,
                userProfile.country?.code
        )
                .doOnSuccess {
                    if (it.isSuccessful) {
                        LsPrefs.jwtToken = it.headers()[LsApi.HEADER_NAME_AUTHORIZATION] ?: return@doOnSuccess
                    }
                    LsPrefs.username = userSecurity.username
                    userDao.saveUserData(userSecurity)
                }
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    LsPrefs.tfaSecret = it.token2fa
                    userDao.saveRegistrationStatus(RegistrationStatus(userProfile.email, false, false, false))
                    true
                }, { it })
    }

    fun confirmTfaRegistration(tfaCode: String): Flowable<Resource<Boolean, ServerException>> {

        return userApi.confirmTfaRegistration(tfaCode)
                .doOnSuccess {
                    if (it.isSuccessful) {
                        LsPrefs.jwtToken = it.headers()[LsApi.HEADER_NAME_AUTHORIZATION] ?: return@doOnSuccess
                    }
                }
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    userDao.saveRegistrationStatus(it.toRegistrationStatus(LsPrefs.username))
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
                        LsPrefs.username = email
                        LsPrefs.jwtToken = it.headers()[LsApi.HEADER_NAME_AUTHORIZATION] ?: return@doOnSuccess
                    }
                }
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    val us = UserSecurity(
                            email,
                            it.publicKeyIndex0,
                            it.sep10TransactionChallenge,
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

    // TODO: fix this to use challenge + user-keypair, add specific error handling
    fun loginStep2(username: String, sep10Challenge: String, userKeyPair: KeyPair): Flowable<Resource<Boolean, ServerException>> {

        var signedSep10Challenge = signSEP10ChallengeIfValid(sep10Challenge, userKeyPair)

        // TODO: error handling

        // patch:
        if (signedSep10Challenge == null) {
            signedSep10Challenge = ""
        }

        return userApi.loginStep2(signedSep10Challenge)
                .doOnSuccess {
                    if (it.isSuccessful) {
                        LsPrefs.jwtToken = it.headers()[LsApi.HEADER_NAME_AUTHORIZATION] ?: return@doOnSuccess
                    }
                }
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    userDao.saveRegistrationStatus(it.toRegistrationStatus(username))
                    it.tfaConfirmed && it.emailConfirmed && it.mnemonicConfirmed
                }, { it })
                .flatMap {
                    return@flatMap if (it.isSuccessful && it.success()) {
                        // /TODO: is this needed?
                        refreshTfaSecret(signedSep10Challenge)
                    } else {
                        Flowable.just(it)
                    }
                }

    }

    // TODO: specific error handling
    fun signSEP10ChallengeIfValid(base64EnvelopeXDR: String, userKeyPair: KeyPair): String? {

        val transaction = validateSEP10Envelope(base64EnvelopeXDR, userKeyPair.accountId)
        if (transaction == null || transaction.sequenceNumber != 0L) {
            return null
        }

        transaction.sign(userKeyPair)

        return transaction.toEnvelopeXdrBase64()
    }

    // TODO: specific error handling
    fun validateSEP10Envelope(base64EnvelopeXDR: String, userAccountId: String): Transaction? {

        val transaction = Transaction.fromEnvelopeXdr(base64EnvelopeXDR)

        // sequence number must be 0
        when {
            transaction?.sequenceNumber != 0L -> return null
            transaction.operations?.size != 1 -> return null
            transaction.operations?.first()?.sourceAccount?.accountId != userAccountId -> return null
            transaction.operations?.first() !is ManageDataOperation -> return null
        }

        transaction.signatures?.let { signatures ->
            if (signatures.size != 1) {
                return null
            }
            val first = signatures.first()

            val serverSigningKey = "GCP4BR7GWG664577XMLX2BRUPSHKHTAXQ4I4HZORLMQNILNNVMSFWVUV"

            val transactionHash = transaction.hash()
            val serverKeyPair = KeyPair.fromAccountId(serverSigningKey)

            val isValidSignature = serverKeyPair.verify(transactionHash, first.signature.signature)
            if (isValidSignature) {
                return transaction
            } else {
                val reloadedServerSigningKey = loadServerSigningKey()

                if (reloadedServerSigningKey == null || reloadedServerSigningKey.equals(serverSigningKey)) {
                    return null
                }
                val newServerKeyPair = KeyPair.fromAccountId(reloadedServerSigningKey)
                val isValidSignature = newServerKeyPair.verify(transactionHash, first.signature.signature)
                if (isValidSignature) {
                    return transaction
                }
            }

        } ?: run {
            null
        }
        return null
    }

    fun loadServerSigningKey(): String? {

        val uriBuilder = StringBuilder()
        uriBuilder.append("https://demo.lumenshine.com/.well-known/stellar.toml")
        val stellarTomlUri = HttpUrl.parse(uriBuilder.toString())
        val httpClient = OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();

        val request = Request.Builder().get().url(stellarTomlUri).build()
        var response: Response? = null
        try {
            response = httpClient.newCall(request).execute()

            if (response!!.code() >= 300) {
                return null
            }

            val stellarToml = Toml().read(response?.body()?.string())

            return stellarToml.getString("SERVER_SIGNING_KEY")

        } catch (e: Exception) {
            return null
        } finally {
            if (response != null) {
                response!!.close()
            }
        }
    }

    fun getUserData(username: String? = null): Single<UserSecurity> {
        val usernameSingle = if (username == null) LsPrefs.loadUsername() else Single.just(username)
        return usernameSingle.flatMap { userDao.getUserDataById(it) }
    }

    fun confirmMnemonic(): Flowable<Resource<Boolean, LsException>> {

        return userApi.confirmMnemonic()
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    userDao.saveRegistrationStatus(RegistrationStatus(LsPrefs.username, true, true, true))
                    true
                }, { it })
    }

    fun resendConfirmationMail(): Flowable<Resource<Boolean, ServerException>> =
            LsPrefs.loadUsername()
                    .toFlowable()
                    .flatMap { username ->
                        userApi.resendConfirmationMail(username)
                                .asHttpResourceLoader(networkStateObserver)
                                .mapResource({ true }, { it })
                    }


    fun refreshRegistrationStatus(): Flowable<Resource<RegistrationStatus?, ServerException>> {

        return userApi.getRegistrationStatus()
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    userDao.saveRegistrationStatus(it.toRegistrationStatus(LsPrefs.username))
                    it.toRegistrationStatus(LsPrefs.username)
                }, { it })
    }

    fun loadTfaSecret(): Single<String> = LsPrefs.loadTfaSecret()

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

    private fun refreshTfaSecret(signedSep10Challenge: String): Flowable<Resource<Boolean, ServerException>> {

        return userApi.getTfaSecret(signedSep10Challenge)
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    Timber.d("Tfa secret refreshed.")
                    LsPrefs.tfaSecret = it.tfaSecret
                    true
                }, { it })
    }

    fun getLastUsername(): Single<String> {

        return Single.create<String> {
            it.onSuccess(LsPrefs.username)
        }
    }

    fun changeUserPassword(userSecurity: UserSecurity): Flowable<Resource<Boolean, ServerException>> {

        return userApi.changePassword(
                userSecurity.passwordKdfSalt.base64String(),
                userSecurity.encryptedMnemonicMasterKey.base64String(),
                userSecurity.mnemonicMasterKeyEncryptionIv.base64String(),
                userSecurity.encryptedWordListMasterKey.base64String(),
                userSecurity.wordListMasterKeyEncryptionIv.base64String(),
                "" // TODO - change to sep10
                //userSecurity.publicKeyIndex188
        )
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({ true }, { it })
    }

    fun changeTfaSecret(publicKey188: String): Flowable<Resource<String, ServerException>> {

        return userApi.changeTfaSecret(publicKey188)
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    LsPrefs.tfaSecret = it.tfaSecret
                    it.tfaSecret
                }, { it })
    }

    fun confirmTfaSecretChange(tfaCode: String): Flowable<Resource<Boolean, ServerException>> {

        return userApi.confirmTfaSecretChange(tfaCode)
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    userDao.saveRegistrationStatus(it.toRegistrationStatus(LsPrefs.username))
                    true
                }, { it })
    }

    fun loadTfaCode(): Single<String> = LsPrefs.loadTfaSecret()
            .map {
                OtpProvider.currentTotpCode(it.decodeBase32()) ?: ""
            }

    fun getRegistrationStatus(): Flowable<RegistrationStatus> {

        return LsPrefs.loadUsername()
                .toFlowable()
                .flatMap { userDao.getRegistrationStatus(it) }
    }

    fun logout(): Completable {
        return Completable.create {
            val username = LsPrefs.username
            LsPrefs.username = ""
            userDao.removeRegistrationStatus(username)
            it.onComplete()
        }
    }

    companion object {

        const val TAG = "UserRepository"
    }
}