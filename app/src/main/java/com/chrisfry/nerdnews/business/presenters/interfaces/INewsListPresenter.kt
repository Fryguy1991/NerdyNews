package com.chrisfry.nerdnews.business.presenters.interfaces

import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.presenters.NewsListPresenter

/**
 * Interface for presenter that will provide article data
 */
interface INewsListPresenter : IBasePresenter<NewsListPresenter.INewsListView> {

    /**
     * Attached view has requested to see articles of the provided type
     *
     * @param articleType: Type of article the view is requesting (see ArticleDisplayType for possible values)
     */
    fun requestArticleType(articleType: ArticleDisplayType)

    /**
     * Attached view has requested that the article list be refreshed
     */
    fun requestArticleRefresh()

    /**
     * Attached view has requested that more articles be provided (reached bottom of list)
     */
    fun requestMoreArticles()
}