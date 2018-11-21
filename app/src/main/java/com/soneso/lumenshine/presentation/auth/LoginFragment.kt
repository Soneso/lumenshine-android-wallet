package com.soneso.lumenshine.presentation.auth


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.ErrorCodes
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.util.Resource
import kotlinx.android.synthetic.main.fragment_login.*


/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : AuthFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeForLiveData()
        setupListeners()
    }

    private fun subscribeForLiveData() {
        authViewModel.liveLogin.observe(this, Observer {
            renderLoginStatus(it ?: return@Observer)
        })
    }

    private fun setupListeners() {

        passwordView.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })
        loginButton.setOnClickListener {
            attemptLogin()
        }
    }

    private fun attemptLogin() {

        if (!isValidForm()) {
            return
        }
        authViewModel.login(
                emailView.trimmedText,
                passwordView.trimmedText,
                tfaCodeView.trimmedText
        )
    }

    private fun renderLoginStatus(resource: Resource<Boolean, ServerException>) {

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
                if (resource.success()) {
                    authActivity.goToMain()
                } else {
                    authActivity.goToSetup()
                }
            }
        }
    }

    /**
     * handling login response errors
     */
    private fun handleError(e: ServerException) {

        when (e.code) {
            ErrorCodes.LOGIN_EMAIL_NOT_EXIST -> emailView.error = e.displayMessage
            ErrorCodes.LOGIN_INVALID_2FA -> tfaCodeView.error = e.displayMessage
            ErrorCodes.LOGIN_WRONG_PASSWORD -> passwordView.error = e.displayMessage
            ErrorCodes.MISSING_TFA -> tfaCodeView.error = e.displayMessage
            else -> showErrorSnackbar(e)
        }
    }

    private fun isValidForm() = emailView.hasValidInput()

    companion object {

        const val TAG = "LoginFragment"

        fun newInstance() = LoginFragment()
    }
}
