package com.soneso.lumenshine.presentation.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.navigation.NavDestination
import com.google.android.material.navigation.NavigationView
import com.mtramin.rxfingerprint.RxFingerprint
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.auth.more.LostCredentialFragment
import com.soneso.lumenshine.presentation.util.showAlert
import kotlinx.android.synthetic.main.activity_base_auth.*
import kotlinx.android.synthetic.main.tabs_auth_logged_user.*

class AuthLoggedUserActivity : BaseAuthActivity() {

    override val tabLayoutId: Int
        get() = R.layout.tabs_auth_logged_user
    private lateinit var tabClickListener: View.OnClickListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDrawer()
        setupTabs()

        val hasFingerprint = intent?.getBooleanExtra(EXTRA_FINGERPRINT, false) ?: false
        if (hasFingerprint) {
//            navigate()
        } else {
            navigate(R.id.to_pass_screen)
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
            R.id.fingerprint_setup_screen -> {
                fingerprintTab.isSelected = true
                selectMenuItem(R.id.fingerprit_item)
            }
        }
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
                    if (RxFingerprint.isAvailable(this)) {
                        navigate(R.id.to_fingerprint_setup_screen)
                    } else {
                        showAlert(R.string.error, R.string.no_fingerprint_available)
                    }
                }
            }
        }
        homeTab.setOnClickListener(tabClickListener)
        signOutTab.setOnClickListener(tabClickListener)
        fingerprintTab.setOnClickListener(tabClickListener)

        if (!RxFingerprint.isHardwareDetected(this)) {
            fingerprintTab.visibility = View.GONE
        }
    }

    private fun setupDrawer() {
        drawerView.inflateMenu(R.menu.drawer_auth_logged_user)
        val navItemListener = NavigationView.OnNavigationItemSelectedListener { item ->
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
            drawerLayout.closeDrawer(GravityCompat.START)
            return@OnNavigationItemSelectedListener true
        }
        selectMenuItem(R.id.login_item)
        drawerView.setNavigationItemSelectedListener(navItemListener)
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
