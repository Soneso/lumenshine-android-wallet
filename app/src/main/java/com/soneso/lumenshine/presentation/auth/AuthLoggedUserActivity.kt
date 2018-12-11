package com.soneso.lumenshine.presentation.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.navigation.NavDestination
import com.mtramin.rxfingerprint.RxFingerprint
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.auth.more.LostCredentialFragment
import com.soneso.lumenshine.presentation.util.showAlert
import kotlinx.android.synthetic.main.tabs_auth_logged_user.*

class AuthLoggedUserActivity : BaseAuthActivity() {

    override val tabLayoutId: Int
        get() = R.layout.tabs_auth_logged_user
    private lateinit var tabClickListener: View.OnClickListener
    private var loginWithTouchConfigured: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        selectMenuItem(R.id.login_item)
        setupTabs()

        loginWithTouchConfigured = intent?.getBooleanExtra(EXTRA_FINGERPRINT, false) ?: false
        if (loginWithTouchConfigured) {
            navigate(R.id.to_fingerprint_screen)
        } else {
            navigate(R.id.to_pass_screen)
        }
    }

    override fun drawerMenu(): Int = R.menu.drawer_auth_logged_user

    override fun onNavItemSelected(item: MenuItem) {
        when (item.itemId) {
            R.id.logout_item -> authViewModel.logout()
            R.id.lostpass_item -> {
                navigate(R.id.to_lost_credential, LostCredentialFragment.argForPassword())
                selectMenuItem(R.id.lostpass_item)
            }
            R.id.home_item -> navigate(R.id.pass_screen)
            R.id.nav_about -> {
            }
            R.id.nav_help -> {
            }
        }
    }

    override fun invalidateCurrentSelection(destination: NavDestination) {
        signOutTab.isSelected = false
        homeTab.isSelected = false
        fingerprintTab.isSelected = false

        when (destination.id) {
            R.id.pass_screen -> {
                homeTab.isSelected = true
                selectMenuItem(R.id.home_item)
            }
            R.id.fingerprint_setup_screen, R.id.fingerprint_screen -> {
                fingerprintTab.isSelected = true
                selectMenuItem(R.id.fingerprit_item)
            }
        }
    }

    fun canLoginWithTouch(): Boolean {
        return loginWithTouchConfigured && fingerprintTab.visibility == View.VISIBLE
    }

    private fun setupTabs() {
        tabClickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.signOutTab -> authViewModel.logout()
                R.id.lostPassTab -> {
                    navigate(R.id.to_lost_credential, LostCredentialFragment.argForPassword())
                    selectMenuItem(R.id.lostpass_item)
                }
                R.id.homeTab -> navigate(R.id.to_pass_screen)
                R.id.fingerprintTab -> {
                    when {
                        loginWithTouchConfigured -> navigate(R.id.to_fingerprint_screen)
                        RxFingerprint.isAvailable(this) -> navigate(R.id.to_fingerprint_setup_screen)
                        else -> showAlert(R.string.error, R.string.no_fingerprint_available)
                    }
                }
            }
        }
        homeTab.setOnClickListener(tabClickListener)
        signOutTab.setOnClickListener(tabClickListener)
        fingerprintTab.setOnClickListener(tabClickListener)

        if (!RxFingerprint.isHardwareDetected(this)) {
            hideFingerprintTab()
        }
    }

    fun hideFingerprintTab() {
        fingerprintTab.visibility = View.GONE
    }

    companion object {
        const val TAG = "AuthLoggedUserActivity"
        private const val EXTRA_FINGERPRINT = "$TAG.EXTRA_FINGERPRINT"

        fun startInstance(context: Context, hasFingerprint: Boolean = false) {
            val intent = Intent(context, AuthLoggedUserActivity::class.java)
            intent.putExtra(EXTRA_FINGERPRINT, hasFingerprint)
            context.startActivity(intent)
        }
    }
}
