package com.chrisfry.nerdnews.userinterface.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.chrisfry.nerdnews.userinterface.fragments.NewsPagingFragment
import com.chrisfry.nerdnews.R
import com.chrisfry.nerdnews.business.eventhandling.EventHandler
import com.chrisfry.nerdnews.userinterface.interfaces.ITabsProvider
import com.chrisfry.nerdnews.utils.LogUtils
import com.google.android.material.tabs.TabLayout
import java.lang.Exception

class MainActivity : AppCompatActivity(), ITabsProvider {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    // UI ELEMENTS
    // Fragments
    private val newsPagingFragment = NewsPagingFragment.getInstance()
    // Reference to app toolbar
    private lateinit var toolbar: Toolbar
    // Tabs for displaying article type
    private lateinit var tabLayout: TabLayout
    // Reference to fragment manager
    private lateinit var fragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Set toolbar as app action bar
        toolbar = findViewById(R.id.app_toolbar)
        setSupportActionBar(toolbar)

        tabLayout = findViewById(R.id.tab_layout_article_types)

        // Ensure we have the correct layout before attempting to add fragment
        if (findViewById<View>(R.id.frag_placeholder) == null) {
            LogUtils.error(TAG, "Error, must have inflated the wrong layout")
            val exception = Exception("Error must have inflated the wrong layout")
            throw exception
        } else {
            if (savedInstanceState != null) {
                // Ensure we don't inflate fragments on top of each other
                return
            }

            // Start app on news list fragment
            fragmentManager = supportFragmentManager
            fragmentManager.beginTransaction().add(R.id.frag_placeholder, newsPagingFragment).commit()
        }
    }

    override fun setupTabs(viewPager: ViewPager) {
        LogUtils.debug(TAG, "Setting up tabs with fragment view pager")
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.visibility = View.VISIBLE
    }

    override fun hideTabs() {
        tabLayout.visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            when (item.itemId) {
                android.R.id.home -> {
                    onBackPressed()
                    return true
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        // Ensure we have no event receivers referenced anymore
        EventHandler.clearAllReceivers()
        super.onDestroy()
    }
}