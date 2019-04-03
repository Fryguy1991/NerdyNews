package com.chrisfry.nerdnews.business.presenters

import android.util.Log
import com.chrisfry.nerdnews.business.dagger.components.NewsComponent
import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.enums.NewsApiLanguages
import com.chrisfry.nerdnews.business.network.NewsCallback
import com.chrisfry.nerdnews.business.network.NewsService
import com.chrisfry.nerdnews.business.presenters.interfaces.INewsListPresenter
import com.chrisfry.nerdnews.model.Article
import com.chrisfry.nerdnews.model.ArticleResponse
import com.chrisfry.nerdnews.model.ResponseError
import com.chrisfry.nerdnews.userinterface.interfaces.IView
import java.util.*
import javax.inject.Inject

class NewsListPresenter(component: NewsComponent) : BasePresenter<NewsListPresenter.INewsListView>(), INewsListPresenter {
    companion object {
        private val TAG = this::class.java.name
    }

    // NEWS SERVICE ELEMENTS
    // Service that allows us to send calls to NewsAPI for article
    @Inject
    lateinit var newsService: NewsService
    // Current type of article being displayed
    private var currentArticleType = ArticleDisplayType.TECH

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

        getView()?.displayRefreshing()

        // Request technology articles
        val params = getDefaultQueryParams()
        params[NewsService.KEY_CATEGORY] = NewsService.TECH_CATEGORY
        var call = newsService.getTopHeadlines(params)
        call.enqueue(techRefreshArticleCallback)

        // Request science articles
        params[NewsService.KEY_CATEGORY] = NewsService.SCIENCE_CATEGORY
        call = newsService.getTopHeadlines(params)
        call.enqueue(scienceRefreshArticleCallback)

        // TODO: Request gaming articles
    }

    override fun detach() {
        Log.d(TAG, "News List Presenter is detaching from view")

        super.detach()
    }

    // Callback for refreshing tech articles
    private val techRefreshArticleCallback = object : NewsCallback<ArticleResponse>() {
        override fun onResponse(response: ArticleResponse) {
            Log.d(TAG, "Successfully retrieved TECH articles")
            // Clear old articles
            techArticles.clear()
            // Add newly retrieved articles
            techArticles.addAll(response.articles)

            if (currentArticleType == ArticleDisplayType.TECH) {
                this@NewsListPresenter.getView()?.refreshArticles(techArticles)
            }
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

            if (currentArticleType == ArticleDisplayType.SCIENCE) {
                this@NewsListPresenter.getView()?.refreshArticles(scienceArticles)
            }
        }

        override fun onFailure(error: ResponseError) {
            Log.e(TAG, "Error refreshing SCIENCE articles")
            Log.e(TAG, "CODE: ${error.code}\nMESSAGE: ${error.message}")

            // TODO: Handle this case
        }
    }


    private fun getDefaultQueryParams(): HashMap<String, String> {
        val queryParameters = HashMap<String, String>()
        queryParameters[NewsService.KEY_LANGUAGE] = NewsApiLanguages.getLanguage(Locale.getDefault().language).code
        // TODO: Do we want to limit articles to user's country?
        return queryParameters
    }

    override fun requestArticleType(articleType: ArticleDisplayType) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun requestArticleRefresh() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun requestMoreArticles() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * View interface for a view that will display a list of articles
     */
    interface INewsListView : IView {

        /**
         * Provides the view with a refreshed list of articles
         *
         * @param articles: List of articles to refresh the view with
         */
        fun refreshArticles(articles: List<Article>)

        /**
         * Provides an updated article list (more articles) to display
         *
         * @param articles: List of articles to update the view with
         */
        fun updateArticleList(articles: List<Article>)

        /**
         * View should be in a "refreshing" state where it will display it is refreshing to the user and handle less input
         */
        fun displayRefreshing()

        /**
         * View should indicate that there are no more articles available (reached bottom of possible list)
         */
        fun noMoreArticlesAvailable()
    }
}