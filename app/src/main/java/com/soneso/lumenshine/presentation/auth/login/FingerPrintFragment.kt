package com.soneso.lumenshine.presentation.auth.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.ErrorCodes
import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.presentation.auth.AuthFragment
import com.soneso.lumenshine.util.LsException
import com.soneso.lumenshine.util.Resource
import kotlinx.android.synthetic.main.fragment_finger_print.*


/**
 * A simple [Fragment] subclass.
 *
 */
class FingerPrintFragment : AuthFragment() {

    private lateinit var viewModel: FingerprintViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[FingerprintViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_finger_print, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        subscribeForLiveData()
    }

    private fun setupListeners() {

        passwordView.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })
        activateButton.setOnClickListener { attemptLogin() }
    }

    private fun subscribeForLiveData() {

    }

    private fun renderLoginStatus(resource: Resource<RegistrationStatus, LsException>) {

        when (resource.state) {
            Resource.FAILURE -> {
                handleError(resource.failure())
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

    }
}
