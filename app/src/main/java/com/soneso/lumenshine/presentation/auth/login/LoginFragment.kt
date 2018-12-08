package com.soneso.lumenshine.presentation.auth.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.ErrorCodes
import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.presentation.auth.AuthFragment
import com.soneso.lumenshine.util.GeneralUtils
import com.soneso.lumenshine.util.LsException
import com.soneso.lumenshine.util.Resource
import kotlinx.android.synthetic.main.fragment_login.*


/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : AuthFragment() {

    private var shouldAutoPaste: Boolean = false
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[LoginViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeForLiveData()
        setupListeners()
    }

    private fun subscribeForLiveData() {
        viewModel.liveLogin.observe(this, Observer {
            renderLoginStatus(it ?: return@Observer)
        })
    }

    private fun setupListeners() {

        passwordView.setOnEditorActionListener { attemptLogin() }
        loginButton.setOnClickListener { attemptLogin() }
    }

    private fun attemptLogin() {

        if (!isValidForm()) {
            return
        }
        viewModel.login(
                emailView.trimmedText,
                passwordView.trimmedText,
                tfaCodeView.trimmedText
        )
    }

    private fun renderLoginStatus(resource: Resource<RegistrationStatus, LsException>) {

        when (resource.state) {
            Resource.LOADING -> {
                showLoadingView()
            }
            Resource.FAILURE -> {
                hideLoadingView()
                handleError(resource.failure())
            }
            else -> {
                hideLoadingView()
                val status = resource.success()
                if (status.isSetupCompleted()) {
                    authActivity.goToMain()
                } else {
                    authActivity.goToSetup(status)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val textFromClipboard: String = GeneralUtils.pasteFromClipboard(context!!)
        if (shouldAutoPaste && textFromClipboard.toIntOrNull() != null) {
            tfaCodeView.trimmedText = textFromClipboard
            tfaCodeView.setSelection(textFromClipboard.length)
        }
    }

    override fun onPause() {
        super.onPause()
        shouldAutoPaste = true
    }

    /**
     * handling login response errors
     */
    private fun handleError(e: LsException) {

        if (e is ServerException) {
            when (e.code) {
                ErrorCodes.LOGIN_EMAIL_NOT_EXIST -> emailView.error = e.displayMessage
                ErrorCodes.LOGIN_INVALID_2FA -> tfaCodeView.error = e.displayMessage
                ErrorCodes.LOGIN_WRONG_PASSWORD -> passwordView.error = e.displayMessage
                ErrorCodes.MISSING_TFA -> tfaCodeView.error = e.displayMessage
            }
        } else {
            showErrorSnackbar(e)
        }
    }

    private fun isValidForm() = emailView.hasValidInput()

    companion object {

        const val TAG = "LoginFragment"

        fun newInstance() = LoginFragment()
    }
}
