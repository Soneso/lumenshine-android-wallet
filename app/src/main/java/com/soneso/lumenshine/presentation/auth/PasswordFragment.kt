package com.soneso.lumenshine.presentation.auth


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.ErrorCodes
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.util.Resource
import kotlinx.android.synthetic.main.fragment_password.*


class PasswordFragment : AuthFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_password, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        if (BuildConfig.DEBUG) {
//            passwordView.trimmedText = "Test1234!"
//        }
        setupListeners()
        subscribeForLiveData()
    }

    private fun setupListeners() {

        unlockButton.setOnClickListener { attemptLogin() }
        lostPassButton.setOnClickListener {
            // TODO: cristi.paval, 8/25/18 - this anti pattern. Implement it accordingly.
//            LsPrefs.removeUserCrendentials()
//            authViewModel.refreshLastUserCredentials()
//            replaceFragment(LostCredentialFragment.newInstance(LostCredentialFragment.Credential.PASSWORD), LostCredentialFragment.TAG)
        }
    }

    private fun subscribeForLiveData() {

        authViewModel.liveLogin.observe(this, Observer {
            renderLoginStatus(it ?: return@Observer)
        })
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
                authActivity.goToMain()
            }
        }
    }

    /**
     * handling login response errors
     */
    private fun handleError(e: ServerException) {

        when (e.code) {
            ErrorCodes.LOGIN_WRONG_PASSWORD -> {
                passwordView.error = e.message
            }
            else -> {
                showErrorSnackbar(e)
            }
        }
    }

    private fun attemptLogin() {
        val username = authViewModel.liveLastUsername.value ?: return
        if (passwordView.isValidPassword()) {
            authViewModel.login(username, passwordView.trimmedText)
        }
    }

    companion object {
        const val TAG = "PasswordFragment"

        fun newInstance() = PasswordFragment()
    }
}
