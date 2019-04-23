package com.chrisfry.nerdnews

import android.app.Instrumentation
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.chrisfry.nerdnews.userinterface.activities.MainActivity
import org.junit.Rule
import org.junit.Test

import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.ViewPagerActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.*
import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.userinterface.adapters.holders.ArticleViewHolder
import kotlinx.android.synthetic.main.fragment_news_list.*
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.chrisfry.nerdnews.model.ArticleListsModel
import org.junit.After
import org.junit.Assert


/**
 * Class for testing application with Espresso
 * TODO: These tests are dependent on pulling actual data. They may be very brittle. Future: Make more robust
 */
@LargeTest
class AppTest {
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    // Content descriptions for article list recycler views
    private val recyclerTechCd = "${ArticleDisplayType.TECH}_recycler_view"
    private val recyclerScienceCd = "${ArticleDisplayType.SCIENCE}_recycler_view"
    private val recyclerGamingCd = "${ArticleDisplayType.GAMING}_recycler_view"

    @After
    fun tearDown() {
        // Ensure any pulled article data is erased between tests
        for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
            ArticleListsModel.getInstance().setArticleList(articleType, listOf())
        }
    }

    @Test
    fun launchApp() {
        // Ensure our fragment is displayed
        onView(withId(R.id.frag_placeholder)).check(matches(isDisplayed()))

        // Allow time for article data to populate and display
        Thread.sleep(2000)
    }

    @Test
    fun scrollArticleList() {
        // Ensure our fragment is displayed
        onView(withId(R.id.frag_placeholder)).check(matches(isDisplayed()))

        // Allow time for article data to populate and display
        Thread.sleep(2000)

        // Get reference to recycler view
        val recyclerView = onView(withContentDescription(recyclerTechCd))
        recyclerView.check(matches(isDisplayed()))

        // Scroll to bottom of technology list
        val scrollToIndex = activityRule.activity.recycler_view_news_list.adapter!!.itemCount - 1
        recyclerView.perform(RecyclerViewActions.scrollToPosition<ArticleViewHolder>(scrollToIndex))

        // Allow time for article data to populate and display
        Thread.sleep(2000)

        // Scroll to bottom loads more articles. scrollToIndex should not longer last value
        Assert.assertTrue(scrollToIndex != activityRule.activity.recycler_view_news_list.adapter!!.itemCount - 1)

        // Allow time for article data to populate and display
        Thread.sleep(2000)
    }

    @Test
    fun selectItemList() {
        // Ensure our fragment is displayed
        onView(withId(R.id.frag_placeholder)).check(matches(isDisplayed()))

        // Allow time for article data to populate and display
        Thread.sleep(2000)

        // Get reference to recycler view
        val recyclerView = onView(withContentDescription(recyclerTechCd))
        recyclerView.check(matches(isDisplayed()))

        // Scroll to bottom of technology list
        var scrollToIndex = activityRule.activity.recycler_view_news_list.adapter!!.itemCount - 1
        recyclerView.perform(RecyclerViewActions.scrollToPosition<ArticleViewHolder>(scrollToIndex))

        // Allow time for more article data to populate and display
        Thread.sleep(2000)

        // Scroll to bottom of technology list
        scrollToIndex = activityRule.activity.recycler_view_news_list.adapter!!.itemCount - 1
        recyclerView.perform(RecyclerViewActions.scrollToPosition<ArticleViewHolder>(scrollToIndex))

        // Allow time for more article data to populate and display
        Thread.sleep(2000)

        // Select an item in the list
        recyclerView.perform(RecyclerViewActions.actionOnItemAtPosition<ArticleViewHolder>(0, click()))

        // Allow time for more article data to populate and display
        Thread.sleep(2000)

        // When an article is selected title is guaranteed to display (unless all article data is null)
        onView(withId(R.id.tv_article_item_title_text)).check(matches(isDisplayed()))

        pressBack()

        // Allow time for more article data to populate and display
        Thread.sleep(2000)

        // Should return to paging fragment
        recyclerView.check(matches(isDisplayed()))
    }

    @Test
    fun testViewFullArticleIntent() {
        // Ensure our fragment is displayed
        onView(withId(R.id.frag_placeholder)).check(matches(isDisplayed()))

        // Allow time for article data to populate and display
        Thread.sleep(2000)

        // Get reference to recycler view
        val recyclerView = onView(withContentDescription(recyclerTechCd))
        recyclerView.check(matches(isDisplayed()))

        // Scroll to bottom of technology list
        var scrollToIndex = activityRule.activity.recycler_view_news_list.adapter!!.itemCount - 1
        recyclerView.perform(RecyclerViewActions.scrollToPosition<ArticleViewHolder>(scrollToIndex))

        // Allow time for more article data to populate and display
        Thread.sleep(2000)

        // Scroll to bottom of technology list
        scrollToIndex = activityRule.activity.recycler_view_news_list.adapter!!.itemCount - 1
        recyclerView.perform(RecyclerViewActions.scrollToPosition<ArticleViewHolder>(scrollToIndex))

        // Allow time for more article data to populate and display
        Thread.sleep(2000)

        // Select an item in the list
        recyclerView.perform(RecyclerViewActions.actionOnItemAtPosition<ArticleViewHolder>(0, click()))

        // Allow time for more article data to populate and display
        Thread.sleep(2000)

        // When an article is selected title is guaranteed to display (unless all article data is null)
        onView(withId(R.id.tv_article_item_title_text)).check(matches(isDisplayed()))

        // Test assumes that the given article has a link to the source (will fail otherwise)
        val sourceButton = onView(withId(R.id.btn_go_to_article)).perform(scrollTo())
        sourceButton.check(matches(isDisplayed()))

        // Setup catch for intent to article source, since we're testing with pulled data we won't
        // actually know what URL we may be attempting to access, but we DO know the intent action
        // Also eat the intent so we don't leave the application
        Intents.init()
        val expectedIntent = hasAction(Intent.ACTION_VIEW)
        intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))

        // Click the button to take us to the full article and verify the intent it should fire
        sourceButton.perform(click())
        intended(expectedIntent)
        Intents.release()
    }

    @Test
    fun tabsTest() {
        // Ensure our fragment is displayed
        onView(withId(R.id.frag_placeholder)).check(matches(isDisplayed()))

        // Allow time for article data to populate and display
        Thread.sleep(2000)

        val tabNameTech = activityRule.activity.getString(R.string.tab_tech)
        val tabNameScience = activityRule.activity.getString(R.string.tab_science)
        val tabNameGaming = activityRule.activity.getString(R.string.tab_gaming)

        val techTab = onView(withContentDescription(tabNameTech))
        val scienceTab = onView(withContentDescription(tabNameScience))
        val gamingTab = onView(withContentDescription(tabNameGaming))

        // Technology recycler view should be visible
        onView(withContentDescription(recyclerTechCd)).check(matches(isDisplayed()))

        // Click science tab
        scienceTab.perform(click())
        // Allow time for viewpager to animate
        Thread.sleep(2000)
        // Science recycler view should be visible
        onView(withContentDescription(recyclerScienceCd)).check(matches(isDisplayed()))

        // Click gaming tab
        gamingTab.perform(click())
        // Allow time for viewpager to animate
        Thread.sleep(2000)
        // Gaming recycler view should be visible
        onView(withContentDescription(recyclerGamingCd)).check(matches(isDisplayed()))

        // Click technology tab
        techTab.perform(click())
        // Allow time for viewpager to animate
        Thread.sleep(2000)
        // Technology recycler view should be visible
        onView(withContentDescription(recyclerTechCd)).check(matches(isDisplayed()))

        // Click gaming tab
        gamingTab.perform(click())
        // Allow time for viewpager to animate
        Thread.sleep(2000)
        // Gaming recycler view should be visible
        onView(withContentDescription(recyclerGamingCd)).check(matches(isDisplayed()))

        // Click science tab
        scienceTab.perform(click())
        // Allow time for viewpager to animate
        Thread.sleep(2000)
        // Science recycler view should be visible
        onView(withContentDescription(recyclerScienceCd)).check(matches(isDisplayed()))
    }

    @Test
    fun testViewPager() {
        // Ensure our fragment is displayed
        onView(withId(R.id.frag_placeholder)).check(matches(isDisplayed()))

        // Allow time for article data to populate and display
        Thread.sleep(2000)

        // Technology recycler view should be visible
        onView(withContentDescription(recyclerTechCd)).check(matches(isDisplayed()))

        // Retrieve viewpager and scroll to the right
        val viewPager = onView(withId(R.id.view_pager_news))

        // Scroll one page to the right
        viewPager.perform(ViewPagerActions.scrollRight(true))
        // Allow time for viewpager to animate
        Thread.sleep(2000)
        // Science recycler view should be visible
        onView(withContentDescription(recyclerScienceCd)).check(matches(isDisplayed()))

        // Scroll one page to the right
        viewPager.perform(ViewPagerActions.scrollRight(true))
        // Allow time for viewpager to animate
        Thread.sleep(2000)
        // Gaming recycler view should be visible
        onView(withContentDescription(recyclerGamingCd)).check(matches(isDisplayed()))

        // Scroll one page to the left
        viewPager.perform(ViewPagerActions.scrollLeft(true))
        // Allow time for viewpager to animate
        Thread.sleep(2000)
        // Science recycler view should be visible
        onView(withContentDescription(recyclerScienceCd)).check(matches(isDisplayed()))

        // Scroll one page to the left
        viewPager.perform(ViewPagerActions.scrollLeft(true))
        // Allow time for viewpager to animate
        Thread.sleep(2000)
        // Technology recycler view should be visible
        onView(withContentDescription(recyclerTechCd)).check(matches(isDisplayed()))

        // Attempt to scroll one page to the left (should already be on leftmost page)
        viewPager.perform(ViewPagerActions.scrollLeft(true))
        // Allow time for viewpager to animate
        Thread.sleep(2000)
        // Technology recycler view should be visible
        onView(withContentDescription(recyclerTechCd)).check(matches(isDisplayed()))

        // Scroll one page to the right
        viewPager.perform(ViewPagerActions.scrollRight(true))
        // Allow time for viewpager to animate
        Thread.sleep(2000)
        // Science recycler view should be visible
        onView(withContentDescription(recyclerScienceCd)).check(matches(isDisplayed()))

        // Scroll one page to the right
        viewPager.perform(ViewPagerActions.scrollRight(true))
        // Allow time for viewpager to animate
        Thread.sleep(2000)
        // Gaming recycler view should be visible
        onView(withContentDescription(recyclerGamingCd)).check(matches(isDisplayed()))

        // Attempt to scroll one page to the right (should already be on rightmost page)
        viewPager.perform(ViewPagerActions.scrollRight(true))
        // Allow time for viewpager to animate
        Thread.sleep(2000)
        // Gaming recycler view should be visible
        onView(withContentDescription(recyclerGamingCd)).check(matches(isDisplayed()))
    }

    @Test
    fun testRefresh() {
        // Ensure our fragment is displayed
        onView(withId(R.id.frag_placeholder)).check(matches(isDisplayed()))

        // Allow time for article data to populate and display
        Thread.sleep(2000)

        // Get reference to recycler view
        val recyclerView = onView(withContentDescription(recyclerTechCd))
        recyclerView.check(matches(isDisplayed()))

        val adapter = activityRule.activity.findViewById<RecyclerView>(R.id.recycler_view_news_list).adapter

        // Scroll to bottom of technology list
        val scrollToIndex = activityRule.activity.recycler_view_news_list.adapter!!.itemCount - 1
        recyclerView.perform(RecyclerViewActions.scrollToPosition<ArticleViewHolder>(scrollToIndex))

        // Allow time for article data to populate and display
        Thread.sleep(2000)

        // Scroll back to top
        recyclerView.perform(RecyclerViewActions.scrollToPosition<ArticleViewHolder>(0))
        // Allow time for scroll to complete
        Thread.sleep(2000)

        // Store adapter item count (this should be more than item count after refresh)
        var adapterItemCount = adapter!!.itemCount

        // Click refresh menu item
        val refreshView = onView(withId(R.id.menu_refresh))
        refreshView.check(matches(isDisplayed()))
        refreshView.perform(click())

        // Allow time for article data to populate and display
        Thread.sleep(2000)

        var newAdapterItemCount = adapter!!.itemCount
        // Technology article could should be less than previous count with 2 pages of data
        Assert.assertTrue(adapterItemCount > newAdapterItemCount)

        // Scroll to bottom of technology list
        recyclerView.perform(RecyclerViewActions.scrollToPosition<ArticleViewHolder>(scrollToIndex))

        // Allow time for article data to populate and display
        Thread.sleep(2000)

        // Scroll back to top
        recyclerView.perform(RecyclerViewActions.scrollToPosition<ArticleViewHolder>(0))
        // Allow time for scroll to complete
        Thread.sleep(2000)


        // Store adapter item count (this should be more than item count after refresh)
        val adapterItemCount2 = adapter!!.itemCount
        // Perform a swipe to refresh
        val swipeRefreshLayout = onView(withId(R.id.swipe_refresh_news_pager))
        swipeRefreshLayout.perform(swipeDown())
        // Allow time for refresh to complete
        Thread.sleep(2000)

        val newAdapterItemCount2 = adapter!!.itemCount
        // Technology article count should be less than previous count with 2 pages of data
        Assert.assertTrue(adapterItemCount2 > newAdapterItemCount2)
    }
}