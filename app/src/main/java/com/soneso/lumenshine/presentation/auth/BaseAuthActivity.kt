package com.soneso.lumenshine.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.soneso.lumenshine.R
import com.soneso.lumenshine.persistence.LsPrefs
import com.soneso.lumenshine.presentation.MainActivity
import com.soneso.lumenshine.presentation.general.LsActivity
import kotlinx.android.synthetic.main.activity_base_auth.*
import kotlinx.android.synthetic.main.layout_auth_activity.*


abstract class BaseAuthActivity : LsActivity() {

    abstract val tabLayoutId: Int
    protected lateinit var navController: NavController
    lateinit var authViewModel: AuthViewModel
        private set

    var view: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_auth)

        setupDrawerToggle()
        setupTabBar()
        setupNavigation()

        authViewModel = ViewModelProviders.of(this, viewModelFactory)[AuthViewModel::class.java]
        subscribeForLiveData()
    }

    private fun setupDrawerToggle() {
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                appBarLayout.translationX = slideOffset * drawerView.width
            }

            override fun onDrawerClosed(drawerView: View) {
                drawerIconView.isSelected = false
            }

            override fun onDrawerOpened(drawerView: View) {
                drawerIconView.isSelected = true
            }
        })
        drawerIconView.setOnClickListener {
            if (it.isSelected) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun setupTabBar() {
        if (tabLayoutId == 0) {
            return
        }
        view = LayoutInflater.from(this).inflate(tabLayoutId, appBarLayout, false)
        appBarLayout.addView(view)
        val set = ConstraintSet()

        val tabs = view
        if (tabs != null) {
            set.clone(appBarLayout)
            set.connect(tabs.id, ConstraintSet.BOTTOM, R.id.horizontal_guideline, ConstraintSet.BOTTOM)
            set.applyTo(appBarLayout)
        }
    }

    private fun setupNavigation() {
        navController = NavHostFragment.findNavController(navHostFragment)
        navController.addOnNavigatedListener { _, _ ->
            val destination = navController.currentDestination ?: return@addOnNavigatedListener
            invalidateCurrentSelection(destination)
        }
    }

    protected open fun invalidateCurrentSelection(destination: NavDestination) {
    }

    fun navigate(@IdRes resId: Int, args: Bundle? = null) {
        navController.navigate(resId, args)
    }

    protected fun selectMenuItem(menuItem: Int) {
        drawerView.menu.findItem(menuItem)?.isChecked = true
    }

    private fun subscribeForLiveData() {

        authViewModel.liveLogout.observe(this, Observer {
            finishAffinity()
            AuthNewUserActivity.startInstance(this)
        })
    }

    fun showLoading(loading: Boolean) {
        if (loading) {
            loadingView.visibility = View.VISIBLE
        } else {
            loadingView.visibility = View.GONE
        }
    }

    fun goToMain() {
        finishAffinity()
        MainActivity.startInstance(this)
    }

    fun goToSetup() {
        finishAffinity()
        AuthSetupActivity.startInstance(this)
    }

    fun setupHeader() {
        set_up_view.visibility = View.VISIBLE
        view?.visibility = View.GONE
        usernameView.visibility = View.VISIBLE
        usernameView.text = LsPrefs.username
    }
}
