package com.soneso.lumenshine.presentation.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.navigation.NavDestination
import com.google.android.material.navigation.NavigationView
import com.soneso.lumenshine.R
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
        navigate(R.id.to_pass_screen)
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
            R.id.fingerprint_screen -> {
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
                R.id.fingerprintTab -> navigate(R.id.to_fingerprint_screen)
            }
        }
        homeTab.setOnClickListener(tabClickListener)
        signOutTab.setOnClickListener(tabClickListener)
        fingerprintTab.setOnClickListener(tabClickListener)
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

        fun startInstance(context: Context) {
            val intent = Intent(context, AuthLoggedUserActivity::class.java)
            context.startActivity(intent)
        }
    }
}
