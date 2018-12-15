package com.soneso.lumenshine.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.soneso.lumenshine.R
import com.soneso.lumenshine.model.entities.RegistrationStatus
import com.soneso.lumenshine.presentation.MainActivity
import com.soneso.lumenshine.presentation.general.SideMenuActivity
import kotlinx.android.synthetic.main.activity_base_auth.*


abstract class BaseAuthActivity : SideMenuActivity() {

    abstract val tabLayoutId: Int
    protected lateinit var navController: NavController
    lateinit var authViewModel: AuthViewModel
        private set

    var view: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_auth)

        setupTabBar()
        setupNavigation()

        authViewModel = ViewModelProviders.of(this, viewModelFactory)[AuthViewModel::class.java]
        subscribeForLiveData()
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
        navController.addOnDestinationChangedListener { _, destination, args ->
            invalidateCurrentSelection(destination)
        }
    }

    protected open fun invalidateCurrentSelection(destination: NavDestination) {
    }

    fun navigate(@IdRes resId: Int, args: Bundle? = null) {
        navController.navigate(resId, args)
    }

    private fun subscribeForLiveData() {

        authViewModel.liveLogout.observe(this, Observer {
            finishAffinity()
            AuthNewUserActivity.startInstance(this)
        })
    }

    fun goToMain() {
        finishAffinity()
        MainActivity.startInstance(this)
    }

    fun goToSetup(registrationStatus: RegistrationStatus = RegistrationStatus()) {
        finishAffinity()
        AuthSetupActivity.startInstance(this, registrationStatus)
    }

    fun setupHeader() {
        set_up_view.visibility = View.VISIBLE
        view?.visibility = View.GONE
        usernameView.visibility = View.VISIBLE
    }
}
