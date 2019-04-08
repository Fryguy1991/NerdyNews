package com.chrisfry.nerdnews.userinterface.interfaces

import androidx.viewpager.widget.ViewPager

/**
 * Object which provides tabs for the viewpager in this fragment
 */
interface ITabsProvider {
    /**
     * Instructs the tab provided to setup with the viewpager
     *
     * @param viewPager: ViewPager for tab provider to setup with
     */
    fun setupTabs(viewPager: ViewPager)

    /**
     * Instructs the tab provider to hide its tabs
     */
    fun hideTabs()
}