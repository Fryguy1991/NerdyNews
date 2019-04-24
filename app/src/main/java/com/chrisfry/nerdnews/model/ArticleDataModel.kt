package com.chrisfry.nerdnews.model

import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.utils.LogUtils
import java.lang.Exception

/**
 * Model which contains lists of article data (based on ArticleDisplayType) to be displayed by the user interface
 */
class ArticleDataModel: IArticleDataModel {
    companion object {
        private val TAG = ArticleDataModel::class.java.simpleName

        @Volatile
        private var instance: ArticleDataModel? = null

        @Synchronized
        fun getInstance(): ArticleDataModel {
            var currentInstance = instance
            if (currentInstance == null) {
                // Need to create a new instance of ArticleListModel
                synchronized(this) {}
                currentInstance = ArticleDataModel()
                instance = currentInstance
            }
            return currentInstance
        }
    }

    // List of article lists
    private val articleLists: List<MutableList<Article>>
    // List of page counts for article lists
    private val pageCounts = mutableListOf<Int>()
    // Flag indicating if a refresh is in progress
    var refreshInProgress = false
    // Flag indicating if the last refresh failed
    var didLastRefreshFail = false

    init {
        val modelList = mutableListOf<MutableList<Article>>()
        for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
            modelList.add(mutableListOf())
            pageCounts.add(0)
        }
        articleLists = modelList.toList()
    }

    /**
     * Gets a list of requested article data (Article) based on given type
     *
     * @param articleDisplayType: Requested type of article to retrieve
     * @return A list of Article models
     */
    override fun getArticleList(articleDisplayType: ArticleDisplayType): MutableList<Article> {
        if (articleLists.size != ArticleDisplayType.values().size) {
            throw Exception("$TAG: Model list size does not match article type size (GET)")
        } else {
            return articleLists[articleDisplayType.ordinal]
        }
    }

    /**
     * Sets list of requested article data (Article) based on given type (refresh)
     *
     * @param articleDisplayType: Requested type of article to replace
     * @param articleList: List of article data to use as a replacement
     */
    override fun setArticleList(articleDisplayType: ArticleDisplayType, articleList: List<Article>) {
        if (articleLists.size != ArticleDisplayType.values().size) {
            LogUtils.wtf(TAG, "Model list size does not match article type size (SET)")
        } else {
            articleLists[articleDisplayType.ordinal].clear()
            articleLists[articleDisplayType.ordinal].addAll(articleList)
        }
    }

    /**
     * Adds article data (Article) to requested article data type
     *
     * @param articleDisplayType: Requested type of article data to add to
     * @param articleList: List of article data to add to the model
     */
    override fun addToArticleList(articleDisplayType: ArticleDisplayType, articleList: List<Article>) {
        if (articleLists.size != ArticleDisplayType.values().size) {
            LogUtils.wtf(TAG, "Model list size does not match article type size (ADD)")
        } else {
            articleLists[articleDisplayType.ordinal].addAll(articleList)
        }
    }

    /**
     * Set current page count used to retrieve article data from NewsAPI
     * THIS IS NOT ZERO INDEXED FOR NEWSAPI!!!!!!!!!!!!!!!!!!!!!!!!!!
     *
     * @param articleDisplayType: Requested type of article data to add to
     * @param count: The count of the page to store
     */
    override fun setPageCount(articleDisplayType: ArticleDisplayType, count: Int) {
        if (articleLists.size != ArticleDisplayType.values().size) {
            LogUtils.wtf(TAG, "Page count list size does not match article type size (ADD)")
        } else {
            pageCounts[articleDisplayType.ordinal] = count
        }
    }

    /**
     * Gets current page count used to retrieve article data from NewsAPI
     * THIS IS NOT ZERO INDEXED FOR NEWSAPI!!!!!!!!!!!!!!!!!!!!!!!!!!
     *
     * @param articleDisplayType: Requested type of article data to add to
     */
    override fun getPageCount(articleDisplayType: ArticleDisplayType): Int {
        if (articleLists.size != ArticleDisplayType.values().size) {
            throw Exception("$TAG: Page count list size does not match article type size (ADD)")
        } else {
            return pageCounts[articleDisplayType.ordinal]
        }
    }

    /**
     * Gets if an article refresh is currently in progress
     *
     * @return: Flag indicating if a refresh is in progress (true) or not (false)
     */
    override fun isRefreshInProgress(): Boolean {
        return refreshInProgress
    }

    /**
     * Gets if our last article refresh failed
     *
     * @return: Flag indicating if the last article refresh failed (true) or not (false)
     */
    override fun didLastRefreshFail(): Boolean {
        return didLastRefreshFail
    }
}