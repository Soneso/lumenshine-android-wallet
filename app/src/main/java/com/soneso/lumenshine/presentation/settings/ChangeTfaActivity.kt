package com.soneso.lumenshine.presentation.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.ErrorCodes
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.presentation.MainActivity
import com.soneso.lumenshine.presentation.general.LsActivity
import com.soneso.lumenshine.util.Resource
import kotlinx.android.synthetic.main.activity_change_tfa.*
import kotlinx.android.synthetic.main.view_change_tfa_new_secret.*
import kotlinx.android.synthetic.main.view_change_tfa_password_confirm.*
import kotlinx.android.synthetic.main.view_change_tfa_success.*

class ChangeTfaActivity : LsActivity() {

    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_tfa)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[SettingsViewModel::class.java]
        subscribeForLiveData()
        setupListeners()
    }

    private fun setupListeners() {

        change_tfa_password_confirm.setOnClickListener {
            attemptTfaChange()
        }

        change_tfa_next_button.setOnClickListener {
            attemptTfaChangeConfirm()
        }

        change_tfa_cancel_button.setOnClickListener {
            finish()
        }

        change_tfa_home_button.setOnClickListener {
            finishAffinity()
            MainActivity.startInstance(this)
        }

        change_tfa_settings_button.setOnClickListener {
            finish()
        }

    }

    private fun subscribeForLiveData() {

        viewModel.liveTfaSecret.observe(this, Observer {
            renderTfaChange(it ?: return@Observer)
        })

        viewModel.liveTfaChangeConfirmation.observe(this, Observer {
            renderTfaChangeConfirmation(it ?: return@Observer)
        })

    }

    private fun attemptTfaChange() {
        if (change_tfa_current_pass.hasValidInput()) {
            viewModel.changeTfaSecret(change_tfa_current_pass.trimmedText)
        }
    }

    private fun attemptTfaChangeConfirm() {
        if (tfaInputVoew.text.isNotEmpty()) {
            viewModel.confirmTfaSecretChange(tfaInputVoew.text.toString())
        } else {
            tfaInputVoew.error = getString(R.string.error_field_required)
        }
    }


    private fun renderTfaChange(resource: Resource<String, ServerException>) {

        when (resource.state) {
            Resource.SUCCESS -> {
                change_tfa_password_confirm_view.visibility = View.GONE
                change_tfa_new_secret_view.visibility = View.VISIBLE
                setupToken(resource.success())
            }
            Resource.LOADING -> {

            }
            Resource.FAILURE -> {

                handleError(resource.failure())
            }
        }
    }

    private fun renderTfaChangeConfirmation(resource: Resource<Boolean, ServerException>) {
        when (resource.state) {
            Resource.SUCCESS -> {

                change_tfa_new_secret_view.visibility = View.GONE
                change_tfa_success_view.visibility = View.VISIBLE
            }

            Resource.LOADING -> {
            }

            Resource.FAILURE -> {
                handleError(resource.failure())
            }
        }
    }

    /**
     * handling response errors
     */
    private fun handleError(error: ServerException) {

        when (error.code) {
            ErrorCodes.LOGIN_WRONG_PASSWORD -> {
                change_tfa_current_pass.error = error.displayMessage
            }
            ErrorCodes.LOGIN_INVALID_2FA -> {
                tfaInputVoew.error = error.displayMessage
            }
            else -> {
                showErrorSnackbar(error)
            }
        }
    }

    private fun setupToken(tfaSecret: String) {

//        qr_code_view.post {
//            val params = qr_code_view.layoutParams
//            params.height = qr_code_view.width
//            qr_code_view.requestLayout()
//
//            qr_code_view.setImageBitmap(BitmapFactory.decodeByteArray(tfaSecret.imageData, 0, tfaSecret.imageData.size))
//        }
        token_view.keyListener = null
        token_view.text = tfaSecret
        copy_button.setOnClickListener {

            val clipboard = getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("", tfaSecret)
            clipboard.primaryClip = clip
            showSnackbar(getString(R.string.secret_copied))
        }
    }
}
