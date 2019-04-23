package com.chrisfry.nerdnews.business.presenters

import android.annotation.SuppressLint
import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.eventhandling.events.RefreshCompleteEvent
import com.chrisfry.nerdnews.business.eventhandling.events.MoreArticleEvent
import com.chrisfry.nerdnews.business.network.INewsApi
import com.chrisfry.nerdnews.business.presenters.interfaces.IArticleListPresenter
import com.chrisfry.nerdnews.model.*
import com.chrisfry.nerdnews.userinterface.interfaces.IView
import com.chrisfry.nerdnews.utils.LogUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Presenter class for displaying a list of news articles
 *
 * @param articleType: Type of article to be displayed by the presenter instance
 */
class ArticleListPresenter(private val articleType: ArticleDisplayType) :
    BasePresenter<ArticleListPresenter.IArticleListView>(), IArticleListPresenter {
    companion object {
        private val TAG = ArticleListPresenter::class.java.simpleName
    }

    // Instance of article model
    @Inject
    lateinit var articleModelInstance: IArticleListsModel
    // Instance of news api to make data requests
    @Inject
    lateinit var newsApiInstance: INewsApi
    // Event bus for receiving events from changed data
    @Inject
    lateinit var eventBus: EventBus

    // Flag to indicate if a request for more articles is in progress
    private var isMoreArticleRequestInProgress = false
    // Cached value of article count (to see if article list has actually changed)
    private var cachedArticleCount = 0

    override fun postDependencyInitiation() {
        // Register for events
        eventBus.register(this)
    }

    override fun attach(view: IArticleListView) {
        super.attach(view)

        LogUtils.debug(TAG, "ArticleListPresenter is attaching to view")
    }

    override fun requestArticles() {
        // Pull articles from model and convert them for display
        val modelArticles = articleModelInstance.getArticleList(articleType)
        // Cache article count
        cachedArticleCount = modelArticles.size
        if (modelArticles.isEmpty()) {
            getView()?.displayNoArticles()
        } else {
            getView()?.displayArticles(convertArticlesToArticleDisplayModel(modelArticles.toList()))
        }
    }

    /**
     * Method for handling RefreshCompleteEvents
     *
     * @param event: Event notifying us that article data has been refreshed
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshEvent(event: RefreshCompleteEvent) {
        // If view is not null (visible) send updated article list to view
        val modelArticles = articleModelInstance.getArticleList(articleType)
        // Cache article count
        cachedArticleCount = modelArticles.size
        if (modelArticles.isEmpty()) {
            getView()?.displayNoArticles()
        } else {
            getView()?.displayArticles(convertArticlesToArticleDisplayModel(modelArticles))
        }

        // Reset flag for more article requests
        isMoreArticleRequestInProgress = false
    }

    /**
     * Method for handling MoreArticleEvents
     *
     * @param event: Event notfying us that we have received more articles. Event object contains article type.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMoreArticleEvent(event: MoreArticleEvent) {
        if (event.articleType == articleType) {
            // If view is not null (visible) send updated article list to view
            val modelArticles = articleModelInstance.getArticleList(articleType)

            if (modelArticles.size == cachedArticleCount) {
                LogUtils.debug(TAG, "No more articles to pull for $articleType category")
                getView()?.noMoreArticlesAvailable()
            } else {
                // Cache article count
                cachedArticleCount = modelArticles.size
                isMoreArticleRequestInProgress = false
                getView()?.displayArticles(convertArticlesToArticleDisplayModel(modelArticles))
            }
        } else {
            LogUtils.debug(TAG, "More article event was not for this presenter $articleType")
        }
    }

    override fun detach() {
        LogUtils.debug(TAG, "ArticleListPresenter is detaching from view")

        super.detach()
    }

    override fun breakDown() {
        eventBus.unregister(this)
    }

    override fun requestMoreArticles() {
        if (!isMoreArticleRequestInProgress) {
            isMoreArticleRequestInProgress = true
            newsApiInstance.requestMoreArticles(articleType)
        }
    }

    /**
     * Converts a list of Article objects into a simpler model for display
     *
     * @param articlesToConvert: List of Article models to convert to ArticleDisplayModel
     * @return: List of ArticleDisplayModel based on provided Article list
     */
    private fun convertArticlesToArticleDisplayModel(articlesToConvert: List<Article>): List<ArticleDisplayModel> {

        return articlesToConvert.map {
            var title = it.title ?: AppConstants.EMPTY_STRING
            var publishedAt = it.publishedAt ?: AppConstants.EMPTY_STRING

            // If the article title is not empty and contains a dash (-), it may have the source
            // name at the end of the title. Attempt to trim it (if source name is not null and not empty).
            if (!it.source.name.isNullOrEmpty() && title.isNotEmpty() && title.contains('-')) {
                title = trimSourceFromArticleTitle(title, it.source.name)
            }

            // If published at is not empty, attempt to parse it to local date format
            if (publishedAt.isNotEmpty()) {
                publishedAt = getLocalPublishedAtString(publishedAt)
            }

            ArticleDisplayModel(
                title,
                it.source.name ?: AppConstants.EMPTY_STRING,
                it.urlToImage ?: AppConstants.EMPTY_STRING,
                it.author ?: AppConstants.EMPTY_STRING,
                it.url ?: AppConstants.EMPTY_STRING,
                it.content ?: AppConstants.EMPTY_STRING,
                publishedAt
            )
        }
    }

    /**
     * Attempts to convert raw published at format (see AppConstants.PUBLISHED_AT_TIME_FORMAT) to local format
     *
     * @param rawPublishedAt: Raw published at string retrieved from NewsAPI
     * @return: Local formatted string of published at time, or empty string if parse fails
     */
    private fun getLocalPublishedAtString(rawPublishedAt: String): String {
        // TODO: If date format is changed (Locale) when on ArticleItemFragment this value will still be displayed
        // in the old format
        var publishedAt: Date? = null

        try {
            // Suppressed because we are retrieving a UTC time. Lint was warning how to get local time format
            @SuppressLint("SimpleDateFormat")
            val dateFormat = SimpleDateFormat(AppConstants.PUBLISHED_AT_TIME_FORMAT)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            publishedAt = dateFormat.parse(rawPublishedAt)
        } catch (exception: ParseException) {
            LogUtils.error(TAG, "Failed to parse published at date")
        }

        // Change published at date to local format
        return if (publishedAt == null) {
            AppConstants.EMPTY_STRING
        } else {
            val dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT)
            dateFormat.timeZone = TimeZone.getDefault()
            dateFormat.format(publishedAt)
        }
    }


    /**
     * Attempts to trim the source from the article title (if it's located at the end of the title)
     *
     * @param title: Article title
     * @param source: Name of the article source
     * @return: Title trimmed of it's source name, or original title if not detectable
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
     * @return: Character index of the dash before a source name
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

    /**
     * View interface for a view that will display a list of articles
     */
    interface IArticleListView : IView {

        /**
         * Provides the view with a refreshed list of articles
         *
         * @param articles: List of articles to refresh the view with
         */
        fun displayArticles(articles: List<ArticleDisplayModel>)

        /**
         * View should indicate that there are no more articles available (reached bottom of possible list)
         */
        fun noMoreArticlesAvailable()

        /**
         * View should indicate that there are no articles to display
         */
        fun displayNoArticles()
    }
}