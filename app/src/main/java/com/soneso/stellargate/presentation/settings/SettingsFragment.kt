package com.soneso.stellargate.presentation.settings


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.soneso.stellargate.R
import com.soneso.stellargate.presentation.general.SgFragment
import com.soneso.stellargate.presentation.general.SgViewState
import com.soneso.stellargate.presentation.general.State
import kotlinx.android.synthetic.main.fragment_settings.*

/**
 * A simple [SgFragment] subclass.
 *
 */
class SettingsFragment : SgFragment() {

    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[SettingsViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeForLiveData()
        setupListeners()
    }

    private fun setupListeners() {

        change_pass_button.setOnClickListener { attemptPassChange() }
    }

    private fun subscribeForLiveData() {

        viewModel.livePasswordChange.observe(this, Observer {
            renderPassChange(it ?: return@Observer)
        })
    }

    private fun attemptPassChange() {

        if (validPassFields()) {
            viewModel.changePassword(current_pass.trimmedText, new_pass.trimmedText)
        }
    }

    private fun validPassFields() =
            current_pass.hasValidInput()
                    && new_pass.hasValidInput()
                    && new_pass.trimmedText == pass_confirmation.trimmedText

    private fun renderPassChange(viewState: SgViewState<Unit>) {

        when (viewState.state) {

            State.READY -> {

                change_pass_button.visibility = View.VISIBLE
                change_pass_progress.visibility = View.GONE
                showSnackbar("Password successfully changed!")
            }
            State.LOADING -> {

                change_pass_button.visibility = View.INVISIBLE
                change_pass_progress.visibility = View.VISIBLE
            }
            State.ERROR -> {

                change_pass_button.visibility = View.VISIBLE
                change_pass_progress.visibility = View.GONE
                showErrorSnackbar(viewState.error)
            }
        }
    }

    companion object {

        const val TAG = "SettingsFragment"

        fun newInstance() = SettingsFragment()
    }
}