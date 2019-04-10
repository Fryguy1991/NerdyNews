package com.chrisfry.nerdnews.mocks

import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.model.Article
import com.chrisfry.nerdnews.model.ArticleListsModel


class MockArticleListsModel : ArticleListsModel() {

    var articleData = mutableListOf<MutableList<Article>>()

    init {
        for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
            articleData.add(mutableListOf())
        }
    }

    /**
     * Gets a list of requested article data (Article) based on given type
     *
     * @param articleDisplayType: Requested type of article to retrieve
     */
    override fun getArticleList(articleDisplayType: ArticleDisplayType): MutableList<Article> {
        return articleData[articleDisplayType.ordinal]
    }

    /**
     * Sets list of requested article data (Article) based on given type (refresh)
     *
     * @param articleDisplayType: Requested type of article to replace
     * @param articleList: List of article data to use as a replacement
     */
    override fun setArticleList(articleDisplayType: ArticleDisplayType, articleList: List<Article>) {
        articleData[articleDisplayType.ordinal].clear()
        articleData[articleDisplayType.ordinal].addAll(articleList)
    }

    /**
     * Mock method for adding article data to list
     *
     * @param articleDisplayType: Requested type of article data to add to
     * @param articleList: List of article data to add to the model
     */
    override fun addToArticleList(articleDisplayType: ArticleDisplayType, articleList: List<Article>) {
        // Currently not implemented in mock
    }

    fun clearAllData() {
        articleData.clear()

        for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
            articleData.add(mutableListOf())
        }
    }
}