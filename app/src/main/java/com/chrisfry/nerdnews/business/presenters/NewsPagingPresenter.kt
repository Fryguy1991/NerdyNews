package com.chrisfry.nerdnews.business.presenters

import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.eventhandling.*
import com.chrisfry.nerdnews.business.eventhandling.events.RefreshCompleteEvent
import com.chrisfry.nerdnews.business.eventhandling.events.RefreshEvent
import com.chrisfry.nerdnews.business.eventhandling.receivers.RefreshCompleteEventReceiver

import com.chrisfry.nerdnews.business.presenters.interfaces.INewsPagingPresenter

import com.chrisfry.nerdnews.userinterface.interfaces.IView
import com.chrisfry.nerdnews.utils.LogUtils
import java.lang.Exception

/**
 * Presenter for displaying a view that displays a paging list for news article types
 */
class NewsPagingPresenter private constructor() : BasePresenter<NewsPagingPresenter.INewsPagingView>(),
    INewsPagingPresenter, RefreshCompleteEventReceiver {
    companion object {
        private val TAG = NewsPagingPresenter::class.java.name

        @Synchronized
        fun getInstance(): NewsPagingPresenter {
            return NewsPagingPresenter()
        }
    }

    // Current type of article being displayed
    private var currentArticleType = ArticleDisplayType.TECH
    // Flags indicating if individual refreshes are in progress (used to determine if full refresh is complete)
    private val refreshInProgressFlagList: MutableList<Boolean> = mutableListOf()

    init {
        // Add a refresh flag for each article type
        for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
            // Initial value is true since we will need to pull articles when app is first entered
            refreshInProgressFlagList.add(true)
        }

        // Register for refresh complete events
        EventHandler.addRefreshCompleteReceiver(this)
    }

    override fun attach(view: INewsPagingView) {
        super.attach(view)

        if (!refreshInProgressFlagList.contains(false)) {
            getView()?.displayRefreshing()
            EventHandler.broadcast(RefreshEvent())
        }

        LogUtils.debug(TAG, "NewsPagingPresenter is attaching to view")
    }

    override fun detach() {
        LogUtils.debug(TAG, "NewsPagingPresenter is detaching from view")

        super.detach()
    }

    override fun movedToPage(pageIndex: Int) {
        if (pageIndex < 0 || pageIndex >= ArticleDisplayType.values().size) {
            throw Exception("$TAG: Invalid position received from onPageSelected")
        } else {
            currentArticleType = ArticleDisplayType.values()[pageIndex]
            LogUtils.debug(TAG, "Moved to $currentArticleType articles")
        }
    }

    override fun requestArticleRefresh() {
        LogUtils.debug(TAG, "View requested article refresh")

        if (!isRefreshInProgress()) {
            getView()?.displayRefreshing()
            EventHandler.broadcast(RefreshEvent())
        }
    }

    override fun onReceive(event: BaseEvent) {
        when(event is RefreshCompleteEvent) {
            true -> {
                handleArticleTypeRefreshCompleteEvent(event.articleDisplayType)
            }
            else -> {
                LogUtils.error(TAG, "Not handling this event here: ${event::class.java.name}")
            }
        }
    }

    private fun handleArticleTypeRefreshCompleteEvent(articleDisplayType: ArticleDisplayType) {
        refreshInProgressFlagList[articleDisplayType.ordinal] = false

        if (!isRefreshInProgress()) {
            getView()?.refreshingComplete()
        }
    }

    private fun isRefreshInProgress(): Boolean {
        return refreshInProgressFlagList.contains(true)
    }

    /**
     * View interface for a view that will display a list of articles
     */
    interface INewsPagingView : IView {

        /**
         * View should be in a "refreshing" state where it will display it is refreshing to the user and handle less input
         */
        fun displayRefreshing()

        /**
         * View should no longer display that it is refreshing
         */
        fun refreshingComplete()
    }
}