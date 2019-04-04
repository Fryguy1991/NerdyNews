package com.chrisfry.nerdnews.business.presenters

import android.util.Log
import com.chrisfry.nerdnews.business.dagger.components.NewsComponent
import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.enums.NewsApiCountrys
import com.chrisfry.nerdnews.business.enums.NewsApiLanguages
import com.chrisfry.nerdnews.business.network.NewsCallback
import com.chrisfry.nerdnews.business.network.NewsService
import com.chrisfry.nerdnews.business.presenters.interfaces.INewsListPresenter
import com.chrisfry.nerdnews.model.Article
import com.chrisfry.nerdnews.model.ArticleResponse
import com.chrisfry.nerdnews.model.ResponseError
import com.chrisfry.nerdnews.userinterface.interfaces.IView
import java.lang.Exception
import java.util.*
import javax.inject.Inject

class NewsListPresenter(component: NewsComponent) : BasePresenter<NewsListPresenter.INewsListView>(),
    INewsListPresenter {
    companion object {
        private val TAG = NewsListPresenter::class.java.name
    }

    // NEWS SERVICE ELEMENTS
    // Service that allows us to send calls to NewsAPI for article
    @Inject
    lateinit var newsService: NewsService
    // Current type of article being displayed
    private var currentArticleType = ArticleDisplayType.TECH
    // Flags indicating if individual refreshes are complete (used to determine if full refresh is complete)
    private var isTechRefreshInProgress = false
    private var isScienceRefreshInProgress = false
    private var isGamingRefreshInProgress = false

    // References to our 3 different article lists
    private val scienceArticles = mutableListOf<Article>()
    private val techArticles = mutableListOf<Article>()
    private val gamingArticles = mutableListOf<Article>()

    init {
        component.inject(this)
    }

    override fun attach(view: INewsListView) {
        super.attach(view)

        Log.d(TAG, "NewsListPresenter is attaching to view")

        refreshAllArticleTypes()
    }

    private fun refreshAllArticleTypes() {
        Log.d(TAG, "Refreshing all article types")

        // Flag all refreshes as in progress
        isTechRefreshInProgress = true
        isScienceRefreshInProgress = true
        // TODO: uncomment this when gaming articles are implemented
//        isGamingRefreshInProgress = true

        getView()?.displayRefreshing()

        // Request technology article refresh
        val params = getDefaultQueryParams()
        params[NewsService.KEY_CATEGORY] = NewsService.TECH_CATEGORY
        var call = newsService.getTopHeadlines(params)
        call.enqueue(techRefreshArticleCallback)

        // Request science article refresh
        params[NewsService.KEY_CATEGORY] = NewsService.SCIENCE_CATEGORY
        call = newsService.getTopHeadlines(params)
        call.enqueue(scienceRefreshArticleCallback)

        // TODO: Request gaming articles
    }

    override fun detach() {
        Log.d(TAG, "News List Presenter is detaching from view")

        super.detach()
    }

    private fun getDefaultQueryParams(): HashMap<String, String> {
        val queryParameters = HashMap<String, String>()
        queryParameters[NewsService.KEY_LANGUAGE] = NewsApiLanguages.getLanguage(Locale.getDefault().language).code
        queryParameters[NewsService.KEY_COUNTRY] = NewsApiCountrys.getCountry(Locale.getDefault().country).code
        return queryParameters
    }

    override fun movedToPage(pageIndex: Int) {
        if (pageIndex < 0 || pageIndex >= ArticleDisplayType.values().size) {
            throw Exception("$TAG: Invalid position received from onPageSelected")
        } else {
            currentArticleType = ArticleDisplayType.values()[pageIndex]
            Log.d(TAG, "Moved to $currentArticleType articles")
        }
    }

    override fun requestArticleRefresh() {
        Log.d(TAG, "View requested article refresh")

        if (!isRefreshInProgress()) {
            refreshAllArticleTypes()
        }
    }

    override fun requestMoreArticles() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun isRefreshInProgress(): Boolean {
        return isTechRefreshInProgress || isScienceRefreshInProgress || isGamingRefreshInProgress
    }

    private fun articleRefreshTypeComplete(articleType: ArticleDisplayType) {
        when (articleType) {
            ArticleDisplayType.TECH -> isTechRefreshInProgress = false
            ArticleDisplayType.SCIENCE -> isScienceRefreshInProgress = false
            ArticleDisplayType.GAMING -> isGamingRefreshInProgress = false
        }

        if (!isRefreshInProgress()) {
            getView()?.refreshingComplete()
        }
    }

    // CALLBACK OBJECTS
    // Callback for refreshing tech articles
    private val techRefreshArticleCallback = object : NewsCallback<ArticleResponse>() {
        override fun onResponse(response: ArticleResponse) {
            Log.d(TAG, "Successfully retrieved TECH articles")
            // Clear old articles
            techArticles.clear()
            // Add newly retrieved articles
            techArticles.addAll(response.articles)

            this@NewsListPresenter.getView()?.refreshArticles(ArticleDisplayType.TECH, techArticles)

            articleRefreshTypeComplete(ArticleDisplayType.TECH)
        }

        override fun onFailure(error: ResponseError) {
            Log.e(TAG, "Error refreshing TECH articles")
            Log.e(TAG, "CODE: ${error.code}\nMESSAGE: ${error.message}")

            // TODO: Handle this case
        }
    }

    // Callback for refreshing tech articles
    private val scienceRefreshArticleCallback = object : NewsCallback<ArticleResponse>() {
        override fun onResponse(response: ArticleResponse) {
            Log.d(TAG, "Successfully retrieved SCIENCE articles")
            // Clear old articles
            scienceArticles.clear()
            // Add newly retrieved articles
            scienceArticles.addAll(response.articles)

            this@NewsListPresenter.getView()?.refreshArticles(ArticleDisplayType.SCIENCE, scienceArticles)

            articleRefreshTypeComplete(ArticleDisplayType.SCIENCE)
        }

        override fun onFailure(error: ResponseError) {
            Log.e(TAG, "Error refreshing SCIENCE articles")
            Log.e(TAG, "CODE: ${error.code}\nMESSAGE: ${error.message}")

            // TODO: Handle this case
        }
    }

    /**
     * View interface for a view that will display a list of articles
     */
    interface INewsListView : IView {

        /**
         * Provides the view with a refreshed list of articles
         *
         * @param articleType: Type of article that should be refreshed
         * @param articles: List of articles to refresh the view with
         */
        fun refreshArticles(articleType: ArticleDisplayType, articles: List<Article>)

        /**
         * Provides an updated article list (more articles) to display
         *
         * @param articleType: Type of article that should be updated
         * @param articles: List of articles to update the view with
         */
        fun updateArticleList(articleType: ArticleDisplayType, articles: List<Article>)

        /**
         * View should be in a "refreshing" state where it will display it is refreshing to the user and handle less input
         */
        fun displayRefreshing()

        /**
         * View should no longer display that it is refreshing
         */
        fun refreshingComplete()

        /**
         * View should indicate that there are no more articles available (reached bottom of possible list)
         */
        fun noMoreArticlesAvailable()
    }
}