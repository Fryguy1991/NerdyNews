package com.chrisfry.nerdnews.userinterface.fragments


import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.chrisfry.nerdnews.R
import com.chrisfry.nerdnews.business.presenters.NewsPagingPresenter
import com.chrisfry.nerdnews.business.presenters.interfaces.INewsPagingPresenter
import com.chrisfry.nerdnews.userinterface.App
import com.chrisfry.nerdnews.userinterface.adapters.NewsPagerAdapter
import com.chrisfry.nerdnews.userinterface.interfaces.ITabsProvider
import kotlinx.android.synthetic.main.fragment_news_pager.*
import java.lang.Exception

/**
 * Fragment class for displaying news article lists in a view pager
 */
class NewsPagingFragment : Fragment(), NewsPagingPresenter.INewsPagingView, ViewPager.OnPageChangeListener {
    companion object {
        private val TAG = NewsPagingFragment::class.java.simpleName

        /**
         * Method for creating an instance of NewsPagingFragment
         */
        fun getInstance(): NewsPagingFragment {
            return NewsPagingFragment()
        }
    }

    // Presenter that provides list of news articles
    private var presenter: INewsPagingPresenter? = null
    // ViewPager adapter
    private lateinit var newsPagerAdapter: NewsPagerAdapter
    // Reference responsible for providing tabs to the ViewPager
    private var tabsProvider: ITabsProvider? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Ensure options menu for fragment will be inflated
        setHasOptionsMenu(true)

        val parentActivity = activity

        if (parentActivity == null || parentActivity !is ITabsProvider) {
            throw Exception("Error invalid activity provided")
        } else {
            // Create presenter and inject news component for NewsAPI elements
            val newPresenter = NewsPagingPresenter()
            (parentActivity.application as App).appComponent.inject(newPresenter)

            presenter = newPresenter
            presenter?.postDependencyInitiation()
            presenter?.initialArticleCheck()

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
        // Set swipe refresh color
        val currentContext = context
        val colorResoureId: Int
        colorResoureId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && currentContext != null) {
            resources.getColor(R.color.colorAccent, currentContext.theme)
        } else {
            resources.getColor(R.color.colorAccent)
        }
        swipe_refresh_news_pager.setColorSchemeColors(colorResoureId)

        // When swipe down to refresh is activated request article refresh from presenter
        swipe_refresh_news_pager.setOnRefreshListener {
            presenter?.requestArticleRefresh()
        }

        // Setup view pager
        val fragManager = childFragmentManager
        if (currentContext != null) {
            newsPagerAdapter = NewsPagerAdapter(fragManager, currentContext)
            view_pager_news.adapter = newsPagerAdapter

            view_pager_news.addOnPageChangeListener(this)

            // Inform Activity tabs to setup with viewpager
            // TODO: These tabs should be in the fragment but due to shadow issues (elevation) tabs are in MainActivity layout
            tabsProvider?.setupTabs(view_pager_news)

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
        presenter?.breakDown()
        presenter = null
        super.onDestroy()
    }

    // PAGE CHANGE LISTENER METHODS
    override fun onPageScrollStateChanged(state: Int) {
        // Don't allow swipe to refresh if paging
        swipe_refresh_news_pager.isEnabled = state == ViewPager.SCROLL_STATE_IDLE
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        // Not currently handling page scrolling
    }

    override fun onPageSelected(position: Int) {
        // Not currently handling page selection
    }

    override fun displayRefreshing(isRefreshing: Boolean) {
        swipe_refresh_news_pager.isRefreshing = isRefreshing
    }

    override fun refreshingComplete() {
        val currentContext = context
        if (currentContext != null) {
            Toast.makeText(currentContext, R.string.toast_articles_refreshed, Toast.LENGTH_LONG).show()
        }
    }

    override fun refreshingFailed() {
        val currentContext = context
        if (currentContext != null) {
            Toast.makeText(currentContext, R.string.toast_refresh_failed, Toast.LENGTH_LONG).show()
        }
    }
}
