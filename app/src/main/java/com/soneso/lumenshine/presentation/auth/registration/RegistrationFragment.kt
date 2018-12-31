package com.soneso.lumenshine.presentation.auth.registration


import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.ErrorCodes
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.presentation.auth.AuthFragment
import com.soneso.lumenshine.presentation.util.color
import com.soneso.lumenshine.presentation.util.setOnTextChangeListener
import com.soneso.lumenshine.presentation.widgets.LsEditText
import com.soneso.lumenshine.util.LsException
import com.soneso.lumenshine.util.Resource
import kotlinx.android.synthetic.main.fragment_registration.*
import kotlinx.android.synthetic.main.ls_input_view.view.*


/**
 * A simple [Fragment] subclass.
 */
class RegistrationFragment : AuthFragment() {

    private var forenameCompleted: Boolean = false
    private var lastNameCompleted: Boolean = false
    private var emailCompleted: Boolean = false
    private var passwordCompleted: Boolean = false
    private var repeatPasswordCompleted: Boolean = false
    private var termsOfServiceAccepted: Boolean = false

    private lateinit var viewModel: RegistrationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[RegistrationViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_registration, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        subscribeForLiveData()

        setupListeners()
    }

    private fun initViews() {
        val termsOfService = getString(R.string.terms_of_service)
        val youAgreeToAbideTermsOfUse = getString(R.string.you_agree_to_abide) + termsOfService

        val spannable = SpannableString(youAgreeToAbideTermsOfUse)

        spannable.setSpan(object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
            }

            override fun onClick(p0: View) {
                TermsOfServiceDialog.showInstance(activity!!.supportFragmentManager)
            }
        }, youAgreeToAbideTermsOfUse.length - termsOfService.length, youAgreeToAbideTermsOfUse.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannable.setSpan(object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
            }

            override fun onClick(p0: View) {
                checkboxTermsOfService.isChecked = !checkboxTermsOfService.isChecked
            }
        }, 0, youAgreeToAbideTermsOfUse.length - termsOfService.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        context?.let {
            val spanColor: Int = it.color(R.color.blue)
            spannable.setSpan(ForegroundColorSpan(spanColor), youAgreeToAbideTermsOfUse.length - termsOfService.length, youAgreeToAbideTermsOfUse.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        agreeToTermsOfService.setText(spannable, TextView.BufferType.SPANNABLE)
        agreeToTermsOfService.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun subscribeForLiveData() {
        viewModel.liveRegistration.observe(this, Observer {
            renderRegistration(it ?: return@Observer)
        })
    }

    private fun showPasswordRequirements() {
        PasswordRequirementsDialog.showInstance(activity?.supportFragmentManager ?: return)
    }

    private fun setupListeners() {
        registerButton.setOnClickListener { attemptRegistration() }

        foreName.editTextView.setOnTextChangeListener {
            val editText = it as LsEditText
            forenameCompleted = editText.text.toString().isNotEmpty()
            enableDisableRegisterButton()
        }

        lastName.editTextView.setOnTextChangeListener {
            val editText = it as LsEditText
            lastNameCompleted = editText.text.toString().isNotEmpty()
            enableDisableRegisterButton()
        }

        emailView.editTextView.setOnTextChangeListener {
            val editText = it as LsEditText
            emailCompleted = editText.text.toString().isNotEmpty()
            enableDisableRegisterButton()
        }
        password.editTextView.setOnTextChangeListener {
            val editText = it as LsEditText
            passwordCompleted = editText.text.toString().isNotEmpty()
            enableDisableRegisterButton()
        }
        passConfirmationView.editTextView.setOnTextChangeListener {
            val editText = it as LsEditText
            repeatPasswordCompleted = editText.text.toString().isNotEmpty()
            enableDisableRegisterButton()
        }

        checkboxTermsOfService.setOnCheckedChangeListener { _, b ->
            termsOfServiceAccepted = b
            enableDisableRegisterButton()
        }

        password.onDrawableEndClickListener = ::showPasswordRequirements
    }

    private fun enableDisableRegisterButton() {
        registerButton.isEnabled = forenameCompleted && lastNameCompleted && emailCompleted && passwordCompleted
                && repeatPasswordCompleted && termsOfServiceAccepted
    }

    private fun renderRegistration(resource: Resource<Unit, LsException>) {
        when (resource.state) {

            Resource.LOADING -> showLoadingView(true)
            Resource.FAILURE -> {
                showLoadingView(false)
                handleError(resource.failure())
            }
            else -> {
                showLoadingView(false)
                authActivity.goToSetup()
            }
        }
    }

    /**
     * handling login response errors
     */
    private fun handleError(e: LsException) {

        if (e is ServerException && e.code == ErrorCodes.SIGNUP_EMAIL_ALREADY_EXIST) {
            emailView.error = e.displayMessage
        } else {
            showErrorSnackbar(e)
        }
    }

    private fun attemptRegistration() {

        if (!isValidForm()) {
            return
        }
        viewModel.register(
                foreName.trimmedText,
                lastName.trimmedText,
                emailView.trimmedText,
                password.trimmedText
        )
    }

    private fun isValidForm() =
            foreName.hasValidInput()
                    && lastName.hasValidInput()
                    && emailView.hasValidInput()
                    && password.isValidPassword()
                    && isPasswordMatch()


    private fun isPasswordMatch(): Boolean {
        val match = password.trimmedText == passConfirmationView.trimmedText
        if (!match) {
            password.error = getString(R.string.password_not_match)
            passConfirmationView.error = getString(R.string.password_not_match)
        }
        return match
    }

    companion object {
        const val TAG = "RegistrationFragment"

        fun newInstance() = RegistrationFragment()
    }
}
