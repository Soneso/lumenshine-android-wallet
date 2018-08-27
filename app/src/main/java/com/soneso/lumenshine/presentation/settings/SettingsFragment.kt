package com.soneso.lumenshine.presentation.settings


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.general.SgFragment
import kotlinx.android.synthetic.main.fragment_settings.*

/**
 * A simple [SgFragment] subclass.
 *
 */
class SettingsFragment : SgFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {

        settings_change_password_setting.setOnClickListener {
            startActivity(Intent(context, ChangePasswordActivity::class.java))
        }
        settings_change_tfa_setting.setOnClickListener {
            startActivity(Intent(context, ChangeTfaActivity::class.java))
        }
    }


    companion object {

        const val TAG = "SettingsFragment"

        fun newInstance() = SettingsFragment()
    }
}
