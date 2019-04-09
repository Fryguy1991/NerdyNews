package com.chrisfry.nerdnews.business.presenters

import android.annotation.SuppressLint
import android.util.Log
import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.business.dagger.components.NewsComponent
import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.enums.NewsApiCountrys
import com.chrisfry.nerdnews.business.enums.NewsApiLanguages
import com.chrisfry.nerdnews.business.eventhandling.BaseEvent
import com.chrisfry.nerdnews.business.eventhandling.EventHandler
import com.chrisfry.nerdnews.business.eventhandling.events.RefreshCompleteEvent
import com.chrisfry.nerdnews.business.eventhandling.events.RefreshEvent
import com.chrisfry.nerdnews.business.eventhandling.receivers.RefreshEventReceiver
import com.chrisfry.nerdnews.business.network.NewsCallback
import com.chrisfry.nerdnews.business.network.NewsService
import com.chrisfry.nerdnews.business.presenters.interfaces.IArticleListPresenter
import com.chrisfry.nerdnews.model.Article
import com.chrisfry.nerdnews.model.ArticleDisplayModel
import com.chrisfry.nerdnews.model.ArticleResponse
import com.chrisfry.nerdnews.model.ResponseError
import com.chrisfry.nerdnews.userinterface.interfaces.IView
import com.chrisfry.nerdnews.utils.AppUtils
import java.lang.Exception
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Presenter class for displaying a list of news articles
 *
 * @param newsComponent: Component used to inject NewsService
 * @param articleType: Type of article to be displayed by the presenter instace
 */
