package com.chrisfry.nerdnews.business.presenters

import android.annotation.SuppressLint
import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.eventhandling.BaseEvent
import com.chrisfry.nerdnews.business.eventhandling.EventHandler
import com.chrisfry.nerdnews.business.eventhandling.events.ArticleRefreshCompleteEvent
import com.chrisfry.nerdnews.business.eventhandling.events.RequestMoreArticleEvent
import com.chrisfry.nerdnews.business.eventhandling.receivers.ArticleRefreshCompleteEventReceiver
import com.chrisfry.nerdnews.business.presenters.interfaces.IArticleListPresenter
import com.chrisfry.nerdnews.model.*
import com.chrisfry.nerdnews.userinterface.interfaces.IView
import com.chrisfry.nerdnews.utils.LogUtils
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Presenter class for displaying a list of news articles
 *
 * @param articleType: Type of article to be displayed by the presenter instance
 */
class ArticleListPresenter private constructor(private val articleType: ArticleDisplayType) :
    BasePresenter<ArticleListPresenter.IArticleListView>(), IArticleListPresenter, ArticleRefreshCompleteEventReceiver {
    companion object {
        private val TAG = ArticleListPresenter::class.java.name

        fun getInstance(articleType: ArticleDisplayType): ArticleListPresenter {
            return ArticleListPresenter(articleType)
        }
    }

    // Instance of article model
    private val articleModelInstance = ArticleListsModel.getInstance()

    init {
        // Register for refresh events
        EventHandler.addEventReceiver(this)
    }

    override fun attach(view: IArticleListView) {
        super.attach(view)

        LogUtils.debug(TAG, "ArticleListPresenter is attaching to view")
    }

    override fun requestArticles() {
        // Pull articles from model and convert them for display
        val modelArticles = articleModelInstance.getArticleList(articleType)
        if (modelArticles.isEmpty()) {
            getView()?.displayNoArticles()
        } else {
            getView()?.displayArticles(convertArticlesToArticleDisplayModel(modelArticles.toList()))
        }
    }

    override fun onReceive(event: BaseEvent) {
        when (event::class.java) {
            ArticleRefreshCompleteEvent::class.java -> {
                // If view is not null (visible) send updated article list to view
                val modelArticles = articleModelInstance.getArticleList(articleType)
                if (modelArticles.isEmpty()) {
                    getView()?.displayNoArticles()
                } else {
                    getView()?.displayArticles(convertArticlesToArticleDisplayModel(modelArticles))
                }
            }
            else -> {
                LogUtils.error(TAG, "Not handling event here: ${event::class.java.simpleName}")
            }
        }
    }

    override fun detach() {
        LogUtils.debug(TAG, "ArticleListPresenter is detaching from view")

        super.detach()
    }

    override fun requestMoreArticles() {
        EventHandler.broadcast(RequestMoreArticleEvent(articleType))
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

            // TODO: If date format is changed (Locale) when on ArticleItemFragment this value will still be displayed
            // in the old format
            var publishedAt: Date? = null
            try {
                // Suppressed because we are retrieving a UTC time. Lint was warning how to get local time format
                @SuppressLint("SimpleDateFormat")
                val dateFormat = SimpleDateFormat(AppConstants.PUBLISHED_AT_TIME_FORMAT)
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")

                publishedAt = dateFormat.parse(article.publishedAt)
            } catch (exception: ParseException) {
                LogUtils.error(TAG, "Failed to parse published at date")
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