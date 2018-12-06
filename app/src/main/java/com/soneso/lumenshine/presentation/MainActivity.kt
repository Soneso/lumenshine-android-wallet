package com.soneso.lumenshine.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.AppBarLayout
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.general.LsActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


class MainActivity : LsActivity(), com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener {

    private var onStopTs = 0L
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        setupNavigation()

        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        collapsingToolbarLayout.isTitleEnabled = false
        changeTitle(R.string.app_name)

        drawerView.setNavigationItemSelectedListener(this)

        val homeItem = drawerView.menu.getItem(0)
        homeItem.isChecked = true
        onNavigationItemSelected(homeItem)
    }

    private fun setupNavigation() {
        navController = NavHostFragment.findNavController(navHostFragment)
        navController.addOnNavigatedListener { _, _ ->
            val destination = navController.currentDestination ?: return@addOnNavigatedListener
            setupDestination(destination)
        }
    }

    private fun setupDestination(destination: NavDestination) {
        when (destination.id) {
            R.id.home_screen -> setAppBarLocked(false)
            else -> setAppBarLocked(true)
        }
    }

    private fun setAppBarLocked(locked: Boolean) {
        appBarLayout.setExpanded(!locked)
        val params = appBarLayout.layoutParams as? CoordinatorLayout.LayoutParams
        val behavior = params?.behavior as? AppBarLayout.Behavior
        behavior?.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout) = !locked
        })
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
            R.id.home_item -> navController.navigate(R.id.to_home_screen)
            R.id.nav_wallets -> navController.navigate(R.id.wallets_screen)
            R.id.nav_transactions -> {
            }
            R.id.nav_currencies -> {
            }
            R.id.nav_contacts -> {
            }
            R.id.nav_extras -> {
            }
            R.id.nav_settings -> navController.navigate(R.id.to_settings_screen)
            R.id.nav_help -> {
            }
            R.id.nav_sign_out -> {
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun changeTitle(titleId: Int) {
        toolbar.title = getString(titleId)
    }

    companion object {

        private const val LOCK_THRESHOLD = 10 * 1000L

        fun startInstance(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}
