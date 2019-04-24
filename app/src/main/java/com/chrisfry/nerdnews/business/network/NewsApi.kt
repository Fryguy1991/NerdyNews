package com.chrisfry.nerdnews.business.network

import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.enums.NewsApiCountrys
import com.chrisfry.nerdnews.business.enums.NewsApiLanguages
import com.chrisfry.nerdnews.business.events.RefreshCompleteEvent
import com.chrisfry.nerdnews.business.events.MoreArticleEvent
import com.chrisfry.nerdnews.model.ArticleListsModel
import com.chrisfry.nerdnews.model.ArticleResponse
import com.chrisfry.nerdnews.model.ResponseError
import com.chrisfry.nerdnews.utils.LogUtils
import okhttp3.OkHttpClient
import org.greenrobot.eventbus.EventBus
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Api class for requesting article data from NewsAPI
 */
@Singleton
class NewsApi @Inject constructor(private val eventBus: EventBus) : INewsApi {
    companion object {
        private val TAG = NewsApi::class.java.simpleName

        private val GAMING_DOMAINS =
            listOf(
                "ign.com", "polygon.com", "kotaku.com", "gamespot.com", "gamesradar.com", "gamerant.com",
                "nintendolife.com", "pushsquare.com"
            )
        private val GAMING_DOMAINS_EXCLUDE = listOf("mashable.com")
    }

    // Instance of service class for retrieving article data
    private val service: NewsService
    // Instance of model class for storing article data
    private val articleModelInstance = ArticleListsModel.getInstance()
    // Flags indicating if individual refreshes are in progress (used to determine if full refresh is complete)
    private val refreshInProgressFlagList: MutableList<Boolean> = mutableListOf()

    init {
        // Create OkHttpClient to add interceptor that adds headers to all our calls
        val client = OkHttpClient.Builder().addNetworkInterceptor(HeaderInterceptor()).build()
        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(NewsService.NEWS_WEB_API_ENDPOINT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(NewsService::class.java)

        // Initiate refresh flag list
        for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
            refreshInProgressFlagList.add(false)
        }
    }

    override fun requestArticleRefresh() {
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
        service.getTopHeadlines(getCallParams(ArticleDisplayType.TECH))
            .enqueue(ArticleRefreshCallback(ArticleDisplayType.TECH))

        // Request science articles
        service.getTopHeadlines(getCallParams(ArticleDisplayType.SCIENCE))
            .enqueue(ArticleRefreshCallback(ArticleDisplayType.SCIENCE))

        // Request gaming articles
        service.getEverything(getCallParams(ArticleDisplayType.GAMING))
            .enqueue(ArticleRefreshCallback(ArticleDisplayType.GAMING))
    }

    override fun requestMoreArticles(articleType: ArticleDisplayType) {
        LogUtils.debug(TAG, "Requesting more $articleType articles")

        when (articleType) {
            ArticleDisplayType.TECH -> {
                // Request more technology articles
                service.getTopHeadlines(getCallParams(ArticleDisplayType.TECH))
                    .enqueue(MoreArticleRefreshCallback(ArticleDisplayType.TECH))
            }
            ArticleDisplayType.SCIENCE -> {
                // Request more science articles
                service.getTopHeadlines(getCallParams(ArticleDisplayType.SCIENCE))
                    .enqueue(MoreArticleRefreshCallback(ArticleDisplayType.SCIENCE))
            }
            ArticleDisplayType.GAMING -> {
                // Request more gaming articles
                service.getEverything(getCallParams(ArticleDisplayType.GAMING))
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
                gamingParams[NewsService.KEY_DOMAINS] = GAMING_DOMAINS.joinToString(separator = ",")
                gamingParams[NewsService.KEY_EXCLUDE_DOMAINS] = GAMING_DOMAINS_EXCLUDE.joinToString(separator = ",")
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

    private fun handleArticleTypeRefreshCompleteEvent(articleDisplayType: ArticleDisplayType) {
        refreshInProgressFlagList[articleDisplayType.ordinal] = false

        if (!refreshInProgressFlagList.contains(true)) {
            // Notify other presenters (articles lists) that the article refresh is complete
            eventBus.post(RefreshCompleteEvent())
        }
    }

    // CALLBACK OBJECTS
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

            // Broadcast that more articles have been retrieved
            eventBus.post(MoreArticleEvent(articleDisplayType))
        }

        override fun onFailure(error: ResponseError) {
            LogUtils.error(TAG, "Error retrieving more $articleDisplayType articles")
            LogUtils.error(TAG, "CODE: ${error.code}\nMESSAGE: ${error.message}")

            // Broadcast that more articles have been retrieved (actual check for this is in ArticleListPresenter)
            eventBus.post(MoreArticleEvent(articleDisplayType))
        }
    }
}