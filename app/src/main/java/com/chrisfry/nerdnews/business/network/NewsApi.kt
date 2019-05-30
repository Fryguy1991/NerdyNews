package com.chrisfry.nerdnews.business.network

import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.enums.NewsApiCountrys
import com.chrisfry.nerdnews.business.enums.NewsApiLanguages
import com.chrisfry.nerdnews.business.events.MoreArticleEvent
import com.chrisfry.nerdnews.business.events.RefreshCompleteEvent
import com.chrisfry.nerdnews.model.*
import com.chrisfry.nerdnews.utils.LogUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.greenrobot.eventbus.EventBus
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Api class for requesting article data from NewsAPI
 *
 * @param eventBus: Instance of event bus for communicating to presenters
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
    // Pulling non-interface as we want ONLY NewsAPI to be able to change refreshInProgress and refreshFailed flags
    private val articleModelInstance = ArticleDataModel.getInstance()
    // Flags indicating if individual refreshes are in progress (used to determine if full refresh is complete)
    private val refreshInProgressFlagList: MutableList<Boolean> = mutableListOf()

    init {
        // Create OkHttpClient to add interceptor that adds headers to all our calls
        val client = OkHttpClient.Builder().addNetworkInterceptor(HeaderInterceptor()).build()
        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(NewsService.NEWS_WEB_API_ENDPOINT)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        service = retrofit.create(NewsService::class.java)

        // Initiate refresh flag list
        for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
            refreshInProgressFlagList.add(false)
        }
    }

    override fun requestArticleRefresh() {
        LogUtils.debug(TAG, "Refreshing articles")

        // Flag refresh as in progress in model
        articleModelInstance.refreshInProgress = true

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
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    verifyArticleRefreshResponse(it!!, ArticleDisplayType.TECH)
                },
                onError = {
                    it.printStackTrace()
                    // TODO: Notify refresh failed
                })

        // Request science articles
        service.getTopHeadlines(getCallParams(ArticleDisplayType.SCIENCE))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    verifyArticleRefreshResponse(it!!, ArticleDisplayType.SCIENCE)
                },
                onError = {
                    it.printStackTrace()
                    // TODO: Notify refresh failed
                })

        // Request gaming articles
        service.getEverything(getCallParams(ArticleDisplayType.GAMING))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    verifyArticleRefreshResponse(it!!, ArticleDisplayType.GAMING)
                },
                onError = {
                    it.printStackTrace()
                    // TODO: Notify refresh failed
                })
    }

    override fun requestMoreArticles(articleType: ArticleDisplayType) {
        LogUtils.debug(TAG, "Requesting more $articleType articles")

        when (articleType) {
            // TODO: Replace with rxjava implementation (see refresh changes)
//            ArticleDisplayType.TECH -> {
//                // Request more technology articles
//                service.getTopHeadlines(getCallParams(ArticleDisplayType.TECH))
//                    .enqueue(MoreArticleRefreshCallback(ArticleDisplayType.TECH))
//            }
//            ArticleDisplayType.SCIENCE -> {
//                // Request more science articles
//                service.getTopHeadlines(getCallParams(ArticleDisplayType.SCIENCE))
//                    .enqueue(MoreArticleRefreshCallback(ArticleDisplayType.SCIENCE))
//            }
//            ArticleDisplayType.GAMING -> {
//                // Request more gaming articles
//                service.getEverything(getCallParams(ArticleDisplayType.GAMING))
//                    .enqueue(MoreArticleRefreshCallback(ArticleDisplayType.GAMING))
//            }
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
            // Flag refresh as complete in model
            articleModelInstance.refreshInProgress = false
            // Notify other presenters (articles lists) that the article refresh is complete
            eventBus.post(RefreshCompleteEvent())

            // Store if refresh failed in the model (if all of our models are empty refresh failed)
            var didRefreshFail = true
            for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
                didRefreshFail = didRefreshFail && articleModelInstance.getArticleList(articleType).isEmpty()
            }
            articleModelInstance.didLastRefreshFail = didRefreshFail
        }
    }

    /**
     * Method for handling article refresh responses (removes any invalid articles, stores articles in model,
     * resets stored page count)
     *
     * @param response: Article response that will be verified
     * @param articleType: The article type of the article response
     */
    private fun verifyArticleRefreshResponse(response: ArticleResponse, articleType: ArticleDisplayType) {
        LogUtils.debug(TAG, "Successfully retrieved $articleType articles")
        // Pull out any empty articles (all null or empty values)
        // Not sure if this case is possible, but there is no documentation in NewsAPI to suggest it is impossible
        val nonNullArticles = response.articles.filter {
            !isArticleEmpty(it)
        }

        // Set articles into model
        articleModelInstance.setArticleList(articleType, nonNullArticles)
        // Store page count into model (first page due to refresh)
        articleModelInstance.setPageCount(articleType, 1)

        handleArticleTypeRefreshCompleteEvent(articleType)
    }


    // TODO: Remove this class when we've replaced it with RxJava calls
    /**
     * Callback class for pulling more articles from NewsAPI
     *
     * @param articleDisplayType: The article type the callback is receiving
     */
    inner class MoreArticleRefreshCallback(private val articleDisplayType: ArticleDisplayType) :
        NewsCallback<ArticleResponse>() {
        override fun onResponse(response: ArticleResponse) {
            LogUtils.debug(TAG, "Successfully retrieved more $articleDisplayType articles")
            // Pull out any empty articles (all null or empty values)
            // Not sure if this case is possible, but there is no documentation in NewsAPI to suggest it is impossible
            val nonNullArticles = response.articles.filter {
                !isArticleEmpty(it)
            }

            // Set articles into model
            articleModelInstance.addToArticleList(articleDisplayType, nonNullArticles)
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

    /**
     * Method to check if an article is empty
     *
     * @param modelToCheck: Model we want to verify is null or not
     * @return: Boolean indicating if the model is empty (true) or not (false)
     */
    private fun isArticleEmpty(modelToCheck: Article): Boolean {
        return modelToCheck.author.isNullOrEmpty() && modelToCheck.source.name.isNullOrEmpty()
                && modelToCheck.source.id.isNullOrEmpty() && modelToCheck.publishedAt.isNullOrEmpty()
                && modelToCheck.title.isNullOrEmpty() && modelToCheck.content.isNullOrEmpty()
                && modelToCheck.description.isNullOrEmpty() && modelToCheck.url.isNullOrEmpty()
                && modelToCheck.urlToImage.isNullOrEmpty()
    }
}