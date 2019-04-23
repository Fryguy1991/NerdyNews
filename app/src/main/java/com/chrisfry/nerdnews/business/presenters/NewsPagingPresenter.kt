package com.chrisfry.nerdnews.business.presenters

import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.eventhandling.events.RefreshCompleteEvent
import com.chrisfry.nerdnews.business.network.INewsApi

import com.chrisfry.nerdnews.business.presenters.interfaces.INewsPagingPresenter
import com.chrisfry.nerdnews.model.IArticleListsModel

import com.chrisfry.nerdnews.userinterface.interfaces.IView
import com.chrisfry.nerdnews.utils.LogUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

/**
 * Presenter for displaying a view that displays a paging list for news article types.
 * This presenters handles article refresh events that will come from the UI.
 */
class NewsPagingPresenter : BasePresenter<NewsPagingPresenter.INewsPagingView>(), INewsPagingPresenter {
    companion object {
        private val TAG = NewsPagingPresenter::class.java.simpleName
    }

    // Instance for model containing article lists to be displayed
    @Inject
    lateinit var articleModelInstance: IArticleListsModel
    // Instance for news api to make data requests
    @Inject
    lateinit var newsApiInstance: INewsApi
    // Flag indicating if an article refresh is in progress
    private var refreshInProgressFlag = false
    // Event bus for receiving events from changed data
    @Inject
    lateinit var eventBus: EventBus

    override fun postDependencyInitiation() {
        // Register for events
        eventBus.register(this)
    }

    override fun initialArticleCheck() {
        for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
            // If a list in the model model is empty request article refresh
            if (articleModelInstance.getArticleList(articleType).isEmpty()) {
                refreshArticles()
                break
            }
        }
    }

    override fun attach(view: INewsPagingView) {
        super.attach(view)
        LogUtils.debug(TAG, "NewsPagingPresenter is attaching to view")

        getView()?.displayRefreshing(isRefreshInProgress())
    }

    override fun detach() {
        LogUtils.debug(TAG, "NewsPagingPresenter is detaching from view")

        super.detach()
    }

    override fun breakDown() {
        eventBus.unregister(this)
    }


    override fun requestArticleRefresh() {
        LogUtils.debug(TAG, "View requested article refresh")

        if (!isRefreshInProgress()) {
            getView()?.displayRefreshing(true)
            refreshArticles()
        }
    }

    /**
     * Method for handling RefreshCompleteEvents
     *
     * @param event: Event notifying us that article data has been refreshed
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshEvent(event: RefreshCompleteEvent) {
        refreshInProgressFlag = false
        getView()?.displayRefreshing(false)

        // If any of our model lists are not empty consider refresh as "successful"
        for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
            if (articleModelInstance.getArticleList(articleType).isNotEmpty()) {
                getView()?.refreshingComplete()
                return
            }
        }
        // If all our model lists are empty consider our refresh as "failed"
        getView()?.refreshingFailed()
    }

    private fun isRefreshInProgress(): Boolean {
        return refreshInProgressFlag
    }

    private fun refreshArticles() {
        refreshInProgressFlag = true
        newsApiInstance.requestArticleRefresh()
    }

    /**
     * View interface for a view that will a paging view of articles
     */
    interface INewsPagingView : IView {

        /**
         * View should be in our out of a "refreshing" state
         *
         * @param isRefreshing: True if the view should display refreshing else false
         */
        fun displayRefreshing(isRefreshing: Boolean)

        /**
         * View should indicate that an article refresh has been completed
         */
        fun refreshingComplete()

        /**
         * View should indicate that an article refresh has failed
         */
        fun refreshingFailed()
    }
}