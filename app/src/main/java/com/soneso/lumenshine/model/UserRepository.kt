package com.soneso.lumenshine.model

import com.google.authenticator.OtpProvider
import com.moandjiezana.toml.Toml
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
import org.stellar.sdk.KeyPair
import org.stellar.sdk.ManageDataOperation
import org.stellar.sdk.Network
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
        private val db: LsDatabase,
        r: Retrofit
) {

    private val userApi = r.create(UserApi::class.java)
    private val userDao = db.userDao()

    fun createUserAccount(forename: String, lastName: String, email: String, userSecurity: UserSecurity): Completable {

        return userApi.registerUser(
                forename,
                lastName,
                email,
                userSecurity.passwordKdfSalt.base64String(),
                userSecurity.encryptedMnemonicMasterKey.base64String(),
                userSecurity.mnemonicMasterKeyEncryptionIv.base64String(),
                userSecurity.encryptedMnemonic.base64String(),
                userSecurity.mnemonicEncryptionIv.base64String(),
                userSecurity.encryptedWordListMasterKey.base64String(),
                userSecurity.wordListMasterKeyEncryptionIv.base64String(),
                userSecurity.encryptedWordList.base64String(),
                userSecurity.wordListEncryptionIv.base64String(),
                userSecurity.publicKeyIndex0
        ).onErrorResumeNext { Single.error(LsException(it)) }
                .doOnSuccess {
                    if (it.isSuccessful) {
                        LsPrefs.jwtToken = it.headers()[LsApi.HEADER_NAME_AUTHORIZATION] ?: return@doOnSuccess
                    } else {
                        throw ServerException(it.errorBody())
                    }
                }.doOnSuccess {
                    val body = it.body()!!
                    LsPrefs.username = userSecurity.username
                    LsPrefs.tfaSecret = body.token2fa ?: ""
                }.ignoreElement()
    }

    fun confirmTfaRegistration(tfaCode: String): Single<RegistrationStatus> {

        return userApi.confirmTfaRegistration(tfaCode)
                .onErrorResumeNext { Single.error(LsException(it)) }
                .doOnSuccess {
                    if (it.isSuccessful) {
                        LsPrefs.jwtToken = it.headers()[LsApi.HEADER_NAME_AUTHORIZATION] ?: return@doOnSuccess
                    } else {
                        throw ServerException(it.errorBody())
                    }
                }.map { it.body()!!.toRegistrationStatus() }
    }

    fun loginStep1(email: String, tfaCode: String? = null): Single<UserSecurity> {

        return userApi.loginStep1(email, tfaCode)
                .onErrorResumeNext { Single.error(LsException(it)) }
                .doOnSuccess {
                    if (it.isSuccessful) {
                        LsPrefs.username = email
                        LsPrefs.jwtToken = it.headers()[LsApi.HEADER_NAME_AUTHORIZATION] ?: return@doOnSuccess
                    } else {
                        throw ServerException(it.errorBody())
                    }
                }
                .map { response ->
                    val dto = response.body()!!
                    UserSecurity(
                            email,
                            dto.publicKeyIndex0,
                            dto.sep10TransactionChallenge,
                            dto.passwordKdfSalt(),
                            dto.encryptedMnemonicMasterKey(),
                            dto.mnemonicMasterKeyEncryptionIv(),
                            dto.encryptedMnemonic(),
                            dto.mnemonicEncryptionIv(),
                            dto.encryptedWordListMasterKey(),
                            dto.wordListMasterKeyEncryptionIv(),
                            dto.encryptedWordList(),
                            dto.wordListEncryptionIv()
                    )
                }
    }

    fun loginStep2(sep10Challenge: String, userKeyPair: KeyPair): Single<RegistrationStatus> =
            Single.create<Transaction> { emitter ->
                // cristi.paval, 11/22/18 - validate sep10 challenge
                val transaction = Transaction.fromEnvelopeXdr(sep10Challenge)
                when {
                    transaction == null -> emitter.onError(LsException("Transaction must not be null!"))
                    transaction.sequenceNumber != 0L -> emitter.onError(LsException("Sequence number is not 0!"))
                    transaction.operations?.size != 1 -> emitter.onError(LsException("Transaction must contain a single operation!"))
                    transaction.operations?.first()?.sourceAccount?.accountId != userKeyPair.accountId -> emitter.onError(LsException("Operation does not belong to this user!"))
                    transaction.operations?.first() !is ManageDataOperation -> emitter.onError(LsException("Operation is not ManageDataOperation!"))
                    transaction.signatures?.size != 1 -> emitter.onError(LsException("Transaction must contain one signature!"))
                }
                emitter.onSuccess(transaction)
            }.flatMap { transaction ->
                // cristi.paval, 11/22/18 - verify server signature
                Network.useTestNetwork()
                val firstSignature = transaction.signatures.first().signature
                val transactionHash = transaction.hash()
                val serverKeyPair = KeyPair.fromAccountId(LsApi.INITIAL_SERVER_KEY)
                val valid = serverKeyPair.verify(transactionHash, firstSignature.signature)
                if (!valid) {
                    // cristi.paval, 11/22/18 - check if server has a new signature
                    userApi.loadServerSigningKey()
                            .onErrorResumeNext { Single.error(ServerException(it)) }
                            .doOnSuccess { response ->
                                if (response.isSuccessful) {
                                    val serverKey = Toml().read(response.body()).getString("SERVER_SIGNING_KEY")
                                    val newServerKeyPair = KeyPair.fromAccountId(serverKey)
                                    val isValidWithNewKey = newServerKeyPair.verify(transactionHash, firstSignature.signature)
                                    if (!isValidWithNewKey) {
                                        throw LsException("Transaction is not signed by server!")
                                    }
                                } else {
                                    throw ServerException(response.errorBody(), Throwable(response.message()))
                                }
                            }.map { transaction }
                } else {
                    Single.just<Transaction>(transaction)
                }
            }.doOnSuccess { transaction ->
                transaction.sign(userKeyPair)
            }.map { transaction ->
                transaction.toEnvelopeXdrBase64()
            }.flatMap { signedSep10Challenge ->
                userApi.loginStep2(signedSep10Challenge)
            }.doOnSuccess {
                if (it.isSuccessful) {
                    LsPrefs.jwtToken = it.headers()[LsApi.HEADER_NAME_AUTHORIZATION] ?: return@doOnSuccess
                } else {
                    throw ServerException(it.errorBody())
                }
            }.map { it.body()?.toRegistrationStatus() }

    fun getUserData(username: String? = null): Single<UserSecurity> {
        val usernameSingle = if (username == null) LsPrefs.loadUsername() else Single.just(username)
        return usernameSingle.flatMap { userDao.getUserDataById(it) }
    }

    fun confirmMnemonic(): Flowable<Resource<Boolean, LsException>> {

        return userApi.confirmMnemonic()
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
                    //                    userDao.saveRegistrationStatus(RegistrationStatus(LsPrefs.username, true, true, true))
                    true
                }, { it })
    }

    fun resendConfirmationMail(): Completable =
            LsPrefs.loadUsername()
                    .flatMap { username ->
                        userApi.resendConfirmationMail(username)
                    }.onErrorResumeNext { Single.error(LsException(it)) }
                    .doOnSuccess {
                        if (!it.isSuccessful) {
                            throw ServerException(it.errorBody())
                        }
                    }
                    .ignoreElement()


    fun loadRegistrationStatus(): Single<RegistrationStatus> {

        return userApi.getRegistrationStatus()
                .onErrorResumeNext { Single.error(LsException(it)) }
                .map {
                    if (it.isSuccessful) {
                        it.body()!!.toRegistrationStatus()
                    } else {
                        throw ServerException(it.errorBody())
                    }
                }
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

    fun loadUsername() = LsPrefs.loadUsername()

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
                    //                    userDao.saveRegistrationStatus(it.toRegistrationStatus(LsPrefs.username))
                    true
                }, { it })
    }

    fun loadTfaCode(): Single<String> = LsPrefs.loadTfaSecret()
            .map {
                OtpProvider.currentTotpCode(it.decodeBase32()) ?: ""
            }

//    fun getRegistrationStatus(): Flowable<RegistrationStatus> {
//
//        return LsPrefs.loadUsername()
//                .toFlowable()
//                .flatMap { userDao.getRegistrationStatus(it) }
//    }

    fun logout(): Completable {
        return Completable.create {
            try {
                db.clearAllTables()
                it.onComplete()
            } catch (e: Exception) {
                it.onError(LsException(e))
            }
        }
    }

    companion object {

        const val TAG = "UserRepository"
    }
}