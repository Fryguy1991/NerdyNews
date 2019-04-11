package com.chrisfry.nerdnews.tests.presenters

import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.eventhandling.BaseEvent
import com.chrisfry.nerdnews.business.eventhandling.EventHandler
import com.chrisfry.nerdnews.business.eventhandling.events.ArticleRefreshCompleteEvent
import com.chrisfry.nerdnews.business.eventhandling.events.MoreArticleEvent
import com.chrisfry.nerdnews.business.eventhandling.events.RequestMoreArticleEvent
import com.chrisfry.nerdnews.business.eventhandling.receivers.RequestMoreArticleEventReceiver
import com.chrisfry.nerdnews.business.presenters.ArticleListPresenter
import com.chrisfry.nerdnews.business.presenters.interfaces.IArticleListPresenter
import com.chrisfry.nerdnews.mocks.MockArticleListsModel
import com.chrisfry.nerdnews.model.Article
import com.chrisfry.nerdnews.model.ArticleDisplayModel
import com.chrisfry.nerdnews.model.ArticleSource
import com.chrisfry.nerdnews.tests.BaseTest
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Class for isolating and testing ArticleListPresenter
 */
class ArticleListPresenterTest: BaseTest() {
    companion object {
        private val TAG = ArticleListPresenterTest::class.java.name
    }

    /**
     * Mock class for interacting with ArticleListPresenter
     */
    class MockArticleListView: ArticleListPresenter.IArticleListView {
        // Mock data we receive
        var articleList = listOf<ArticleDisplayModel>()
        var areMoreArticlesAvailable = true
        var areDisplayingNoArticles = false

        override fun displayArticles(articles: List<ArticleDisplayModel>) {
            articleList = articles

            areDisplayingNoArticles = false
        }

        override fun noMoreArticlesAvailable() {
            areMoreArticlesAvailable = false
        }

        override fun displayNoArticles() {
            areDisplayingNoArticles = true
        }
    }

    // Presenter instance we till use to test
    private var articleListPresenter: IArticleListPresenter? = null
    // Mock model to test presenter with
    private lateinit var mockArticleModel: MockArticleListsModel
    // Mock view object to interact with presenter
    private lateinit var mockArticleListView: MockArticleListView
    // Type of the article list presenter. Used to randomize and

    override fun setUp() {
        super.setUp()

        // TODO: Currently testing presenter with aricle type TECH, consider testing others

        // Instantiate presenter
        val presenter = ArticleListPresenter.getInstance(ArticleDisplayType.TECH)
        // Create mock model and inject to presenter
        mockArticleModel = MockArticleListsModel()
        presenter.articleModelInstance = mockArticleModel

        articleListPresenter = presenter

        // Create mock view
        mockArticleListView = MockArticleListView()
    }

    override fun tearDown() {
        super.tearDown()

        articleListPresenter?.detach()
        mockArticleModel.clearAllData()
    }

    @Test
    fun testPresenterNotNull() {
        Assert.assertNotNull(articleListPresenter)
    }

