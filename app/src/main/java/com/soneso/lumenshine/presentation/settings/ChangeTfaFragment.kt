package com.soneso.lumenshine.presentation.settings


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.ErrorCodes
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.presentation.FragmentInMain
import com.soneso.lumenshine.util.GeneralUtils
import com.soneso.lumenshine.util.LsException
import com.soneso.lumenshine.util.Resource
import kotlinx.android.synthetic.main.fragment_change_tfa.*

/**
 * A simple [Fragment] subclass.
 */
class ChangeTfaFragment : FragmentInMain() {

    private var shouldAutoPaste: Boolean = false
    private lateinit var viewModel: ChangeTfaViewModel
    private lateinit var tfaSecret: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[ChangeTfaViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_change_tfa, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        subscribeToLiveData()
    }

    override fun onResume() {
        super.onResume()
        val textFromClipboard: String = GeneralUtils.pasteFromClipboard(context!!)
        if (shouldAutoPaste && textFromClipboard.toIntOrNull() != null) {
            tfaInputView.trimmedText = textFromClipboard
            tfaInputView.setSelection(textFromClipboard.length)
        }
    }

    override fun onPause() {
        super.onPause()
        shouldAutoPaste = true
    }

    private fun setupListeners() {
        passInput.setOnEditorActionListener { attemptTfaChange() }
        passNextButton.setOnClickListener { attemptTfaChange() }
        copyButton.setOnClickListener {
            showSnackbar(R.string.copied_to_clipboard)
            context?.let { it1 ->
                GeneralUtils.copyToClipboard(it1, tfaSecret)
            }
        }
        tfaCancelButton.setOnClickListener { viewFlipper.displayedChild = viewFlipper.indexOfChild(passStepLayout) }
        tfaInputView.setOnEditorActionListener { attemptConfirmTfa() }
        tfaNextButton.setOnClickListener { attemptConfirmTfa() }
        doneButton.setOnClickListener { NavHostFragment.findNavController(this).navigateUp() }
    }

    private fun attemptTfaChange() {
        if (passInput.isValidPassword()) {
            viewModel.changeTfa(passInput.trimmedText)
        }
    }

    private fun attemptConfirmTfa() {
        if (tfaInputView.hasValidInput()) {
            viewModel.confirmTfa(tfaSecret, tfaInputView.trimmedText)
        }
    }

    private fun subscribeToLiveData() {
        viewModel.liveTfaSecret.observe(this, Observer {
            renderTfaSecret(it ?: return@Observer)
        })
        viewModel.liveTfaConfirmation.observe(this, Observer {
            renderTfaConfirmation(it ?: return@Observer)
        })
    }

    private fun renderTfaSecret(resource: Resource<String, LsException>) {
        when (resource.state) {
            Resource.LOADING -> showLoadingView(true)
            Resource.FAILURE -> {
                showLoadingView(false)
                handlePassError(resource.failure())
            }
            Resource.SUCCESS -> {
                showLoadingView(false)
                tfaSecret = resource.success()
                tfaSecretView.text = getString(R.string.lbl_tfa_secret, resource.success())
                tfaInputView.trimmedText = ""
                viewFlipper.displayedChild = viewFlipper.indexOfChild(tfaSecretStepLayout)
            }
        }
    }

    private fun handlePassError(failure: LsException) {

        if (failure is ServerException && failure.code == ErrorCodes.LOGIN_WRONG_PASSWORD) {
            passInput.error = getString(R.string.login_password_wrong)
        } else {
            showErrorSnackbar(failure)
        }
    }

    private fun renderTfaConfirmation(resource: Resource<Unit, LsException>) {
        when (resource.state) {
            Resource.LOADING -> showLoadingView(true)
            Resource.FAILURE -> {
                showLoadingView(false)
                handleTfaError(resource.failure())
            }
            Resource.SUCCESS -> {
                showLoadingView(false)
                viewFlipper.displayedChild = viewFlipper.indexOfChild(successLayout)
            }
        }
    }

    private fun handleTfaError(failure: LsException) {

        if (failure is ServerException) {
            tfaInputView.error = failure.displayMessage
        } else {
            showErrorSnackbar(failure)
        }
    }
}
