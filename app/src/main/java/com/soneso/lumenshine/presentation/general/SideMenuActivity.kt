package com.soneso.lumenshine.presentation.general

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.soneso.lumenshine.R
import kotlinx.android.synthetic.main.activity_side_menu.*

abstract class SideMenuActivity : LsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_side_menu)

        setupDrawer()
    }

    override fun setContentView(layoutResID: Int) {
        val view = LayoutInflater.from(this).inflate(layoutResID, drawerLayout, false)
        drawerLayout.addView(view, 0)
        setupDrawerToggle(view)
    }

    private fun setupDrawerToggle(content: View) {
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                content.translationX = slideOffset * drawerView.width
            }

            override fun onDrawerClosed(drawerView: View) {
//                drawerIconView.isSelected = false
            }

            override fun onDrawerOpened(drawerView: View) {
//                drawerIconView.isSelected = true
            }
        })
//        drawerIconView.setOnClickListener {
//            if (it.isSelected) {
//                drawerLayout.closeDrawer(GravityCompat.START)
//            } else {
//                drawerLayout.openDrawer(GravityCompat.START)
//            }
//        }
    }

    private fun setupDrawer() {
        drawerView.inflateMenu(drawerMenu())
        val navItemListener = NavigationView.OnNavigationItemSelectedListener { item ->
            onNavItemSelected(item)
            drawerLayout.closeDrawer(GravityCompat.START)
            return@OnNavigationItemSelectedListener true
        }
        drawerView.setNavigationItemSelectedListener(navItemListener)
    }

    protected fun selectMenuItem(menuItem: Int) {
        drawerView.menu.findItem(menuItem)?.isChecked = true
    }

    @MenuRes
    abstract fun drawerMenu(): Int

    abstract fun onNavItemSelected(item: MenuItem)

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
