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
import kotlinx.android.synthetic.main.fragment_lost_credential.*

/**
 * A simple [Fragment] subclass.
 */
class LostCredentialFragment : AuthFragment() {

    private lateinit var credential: Credential
    private lateinit var lostCredentialViewModel: LostCredentialViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lostCredentialViewModel = ViewModelProviders.of(this, viewModelFactory)[LostCredentialViewModel::class.java]

        credential = arguments?.getSerializable(ARG_CREDENTIAL) as? Credential ?: Credential.PASSWORD
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_lost_credential, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateViews()
        setupListeners()
        subscribeForLiveData()
    }

    private fun populateViews() {

        when (credential) {
            Credential.PASSWORD -> titleView.setText(R.string.reset_password)
            Credential.TFA -> titleView.setText(R.string.reset_2fa)
        }
    }

    private fun setupListeners() {
        nextButton.setOnClickListener {
            sendRequest()
        }
    }

    private fun sendRequest() {
        if (!isValidForm()) {
            return
        }

        when (credential) {
            Credential.PASSWORD -> {
                lostCredentialViewModel.requestPasswordResetEmail(emailView.trimmedText)
            }
            Credential.TFA -> {
                lostCredentialViewModel.requestTfaResetEmail(emailView.trimmedText)
            }
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
                when (credential) {
                    Credential.PASSWORD -> authActivity.navigate(R.id.to_email_lost_credential_screen,
                            EmailLostCredentialFragment.argForPassword(emailView.trimmedText.toString()))
                    Credential.TFA -> authActivity.navigate(R.id.to_email_lost_credential_screen,
                            EmailLostCredentialFragment.argForTfa(emailView.trimmedText.toString()))
                }
            }
        }
    }

    private fun isValidForm() = emailView.hasValidInput()

    companion object {

        const val TAG = "LostCredentialFragment"
        private const val ARG_CREDENTIAL = "$TAG.ARG_CREDENTIAL"

        fun argForPassword() = Bundle().apply {
            putSerializable(ARG_CREDENTIAL, Credential.PASSWORD)
        }

        fun argForTfa() = Bundle().apply {
            putSerializable(ARG_CREDENTIAL, Credential.TFA)
        }
    }
}
