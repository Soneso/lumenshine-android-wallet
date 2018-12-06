package com.soneso.lumenshine.presentation.auth.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.auth.AuthFragment
import com.soneso.lumenshine.util.LsException
import com.soneso.lumenshine.util.Resource
import kotlinx.android.synthetic.main.fragment_mail_lost_credential.*


/**
 * A simple [Fragment] subclass.
 */
class EmailLostCredentialFragment : AuthFragment() {
    private lateinit var credential: Credential
    private lateinit var email: String
    private lateinit var lostCredentialViewModel: LostCredentialViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lostCredentialViewModel = ViewModelProviders.of(this, viewModelFactory)[LostCredentialViewModel::class.java]

        credential = arguments?.getSerializable(ARG_CREDENTIAL) as? Credential ?: Credential.PASSWORD
        email = arguments?.getString(ARG_EMAIL).toString()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_mail_lost_credential, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateViews()
        setupListeners()
        subscribeForLiveData()
    }

    private fun populateViews() {
        when (credential) {
            Credential.PASSWORD -> {
                subtitleSuccessView.setText(R.string.reset_password_email_sent)
                subTitleView.setText(R.string.enter_email_address_reset_password_description)
            }
            Credential.TFA -> {
                subtitleSuccessView.setText(R.string.reset_2fa_email_sent)
                subTitleView.setText(R.string.enter_email_address_reset_2fa_description)
            }
        }
    }

    private fun setupListeners() {
        resendEmailButton.setOnClickListener {
            sendRequest()
        }

        doneButton.setOnClickListener {
            authActivity.navigate(R.id.to_login)
        }
    }

    private fun subscribeForLiveData() {
        lostCredentialViewModel.liveCredentialResetEmail.observe(this, Observer {
            renderCredentialReset(it ?: return@Observer)
        })
    }

    private fun renderCredentialReset(resource: Resource<Unit, LsException>) {
        when (resource.state) {
            Resource.LOADING -> {
                showLoadingView()
            }
            Resource.FAILURE -> {
                hideLoadingView()
                showErrorSnackbar(resource.failure())
            }
            else -> {
                hideLoadingView()
                showSnackbar(getString(R.string.email_sent))
            }
        }
    }

    private fun sendRequest() {
        when (credential) {
            Credential.PASSWORD -> lostCredentialViewModel.requestPasswordResetEmail(email)
            Credential.TFA -> lostCredentialViewModel.requestTfaResetEmail(email)
        }
    }

    companion object {

        const val TAG = "EmailLostCredentialFragment"
        private const val ARG_CREDENTIAL = "$TAG.ARG_CREDENTIAL"
        private const val ARG_EMAIL = "$TAG.ARG_EMAIL"

        fun argForPassword(email: String) = Bundle().apply {
            putSerializable(ARG_CREDENTIAL, Credential.PASSWORD)
            putString(ARG_EMAIL, email)
        }

        fun argForTfa(email: String) = Bundle().apply {
            putSerializable(ARG_CREDENTIAL, Credential.TFA)
            putString(ARG_EMAIL, email)
        }
    }
}