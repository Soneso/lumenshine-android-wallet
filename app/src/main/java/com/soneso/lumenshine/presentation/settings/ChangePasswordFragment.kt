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
import com.soneso.lumenshine.presentation.auth.registration.PasswordRequirementsDialog
import com.soneso.lumenshine.presentation.general.LsFragment
import com.soneso.lumenshine.util.LsException
import com.soneso.lumenshine.util.Resource
import kotlinx.android.synthetic.main.fragment_change_password.*

/**
 * A simple [Fragment] subclass.
 *
 */
class ChangePasswordFragment : LsFragment() {

    private lateinit var viewModel: ChangePassViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[ChangePassViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_change_password, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        subscribeToLiveData()
    }

    private fun subscribeToLiveData() {
        viewModel.livePassChange.observe(this, Observer {
            renderPassChange(it ?: return@Observer)
        })
    }

    private fun setupListeners() {
        newPassInput.onDrawableEndClickListener = { activity?.let { PasswordRequirementsDialog.showInstance(it.supportFragmentManager) } }
        newPassConfirmationInput.setOnEditorActionListener { attemptPassChange() }
        submitButton.setOnClickListener { attemptPassChange() }
        doneButton.setOnClickListener { NavHostFragment.findNavController(this).navigateUp() }
    }

    private fun attemptPassChange() {
        if (!oldPassInput.isValidPassword() || !newPassInput.isValidPassword()) {
            return
        }
        if (newPassInput.trimmedText.toString() != newPassConfirmationInput.trimmedText.toString()) {
            newPassConfirmationInput.error = getString(R.string.invalid_repassword)
            return
        }
        viewModel.changePassword(oldPassInput.trimmedText, newPassInput.trimmedText)
    }

    private fun renderPassChange(resource: Resource<Unit, LsException>) {
        when (resource.state) {
            Resource.LOADING -> showLoadingView(true)
            Resource.FAILURE -> {
                showLoadingView(false)
                handleError(resource.failure())
            }
            Resource.SUCCESS -> {
                showLoadingView(false)
                viewFlipper.displayedChild = viewFlipper.indexOfChild(successLayout)
            }
        }
    }

    private fun handleError(failure: LsException) {

        if (failure is ServerException && failure.code == ErrorCodes.LOGIN_WRONG_PASSWORD) {
            oldPassInput.error = getString(R.string.login_password_wrong)
        } else {
            showErrorSnackbar(failure)
        }
    }
}
