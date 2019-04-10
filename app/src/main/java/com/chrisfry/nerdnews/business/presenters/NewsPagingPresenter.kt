package com.chrisfry.nerdnews.business.presenters

import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.enums.NewsApiCountrys
import com.chrisfry.nerdnews.business.enums.NewsApiLanguages
import com.chrisfry.nerdnews.business.eventhandling.*
import com.chrisfry.nerdnews.business.eventhandling.events.ArticleRefreshCompleteEvent
import com.chrisfry.nerdnews.business.eventhandling.events.RequestMoreArticleEvent
import com.chrisfry.nerdnews.business.eventhandling.receivers.RequestMoreArticleEventReceiver
import com.chrisfry.nerdnews.business.network.NewsCallback
import com.chrisfry.nerdnews.business.network.NewsService

import com.chrisfry.nerdnews.business.presenters.interfaces.INewsPagingPresenter
import com.chrisfry.nerdnews.model.ArticleListsModel
import com.chrisfry.nerdnews.model.ArticleResponse
import com.chrisfry.nerdnews.model.ResponseError

import com.chrisfry.nerdnews.userinterface.interfaces.IView
import com.chrisfry.nerdnews.utils.AppUtils
import com.chrisfry.nerdnews.utils.LogUtils
import java.util.*
import javax.inject.Inject

/**
 * Presenter for displaying a view that displays a paging list for news article types.
 * This presenters handles article refresh events that will come form the UI.
 */
