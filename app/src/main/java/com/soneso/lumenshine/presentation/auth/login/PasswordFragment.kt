package com.soneso.lumenshine.presentation.auth.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.ErrorCodes
import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.presentation.auth.AuthFragment
import com.soneso.lumenshine.util.LsException
import com.soneso.lumenshine.util.Resource
import kotlinx.android.synthetic.main.fragment_password.*


class PasswordFragment : AuthFragment() {

    private lateinit var viewModel: PasswordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[PasswordViewModel::class.java]
    }

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
        }
    }

    private fun subscribeForLiveData() {

        viewModel.liveLogin.observe(this, Observer {
            renderLoginStatus(it ?: return@Observer)
        })
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
                authActivity.goToMain()
            }
        }
    }

    /**
     * handling login response errors
     */
    private fun handleError(e: LsException) {

        if (e is ServerException && e.code == ErrorCodes.LOGIN_WRONG_PASSWORD) {
            passwordView.error = e.displayMessage
        } else {
            showErrorSnackbar(e)
        }
    }

    private fun attemptLogin() {
        if (passwordView.isValidPassword()) {
            viewModel.login(passwordView.trimmedText)
        }
    }

    companion object {
        const val TAG = "PasswordFragment"

        fun newInstance() = PasswordFragment()
    }
}
