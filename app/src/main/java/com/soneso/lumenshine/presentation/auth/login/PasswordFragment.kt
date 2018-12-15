package com.soneso.lumenshine.presentation.auth.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mtramin.rxfingerprint.RxFingerprint
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.ErrorCodes
import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.presentation.auth.AuthFragment
import com.soneso.lumenshine.util.GeneralUtils
import com.soneso.lumenshine.util.LsException
import com.soneso.lumenshine.util.Resource
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_password.*


class PasswordFragment : AuthFragment() {

    private lateinit var viewModel: PasswordViewModel
    private var disposable: Disposable? = null

    // TODO: cristi.paval, 11/29/18 - another solution?
    private var shouldAutoPaste: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[PasswordViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_password, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        subscribeForLiveData()
    }

    override fun onResume() {
        super.onResume()

        val textFromClipboard: String = GeneralUtils.pasteFromClipboard(context!!)
        if (shouldAutoPaste && textFromClipboard.toIntOrNull() != null) {
            tfaCodeView.trimmedText = textFromClipboard
            tfaCodeView.setSelection(textFromClipboard.length)
        }
        listenForTouch()
    }

    private fun listenForTouch() {
        disposable?.dispose()
        disposable = RxFingerprint.authenticate(context ?: return)
                .subscribe {
                    if (it.isSuccess) {
                        viewModel.login()
                    } else {
                        handleError(LsException(it.message))
                    }
                }
    }

    override fun onPause() {
        super.onPause()
        if (tfaCodeView.visibility == View.VISIBLE) {
            shouldAutoPaste = true
        }
        disposable?.dispose()
    }

    private fun setupListeners() {

        unlockButton.setOnClickListener { attemptLogin() }
        lostPassButton.setOnClickListener {}
    }

    private fun subscribeForLiveData() {

        authViewModel.liveFingerprintActive.observe(this, Observer {
            fingerprintButton.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.liveLogin.observe(this, Observer {
            renderLoginStatus(it ?: return@Observer)
        })
    }

    private fun renderLoginStatus(resource: Resource<RegistrationStatus, LsException>) {

        when (resource.state) {
            Resource.LOADING -> {
                showLoadingView(true)
            }
            Resource.FAILURE -> {
                showLoadingView(false)
                handleError(resource.failure())
            }
            else -> {
                showLoadingView(false)
                authActivity.goToMain()
            }
        }
    }

    /**
     * handling login response errors
     */
    private fun handleError(e: LsException) {

        if (e !is ServerException) {
            showErrorSnackbar(e)
            return
        }
        when (e.code) {
            ErrorCodes.LOGIN_WRONG_PASSWORD -> {
                passwordView.error = e.displayMessage
                fingerprintButton.visibility = View.GONE
            }
            ErrorCodes.LOGIN_INVALID_2FA -> {
                if (tfaCodeView.visibility == View.VISIBLE) {
                    tfaCodeView.error = e.displayMessage
                } else {
                    tfaCodeView.visibility = View.VISIBLE
                    fingerprintButton.visibility = View.GONE
                }
            }
        }
    }

    private fun attemptLogin() {
        if (!passwordView.isValidPassword()) {
            return
        }
        when {
            // onisio, 15/12/2018 - if the tfa secret was changed and has to be updated
            tfaCodeView.visibility == View.VISIBLE -> viewModel.login(passwordView.trimmedText, tfaCodeView.trimmedText)

            else -> viewModel.login(passwordView.trimmedText)
        }
    }

    companion object {
        const val TAG = "PasswordFragment"

        fun newInstance() = PasswordFragment()
    }
}
