package com.chrisfry.nerdnews.model

import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.utils.LogUtils
import java.lang.Exception

/**
 * Model which contains lists of article data (based on ArticleDisplayType) to be displayed by the user interface
 */
class ArticleListsModel {
    companion object {
        private val TAG = ArticleListsModel::class.java.name

        @Volatile
        private var instance: ArticleListsModel? = null

        @Synchronized
        fun getInstance(): ArticleListsModel {
            var currentInstance = instance
            if (currentInstance == null) {
                // Need to create a new instance of ArticleListModel
                synchronized(this) {}
                currentInstance = ArticleListsModel()
                instance = currentInstance
            }
            return currentInstance
        }
    }

    // List of article lists
    private val articleLists: List<MutableList<Article>>

    init {
        val modelList = mutableListOf<MutableList<Article>>()
        for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
            modelList.add(mutableListOf())
        }
        articleLists = modelList.toList()
    }

    /**
     * Gets a list of requested article data (Article) based on given type
     *
     * @param articleDisplayType: Requested type of article to retrieve
     */
    fun getArticleList(articleDisplayType: ArticleDisplayType): MutableList<Article> {
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
    fun setArticleList(articleDisplayType: ArticleDisplayType, articleList: List<Article>) {
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
    fun addToArticleList(articleDisplayType: ArticleDisplayType, articleList: List<Article>) {
        if (articleLists.size != ArticleDisplayType.values().size) {
            LogUtils.wtf(TAG, "Model list size does not match article type size (ADD)")
        } else {
            articleLists[articleDisplayType.ordinal].addAll(articleList)
        }
    }
}