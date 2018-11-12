package com.soneso.lumenshine.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProviders
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.general.LsActivity
import com.soneso.lumenshine.presentation.general.LsFragment
import com.soneso.lumenshine.presentation.home.HomeFragment
import com.soneso.lumenshine.presentation.settings.FingerPrintSetupActivity
import com.soneso.lumenshine.presentation.settings.SettingsFragment
import com.soneso.lumenshine.presentation.wallets.WalletsFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


class MainActivity : LsActivity(), com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener {

    private var onStopTs = 0L
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[MainViewModel::class.java]
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->

            com.google.android.material.snackbar.Snackbar.make(view, "No action set on this button!", com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        collapsing_toolbar.isTitleEnabled = false
        changeTitle(R.string.app_name)

        drawerView.setNavigationItemSelectedListener(this)

        val homeItem = drawerView.menu.getItem(0)
        homeItem.isChecked = true
        onNavigationItemSelected(homeItem)
        if (intent.hasExtra(EXTRA_FINGERPRINT_SETUP)) {
            if (intent.getBooleanExtra(EXTRA_FINGERPRINT_SETUP, false))
                startActivity(Intent(this, FingerPrintSetupActivity::class.java))
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()

        lockIfThresholdPassed()
    }

    private fun lockIfThresholdPassed() {
        if (onStopTs > 0 && System.currentTimeMillis() - onStopTs > LOCK_THRESHOLD) {
            finishAffinity()
            SplashActivity.startInstance(this)
        }
    }

    override fun onStop() {
        onStopTs = System.currentTimeMillis()
        super.onStop()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.home_item -> {
                fab.show()
                changeTitle(R.string.app_name)
                app_bar_layout.setExpanded(true)
                replaceFragment(HomeFragment.newInstance(), HomeFragment.TAG)
            }
            R.id.nav_wallets -> {
                app_bar_layout.setExpanded(false)
                replaceFragment(WalletsFragment.newInstance(), WalletsFragment.TAG)
            }
            R.id.nav_transactions -> {

            }
            R.id.nav_currencies -> {

            }
            R.id.nav_contacts -> {

            }
            R.id.nav_extras -> {

            }
            R.id.nav_settings -> {
                app_bar_layout.setExpanded(false)
                changeTitle(R.string.settings)
                fab.hide()
                replaceFragment(SettingsFragment.newInstance(), SettingsFragment.TAG)
            }
            R.id.nav_help -> {

            }
            R.id.nav_sign_out -> {

            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragment(fragment: LsFragment, tag: String) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit()
    }

    private fun changeTitle(titleId: Int) {
        toolbar.title = getString(titleId)
    }

    companion object {

        private const val LOCK_THRESHOLD = 10 * 1000L

        fun startInstance(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }

        fun startInstanceWithFingerprintSetup(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(EXTRA_FINGERPRINT_SETUP, true)
            context.startActivity(intent)
        }

        const val EXTRA_FINGERPRINT_SETUP = "fingerpirnt_setup"
    }
}
