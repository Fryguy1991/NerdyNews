package com.chrisfry.nerdnews.model

import com.chrisfry.nerdnews.business.enums.ArticleDisplayType

/**
 * Interface for interactions with ArticleListsModel
 */
interface IArticleDataModel {

    /**
     * Gets a list of requested article data (Article) based on given type
     *
     * @param articleDisplayType: Requested type of article to retrieve
     * @return A list of Article models
     */
    fun getArticleList(articleDisplayType: ArticleDisplayType): MutableList<Article>

    /**
     * Sets list of requested article data (Article) based on given type (refresh)
     *
     * @param articleDisplayType: Requested type of article to replace
     * @param articleList: List of article data to use as a replacement
     */
    fun setArticleList(articleDisplayType: ArticleDisplayType, articleList: List<Article>)

    /**
     * Adds article data (Article) to requested article data type
     *
     * @param articleDisplayType: Requested type of article data to add to
     * @param articleList: List of article data to add to the model
     */
    fun addToArticleList(articleDisplayType: ArticleDisplayType, articleList: List<Article>)

    /**
     * Set current page count used to retrieve article data from NewsAPI
     * THIS IS NOT ZERO INDEXED FOR NEWSAPI!!!!!!!!!!!!!!!!!!!!!!!!!!
     *
     * @param articleDisplayType: Requested type of article data to add to
     * @param count: The count of the page to store
     */
    fun setPageCount(articleDisplayType: ArticleDisplayType, count: Int)

    /**
     * Gets current page count used to retrieve article data from NewsAPI
     * THIS IS NOT ZERO INDEXED FOR NEWSAPI!!!!!!!!!!!!!!!!!!!!!!!!!!
     *
     * @param articleDisplayType: Requested type of article data to add to
     * @return: Current page count for the given article type
     */
    fun getPageCount(articleDisplayType: ArticleDisplayType): Int

    /**
     * Gets if an article refresh is currently in progress
     *
     * @return: Flag indicating if a refresh is in progress (true) or not (false)
     */
    fun isRefreshInProgress(): Boolean

    /**
     * Gets if our last article refresh failed
     *
     * @return: Flag indicating if the last article refresh failed (true) or not (false)
     */
    fun didLastRefreshFail(): Boolean
}