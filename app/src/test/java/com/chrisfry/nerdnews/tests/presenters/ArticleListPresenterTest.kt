package com.chrisfry.nerdnews.tests.presenters

import com.chrisfry.nerdnews.AppConstants
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

/**
 * Class for isolating and testing ArticleListPresenter
 */
class ArticleListPresenterTest: BaseTest() {
    companion object {
        private val TAG = ArticleListPresenterTest::class.java.simpleName
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
        // Not priority tho as function is the same no matter the article type

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

    @Test
    fun testRealisticArticleData() {
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

            // Create some mock data that looks like an actual article and load into model
            val article1 = Article(
                ArticleSource(null, "Gizmodo.com"),
                "Bryan Menegus",
                "Here's What Could Sink Uber, According to Uber - Gizmodo",
                "Here's What Could Sink Uber, According to Uber - Gizmodo" + "Uber, the money-losing ridehailing platform which by its own admission in its S-1 filing claims it has “incurred significant losses since inception”  and “may not achieve profitability,” has overcome a lot on the road to today’s initial public offering. It’s …",
                "https://gizmodo.com/heres-what-could-sink-uber-according-to-uber-1833982890",
                "https://i.kinja-img.com/gawker-media/image/upload/s--bvCZvrQS--/c_fill,fl_progressive,g_center,h_900,q_80,w_1600/a2ls8hdckvwzsydjsbys.jpg",
                "2019-04-11T21:27:00Z",
                "Uber, the money-losing ridehailing platform which by its own admission in its S-1 filing claims it has incurred significant losses since inception and may not achieve profitability, has overcome a lot on the road to todays initial public offering. Its entire … [+8812 chars]"
            )
            val article2 = Article(
                ArticleSource("chrisfry.com", "Chris-J-Fry"),
                "Chris Fry",
                "Android-Dev-Takes-On-Different-Kind-of-Writing - Chris J Fry",
                "This is probably a poorly written article.",
                "chrisfry/articles/1.com",
                "chrisfry/images/1.com",
                "1991-10-30T21:27:00Z",
                "I should not have quit my job to become a writer. THE END."
            )
            val article3 = Article(
                ArticleSource("the-verge", "The-Verge-"),
                "Julia Alexander",
                "Disney CEO calls social media a ‘powerful marketing tool’ for extremism - The-Verge-",
                "Disney CEO Bob Iger called social media a place that spreads extremism and hate. He added that it’s something Hitler would have loved because it’s easy to market extreme ideas.",
                "https://www.theverge.com/2019/4/11/18306763/disney-ceo-bob-iger-social-media-hitler-extremism",
                "https://cdn.vox-cdn.com/thumbor/lWIZAGYxtYlsk7Og_Pmpy1fsRMI=/0x399:5568x3314/fit-in/1200x630/cdn.vox-cdn.com/uploads/chorus_asset/file/16026168/1048053334.jpg.jpg",
                "2019-04-11T21:17:16Z",
                "Disney CEO Bob Iger criticized social media platforms for allowing hate to spread, saying they enable the distribution of misinformation and the propagation of vile ideology. Iger referred to social media as something Hitler would have loved, according to Var… [+1891 chars]"
            )
            val articleList = listOf(article1, article2, article3)
            mockArticleModel.setArticleList(ArticleDisplayType.TECH, articleList)

            // Tell presenter that the view wants article data
            presenter.requestArticles()

            // View should have the 3 articles we sent
            Assert.assertTrue(mockArticleListView.articleList.size == 3)
            Assert.assertFalse(mockArticleListView.areDisplayingNoArticles)
            Assert.assertTrue(mockArticleListView.areMoreArticlesAvailable)

            // Titles have source trimmed from the end. The should now appear as:
            val dataList = mockArticleListView.articleList
            Assert.assertEquals("Here's What Could Sink Uber, According to Uber ", dataList[0].title)
            Assert.assertEquals("Android-Dev-Takes-On-Different-Kind-of-Writing ", dataList[1].title)
            Assert.assertEquals("Disney CEO calls social media a ‘powerful marketing tool’ for extremism ", dataList[2].title)

            // Test article data view has received
            for(i in 0 until 3) {
                val demoData = articleList[i]
                val viewData = mockArticleListView.articleList[i]

                // Below fields will match unless sent null, then they will be converted to empty strings
                Assert.assertEquals(demoData.source.name ?: AppConstants.EMPTY_STRING, viewData.sourceName)
                Assert.assertEquals(demoData.author ?: AppConstants.EMPTY_STRING, viewData.author)
                Assert.assertEquals(demoData.url ?: AppConstants.EMPTY_STRING, viewData.articleUrl)
                Assert.assertEquals(demoData.urlToImage ?: AppConstants.EMPTY_STRING, viewData.imageUrl)
                Assert.assertEquals(demoData.content ?: AppConstants.EMPTY_STRING, viewData.articleContent)


                if (demoData.publishedAt == null) {
                    Assert.assertNotEquals(demoData.publishedAt ?: AppConstants.EMPTY_STRING, viewData.publishedAt)
                } else {
                    // Published at date is converted. This should never be the same
                    Assert.assertNotEquals(demoData.publishedAt, viewData.publishedAt)
                }
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