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
import kotlinx.android.synthetic.main.tabs_auth_new_user.*

/**
 * A login screen that offers login via email/password.
 */
class AuthNewUserActivity : BaseAuthActivity() {

    override val tabLayoutId: Int
        get() = R.layout.tabs_auth_new_user
    private lateinit var tabClickListener: View.OnClickListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDrawer()
        setupTabs()
    }


    override fun invalidateCurrentSelection(destination: NavDestination) {
        loginTab.isSelected = false
        signUpTab.isSelected = false
        moreTab.isSelected = false

        when (destination.id) {
            R.id.login_screen -> {
                loginTab.isSelected = true
                selectMenuItem(R.id.login_item)
            }
            R.id.register_screen -> {
                signUpTab.isSelected = true
                selectMenuItem(R.id.signup_item)
            }
        }
    }

    private fun setupTabs() {
        tabClickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.loginTab -> navigate(R.id.to_login)
                R.id.signUpTab -> navigate(R.id.to_confirm_tfa_screen)
                R.id.moreTab -> MoreDialog.showInstance(supportFragmentManager, tabClickListener)
                R.id.lostPassTab -> {
                    navigate(R.id.to_lost_credential, LostCredentialFragment.argForPassword())
                    selectMenuItem(R.id.lostpass_item)
                }
                R.id.lostTfaTab -> {
                    navigate(R.id.to_lost_credential, LostCredentialFragment.argForTfa())
                    selectMenuItem(R.id.losttfa_item)
                }
            }
        }
        loginTab.setOnClickListener(tabClickListener)
        signUpTab.setOnClickListener(tabClickListener)
        moreTab.setOnClickListener(tabClickListener)
    }

    private fun setupDrawer() {
        drawerView.inflateMenu(R.menu.drawer_auth_new_user)
        val navItemListener = NavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.login_item -> navigate(R.id.to_login)
                R.id.signup_item -> navigate(R.id.to_register)
                R.id.lostpass_item -> {
                    navigate(R.id.to_lost_credential, LostCredentialFragment.argForPassword())
                    selectMenuItem(R.id.lostpass_item)
                }
                R.id.losttfa_item -> {
                    navigate(R.id.to_lost_credential, LostCredentialFragment.argForTfa())
                    selectMenuItem(R.id.losttfa_item)
                }
                R.id.mnemonic_item -> {

                }
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

    companion object {
        const val TAG = "AuthNewUserActivity"

        fun startInstance(context: Context) {
            val intent = Intent(context, AuthNewUserActivity::class.java)
            context.startActivity(intent)
        }
    }
}
