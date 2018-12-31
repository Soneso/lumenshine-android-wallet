package com.soneso.lumenshine.presentation.settings


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.general.LsFragment
import kotlinx.android.synthetic.main.fragment_settings.*

/**
 * A simple [LsFragment] subclass.
 *
 */
class SettingsFragment : LsFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {

        settings_change_password_setting.setOnClickListener { NavHostFragment.findNavController(this).navigate(R.id.to_change_pass_screen) }
        settings_change_tfa_setting.setOnClickListener { NavHostFragment.findNavController(this).navigate(R.id.to_change_tfa_screen) }
    }

    companion object {

        const val TAG = "SettingsFragment"

        fun newInstance() = SettingsFragment()
    }
}
