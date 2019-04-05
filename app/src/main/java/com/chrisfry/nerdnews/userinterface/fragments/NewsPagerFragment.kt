package com.chrisfry.nerdnews.userinterface.fragments


import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.chrisfry.nerdnews.R
import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.presenters.NewsListPresenter
import com.chrisfry.nerdnews.business.presenters.interfaces.INewsListPresenter
import com.chrisfry.nerdnews.model.ArticleDisplayModel
import com.chrisfry.nerdnews.userinterface.App
import com.chrisfry.nerdnews.userinterface.adapters.NewsPagerAdapter
import java.lang.Exception
import javax.inject.Inject

/**
 * Fragment class for displaying news article lists in a view pager
 */
class NewsPagerFragment : Fragment(), NewsListPresenter.INewsListView, ViewPager.OnPageChangeListener {
    companion object {
        private val TAG = NewsPagerFragment::class.java.name
    }

    // Presenter that provides list of news articles
    @Inject
    lateinit var presenter: INewsListPresenter
    // ViewPager elements
    private lateinit var newsListViewPager: ViewPager
    private lateinit var newsPagerAdapter: NewsPagerAdapter
    // Reference responsible for providing tabs to the ViewPager
    private var tabsProvider: TabsProvider? = null
    // Swipe refresh layout containing ViewPager
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val parentActivity = activity

        if (parentActivity == null || parentActivity !is TabsProvider) {
            throw Exception("Error invalid activity provided")
        } else {
            // Inject presenter from presenter component
            val presenterComponent = (parentActivity.application as App).presenterComponent
            presenterComponent.inject(this)

            tabsProvider = parentActivity
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Retrieve UI elements
        newsListViewPager = view.findViewById(R.id.view_pager_news)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_news_pager)

        // Set swipe refresh color
        val currentContext = context
        val colorResoureId: Int
        colorResoureId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && currentContext != null) {
            resources.getColor(R.color.colorAccent, currentContext.theme)
        } else {
            resources.getColor(R.color.colorAccent)
        }
        swipeRefreshLayout.setColorSchemeColors(colorResoureId)
        swipeRefreshLayout.setOnRefreshListener {
            presenter?.requestArticleRefresh()
        }

        // Setup view pager
        val fragManager = fragmentManager
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

        presenter?.detach()

        super.onDestroy()
    }

    // PAGE CHANGE LISTENER METHODS
    override fun onPageScrollStateChanged(state: Int) {
        // Don't allow swipe to refresh if paging
        swipeRefreshLayout.isEnabled = state == ViewPager.SCROLL_STATE_IDLE
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        // Not currently handling page scrolling
    }

    override fun onPageSelected(position: Int) {
        presenter?.movedToPage(position)
    }

    override fun refreshArticles(articleType: ArticleDisplayType, articles: List<ArticleDisplayModel>) {
        newsPagerAdapter.refreshFragment(articleType, articles)
    }

    override fun updateArticleList(articleType: ArticleDisplayType, articles: List<ArticleDisplayModel>) {
        newsPagerAdapter.updateFragment(articleType, articles)
    }

    override fun displayRefreshing() {
        swipeRefreshLayout.isRefreshing = true
    }

    override fun refreshingComplete() {
        swipeRefreshLayout.isRefreshing = false
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
    }
}
