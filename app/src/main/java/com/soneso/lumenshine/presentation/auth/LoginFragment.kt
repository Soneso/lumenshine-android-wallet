package com.soneso.lumenshine.presentation.auth


import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.google.authenticator.OtpProvider
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.ErrorCodes
import com.soneso.lumenshine.domain.data.RegistrationStatus
import com.soneso.lumenshine.domain.data.SgError
import com.soneso.lumenshine.domain.data.UserCredentials
import com.soneso.lumenshine.presentation.general.SgViewState
import com.soneso.lumenshine.presentation.general.State
import com.soneso.lumenshine.presentation.util.hideProgressDialog
import com.soneso.lumenshine.presentation.util.setOnTextChangeListener
import com.soneso.lumenshine.presentation.util.showProgressDialog
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.sg_input_view.view.*


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

//        authViewModel.refreshLastUserCredentials()
    }

    private fun subscribeForLiveData() {

        authViewModel.liveRegistrationStatus.observe(this, Observer {
            renderRegistrationStatus(it ?: return@Observer)
        })

//        authViewModel.liveLastCredentials.observe(this, Observer {
//            renderLastCredentials(it ?: return@Observer)
//        })
    }

    private fun setupListeners() {

        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })
        email_sign_in_button.setOnClickListener { attemptLogin() }

        //TODO remove these buttons
//        create_account_button.setOnClickListener {
//            replaceFragment(RegistrationFragment.newInstance(), RegistrationFragment.TAG)
//        }
//        lost_pass_button.setOnClickListener {
//            replaceFragment(LostCredentialFragment.newInstance(LostCredentialFragment.Credential.PASSWORD), LostCredentialFragment.TAG)
//        }
//        lost_tfa_button.setOnClickListener {
//            replaceFragment(LostCredentialFragment.newInstance(LostCredentialFragment.Credential.TFA), LostCredentialFragment.TAG)
//        }
        email.input_edit_text.setOnTextChangeListener {
            renderLastCredentials(authViewModel.liveLastCredentials.value
                    ?: return@setOnTextChangeListener)
        }
//        show_dialog.setOnClickListener {
//            activity!!.showInfoDialog()
//        }
    }

    private fun attemptLogin() {

        if (!isValidForm()) {
            return
        }

        var tfaCode = two_factor_code.trimmedText
        if (tfaCode.contains("*")) {

            val credentials = authViewModel.liveLastCredentials.value?.data ?: return
            tfaCode = OtpProvider.currentTotpCode(credentials.tfaSecret) ?: return
        }
        authViewModel.login(email.trimmedText, password.trimmedText, tfaCode)
    }

    private fun renderLastCredentials(viewState: SgViewState<UserCredentials>) {

        when (viewState.state) {
            State.LOADING -> {
            }
            State.ERROR -> {
            }
            State.READY -> {

                val credentials = viewState.data!!
                when {
                    credentials.username.contentEquals(email.trimmedText) && credentials.username.isNotEmpty() -> {
                        two_factor_code.trimmedText = "******"
                    }
                    email.trimmedText.isEmpty() && !credentials.username.contentEquals(email.trimmedText) -> {
                        email.trimmedText = credentials.username
                        two_factor_code.trimmedText = "******"
                    }
                    else -> {
                        two_factor_code.trimmedText = ""
                    }
                }
            }
        }
    }

    private fun renderRegistrationStatus(viewState: SgViewState<RegistrationStatus>) {

        when (viewState.state) {
            State.LOADING -> {

                context?.showProgressDialog()
            }
            State.ERROR -> {

                hideProgressDialog()
                handleError(viewState.error)
            }
            else -> {

                hideProgressDialog()
            }
        }
    }

    /**
     * handling login response errors
     */
    private fun handleError(e: SgError?) {
        val error = e ?: return

        when (error.errorCode) {
            ErrorCodes.LOGIN_EMAIL_NOT_EXIST -> {
                email.error = if (error.errorResId == 0) error.message!! else getString(error.errorResId)
            }
            ErrorCodes.LOGIN_INVALID_2FA -> {
                two_factor_code.error = if (error.errorResId == 0) error.message!! else getString(error.errorResId)
            }
            ErrorCodes.LOGIN_WRONG_PASSWORD -> {
                password.error = if (error.errorResId == 0) error.message!! else getString(error.errorResId)
            }
            else -> {
                showErrorSnackbar(error)
            }
        }
    }

    private fun isValidForm() = email.hasValidInput()

    companion object {

        const val TAG = "LoginFragment"

        fun newInstance() = LoginFragment()
    }
}
