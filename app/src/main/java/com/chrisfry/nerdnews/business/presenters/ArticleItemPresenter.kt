package com.chrisfry.nerdnews.business.presenters

import com.chrisfry.nerdnews.business.presenters.interfaces.IArticleItemPresenter
import com.chrisfry.nerdnews.model.ArticleDisplayModel
import com.chrisfry.nerdnews.userinterface.interfaces.IView
import com.chrisfry.nerdnews.utils.LogUtils

/**
 * Presenter for displaying one article to a view
 *
 * @param articleDisplayModel: Article model that should be displayed in the item view
 */
class ArticleItemPresenter (private val articleDisplayModel: ArticleDisplayModel?) : BasePresenter<ArticleItemPresenter.IArticleItemView>(),
    IArticleItemPresenter {
    companion object {
        private val TAG = ArticleItemPresenter::class.java.simpleName
    }

    override fun attach(view: IArticleItemView) {
        super.attach(view)

        LogUtils.debug(TAG, "ArticleItemPresenter is attaching to view")

        val articleData = articleDisplayModel

        if (articleData == null) {
            LogUtils.error(TAG, "Article is null, closing item view")
            getView()?.closeView()
        } else {
            LogUtils.debug(TAG, "Injecting article data into view")
            getView()?.displaySourceName(articleData.sourceName)
            getView()?.displayTitle(articleData.title)
            getView()?.displayImage(articleData.imageUrl)
            getView()?.displayAuthor(articleData.author)
            getView()?.displayPublishedAt(articleData.publishedAt)
            getView()?.displayContent(articleData.articleContent)
            getView()?.displayLinkToArticle(articleData.articleUrl)
        }
    }

    override fun detach() {
        LogUtils.debug(TAG, "ArticleItemPresenter is detaching from view")

        super.detach()
    }

    override fun goToArticleClicked() {
        val article = articleDisplayModel
        if (article != null) {
            getView()?.navigateToArticleSource(article.articleUrl)
        }
    }

    /**
     * View interface for displaying a single article
     */
    interface IArticleItemView : IView {

        /**
         * Instruct view to display the source name
         *
         * @param sourceName: Name of the article source
         */
        fun displaySourceName(sourceName: String)

        /**
         * Instruct view to display the title
         *
         * @param title: Title of the article
         */
        fun displayTitle(title: String)

        /**
         * Instruct view to display the article image
         *
         * @param imageUrl: URL of the article image
         */
        fun displayImage(imageUrl: String)

        /**
         * Instruct view to display the author
         *
         * @param author: Author of the article
         */
        fun displayAuthor(author: String)

        /**
         * Instruct view to display the published at timestamp
         *
         * @param publishedAt: Timestamp of the article
         */
        fun displayPublishedAt(publishedAt: String)

        /**
         * Instruct view to display the article content
         *
         * @param content: Content of the article
         */
        fun displayContent(content: String)

        /**
         * Instruct view to display a link to the article URL
         *
         * @param articleUrl: URL of the article
         */
        fun displayLinkToArticle(articleUrl: String)

        /**
         * Instruct view that it needs to close
         */
        fun closeView()

        /**
         * Instruct the view to navigate to the article source URL
         *
         * @param articleUrl: URL of the source of the article
         */
        fun navigateToArticleSource(articleUrl: String)
    }
}