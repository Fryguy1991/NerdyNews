package com.chrisfry.nerdnews.business.presenters

import android.annotation.SuppressLint
import android.util.Log
import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.business.dagger.components.NewsComponent
import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.enums.NewsApiCountrys
import com.chrisfry.nerdnews.business.enums.NewsApiLanguages
import com.chrisfry.nerdnews.business.network.NewsCallback
import com.chrisfry.nerdnews.business.network.NewsService
import com.chrisfry.nerdnews.business.presenters.interfaces.INewsListPresenter
import com.chrisfry.nerdnews.model.Article
import com.chrisfry.nerdnews.model.ArticleDisplayModel
import com.chrisfry.nerdnews.model.ArticleResponse
import com.chrisfry.nerdnews.model.ResponseError
import com.chrisfry.nerdnews.userinterface.interfaces.IView
import com.chrisfry.nerdnews.utils.AppUtils
import java.lang.Exception
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class NewsListPresenter private constructor(newsComponent: NewsComponent): BasePresenter<NewsListPresenter.INewsListView>(),
    INewsListPresenter {
    companion object {
        private val TAG = NewsListPresenter::class.java.name

        private val GAMING_DOMAINS = listOf("ign.com", "polygon.com", "kotaku.com", "gamesspot.com", "gamesradar.com", "gamerant.com")
        private val GAMING_DOMAINS_EXCLUDE = listOf("mashable.com")

        @Volatile
        private var instance: NewsListPresenter? = null

        @Synchronized
        fun getInstance(newsComponent: NewsComponent): NewsListPresenter {
            return instance ?: synchronized(this) {
                instance ?: NewsListPresenter(newsComponent)
            }
        }
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
        newsComponent.inject(this)
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
        isGamingRefreshInProgress = true

        getView()?.displayRefreshing()

        // Request technology article refresh
        val params = getDefaultQueryParams()
        params[NewsService.KEY_CATEGORY] = NewsService.TECH_CATEGORY
        val techCall = newsService.getTopHeadlines(params)
        techCall.enqueue(techRefreshArticleCallback)

        // Request science article refresh
        params[NewsService.KEY_CATEGORY] = NewsService.SCIENCE_CATEGORY
        val scienceCall = newsService.getTopHeadlines(params)
        scienceCall.enqueue(scienceRefreshArticleCallback)

        // Request gaming article refresh, gaming does not have a category so we cannot use top headlines. Instead
        // we'll request all articles from domains that generally provide gaming news (see GAMING_DOMAINS)
        val gamingParams = HashMap<String, String>()
        gamingParams[NewsService.KEY_LANGUAGE] = NewsApiLanguages.getLanguage(Locale.getDefault().language).code
        gamingParams[NewsService.KEY_DOMAINS] = AppUtils.buildCommaSeparatedString(GAMING_DOMAINS)
        gamingParams[NewsService.KEY_EXCLUDE_DOMAINS] = AppUtils.buildCommaSeparatedString(GAMING_DOMAINS_EXCLUDE)
        val gamingCall = newsService.getEverything(gamingParams)
        gamingCall.enqueue(gamingRefreshArticleCallback)
    }

    override fun detach() {
        Log.d(TAG, "News List Presenter is detaching from view")

        super.detach()
    }

    private fun getDefaultQueryParams(): HashMap<String, String> {
        val queryParameters = HashMap<String, String>()
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

    /**
     * Converts a list of Article objects into a simpler model for display
     *
     * @param articlesToConvert: List of Article models to convert to ArticleDisplayModel
     * @return: List of ArticleDisplayModel based on provided Article list
     */
    private fun convertArticlesToArticleDisplayModel(articlesToConvert: List<Article>): List<ArticleDisplayModel> {

        val articleDisplayModelList = mutableListOf<ArticleDisplayModel>()

        for (article: Article in articlesToConvert) {
            var title = article.title ?: AppConstants.EMPTY_STRING
            val sourceName = article.source.name ?: AppConstants.EMPTY_STRING
            val imageUrl = article.urlToImage ?: AppConstants.EMPTY_STRING
            val author = article.author ?: AppConstants.EMPTY_STRING
            val articleUrl = article.url ?: AppConstants.EMPTY_STRING

            var publishedAt: Date? = null
            try {
                // Suppressed because we are retrieving a UTC time. Lint was warning how to get local time format
                @SuppressLint("SimpleDateFormat")
                val dateFormat = SimpleDateFormat(AppConstants.PUBLISHED_AT_TIME_FORMAT)

                publishedAt = dateFormat.parse(article.publishedAt)
            } catch (exception: ParseException) {
                Log.e(TAG, "Failed to parse published at date")
            }

            // If the article title contains a dash (-) it may have the source
            // name at the end of the title. Attempt to trim it.
            if (sourceName.isNotEmpty() && title.isNotEmpty() && title.contains('-')) {
                title = trimSourceFromArticleTitle(title, sourceName)
            }

            articleDisplayModelList.add(ArticleDisplayModel(title, sourceName, imageUrl, author, articleUrl, publishedAt))
        }

        return articleDisplayModelList.toList()
    }


    /**
     * Attempts to trim the source from the article title (if it's located at the end of the title)
     *
     * @param title: Article title
     * @param source: Name of the article source
     */
    private fun trimSourceFromArticleTitle(title: String, source: String): String {
        // Check if title ends with direct source name
        var possibleSourceNameNoWhiteSpace = "-" + source.replace(" ", "")
        val titleNoWhiteSpace = title.replace(" ", "")
        if (titleNoWhiteSpace.endsWith(possibleSourceNameNoWhiteSpace, true)) {
            return title.substring(0, getSourceDashIndex(possibleSourceNameNoWhiteSpace, title))
        }

        // Check if title ends with source name minus anything after last period or source name (google.com -> google)
        if (possibleSourceNameNoWhiteSpace.contains(".")) {
            val periodIndex = possibleSourceNameNoWhiteSpace.indexOfLast { it == '.' }
            possibleSourceNameNoWhiteSpace = possibleSourceNameNoWhiteSpace.substring(0, periodIndex)

            if (titleNoWhiteSpace.endsWith(possibleSourceNameNoWhiteSpace, true)) {
                return title.substring(0, getSourceDashIndex(possibleSourceNameNoWhiteSpace, title))
            }
        }

        // See if title ends with source name where dashes are removed
        possibleSourceNameNoWhiteSpace = source.replace(" ", "")
        possibleSourceNameNoWhiteSpace = "-" + possibleSourceNameNoWhiteSpace.replace("-", "")
        if (titleNoWhiteSpace.endsWith(possibleSourceNameNoWhiteSpace)) {
            return title.substring(0, getSourceDashIndex(possibleSourceNameNoWhiteSpace, title))
        }

        // Did not find a source value to trim. Return original title
        return title
    }

    /**
     * Retrieves dash index that comes before a source in an article title
     * Ex: (Article Title - Source Name), (- Source Name) returns 14
     *
     * @param sourceName: Source name that we are checking.
     * @param title: Full title of article
     */
    private fun getSourceDashIndex(sourceName: String, title: String): Int {
        val dashesInSource = sourceName.count { it == '-' }
        val dashesInTitle = title.count { it == '-' }

        val dashesToSkip = dashesInTitle - dashesInSource
        var sourceDashIndex = title.indexOfFirst { it == '-' }

        for (i in 0 until dashesToSkip) {
            sourceDashIndex++
            sourceDashIndex = title.indexOf('-', sourceDashIndex)
        }
        return sourceDashIndex
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

            this@NewsListPresenter.getView()?.refreshArticles(ArticleDisplayType.TECH, convertArticlesToArticleDisplayModel(techArticles))

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

            this@NewsListPresenter.getView()?.refreshArticles(ArticleDisplayType.SCIENCE, convertArticlesToArticleDisplayModel(scienceArticles))

            articleRefreshTypeComplete(ArticleDisplayType.SCIENCE)
        }

        override fun onFailure(error: ResponseError) {
            Log.e(TAG, "Error refreshing SCIENCE articles")
            Log.e(TAG, "CODE: ${error.code}\nMESSAGE: ${error.message}")

            // TODO: Handle this case
        }
    }

    // Callback for refreshing gaming articles
    private val gamingRefreshArticleCallback = object : NewsCallback<ArticleResponse>() {
        override fun onResponse(response: ArticleResponse) {
            Log.d(TAG, "Successfully retrieved GAMING articles")
            // Clear old articles
            gamingArticles.clear()
            // Add newly retrieved articles
            gamingArticles.addAll(response.articles)

            this@NewsListPresenter.getView()?.refreshArticles(ArticleDisplayType.GAMING, convertArticlesToArticleDisplayModel(gamingArticles))

            articleRefreshTypeComplete(ArticleDisplayType.GAMING)
        }

        override fun onFailure(error: ResponseError) {
            Log.e(TAG, "Error refreshing GAMING articles")
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
        fun refreshArticles(articleType: ArticleDisplayType, articles: List<ArticleDisplayModel>)

        /**
         * Provides an updated article list (more articles) to display
         *
         * @param articleType: Type of article that should be updated
         * @param articles: List of articles to update the view with
         */
        fun updateArticleList(articleType: ArticleDisplayType, articles: List<ArticleDisplayModel>)

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