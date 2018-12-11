package com.soneso.lumenshine.model

import com.moandjiezana.toml.Toml
import com.soneso.lumenshine.domain.util.base64String
import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.model.entities.UserCredentialsEntity
import com.soneso.lumenshine.model.entities.UserSecurity
import com.soneso.lumenshine.model.wrapper.toRegistrationStatus
import com.soneso.lumenshine.networking.api.LsApi
import com.soneso.lumenshine.networking.api.UserApi
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.persistence.LsPrefs
import com.soneso.lumenshine.persistence.room.LsDatabase
import com.soneso.lumenshine.util.LsException
import io.reactivex.Completable
import io.reactivex.Single
import org.stellar.sdk.KeyPair
import org.stellar.sdk.ManageDataOperation
import org.stellar.sdk.Transaction
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Class used to user operations to server.
 * Created by cristi.paval on 3/26/18.
 */
@Singleton
class UserRepository @Inject constructor(
        private val db: LsDatabase,
        r: Retrofit
) {

    private val userApi = r.create(UserApi::class.java)

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
        )
                .onErrorResumeNext { Single.error(LsException(it)) }
                .map {
                    if (it.isSuccessful) {
                        LsPrefs.jwtToken = it.headers()[LsApi.HEADER_NAME_AUTHORIZATION] ?: ""
                        val body = it.body()!!
                        LsPrefs.tfaSecret = body.token2fa ?: ""
                        body
                    } else {
                        throw ServerException(it.errorBody())
                    }
                }.doOnSuccess {
                    LsPrefs.username = email
                    LsPrefs.registrationCompleted = false
                }.ignoreElement()
    }

    fun confirmTfaRegistration(tfaCode: String): Single<RegistrationStatus> {

        return userApi.confirmTfaRegistration(tfaCode)
                .onErrorResumeNext { Single.error(LsException(it)) }
                .map {
                    if (it.isSuccessful) {
                        LsPrefs.jwtToken = it.headers()[LsApi.HEADER_NAME_AUTHORIZATION] ?: ""
                        it.body()!!.toRegistrationStatus()
                    } else {
                        throw ServerException(it.errorBody())
                    }
                }
                .doOnSuccess { LsPrefs.registrationCompleted = it.isSetupCompleted() }
    }

    fun resendConfirmationMail(): Completable =
            userApi.resendConfirmationMail(LsPrefs.username)
                    .onErrorResumeNext { Single.error(LsException(it)) }
                    .doOnSuccess {
                        if (!it.isSuccessful) {
                            throw ServerException(it.errorBody())
                        }
                    }
                    .ignoreElement()


    fun confirmMnemonic(): Completable {

        return userApi.confirmMnemonic()
                .onErrorResumeNext { Single.error(LsException(it)) }
                .doOnSuccess {
                    if (!it.isSuccessful) {
                        throw ServerException(it.errorBody())
                    }
                    LsPrefs.registrationCompleted = true
                }
                .ignoreElement()
    }

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
                .doOnSuccess { LsPrefs.registrationCompleted = it.isSetupCompleted() }
    }

    fun loginStep1(email: String, tfaCode: String?): Single<UserSecurity> {

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
                            dto.publicKeyIndex0,
                            dto.passwordKdfSalt(),
                            dto.encryptedMnemonicMasterKey(),
                            dto.mnemonicMasterKeyEncryptionIv(),
                            dto.encryptedMnemonic(),
                            dto.mnemonicEncryptionIv(),
                            dto.encryptedWordListMasterKey(),
                            dto.wordListMasterKeyEncryptionIv(),
                            dto.encryptedWordList(),
                            dto.wordListEncryptionIv(),
                            dto.sep10TransactionChallenge
                    )
                }
    }

    fun loginStep2(sep10Challenge: String, userKeyPair: KeyPair): Single<RegistrationStatus> =
            signSep10Challenge(sep10Challenge, userKeyPair)
                    .flatMap { signedSep10Challenge ->
                        // cristi.paval, 11/23/18 - send to server
                        userApi.loginStep2(signedSep10Challenge).map {
                            if (it.isSuccessful) {
                                LsPrefs.jwtToken = it.headers()[LsApi.HEADER_NAME_AUTHORIZATION] ?: ""
                                it.body()!!.toRegistrationStatus()
                            } else {
                                throw ServerException(it.errorBody())
                            }
                        }.flatMap { status ->
                            if (status.isSetupCompleted()) {
                                // cristi.paval, 11/28/18 - update tfa secret in local storage
                                LsPrefs.registrationCompleted = true
                                refreshTfaSecret(signedSep10Challenge).doOnSuccess { LsPrefs.tfaSecret = it }.flatMap { Single.just(status) }
                            } else {
                                Single.just(status)
                            }
                        }
                    }

    private fun signSep10Challenge(sep10Challenge: String, userKeyPair: KeyPair): Single<String> =
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
            }        // cristi.paval, 11/23/18 - sign transaction
                    .doOnSuccess { transaction -> transaction.sign(userKeyPair) }
                    // cristi.paval, 11/23/18 - encode transaction
                    .map { transaction -> transaction.toEnvelopeXdrBase64() }

    private fun refreshTfaSecret(signedSep10Challenge: String): Single<String> {

        return userApi.getTfaSecret(signedSep10Challenge)
                .onErrorResumeNext { Single.error(LsException(it)) }
                .map {
                    if (it.isSuccessful) {
                        val secret = it.body()!!.tfaSecret
                        LsPrefs.tfaSecret = secret
                        secret
                    } else {
                        throw ServerException(it.errorBody())
                    }
                }
    }

    fun loadTfaSecret(): Single<String> = Single.just(LsPrefs.tfaSecret)

    fun loadUserCredentials(): Single<UserCredentialsEntity> =
            Single.just(UserCredentialsEntity(LsPrefs.username, LsPrefs.tfaSecret, LsPrefs.userPassword, LsPrefs.registrationCompleted))

    fun requestEmailForPasswordReset(email: String): Completable =
            userApi.requestResetPasswordEmail(email)
                    .onErrorResumeNext { Single.error(LsException(it)) }
                    .doOnSuccess {
                        if (!it.isSuccessful) {
                            throw ServerException(it.errorBody())
                        }
                    }
                    .ignoreElement()

    fun requestEmailForTfaReset(email: String): Completable =
            userApi.requestResetTfaEmail(email)
                    .onErrorResumeNext { Single.error(LsException(it)) }
                    .doOnSuccess {
                        if (!it.isSuccessful) {
                            throw ServerException(it.errorBody())
                        }
                    }
                    .ignoreElement()

    fun loadUsername(): Single<String> = Single.just(LsPrefs.username)

    fun loadUserAuthData(): Single<UserSecurity> =
            userApi.getUserAuthData()
                    .onErrorResumeNext { Single.error(LsException(it)) }
                    .map {
                        if (it.isSuccessful) {
                            val dto = it.body()!!
                            UserSecurity(
                                    dto.publicKeyIndex0,
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
                        } else {
                            throw ServerException(it.errorBody())
                        }
                    }

    fun changeUserPassword(userSecurity: UserSecurity): Completable =
            userApi.changePassword(
                    userSecurity.passwordKdfSalt.base64String(),
                    userSecurity.encryptedMnemonicMasterKey.base64String(),
                    userSecurity.mnemonicMasterKeyEncryptionIv.base64String(),
                    userSecurity.encryptedWordListMasterKey.base64String(),
                    userSecurity.wordListMasterKeyEncryptionIv.base64String(),
                    userSecurity.sep10Challenge
            )
                    .onErrorResumeNext { Single.error(LsException(it)) }
                    .doOnSuccess {
                        if (!it.isSuccessful) {
                            throw ServerException(it.errorBody())
                        }
                    }
                    .ignoreElement()

    fun loadAndSignSep10Challenge(keyPair: KeyPair): Single<String> =
            userApi.getSep10Challenge()
                    .onErrorResumeNext { Single.error(LsException(it)) }
                    .map {
                        if (it.isSuccessful) {
                            it.body()!!.sep10TransactionChallenge
                        } else {
                            throw ServerException(it.errorBody())
                        }
                    }
                    .flatMap { signSep10Challenge(it, keyPair) }

    fun changeTfaSecret(signedSep10Challenge: String): Single<String> {

        return userApi.changeTfaSecret(signedSep10Challenge)
                .onErrorResumeNext { Single.error(LsException(it)) }
                .map {
                    if (it.isSuccessful) {
                        it.body()!!.tfaSecret
                    } else {
                        throw ServerException(it.errorBody())
                    }
                }
    }

    fun confirmTfaSecretChange(tfaSecret: String, tfaCode: String): Completable =
            userApi.confirmTfaSecretChange(tfaCode)
                    .onErrorResumeNext { Single.error(LsException(it)) }
                    .doOnSuccess {
                        if (it.isSuccessful) {
                            LsPrefs.tfaSecret = tfaSecret
                        } else {
                            throw ServerException(it.errorBody())
                        }
                    }
                    .ignoreElement()

    fun logout(): Completable {
        return Completable.create {
            try {
                LsPrefs.clearAllKeys()
                db.clearAllTables()
                it.onComplete()
            } catch (e: Exception) {
                it.onError(LsException(e))
            }
        }
    }

    fun savePassword(password: String): Completable =
            Completable.create {
                LsPrefs.userPassword = password
                it.onComplete()
            }

    companion object {

        const val TAG = "UserRepository"
    }
}