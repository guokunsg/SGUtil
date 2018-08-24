package com.gk.sgutil

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.MenuItem
import com.gk.sgutil.bus.model.BusStop
import com.gk.sgutil.bus.viewmodel.BusActionController
import com.gk.sgutil.dagger.module.BusViewModule
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : DaggerAppCompatActivity(), BusActionController, NavigationView.OnNavigationItemSelectedListener {

    override fun onViewBusArrivalsOnBusStop(busStop: BusStop) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BusViewModule.provideBusArrivalsFragment(busStop))
                .addToBackStack(null)
                .commit()
    }

    override fun onViewBusRoutesForBusService(busServiceNo: String) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BusViewModule.provideBusRoutesFragment(busServiceNo))
                .addToBackStack(null)
                .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        supportFragmentManager.addOnBackStackChangedListener{ shouldDisplayHomeUp() }
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, BusViewModule.provideBusStopsNearbyFragment())
                    .commit()
        }

        shouldDisplayHomeUp()
        nav_view.setNavigationItemSelectedListener(this)
    }

    // Check and enable/disable home up icon
    private fun shouldDisplayHomeUp(){
        // Whether can go back to previous fragment
        val backable = supportFragmentManager.backStackEntryCount > 0
        val bar = supportActionBar!!
        bar.setDisplayShowHomeEnabled(false)
        bar.setDisplayHomeAsUpEnabled(true)
        toolbar.titleMarginStart = resources.getDimension(R.dimen.toolbar_title_margin_start_no_icon).toInt()
        // Set null to use default back icon or use icon when at root fragment.
        bar.setHomeAsUpIndicator(if (backable) null else getDrawable(R.mipmap.bus))
        drawer_layout.setDrawerLockMode(
                if (backable) DrawerLayout.LOCK_MODE_LOCKED_CLOSED else DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    override fun onSupportNavigateUp(): Boolean {
        supportFragmentManager.popBackStack()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == android.R.id.home) {
            // Handle the click on the icon when at the root fragment
            if (supportFragmentManager.backStackEntryCount == 0) {
                drawer_layout.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        // Clear all the fragments in the back stack
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val fragment : Fragment?
        if (menuItem.itemId == R.id.nav_menu_traffic_images) {
            fragment = BusViewModule.provideTrafficImages()
        } else {
            fragment = BusViewModule.provideBusStopsNearbyFragment()
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        drawer_layout.closeDrawers()
        return true
    }
}
