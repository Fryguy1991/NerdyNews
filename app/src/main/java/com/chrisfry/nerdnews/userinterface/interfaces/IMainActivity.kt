package com.chrisfry.nerdnews.userinterface.interfaces

import androidx.viewpager.widget.ViewPager
import com.chrisfry.nerdnews.model.ArticleDisplayModel

/**
 * Interface which allows fragments to talk to the main activity
 */
interface IMainActivity {
    /**
     * Instructs tabs to be setup with the provided viewpager
     *
     * @param viewPager: ViewPager for tab provider to setup with
     */
    fun setupTabs(viewPager: ViewPager)

    /**
     * Instructs the tabs to be hidden
     */
    fun hideTabs()

    /**
     *  Instructs the activity to either show or hide a up navigation arrow
     */
    fun showHomeAsUpEnabled(isEnabled: Boolean)

    /**
     * Instructs the activity to display the given string as its title
     */
    fun setAppTitle(title: String)

    /**
     * Instructs the activity to navigate to an article details view
     */
    fun navigateToArticle(articleToDisplay: ArticleDisplayModel)
}