class NewsPagingPresenter private constructor() : BasePresenter<NewsPagingPresenter.INewsPagingView>(), INewsPagingPresenter,
    RequestMoreArticleEventReceiver {
    companion object {
        private val TAG = NewsPagingPresenter::class.java.name

        private val GAMING_DOMAINS =
            listOf(
                "ign.com", "polygon.com", "kotaku.com", "gamesspot.com", "gamesradar.com", "gamerant.com",
                "nintendolife.com", "pushsquare.com"
            )
        private val GAMING_DOMAINS_EXCLUDE = listOf("mashable.com")

        fun getInstance(): NewsPagingPresenter {
            return NewsPagingPresenter()
        }
    }

    // Service for making calls for article data
    @Inject
    lateinit var newsService: NewsService
    // Instance for model containing article lists to be displayed
    private val articleModelInstance = ArticleListsModel.getInstance()
    // Flags indicating if individual refreshes are in progress (used to determine if full refresh is complete)
    private val refreshInProgressFlagList: MutableList<Boolean> = mutableListOf()

    init {
        // Add a refresh flag for each article type
        for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
            // If model article list is empty mark refresh as in progress (will refresh articles in attach)
            refreshInProgressFlagList.add(articleModelInstance.getArticleList(articleType).isEmpty())
        }

        // Add presenter to event receiver list (RequestMoreArticleEventReceiver)
        EventHandler.addEventReceiver(this)
    }

    override fun attach(view: INewsPagingView) {
        super.attach(view)
        LogUtils.debug(TAG, "NewsPagingPresenter is attaching to view")

        if (refreshInProgressFlagList.contains(true)) {
            // A list in our article model needs to be refreshed, so refresh everything
            getView()?.displayRefreshing()
            refreshArticles()
        }
    }

    override fun detach() {
        LogUtils.debug(TAG, "NewsPagingPresenter is detaching from view")

        super.detach()
    }

    override fun requestArticleRefresh() {
        LogUtils.debug(TAG, "View requested article refresh")

        if (!isRefreshInProgress()) {
            getView()?.displayRefreshing()
            refreshArticles()
        }
    }

    override fun onReceive(event: BaseEvent) {
        when (event is RequestMoreArticleEvent) {
            true -> {
                // TODO: Request more articles for given article type
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

            // Notify other presenters (articles lists) that the article refresh is complete
            EventHandler.broadcast(ArticleRefreshCompleteEvent())
        }
    }

    private fun isRefreshInProgress(): Boolean {
        return refreshInProgressFlagList.contains(true)
    }

    private fun refreshArticles() {
        LogUtils.debug(TAG, "Refreshing articles")

        // Flag all refreshes as in progress
        for (i in 0 until refreshInProgressFlagList.size) {
            refreshInProgressFlagList[i] = true
        }

        // Request technology articles
        val techParams = getDefaultQueryParams()
        techParams[NewsService.KEY_CATEGORY] = NewsService.TECH_CATEGORY
        newsService.getTopHeadlines(techParams).enqueue(ArticleRefreshCallback(ArticleDisplayType.TECH))

        // Request science articles
        val scienceParams = getDefaultQueryParams()
        scienceParams[NewsService.KEY_CATEGORY] = NewsService.SCIENCE_CATEGORY
        newsService.getTopHeadlines(scienceParams).enqueue(ArticleRefreshCallback(ArticleDisplayType.SCIENCE))

        // Request gaming articles, gaming does not have a category so we cannot use top headlines. Instead
        // we'll request all articles from domains that generally provide gaming news (see GAMING_DOMAINS)
        // By default NewsAPI sorts these by published by date
        val gamingParams = HashMap<String, String>()
        gamingParams[NewsService.KEY_LANGUAGE] = NewsApiLanguages.getLanguage(Locale.getDefault().language).code
        gamingParams[NewsService.KEY_DOMAINS] = AppUtils.buildCommaSeparatedString(GAMING_DOMAINS)
        gamingParams[NewsService.KEY_EXCLUDE_DOMAINS] = AppUtils.buildCommaSeparatedString(GAMING_DOMAINS_EXCLUDE)
        gamingParams[NewsService.KEY_PAGE_SIZE] = getPageSize().toString()
        newsService.getEverything(gamingParams).enqueue(ArticleRefreshCallback(ArticleDisplayType.GAMING))
    }

    private fun getDefaultQueryParams(): HashMap<String, String> {
        val queryParameters = HashMap<String, String>()
        queryParameters[NewsService.KEY_COUNTRY] = NewsApiCountrys.getCountry(Locale.getDefault().country).code
        queryParameters[NewsService.KEY_PAGE_SIZE] = getPageSize().toString()
        return queryParameters
    }

    /**
     * Calculate the page size we want to pull from NewsApi
     */
    private fun getPageSize(): Int {
        // Determine page size so in landscape mode we don't display any empty spaces
        var pageSize = NewsService.DEFAULT_PAGE_SIZE
        val articleRemainder = pageSize % AppConstants.LANDSCAPE_ARTICLE_COLUMN_COUNT
        if (articleRemainder > 0) {
            pageSize += AppConstants.LANDSCAPE_ARTICLE_COLUMN_COUNT - articleRemainder
        }
        return pageSize
    }

    /**
     * Callback class for handling article refresh responses from NewsAPI
     *
     * @param articleDisplayType: The article type the callback is receiving
     */
    inner class ArticleRefreshCallback(private val articleDisplayType: ArticleDisplayType): NewsCallback<ArticleResponse>() {
        override fun onResponse(response: ArticleResponse) {
            LogUtils.debug(TAG, "Successfully retrieved $articleDisplayType articles")
            // Set articles into model
            articleModelInstance.setArticleList(articleDisplayType, response.articles)

            handleArticleTypeRefreshCompleteEvent(articleDisplayType)
        }

        override fun onFailure(error: ResponseError) {
            LogUtils.error(TAG, "Error refreshing $articleDisplayType articles")
            LogUtils.error(TAG, "CODE: ${error.code}\nMESSAGE: ${error.message}")

            // Clear articles in model so user will see empty list and hopefully try again
            articleModelInstance.setArticleList(articleDisplayType, listOf())
            handleArticleTypeRefreshCompleteEvent(articleDisplayType)
        }
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