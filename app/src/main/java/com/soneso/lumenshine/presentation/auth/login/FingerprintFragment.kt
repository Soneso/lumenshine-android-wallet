package com.soneso.lumenshine.presentation.auth.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mtramin.rxfingerprint.RxFingerprint
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.ErrorCodes
import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.presentation.auth.AuthFragment
import com.soneso.lumenshine.presentation.auth.AuthLoggedUserActivity
import com.soneso.lumenshine.util.LsException
import com.soneso.lumenshine.util.Resource
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_fingerprint.*

/**
 * A simple [Fragment] subclass.
 *
 */
class FingerprintFragment : AuthFragment() {

    private var disposable: Disposable? = null
    private lateinit var viewModel: FingerprintViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[FingerprintViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_fingerprint, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        subscribeToLiveData()
    }

    private fun setupListeners() {
        cancelButton.setOnClickListener { authActivity.navigate(R.id.to_pass_screen) }
    }

    private fun subscribeToLiveData() {

        viewModel.liveLogin.observe(this, Observer {
            renderLoginStatus(it ?: return@Observer)
        })
    }

    override fun onResume() {
        super.onResume()

        disposable = RxFingerprint.authenticate(context!!).subscribe {
            if (it.isSuccess) {
                viewModel.login()
            } else {
                handleError(LsException(it.message))
            }
        }
    }

    override fun onPause() {
        disposable?.dispose()
        super.onPause()
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
                val status = resource.success()
                if (status.isSetupCompleted()) {
                    authActivity.goToMain()
                } else {
                    authActivity.goToSetup(status)
                }
            }
        }
    }

    private fun handleError(e: LsException) {

        if (e !is ServerException) {
            showErrorSnackbar(e)
            return
        }
        when (e.code) {
            ErrorCodes.LOGIN_WRONG_PASSWORD, ErrorCodes.LOGIN_INVALID_2FA -> {
                authActivity.navigate(R.id.to_pass_screen)
                (authActivity as AuthLoggedUserActivity).hideFingerprintTab()
            }
        }
    }

    companion object {
        const val TAG = "FingerprintFragment"

        fun newInstance() = FingerprintFragment()
    }
}
