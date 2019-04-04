package com.chrisfry.nerdnews.userinterface.fragments


import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.chrisfry.nerdnews.R
import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.presenters.NewsListPresenter
import com.chrisfry.nerdnews.business.presenters.interfaces.INewsListPresenter
import com.chrisfry.nerdnews.model.Article
import com.chrisfry.nerdnews.userinterface.App
import com.chrisfry.nerdnews.userinterface.adapters.NewsPagerAdapter
import java.lang.Exception

/**
 * Fragment class for displaying news article lists in a view pager
 */
class NewsPagerFragment : Fragment(), NewsListPresenter.INewsListView, ViewPager.OnPageChangeListener {
    companion object {
        private val TAG = this::class.java.name
    }

    // Presenter that provides list of news articles
    private var presenter: INewsListPresenter? = null
    // ViewPager elements
    private lateinit var newsListViewPager: ViewPager
    private lateinit var newsPagerAdapter: NewsPagerAdapter
    // Reference responsible for providing tabs to the ViewPager
    private var tabsProvider: TabsProvider? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val parentActivity = activity

        if (parentActivity == null || parentActivity !is TabsProvider) {
            throw Exception("Error invalid activity provided")
        } else {
            tabsProvider = parentActivity
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val parentActivity = activity
        if (parentActivity != null) {
            // Create presenter and provide it with news component for dagger injection
            val newsComponent = (parentActivity.application as App).newsComponent
            presenter = NewsListPresenter(newsComponent)
        } else {
            Log.e(TAG, "Error: Activity was null in onCreate")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Setup view pager
        newsListViewPager = view.findViewById(R.id.view_pager_news)

        val fragManager = fragmentManager
        val currentContext = context
        if (fragManager != null && currentContext != null) {
            newsPagerAdapter = NewsPagerAdapter(fragManager, currentContext)
            newsListViewPager.adapter = newsPagerAdapter

            newsListViewPager.addOnPageChangeListener(this)

            // Inform Activity tabs to setup with viewpager
            // TODO: These tabs should be in the fragment but due to shadow issues (elevation) tabs are in MainActivity layout
            tabsProvider?.setupTabs(newsListViewPager)

            // Fragment view has been created, attach to presenter
            presenter?.attach(this)
        } else {
            val exception = Exception("$TAG: Error fragment manager or context was null")
            throw exception
        }
    }

    override fun onDestroy() {
        tabsProvider = null

        super.onDestroy()
    }

    // PAGE CHANGE LISTENER METHODS
    override fun onPageScrollStateChanged(state: Int) {
        // Not currently handling page scroll state change
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        // Not currently handling page scrolling
    }

    override fun onPageSelected(position: Int) {
        presenter?.movedToPage(position)
    }

    override fun refreshArticles(articleType: ArticleDisplayType, articles: List<Article>) {
        Log.d(TAG, "TODO: Display refreshed articles")

        newsPagerAdapter.refreshFragment(articleType, articles)
    }

    override fun updateArticleList(articleType: ArticleDisplayType, articles: List<Article>) {
        newsPagerAdapter.updateFragment(articleType, articles)
    }

    override fun displayRefreshing() {
        Log.d(TAG, "TODO: Display view as refreshing")

        tabsProvider?.setTabbingEnabled(false)
    }

    override fun noMoreArticlesAvailable() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Object which provides tabs for the viewpager in this fragment
     */
    interface TabsProvider {
        /**
         * Instructs the tab provided to setup with the viewpager
         *
         * @param viewPager: ViewPager for tab provider to setup with
         */
        fun setupTabs(viewPager: ViewPager)

        /**
         * Instructs the tab provider to enbale/disable the tabs
         *
         * @param enabledFlag: Flag to enable/disable the tabs
         */
        fun setTabbingEnabled(enabledFlag: Boolean)
    }
}
