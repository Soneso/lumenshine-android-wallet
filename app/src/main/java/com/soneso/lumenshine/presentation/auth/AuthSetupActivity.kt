package com.soneso.lumenshine.presentation.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.soneso.lumenshine.R
import com.soneso.lumenshine.model.entities.RegistrationStatus
import kotlinx.android.synthetic.main.activity_base_auth.*

class AuthSetupActivity : BaseAuthActivity() {

    override val tabLayoutId: Int
        get() = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDrawer()
        setupHeader()
        val registrationStatus = intent?.getSerializableExtra(EXTRA_REGISTRATION_STATUS) as? RegistrationStatus
                ?: RegistrationStatus()
        renderRegistrationStatus(registrationStatus)
    }


    fun renderRegistrationStatus(status: RegistrationStatus) {

        if (status.tfaConfirmed && status.mailConfirmed && status.mnemonicConfirmed) {
            goToMain()
            return
        }
        when {
            !status.tfaConfirmed -> {
                navigate(R.id.to_confirm_tfa_screen)
            }
            !status.mailConfirmed -> {
                if (navController.currentDestination?.id != R.id.confirm_mail_screen) {
                    navigate(R.id.to_confirm_mail_screen)
                }
            }
            !status.mnemonicConfirmed -> {
                navigate(R.id.to_mnemonic_screen)
            }
//            authViewModel.isFingerprintFlow -> {
//                finishAffinity()
//                MainActivity.startInstanceWithFingerprintSetup(this)
//            }
        }
    }

    private fun setupDrawer() {
        drawerView.inflateMenu(R.menu.drawer_auth_setup)
        val navItemListener = NavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.logout_item -> authViewModel.logout()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            return@OnNavigationItemSelectedListener true
        }
        drawerView.setNavigationItemSelectedListener(navItemListener)
    }

    companion object {
        const val TAG = "AuthSetupActivity"
        private const val EXTRA_REGISTRATION_STATUS = "$TAG.EXTRA_REGISTRATION_STATUS"

        fun startInstance(context: Context, registrationStatus: RegistrationStatus) {
            val intent = Intent(context, AuthSetupActivity::class.java).apply {
                putExtra(EXTRA_REGISTRATION_STATUS, registrationStatus)
            }
            context.startActivity(intent)
        }
    }
}