    @Test
    fun testAttachView() {
        val presenter = articleListPresenter
        Assert.assertNotNull(presenter)

        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            presenter.attach(mockArticleListView)

            // Nothing happpens on attach, values should be default
            Assert.assertTrue(mockArticleListView.articleList.isEmpty())
            Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
            Assert.assertTrue(mockArticleListView.areMoreArticlesAvailable)
        }
    }

    @Test
    fun testAttachWithNoArticles() {
        val presenter = articleListPresenter
        Assert.assertNotNull(presenter)

        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            presenter.attach(mockArticleListView)

            // Nothing happpens on attach, values should be default
            Assert.assertTrue(mockArticleListView.articleList.isEmpty())
            Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
            Assert.assertTrue(mockArticleListView.areMoreArticlesAvailable)

            // Request articles from presenter
            presenter.requestArticles()

            // No articles loaded in model. View should be displaying no articles
            Assert.assertTrue(mockArticleListView.articleList.isEmpty())
            Assert.assertTrue(mockArticleListView.areDisplayingNoArticles)
            Assert.assertTrue(mockArticleListView.areMoreArticlesAvailable)
        }
    }

    @Test
    fun testAttachWithArticles() {
        val presenter = articleListPresenter
        Assert.assertNotNull(presenter)

        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            presenter.attach(mockArticleListView)

            // Nothing happpens on attach, values should be default
            Assert.assertTrue(mockArticleListView.articleList.isEmpty())
            Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
            Assert.assertTrue(mockArticleListView.areMoreArticlesAvailable)

            // Load 50 mock articles into the model
            addArticlesToModelList(50, ArticleDisplayType.TECH)
            // Notify presenter article model has been updated
            EventHandler.broadcast(ArticleRefreshCompleteEvent())

            // View should be displaying 50 articles
            Assert.assertTrue(mockArticleListView.articleList.size == 50)
            Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
            Assert.assertTrue(mockArticleListView.areMoreArticlesAvailable)
        }
    }

    @Test
    fun testRequestMoreArticles() {
        val presenter = articleListPresenter
        Assert.assertNotNull(presenter)

        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            presenter.attach(mockArticleListView)

            // Nothing happpens on attach, values should be default
            Assert.assertTrue(mockArticleListView.articleList.isEmpty())
            Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
            Assert.assertTrue(mockArticleListView.areMoreArticlesAvailable)

            // Load 50 mock articles into the model
            addArticlesToModelList(50, ArticleDisplayType.TECH)
            // Notify presenter article model has been updated
            EventHandler.broadcast(ArticleRefreshCompleteEvent())

            // View should be displaying 50 articles
            Assert.assertTrue(mockArticleListView.articleList.size == 50)
            Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
            Assert.assertTrue(mockArticleListView.areMoreArticlesAvailable)

            var timestamp = System.currentTimeMillis()
            // Add receiver for RequestMoreArticleEvent
            val receiver = object : RequestMoreArticleEventReceiver{
                override fun onReceive(event: BaseEvent) {
                    when (event is RequestMoreArticleEvent) {
                        true -> {
                            // Ensure our broadcast is quick
                            Assert.assertTrue(System.currentTimeMillis() - timestamp < TimeUnit.SECONDS.toMillis(1))
                        }
                        else -> {
                            // Not handling in test
                        }
                    }
                }
            }
            EventHandler.addEventReceiver(receiver)

            // Tell presenter we want more articles (will fire receiver above)
            presenter.requestMoreArticles()
            // Add more articles to mock model
            addArticlesToModelList(50, ArticleDisplayType.TECH)

            // Haven't notified presenter more articles are available, should be same as pre-request
            // View should be displaying 50 articles
            Assert.assertTrue(mockArticleListView.articleList.size == 50)
            Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
            Assert.assertTrue(mockArticleListView.areMoreArticlesAvailable)

            // Notify presenter more articles are available
            EventHandler.broadcast(MoreArticleEvent(ArticleDisplayType.TECH))

            // View should be displaying 100 articles
            Assert.assertTrue(mockArticleListView.articleList.size == 100)
            Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
            Assert.assertTrue(mockArticleListView.areMoreArticlesAvailable)

            // Let's do this a bunch of times (as if we've requested 5000 articles)
            for (i in 1..100) {
                timestamp = System.currentTimeMillis()
                // Tell presenter we want more articles (will fire receiver above)
                presenter.requestMoreArticles()
                // Add more articles to mock model
                addArticlesToModelList(50, ArticleDisplayType.TECH)
                // Notify presenter more articles are available
                EventHandler.broadcast(MoreArticleEvent(ArticleDisplayType.TECH))

                // View should be displaying 100 + i * 50 articles
                Assert.assertTrue(mockArticleListView.articleList.size == i * 50 + 100)
                Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
                Assert.assertTrue(mockArticleListView.areMoreArticlesAvailable)
            }
        }
    }

    @Test
    fun testRefreshArticles() {
        val presenter = articleListPresenter
        Assert.assertNotNull(presenter)

        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            presenter.attach(mockArticleListView)

            // Nothing happpens on attach, values should be default
            Assert.assertTrue(mockArticleListView.articleList.isEmpty())
            Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
            Assert.assertTrue(mockArticleListView.areMoreArticlesAvailable)

            // Load 50 mock articles into the model
            addArticlesToModelList(50, ArticleDisplayType.TECH)
            // Notify presenter article model has been updated
            EventHandler.broadcast(ArticleRefreshCompleteEvent())

            // View should be displaying 50 articles
            Assert.assertTrue(mockArticleListView.articleList.size == 50)
            Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
            Assert.assertTrue(mockArticleListView.areMoreArticlesAvailable)

            // Request more articles from presenter
            presenter.requestMoreArticles()
            // Load 50 more mock articles into the model
            addArticlesToModelList(50, ArticleDisplayType.TECH)
            // Notify presenter model has been updated
            EventHandler.broadcast(MoreArticleEvent(ArticleDisplayType.TECH))

            // View should be displaying 100 articles
            Assert.assertTrue(mockArticleListView.articleList.size == 100)
            Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
            Assert.assertTrue(mockArticleListView.areMoreArticlesAvailable)

            // Simulate a refresh by clearing model data and adding 20 articles to model list
            mockArticleModel.clearAllData()
            addArticlesToModelList(20, ArticleDisplayType.TECH)
            // Notify presenter article data has been refreshed
            EventHandler.broadcast(ArticleRefreshCompleteEvent())

            // View should be displaying the 20 new articles
            Assert.assertTrue(mockArticleListView.articleList.size == 20)
            Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
            Assert.assertTrue(mockArticleListView.areMoreArticlesAvailable)
        }
    }

    @Test
    fun testNoMoreArticlesAvailable() {
        val presenter = articleListPresenter
        Assert.assertNotNull(presenter)

        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            presenter.attach(mockArticleListView)

            // Nothing happpens on attach, values should be default
            Assert.assertTrue(mockArticleListView.articleList.isEmpty())
            Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
            Assert.assertTrue(mockArticleListView.areMoreArticlesAvailable)

            // Load 50 mock articles into the model
            addArticlesToModelList(50, ArticleDisplayType.TECH)
            // Notify presenter article model has been updated
            EventHandler.broadcast(ArticleRefreshCompleteEvent())

            // View should be displaying 50 articles
            Assert.assertTrue(mockArticleListView.articleList.size == 50)
            Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
            Assert.assertTrue(mockArticleListView.areMoreArticlesAvailable)

            // Request more articles from presenter
            presenter.requestMoreArticles()
            // Load 50 more mock articles into the model
            addArticlesToModelList(50, ArticleDisplayType.TECH)
            // Notify presenter model has been updated
            EventHandler.broadcast(MoreArticleEvent(ArticleDisplayType.TECH))

            // View should be displaying 100 articles
            Assert.assertTrue(mockArticleListView.articleList.size == 100)
            Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
            Assert.assertTrue(mockArticleListView.areMoreArticlesAvailable)

            // Request more articles from the presenter
            presenter.requestMoreArticles()
            // Don't modify model data and notify presenter that more article
            // data is available (actually checked in presenter)
            EventHandler.broadcast(MoreArticleEvent(ArticleDisplayType.TECH))

            // View should still be displaying 100 articles and indicate that there are no more articles to display
            Assert.assertTrue(mockArticleListView.articleList.size == 100)
            Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
            Assert.assertFalse(mockArticleListView.areMoreArticlesAvailable)

            // Add receiver for RequestMoreArticleEvent
            val receiver = object : RequestMoreArticleEventReceiver{
                override fun onReceive(event: BaseEvent) {
                    when (event is RequestMoreArticleEvent) {
                        true -> {
                            // This receiver should not be called because presenter should know not to
                            // request more articles if it has already detected there are not more to display
                            Assert.assertTrue(false)
                        }
                        else -> {
                            // Not handling in test
                        }
                    }
                }
            }
            EventHandler.addEventReceiver(receiver)

            // Request more articles from the presenter above receiver should NOT be called
            presenter.requestMoreArticles()

            // Let's request a bunch more times to ensure calls aren't getting through
            for (i in 0 until 1000) {
                presenter.requestMoreArticles()

                // View should still be displaying 100 articles and indicate that there are no more articles to display
                Assert.assertTrue(mockArticleListView.articleList.size == 100)
                Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
                Assert.assertFalse(mockArticleListView.areMoreArticlesAvailable)
            }
        }
    }


    /**
     * Function for injecting dummy data into the article model
     *
     * @param count: Number of articles we want to add
     * @param articleType: Type of article to add to
     */
    private fun addArticlesToModelList(count: Int, articleType: ArticleDisplayType) {
        val currentSize = mockArticleModel.getArticleList(articleType).size

        val articlesToAdd = mutableListOf<Article>()
        for (i: Int in currentSize until currentSize + count) {
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
            articlesToAdd.add(articleModel)
        }

        mockArticleModel.addToArticleList(articleType, articlesToAdd)
    }
}