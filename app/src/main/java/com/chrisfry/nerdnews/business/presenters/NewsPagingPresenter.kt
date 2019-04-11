package com.chrisfry.nerdnews.business.presenters

import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.enums.NewsApiCountrys
import com.chrisfry.nerdnews.business.enums.NewsApiLanguages
import com.chrisfry.nerdnews.business.eventhandling.*
import com.chrisfry.nerdnews.business.eventhandling.events.ArticleRefreshCompleteEvent
import com.chrisfry.nerdnews.business.eventhandling.events.MoreArticleEvent
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
import kotlin.collections.HashMap

/**
 * Presenter for displaying a view that displays a paging list for news article types.
 * This presenters handles article refresh events that will come form the UI.
 */
class NewsPagingPresenter private constructor() : BasePresenter<NewsPagingPresenter.INewsPagingView>(),
    INewsPagingPresenter, RequestMoreArticleEventReceiver {
    companion object {
        private val TAG = NewsPagingPresenter::class.java.simpleName

        private val GAMING_DOMAINS =
            listOf(
                "ign.com", "polygon.com", "kotaku.com", "gamespot.com", "gamesradar.com", "gamerant.com",
                "nintendolife.com", "pushsquare.com"
            )
        private val GAMING_DOMAINS_EXCLUDE = listOf("mashable.com")

        fun getInstance(): NewsPagingPresenter {
            return NewsPagingPresenter()
        }
    }

    // TODO: Suggest moving NewsService access to model so we don't have to fire events between presenters
    // Service for making calls for article data
    @Inject
    lateinit var newsService: NewsService
    // Instance for model containing article lists to be displayed
    @Inject
    lateinit var articleModelInstance: ArticleListsModel
    // Flags indicating if individual refreshes are in progress (used to determine if full refresh is complete)
    private val refreshInProgressFlagList: MutableList<Boolean> = mutableListOf()

    init {
        // Add presenter to event receiver list (RequestMoreArticleEventReceiver)
        EventHandler.addEventReceiver(this)
    }

    fun initialArticleCheck() {
        // Add a refresh flag for each article type
        for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
            // If model article list is empty mark refresh as in progress
            refreshInProgressFlagList.add(articleModelInstance.getArticleList(articleType).isEmpty())
        }

        if (refreshInProgressFlagList.contains(true)) {
            // A list in our article model needs to be refreshed, so refresh everything
            refreshArticles()
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
        when (event is RequestMoreArticleEvent) {
            true -> {
                requestMoreArticles(event.articleDisplayType)
            }
            else -> {
                LogUtils.error(TAG, "Not handling this event here: ${event::class.java.simpleName}")
            }
        }
    }

    private fun handleArticleTypeRefreshCompleteEvent(articleDisplayType: ArticleDisplayType) {
        refreshInProgressFlagList[articleDisplayType.ordinal] = false

        if (!isRefreshInProgress()) {
            getView()?.displayRefreshing(false)
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

        // Reset page counts in model
        for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
            articleModelInstance.setPageCount(articleType, 0)
        }

        // Request technology articles
        newsService.getTopHeadlines(getCallParams(ArticleDisplayType.TECH))
            .enqueue(ArticleRefreshCallback(ArticleDisplayType.TECH))

        // Request science articles
        newsService.getTopHeadlines(getCallParams(ArticleDisplayType.SCIENCE))
            .enqueue(ArticleRefreshCallback(ArticleDisplayType.SCIENCE))

        // Request gaming articles
        newsService.getEverything(getCallParams(ArticleDisplayType.GAMING))
            .enqueue(ArticleRefreshCallback(ArticleDisplayType.GAMING))
    }

    private fun requestMoreArticles(articleType: ArticleDisplayType) {
        LogUtils.debug(TAG, "Requesting more $articleType articles")

        when (articleType) {
            ArticleDisplayType.TECH ->  {
                // Request more technology articles
                newsService.getTopHeadlines(getCallParams(ArticleDisplayType.TECH))
                    .enqueue(MoreArticleRefreshCallback(ArticleDisplayType.TECH))
            }
            ArticleDisplayType.SCIENCE -> {
                // Request more science articles
                newsService.getTopHeadlines(getCallParams(ArticleDisplayType.SCIENCE))
                    .enqueue(MoreArticleRefreshCallback(ArticleDisplayType.SCIENCE))
            }
            ArticleDisplayType.GAMING -> {
                // Request more gaming articles
                newsService.getEverything(getCallParams(ArticleDisplayType.GAMING))
                    .enqueue(MoreArticleRefreshCallback(ArticleDisplayType.GAMING))
            }
        }
    }

    private fun getCallParams(articleType: ArticleDisplayType): HashMap<String, String> {
        // Get current page type and increment for more article call (THIS IS NOT ZERO BASED)
        val pageCount = articleModelInstance.getPageCount(articleType) + 1

        when (articleType) {
            ArticleDisplayType.TECH -> {
                // Technology article parameters
                val techParams = getDefaultQueryParams()
                techParams[NewsService.KEY_CATEGORY] = NewsService.TECH_CATEGORY
                techParams[NewsService.KEY_PAGE] = pageCount.toString()
                return techParams
            }
            ArticleDisplayType.SCIENCE -> {
                // Science article parameters
                val scienceParams = getDefaultQueryParams()
                scienceParams[NewsService.KEY_CATEGORY] = NewsService.SCIENCE_CATEGORY
                // Calculate page to retrieve (NEED TO ADD ONE, THIS IS NOT ZERO BASED)
                scienceParams[NewsService.KEY_PAGE] = pageCount.toString()
                return scienceParams
            }
            ArticleDisplayType.GAMING -> {
                // Gaming article parameters, gaming does not have a category so we cannot use top headlines.
                // Instead  we'll request all articles from domains that generally provide gaming news
                // (see GAMING_DOMAINS). By default NewsAPI sorts these by published by date
                val gamingParams = HashMap<String, String>()
                gamingParams[NewsService.KEY_LANGUAGE] = NewsApiLanguages.getLanguage(Locale.getDefault().language).code
                gamingParams[NewsService.KEY_DOMAINS] = AppUtils.buildCommaSeparatedString(GAMING_DOMAINS)
                gamingParams[NewsService.KEY_EXCLUDE_DOMAINS] =
                    AppUtils.buildCommaSeparatedString(GAMING_DOMAINS_EXCLUDE)
                gamingParams[NewsService.KEY_PAGE_SIZE] = getPageSize().toString()
                // Calculate page to retrieve (NEED TO ADD ONE, THIS IS NOT ZERO BASED)
                gamingParams[NewsService.KEY_PAGE] = pageCount.toString()
                return gamingParams
            }
        }
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
    inner class ArticleRefreshCallback(private val articleDisplayType: ArticleDisplayType) :
        NewsCallback<ArticleResponse>() {
        override fun onResponse(response: ArticleResponse) {
            LogUtils.debug(TAG, "Successfully retrieved $articleDisplayType articles")
            // Set articles into model
            articleModelInstance.setArticleList(articleDisplayType, response.articles)
            // Store page count into model (first page due to refresh)
            articleModelInstance.setPageCount(articleDisplayType, 1)

            handleArticleTypeRefreshCompleteEvent(articleDisplayType)
        }

        override fun onFailure(error: ResponseError) {
            LogUtils.error(TAG, "Error refreshing $articleDisplayType articles")
            LogUtils.error(TAG, "CODE: ${error.code}\nMESSAGE: ${error.message}")

            // Clear articles and page count in model
            articleModelInstance.setArticleList(articleDisplayType, listOf())
            articleModelInstance.setPageCount(articleDisplayType, 0)
            handleArticleTypeRefreshCompleteEvent(articleDisplayType)
        }
    }

    /**
     * Callback class for pulling more articles from NewsAPI
     *
     * @param articleDisplayType: The article type the callback is receiving
     */
    inner class MoreArticleRefreshCallback(private val articleDisplayType: ArticleDisplayType) :
        NewsCallback<ArticleResponse>() {
        override fun onResponse(response: ArticleResponse) {
            LogUtils.debug(TAG, "Successfully retrieved more $articleDisplayType articles")
            // Set articles into model
            articleModelInstance.addToArticleList(articleDisplayType, response.articles)
            // Store page count into model (old page count + 1)
            articleModelInstance.setPageCount(articleDisplayType, articleModelInstance.getPageCount(articleDisplayType) + 1)

            EventHandler.broadcast(MoreArticleEvent(articleDisplayType))
        }

        override fun onFailure(error: ResponseError) {
            LogUtils.error(TAG, "Error retrieving more $articleDisplayType articles")
            LogUtils.error(TAG, "CODE: ${error.code}\nMESSAGE: ${error.message}")

            // Broadcast that more articles have been retrieved (actual check for this is in ArticleListPresenter)
            EventHandler.broadcast(MoreArticleEvent(articleDisplayType))
        }
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