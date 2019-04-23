package com.chrisfry.nerdnews.tests

import com.chrisfry.nerdnews.model.Article
import com.chrisfry.nerdnews.model.ArticleSource
import com.chrisfry.nerdnews.utils.LogUtils
import org.junit.Before

open class BaseTest {
    @Before
    open fun setUp() {
        // Ensure we're using test log methods so presenters don't need Android implementation
        LogUtils.isTesting = true
    }

    /**
     * Function for getting a fake list of articles
     *
     * @param count: Number of fake articles we want
     */
    protected fun getFakeArticleModelList(count: Int): MutableList<Article> {
        val articleList = mutableListOf<Article>()
        for (i: Int in 0 until count) {
            val articleModel = Article(
                ArticleSource(i.toString(), "Source $i"),
                "Author $i",
                "Title $i",
                "Description $i",
                "URL $i",
                "Image URL $i",
                "Published At $i",
                "Article Content $i"
            )
            articleList.add(articleModel)
        }

        return articleList
    }
}