class ArticleListPresenter private constructor(newsComponent: NewsComponent, private val articleType: ArticleDisplayType) :
    BasePresenter<ArticleListPresenter.IArticleListView>(), IArticleListPresenter,
    RefreshEventReceiver {
    companion object {
        private val TAG = ArticleListPresenter::class.java.name

        private val GAMING_DOMAINS =
            listOf(
                "ign.com", "polygon.com", "kotaku.com", "gamesspot.com", "gamesradar.com", "gamerant.com",
                "nintendolife.com"
            )
        private val GAMING_DOMAINS_EXCLUDE = listOf("mashable.com")

        @Volatile
        private var instanceList: List<ArticleListPresenter>? = null

        /**
         * Instantiates a ArticleListPresenter for each article type in ArticleDisplayType
         *
         * @param newsComponent: Component used for injecting NewsService
         */
        @Synchronized
        fun createPresenters(newsComponent: NewsComponent) {
            if (instanceList == null) {
                synchronized(this) {
                    val presenterList = mutableListOf<ArticleListPresenter>()
                    for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
                        presenterList.add(ArticleListPresenter(newsComponent, articleType))
                    }
                    instanceList = presenterList.toList()
                }
            }
        }

        @Synchronized
        fun getInstance(articleType: ArticleDisplayType): ArticleListPresenter {
            val instances = instanceList
            if (instances == null || articleType.ordinal > instances.size) {
                throw Exception("$TAG: Error article list presenters were not properly created")
            } else {
                return instances[articleType.ordinal]
            }
        }
    }

    // Service that allows us to send calls to NewsAPI for article
    @Inject
    lateinit var newsService: NewsService
    // Flag indicating if an article refresh is in progress
    private var isRefreshInProgress = false
    // List of articles to be displayed by the view
    private val articleList = mutableListOf<Article>()

    init {
        newsComponent.inject(this)

        // Register for refresh events
        EventHandler.addRefreshReceiver(this)
    }

    override fun attach(view: IArticleListView) {
        super.attach(view)

        Log.d(TAG, "ArticleListPresenter is attaching to view")
    }

    override fun requestArticles() {
        // First attachment article list will be empty. Subsequent attachments will just need to reload articles
        if (articleList.isEmpty() && !isRefreshInProgress) {
            refreshArticles()
        } else {
            getView()?.refreshArticles(convertArticlesToArticleDisplayModel(articleList))
        }
    }

    override fun onReceive(event: BaseEvent) {
        when (event::class.java) {
            RefreshEvent::class.java -> {
                requestArticleRefresh()
            }
            else -> {
                Log.e(TAG, "Not handling event here: ${event::class.java.simpleName}")
            }
        }
    }

    private fun requestArticleRefresh() {
        if (!isRefreshInProgress) {
            refreshArticles()
        }
    }

    private fun refreshArticles() {
        Log.d(TAG, "Refreshing $articleType articles")

        // Flag refresh as in progress
        isRefreshInProgress = true

        val serviceCall = when (articleType) {
            ArticleDisplayType.TECH -> {
                // Request technology article refresh
                val params = getDefaultQueryParams()
                params[NewsService.KEY_CATEGORY] = NewsService.TECH_CATEGORY
                newsService.getTopHeadlines(params)
            }
            ArticleDisplayType.SCIENCE -> {
                // Request technology article refresh
                val params = getDefaultQueryParams()
                params[NewsService.KEY_CATEGORY] = NewsService.SCIENCE_CATEGORY
                newsService.getTopHeadlines(params)
            }
            ArticleDisplayType.GAMING -> {
                // Request gaming article refresh, gaming does not have a category so we cannot use top headlines. Instead
                // we'll request all articles from domains that generally provide gaming news (see GAMING_DOMAINS)
                val gamingParams = HashMap<String, String>()
                gamingParams[NewsService.KEY_LANGUAGE] = NewsApiLanguages.getLanguage(Locale.getDefault().language).code
                gamingParams[NewsService.KEY_DOMAINS] = AppUtils.buildCommaSeparatedString(GAMING_DOMAINS)
                gamingParams[NewsService.KEY_EXCLUDE_DOMAINS] = AppUtils.buildCommaSeparatedString(GAMING_DOMAINS_EXCLUDE)
                gamingParams[NewsService.KEY_PAGE_SIZE] = getPageSize().toString()
                newsService.getEverything(gamingParams)
            }
        }

        serviceCall.enqueue(articleRefreshCallback)
    }

    override fun detach() {
        Log.d(TAG, "ArticleListPresenter is detaching from view")

        super.detach()
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

    override fun requestMoreArticles() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
            val articleContent = article.content ?: AppConstants.EMPTY_STRING

            var publishedAt: Date? = null
            try {
                // Suppressed because we are retrieving a UTC time. Lint was warning how to get local time format
                @SuppressLint("SimpleDateFormat")
                val dateFormat = SimpleDateFormat(AppConstants.PUBLISHED_AT_TIME_FORMAT)
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")

                publishedAt = dateFormat.parse(article.publishedAt)
            } catch (exception: ParseException) {
                Log.e(TAG, "Failed to parse published at date")
            }

            // If the article title contains a dash (-) it may have the source
            // name at the end of the title. Attempt to trim it.
            if (sourceName.isNotEmpty() && title.isNotEmpty() && title.contains('-')) {
                title = trimSourceFromArticleTitle(title, sourceName)
            }

            // Change published at date to local format
            val publishedAtString = if (publishedAt == null) {
                AppConstants.EMPTY_STRING
            } else {
                val dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT)
                dateFormat.timeZone = TimeZone.getDefault()
                dateFormat.format(publishedAt)
            }

            articleDisplayModelList.add(
                ArticleDisplayModel(title, sourceName, imageUrl, author, articleUrl, articleContent, publishedAtString)
            )
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
    private val articleRefreshCallback = object : NewsCallback<ArticleResponse>() {
        override fun onResponse(response: ArticleResponse) {
            Log.d(TAG, "Successfully retrieved $articleType articles")
            // Clear old articles
            articleList.clear()
            // Add newly retrieved articles
            articleList.addAll(response.articles)

            isRefreshInProgress = false

            this@ArticleListPresenter.getView()?.refreshArticles(convertArticlesToArticleDisplayModel(articleList))
            EventHandler.broadcast(RefreshCompleteEvent(articleType))
        }

        override fun onFailure(error: ResponseError) {
            Log.e(TAG, "Error refreshing TECH articles")
            Log.e(TAG, "CODE: ${error.code}\nMESSAGE: ${error.message}")

            // TODO: Handle this case
        }
    }

    /**
     * View interface for a view that will display a list of articles
     */
    interface IArticleListView : IView {

        /**
         * Provides the view with a refreshed list of articles
         *
         * @param articles: List of articles to refresh the view with
         */
        fun refreshArticles(articles: List<ArticleDisplayModel>)

        /**
         * Provides an updated article list (more articles) to display
         *
         * @param articles: List of articles to update the view with
         */
        fun updateArticleList(articles: List<ArticleDisplayModel>)

        /**
         * View should indicate that there are no more articles available (reached bottom of possible list)
         */
        fun noMoreArticlesAvailable()
    }
}