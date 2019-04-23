package com.chrisfry.nerdnews.business.presenters

import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.eventhandling.*
import com.chrisfry.nerdnews.business.eventhandling.events.ArticleRefreshCompleteEvent
import com.chrisfry.nerdnews.business.eventhandling.receivers.ArticleRefreshCompleteEventReceiver
import com.chrisfry.nerdnews.business.network.INewsApi

import com.chrisfry.nerdnews.business.presenters.interfaces.INewsPagingPresenter
import com.chrisfry.nerdnews.model.IArticleListsModel

import com.chrisfry.nerdnews.userinterface.interfaces.IView
import com.chrisfry.nerdnews.utils.LogUtils
import javax.inject.Inject

/**
 * Presenter for displaying a view that displays a paging list for news article types.
 * This presenters handles article refresh events that will come from the UI.
 */
class NewsPagingPresenter private constructor() : BasePresenter<NewsPagingPresenter.INewsPagingView>(),
    INewsPagingPresenter, ArticleRefreshCompleteEventReceiver {
    companion object {
        private val TAG = NewsPagingPresenter::class.java.simpleName

        fun getInstance(): NewsPagingPresenter {
            return NewsPagingPresenter()
        }
    }

    // Instance for model containing article lists to be displayed
    @Inject
    lateinit var articleModelInstance: IArticleListsModel
    // Instance for news api to make data requests
    @Inject
    lateinit var newsApiInstance: INewsApi
    // Flag indicating if an article refresh is in progress
    private var refreshInProgressFlag = false

    init {
        // TODO: Replace event handler
        // Add presenter to event receiver list (RequestMoreArticleEventReceiver)
        EventHandler.addEventReceiver(this)
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

    override fun requestArticleRefresh() {
        LogUtils.debug(TAG, "View requested article refresh")

        if (!isRefreshInProgress()) {
            getView()?.displayRefreshing(true)
            refreshArticles()
        }
    }

    override fun onReceive(event: BaseEvent) {
        when (event) {
            is ArticleRefreshCompleteEvent -> {
                refreshInProgressFlag = false
                getView()?.displayRefreshing(false)
                getView()?.refreshingComplete()
            }
            else -> {
                LogUtils.error(TAG, "Not handling this event here: ${event::class.java.simpleName}")
            }
        }
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
    }
}