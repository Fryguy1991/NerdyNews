package com.chrisfry.nerdnews.userinterface.fragments


import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.chrisfry.nerdnews.R
import com.chrisfry.nerdnews.business.presenters.NewsPagingPresenter
import com.chrisfry.nerdnews.business.presenters.interfaces.INewsPagingPresenter
import com.chrisfry.nerdnews.userinterface.App
import com.chrisfry.nerdnews.userinterface.adapters.NewsPagerAdapter
import com.chrisfry.nerdnews.userinterface.interfaces.ITabsProvider
import java.lang.Exception

/**
 * Fragment class for displaying news article lists in a view pager
 */
class NewsPagingFragment : Fragment(), NewsPagingPresenter.INewsPagingView, ViewPager.OnPageChangeListener {
    companion object {
        private val TAG = NewsPagingFragment::class.java.name
    }

    // Presenter that provides list of news articles
    private var presenter: INewsPagingPresenter? = null
    // ViewPager elements
    private lateinit var newsListViewPager: ViewPager
    private lateinit var newsPagerAdapter: NewsPagerAdapter
    // Reference responsible for providing tabs to the ViewPager
    private var tabsProvider: ITabsProvider? = null
    // Swipe refresh layout containing ViewPager
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Ensure options menu for fragment will be inflated
        setHasOptionsMenu(true)

        val parentActivity = activity

        if (parentActivity == null || parentActivity !is ITabsProvider) {
            throw Exception("Error invalid activity provided")
        } else {
            // Create presenter and inject news component for NewsAPI elements
            val newPresenter = NewsPagingPresenter.getInstance()
            val presenterComponent = (parentActivity.application as App).newsComponent
            presenterComponent.inject(newPresenter)

            presenter = newPresenter

            tabsProvider = parentActivity
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val parentActivity = activity
        if (parentActivity == null || parentActivity !is AppCompatActivity) {
            throw Exception("Error invalid activity provided")
        } else {
            // Reset toolbar appearance
            parentActivity.supportActionBar?.title = getString(R.string.app_name)
            parentActivity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_pager, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Load menu with refresh icon
        inflater.inflate(R.menu.menu_fragment_news_paging, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_refresh -> {
                // User requested refresh from options menu (toolbar refresh icon)
                presenter?.requestArticleRefresh()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
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

        // When swipe down to refresh is activated request article refresh from presenter
        swipeRefreshLayout.setOnRefreshListener {
            presenter?.requestArticleRefresh()
        }

        // Setup view pager
        val fragManager = childFragmentManager
        if (currentContext != null) {
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

    override fun onDestroyView() {
        presenter?.detach()
        super.onDestroyView()
    }

    override fun onDestroy() {
        tabsProvider = null
        presenter = null
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
        // Not currently handling page selection
    }

    override fun displayRefreshing() {
        swipeRefreshLayout.isRefreshing = true
    }

    override fun refreshingComplete() {
        swipeRefreshLayout.isRefreshing = false
    }
}