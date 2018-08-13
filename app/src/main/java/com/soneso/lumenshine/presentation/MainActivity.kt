package com.soneso.lumenshine.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.MenuItem
import com.google.firebase.iid.FirebaseInstanceId
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.accounts.WalletsFragment
import com.soneso.lumenshine.presentation.general.SgActivity
import com.soneso.lumenshine.presentation.general.SgFragment
import com.soneso.lumenshine.presentation.home.HomeFragment
import com.soneso.lumenshine.presentation.settings.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


class MainActivity : SgActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->

            copyFcmIdToClipboard()

            shareFcmId()

            Snackbar.make(view, "Your FCM device id was copied to clipboard!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        collapsing_toolbar.isTitleEnabled = false
        changeTitle(R.string.app_name)

        nav_view.setNavigationItemSelectedListener(this)

        val homeItem = nav_view.menu.getItem(0)
        homeItem.isChecked = true
        onNavigationItemSelected(homeItem)
    }

    private fun shareFcmId() {
        val shareBody = FirebaseInstanceId.getInstance().token
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, null))
    }

    private fun copyFcmIdToClipboard() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("FCM_ID", FirebaseInstanceId.getInstance().token)
        clipboard.primaryClip = clip
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
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

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun replaceFragment(fragment: SgFragment, tag: String) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit()
    }

    private fun changeTitle(titleId: Int) {
        toolbar.title = getString(titleId)
    }

    companion object {

        fun startInstance(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}
