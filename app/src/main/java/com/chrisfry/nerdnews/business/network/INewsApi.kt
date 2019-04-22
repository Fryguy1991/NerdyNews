package com.chrisfry.nerdnews.business.network

import com.chrisfry.nerdnews.business.enums.ArticleDisplayType

/**
 * Interface for object that provides presenters with data from NewsAPI
 */
interface INewsApi {
    /**
     * Request that all article lists are refreshed
     */
    fun requestArticleRefresh()

    /**
     * Request more articles for the specified article type
     *
     * @param articleType: Type of article we want more of
     */
    fun requestMoreArticles(articleType: ArticleDisplayType)
}