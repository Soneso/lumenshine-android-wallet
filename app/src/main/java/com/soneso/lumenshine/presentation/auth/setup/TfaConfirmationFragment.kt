package com.soneso.lumenshine.presentation.auth.setup


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
import com.soneso.lumenshine.util.GeneralUtils
import com.soneso.lumenshine.util.LsException
import com.soneso.lumenshine.util.Resource
import kotlinx.android.synthetic.main.fragment_tfa_registration.*

/**
 * A simple [Fragment] subclass.
 *
 */
class TfaConfirmationFragment : SetupFragment() {

    private lateinit var viewModel: TFAConfirmationViewModel
    private var tfaSecret: String = ""
    private var shouldAutoPaste: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_tfa_registration, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[TFAConfirmationViewModel::class.java]

        viewModel.fetchTfaSecret()
        subscribeForLiveData()
        setupListeners()
    }

    override fun onPause() {
        super.onPause()
        shouldAutoPaste = true
    }

    override fun onResume() {
        super.onResume()
        val textFromClipboard: String = GeneralUtils.pasteFromClipboard(context!!)
        if (shouldAutoPaste && textFromClipboard.toIntOrNull() != null) {
            tfaInputView.trimmedText = textFromClipboard
            tfaInputView.setSelection(textFromClipboard.length)
        }
    }

    private fun subscribeForLiveData() {

        viewModel.liveTfaSecret.observe(this, Observer {
            setupToken(it ?: return@Observer)
        })

        viewModel.liveTfaConfirmation.observe(this, Observer {
            renderTfaConfirmation(it ?: return@Observer)
        })
    }

    private fun setupListeners() {
        nextButton.setOnClickListener {
            subscribeForLiveData()
            if (tfaInputView.hasValidInput()) {
                viewModel.confirmTfaRegistration(tfaInputView.trimmedText)
            }
        }

        copyButton.setOnClickListener {
            showSnackbar(R.string.copied_to_clipboard)
            context?.let { it1 -> GeneralUtils.copyToClipboard(it1, tfaSecret) }
        }
    }

    private fun renderTfaConfirmation(resource: Resource<RegistrationStatus, LsException>) {
        when (resource.state) {
            Resource.FAILURE -> {
                showLoadingView(false)
                handleError(resource.failure())
            }
            Resource.LOADING -> showLoadingView(true)

            Resource.SUCCESS -> {
                showLoadingView(false)
                renderRegistrationStatus(resource.success())
            }
        }
    }

    /**
     * handling response errors
     */
    private fun handleError(error: LsException) {

        if (error is ServerException && error.code == ErrorCodes.LOGIN_INVALID_2FA) {
            tfaInputView.error = error.displayMessage
        } else {
            showErrorSnackbar(error)
        }
    }

    private fun setupToken(tfaSecret: String) {
        this.tfaSecret = tfaSecret
        tfaSecretView.text = getString(R.string.lbl_tfa_secret, tfaSecret)
    }

    companion object {

        const val TAG = "TfaConfirmationFragment"

        fun newInstance() = TfaConfirmationFragment()
    }
}
