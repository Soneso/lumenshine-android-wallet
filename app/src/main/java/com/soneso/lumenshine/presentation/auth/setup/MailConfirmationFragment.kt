package com.soneso.lumenshine.presentation.auth.setup


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.soneso.lumenshine.R
import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.presentation.util.TypefaceSpan
import com.soneso.lumenshine.util.LsException
import com.soneso.lumenshine.util.Resource
import kotlinx.android.synthetic.main.fragment_mail_confirmation.*


/**
 * A simple [Fragment] subclass.
 *
 */
class MailConfirmationFragment : SetupFragment() {

    private lateinit var viewModel: MailConfirmationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[MailConfirmationViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_mail_confirmation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        subscribeForLiveData()
    }

    private fun setupDescription(username: String) {
        descriptionView.text = buildSpannedString {
            append(getText(R.string.email_confirmation_prehint))
            append(' ')
            append(username)
            val font = context?.let {
                ResourcesCompat.getFont(it, R.font.encodesans_bold)
            }
            font?.let {
                setSpan(TypefaceSpan(it), length - username.length, length, 0)
            }
            append(". ")
            append(getText(R.string.email_confirmation_posthint))
        }
    }

    private fun subscribeForLiveData() {

        viewModel.liveUsername.observe(this, Observer {
            setupDescription(it ?: return@Observer)
        })
        viewModel.liveRegistrationStatus.observe(this, Observer {
            renderRegistrationRefresh(it ?: return@Observer)
        })
        viewModel.liveMailResend.observe(this, Observer {
            renderMailResend(it ?: return@Observer)
        })
    }

    private fun setupListeners() {

        resendButton.setOnClickListener {
            errorView.text = ""
            viewModel.resendConfirmationMail()
        }
        submitButton.setOnClickListener {
            errorView.text = ""

            viewModel.refreshRegistrationStatus()
        }
    }

    private fun renderRegistrationRefresh(resource: Resource<RegistrationStatus, LsException>) {

        when (resource.state) {
            Resource.LOADING -> showLoadingView(true)
            Resource.SUCCESS -> {
                showLoadingView(false)
                val status = resource.success()
                if (!status.mailConfirmed) {
                    errorView.setText(R.string.error_verify_email)
                } else {
                    renderRegistrationStatus(status)
                }
            }
            Resource.FAILURE -> {
                showLoadingView(false)
                showErrorSnackbar(resource.failure())
            }
        }
    }

    private fun renderMailResend(resource: Resource<Unit, LsException>) {

        when (resource.state) {
            Resource.LOADING -> showLoadingView(true)
            Resource.SUCCESS -> {
                showLoadingView(false)
                showSnackbar(R.string.confirmation_mail_resent)
            }
            Resource.FAILURE -> {
                showLoadingView(false)
                handleError(resource.failure())
            }
        }
    }

    private fun handleError(e: LsException?) {
        if (e is ServerException) {
            errorView.text = e.displayMessage
        } else {
            showErrorSnackbar(e)
        }
    }

    companion object {

        const val TAG = "MailConfirmationFragment"

        fun newInstance() = MailConfirmationFragment()
    }
}