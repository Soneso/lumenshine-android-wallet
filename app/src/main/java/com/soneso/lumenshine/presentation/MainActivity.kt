package com.soneso.lumenshine.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.AppBarLayout
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.general.SideMenuActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : SideMenuActivity() {

    private var onStopTs = 0L
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        setupNavigation()
        selectMenuItem(R.id.homeItem)
    }

    override fun drawerMenu(): Int = R.menu.drawer_main

    override fun onNavItemSelected(item: MenuItem) {
        when (item.itemId) {
            R.id.homeItem -> navController.navigate(R.id.to_home_screen)
            R.id.nav_wallets -> navController.navigate(R.id.wallets_screen)
            R.id.nav_transactions -> {
            }
            R.id.nav_contacts -> {
            }
            R.id.nav_settings -> navController.navigate(R.id.to_settings_screen)
            R.id.nav_help -> {
            }
            R.id.nav_sign_out -> {
            }
        }
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

    companion object {

        private const val LOCK_THRESHOLD = 20 * 1000L

        fun startInstance(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}
