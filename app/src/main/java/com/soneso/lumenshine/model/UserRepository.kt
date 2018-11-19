package com.soneso.lumenshine.model

import com.google.authenticator.OtpProvider
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
import io.reactivex.subjects.BehaviorSubject
import org.stellar.sdk.KeyPair
import org.stellar.sdk.xdr.*
import retrofit2.Retrofit
import timber.log.Timber
import java.io.ByteArrayInputStream
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
    private val passSubject = BehaviorSubject.create<String>()

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
                userSecurity.publicKeyIndex188,
                userProfile.country?.code
        )
                .doOnSuccess {
                    if (it.isSuccessful) {
                        LsPrefs.jwtToken = it.headers()[SgApi.HEADER_NAME_AUTHORIZATION] ?: return@doOnSuccess
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

    fun setPassword(pass: String) {
        passSubject.onNext(pass)
    }

    fun getPassword() = passSubject.firstOrError()

    fun confirmTfaRegistration(tfaCode: String): Flowable<Resource<Boolean, ServerException>> {

        return userApi.confirmTfaRegistration(tfaCode)
                .doOnSuccess {
                    if (it.isSuccessful) {
                        LsPrefs.jwtToken = it.headers()[SgApi.HEADER_NAME_AUTHORIZATION] ?: return@doOnSuccess
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
                        LsPrefs.jwtToken = it.headers()[SgApi.HEADER_NAME_AUTHORIZATION] ?: return@doOnSuccess
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
                        LsPrefs.jwtToken = it.headers()[SgApi.HEADER_NAME_AUTHORIZATION] ?: return@doOnSuccess
                    }
                }
                .asHttpResourceLoader(networkStateObserver)
                .mapResource({
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

    fun signSEP10ChallengeIfValid(base64EnvelopeXDR: String, userKeyPair: KeyPair): String? {
        val envelope = validateSEP10Envelope(base64EnvelopeXDR, userKeyPair.xdrPublicKey)
        return envelope?.let { env ->
            // TODO not clear
            //val userSignature = userKeyPair.signDecorated(env.tx)
           // val encoded = TransactionEnvelope.encode(XdrDataOutputStream())
            null
        }
    }

    fun validateSEP10Envelope(base64EnvelopeXDR: String, userPublicKey: PublicKey): TransactionEnvelope? {
        val stream2 = XdrDataInputStream(ByteArrayInputStream(base64EnvelopeXDR.toByteArray()))
        val transactionEnvelope = TransactionEnvelope.decode(stream2)
        val transactionXdr = transactionEnvelope.tx

        val value = Uint64()
        value.uint64 = 0L
        if (transactionXdr.seqNum.sequenceNumber == value) {
            return transactionEnvelope
        }

        transactionXdr?.operations?.let { operations ->
            if (operations.size != 1) {
                return null
            }

            val first = operations.first()
            first?.sourceAccount?.accountID?.let {
                if (it != userPublicKey) {
                    return null
                }
            } ?: run {
                return null
            }

            first.body?.discriminant?.let { opType ->
                if (opType != OperationType.MANAGE_DATA) {
                    return null
                }
            } ?: run {
                return null
            }
        } ?: run {
            return null
        }

        transactionEnvelope.signatures?.let { signatures ->
            if (signatures.size != 1) {
                return null
            }
            val first = signatures.first()
            // TODO
            val transactionHash = byteArrayOf()
            // serverSingningKey
            val serverKeyPair = KeyPair(null)
            val isValidSignature = serverKeyPair.verify(transactionHash, first.signature.signature)
            if (isValidSignature) {

            } else {
                val signature = loadServerSigningKey()
                if (isValidSignature) {
                    return transactionEnvelope
                }
            }


        } ?: run {
            null
        }
        return null
    }

    fun loadServerSigningKey(): String {
        return ""
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

    private fun refreshTfaSecret(publicKey188: String): Flowable<Resource<Boolean, ServerException>> {

        return userApi.getTfaSecret(publicKey188)
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
                userSecurity.publicKeyIndex188
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

        return LsPrefs.loadUsername().filter { it.isNotBlank() }
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

//    //TODO: add callback to method
//    fun signSEP10ChallengeIfValid(base64EnvelopeXDR: String, userKeyPair: KeyPair) {
//        val validationResult = validateSEP10Envelope(base64EnvelopeXDR, userKeyPair.accountId)
//        when (validationResult.state) {
//            validationResult.SUCCES -> {
//                val isValid: Boolean = validationResult.isValid
//                val envelopeXDR: TransactionEnvelope = validationResult.transactionEnvelopeXDR
//                val default = 0
//                if (isValid && envelopeXDR != null && envelopeXDR.tx.seqNum.sequenceNumber.int64 == default.toLong()) {
//                    //sign
//                    // get currently used stellar network
//                    var network = Network.useTestNetwork()
//                    if (Services.shared.usePublicStellarNetwork) {
//                        network = Network.public
//                    }
//                    try {
//                        val tx = envelopeXDR.tx
//                        // user signature
//                        val transactionHash = [UInt8](tx.hash(network))
//                        val userSignature = userKeyPair.signDecorated(transactionHash)
//                        envelopeXDR.signatures.append(userSignature)
//                        val xdrEncodedEnvelope = envelopeXDR.xdrEncoded
//                        if (xdrEncodedEnvelope != null) {
//                            //TODO callback return .success (xdrEncodedEnvelope))
//                            return
//                        } else {
//                            //TODO callback return .failure (.xdrError))
//                        }
//                    } catch (exception: Exception) {
//                        //TODO callback return .failure (.xdrError))
//                    }
//                } else {
//                    //TODO callback return .failure (.invalidSEP10Challenge))
//                }
//            }
//            validationResult.FAILURE -> {
//                val error = validationResult.error
//                //TODO callback return .failure(error))
//            }
//        }
//    }
//    //TODO add callback to method
//    private fun validateSEP10Envelope(base64EnvelopeXDR: String, userAccountId: String) {
//        try {
//            // decode the envelope
//            val stream = ByteArrayInputStream(base64EnvelopeXDR.toByteArray())
//            val stream2 = XdrDataInputStream(stream)
//            val transactionEnvelopeXDR = TransactionEnvelope.decode(stream2)
//            val transactionXDR = transactionEnvelopeXDR.tx
//            // sequence number of transaction must be 0
//            val default = 0
//            if (transactionXDR.seqNum.sequenceNumber.int64 != default.toLong()) {
//                //TODO callback return false, transactionEnvelopeXDR
//                return
//            }
//            // the transaction must contain one operation
//            if (transactionXDR.operations.size == 1) {
//                val operationXDR = transactionXDR.operations.first()
//                // the source account of the operation must match
//                val operationSourceAccount = operationXDR.sourceAccount
//                //TODO to check if the conversion toString of operationSourceAccount.accountID
//                //TODO is correct logically speaking
//                if (operationSourceAccount.accountID.toString() != userAccountId) {
//                    //TODO review if this should replace the comparison of
//                    //TODO operationSourceAccount.accountId != userAccountId from swift or the one above
//                    if (operationSourceAccount.accountID.ed25519.uint256.toString() != userAccountId) {
//                        // source account of transaction doese not match user account
//                        // TODO callback return false, transactionEnvelopeXDR
//                        return
//                    }
//                } else {
//                    // source account of transaction not found
//                    //TODO callback return false, transactionEnvelopeXDR
//                    return
//                }
//                //operation must be manage data operation
//                val operationBodyXDR = operationXDR.body
//                if (operationBodyXDR.discriminant != OperationType.MANAGE_DATA) {
//                    // not a manage data operation
//                    //TODO callback return false, transactionEnvelopeXDR
//                    return
//                }
//            } else {
//                // the transaction has no operation or contains more than one operation
//                //TODO callback return false, transactionEnvelopeXDR
//                return
//            }
//            // the envelope must have one signature and it must be valid: transaction signed by the server
//            if (transactionEnvelopeXDR.signatures.count() == 1) {
//                val signature = transactionEnvelopeXDR.signatures.first().signature
//                // get currently used stellar network
//                var network = Network.useTestNetwork()
//                if (Services.shared.usePublicStellarNetwork) {
//                    network = Network.public
//                }
//                // transaction hash is the signed payload
//                var transactionHash = [UInt8](transactionXDR.hash(network))
//                // validate signature
//                var serverKeyPair = KeyPair(Services.shared.serverSigningKey)
//                var signatureIsValid = serverKeyPair.verify([UInt8](signature), transactionHash)
//                if (signatureIsValid) {
////                    signature is valid
//                    //TODO callback return true, transactionEnvelopeXDR
//                    return
//                } else { // signature is not valid
//                    //check if our server key is still the same. Load from server and if different, try validation again
//                    var response = loadServerSigningKey()
//                    when (response.state) {
//                        response.SUCCESS -> {
//                            var signingKey = response.signingKey
//                            // check if key loaded from the server is different to our locally stored key
//                            if (signingKey != Services.shared.serverSigningKey) {
//                                // store new signing key
//                                Services.shared.serverSigningKey = signingKey
//                                try {
//                                    // validate signature again
//                                    serverKeyPair = KeyPair(signingKey)
//                                    signatureIsValid = serverKeyPair.verify([UInt8](signature), transactionHash)
//                                    if (signatureIsValid) {
//                                        // signature is valid
//                                        //TODO callback return true, transactionEnvelopeXDR))
//                                        return
//                                    } else {
//                                        // signature is not valid
//                                        //TODO callback return false, transactionEnvelopeXDR))
//                                        return
//                                    }
//                                } catch (error: Exception) {
////                                print(error.localizedDescription)
//                                    // validation failed
//                                    //TODO callback return false, transactionEnvelopeXDR))
//                                }
//                            } else {
//                                // server key is not different
//                                // signature is not valid
//                                //TODO callback return false, envelopeXDR: transactionEnvelopeXDR))
//                                return
//                            }
//                        }
//                        response.FAILURE -> {
//                            var error = response.error
//                            // could not load signing key from server signature
//                            //TODO callback return. failure (error: error))
//                        }
//                    }
//                }
//            } else {
//                // could not find signature
//                //TODO callback return false, transactionEnvelopeXDR
//                return
//            }
//        } catch (error: Exception) {
//            print(error.localizedMessage)
//            // validation failed
//            //TODO callback return false, null
//        }
//    }
    /// Loads the server signing key from the servers stellar.toml file
    /// - Parameter: ServerSigningKeyClosure
//    private fun loadServerSigningKey() {
//        //TODO add callback to function
//        //TODO Services class
//        val url = URL(Services.shared.tomlURL)
//        if (url != null) {
//            try {
//                val tomlString = String(url.readBytes())
//                val toml = StellarToml(tomlString)
//                val serverKey = toml.accountInformation.signingKey
//                if (serverKey != null) {
//                    //TODO callback .success(serverKey))
//                } else {
//                    //TODO callback .failure(.noSigningKeySet))
//                }
//            } catch (error: Exception) {
//                //TODO callback .failure(error: .invalidToml))
//            }
//        } else {
//            //TODO callback return .failure(error: .invalidRequest))
//            return
//        }
//    }

    companion object {

        const val TAG = "UserRepository"
    }
}