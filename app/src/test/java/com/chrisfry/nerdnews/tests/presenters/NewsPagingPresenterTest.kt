package com.chrisfry.nerdnews.tests.presenters

import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.eventhandling.BaseEvent
import com.chrisfry.nerdnews.business.eventhandling.EventHandler
import com.chrisfry.nerdnews.business.eventhandling.events.ArticleRefreshCompleteEvent
import com.chrisfry.nerdnews.business.eventhandling.events.RequestMoreArticleEvent
import com.chrisfry.nerdnews.business.eventhandling.receivers.ArticleRefreshCompleteEventReceiver
import com.chrisfry.nerdnews.business.presenters.NewsPagingPresenter
import com.chrisfry.nerdnews.business.presenters.interfaces.INewsPagingPresenter
import com.chrisfry.nerdnews.mocks.MockArticleListsModel
import com.chrisfry.nerdnews.mocks.MockNewsService
import com.chrisfry.nerdnews.tests.BaseTest
import com.chrisfry.nerdnews.utils.LogUtils
import org.junit.*
import java.util.concurrent.TimeUnit

/**
 * Class for isolating and testing NewsPagingPresenter
 */
class NewsPagingPresenterTest : BaseTest() {
    companion object {
        private val TAG = NewsPagingPresenterTest::class.java.name
    }

    /**
     * Mock class for view that attaches to NewsPagingPresenter (INewsPagingView)
     */
    class MockNewsPagingView : NewsPagingPresenter.INewsPagingView {
        // Variable to represent if view is in refreshing state
        var isRefreshing = false
        // Variable to track if we've received a refreshing complete event from the presenter
        var isRefreshingComplete = true

        override fun displayRefreshing(isRefreshing: Boolean) {
            this.isRefreshing = isRefreshing
            if (isRefreshing) {
                isRefreshingComplete = false
            }
        }

        override fun refreshingComplete() {
            isRefreshingComplete = true
        }
    }

    // Presenter instance we will be testing with
    private var newsPagingPresenter: INewsPagingPresenter? = null
    // Mock for view that attaches to presenter
    private lateinit var mockNewsPagingView: MockNewsPagingView
    // Mock for service that retrieves data for presenter
    private lateinit var mockNewsService: MockNewsService
    // Mock model that presenter will use
    private lateinit var mockArticleListsModel: MockArticleListsModel

    override fun setUp() {
        super.setUp()

        // Create presenter
        val presenter = NewsPagingPresenter.getInstance()

        // Instantiate mocks
        mockNewsService = MockNewsService()
        mockArticleListsModel = MockArticleListsModel()

        // Inject mocks into presenter
        presenter.newsService = mockNewsService
        presenter.articleModelInstance = mockArticleListsModel

        newsPagingPresenter = presenter
        mockNewsPagingView = MockNewsPagingView()
    }

    override fun tearDown() {
        super.tearDown()

        newsPagingPresenter?.detach()
        mockNewsService.clearCallbacks()
        mockArticleListsModel.clearAllData()
    }

    @Test
    fun testPresenterNotNull() {
        Assert.assertNotNull(newsPagingPresenter)
    }

    @Test
    fun testAttachView() {
        // Tell presenter to pull initial article list
        val presenter = newsPagingPresenter
        if (presenter is NewsPagingPresenter) {
            presenter.initialArticleCheck()
        }

        newsPagingPresenter?.attach(mockNewsPagingView)

        // We haven't told mock service to fire callbacks so view should still be refreshing
        Assert.assertTrue(mockNewsPagingView.isRefreshing)

        // Simulate API callbacks
        mockNewsService.fireCallbacks()

        // View should no longer be refreshing
        Assert.assertFalse(mockNewsPagingView.isRefreshing)
    }

    @Test
    fun testRefreshRequest() {
        // Ensure that when presenter completes a refresh we receive the complete event quickly (less than 1 second)
        // Service providing news data is mocked so this should be very fast
        val timeStamp = System.currentTimeMillis()

        // Add a mock item that will receive the event that article data has been refreshed
        val refreshReceiver = object : ArticleRefreshCompleteEventReceiver {
            override fun onReceive(event: BaseEvent) {
                if (event is ArticleRefreshCompleteEvent) {
                    val eventTime = System.currentTimeMillis()
                    LogUtils.debug(TAG, "testRefreshRequest received refresh event")
                    Assert.assertTrue(eventTime - timeStamp < TimeUnit.SECONDS.toMicros(1))
                } else {
                    // Ignore other events
                }
            }
        }
        EventHandler.addEventReceiver(refreshReceiver)

        newsPagingPresenter?.requestArticleRefresh()
    }

    @Test
    fun testRefreshComplete() {
        Assert.assertNotNull(newsPagingPresenter)

        val presenter = newsPagingPresenter
        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            // Model should start off empty
            for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
                Assert.assertTrue(mockArticleListsModel.getArticleList(articleType).isEmpty())
            }

            // Have presenter check for initial articles (will do a refresh)
            if (presenter is NewsPagingPresenter) {
                presenter.initialArticleCheck()
            }
            presenter.attach(mockNewsPagingView)
            // First attachment, view should be displaying refreshing
            Assert.assertTrue(mockNewsPagingView.isRefreshing)

            //Simulate callbacks
            mockNewsService.fireCallbacks()
            // View should not be refreshing anymore
            Assert.assertFalse(mockNewsPagingView.isRefreshing)

            // Let's request another refresh
            presenter.requestArticleRefresh()
            // View should be refreshing
            Assert.assertTrue(mockNewsPagingView.isRefreshing)

            // Simulate callbacks
            mockNewsService.fireCallbacks()
            // View should not be refreshing anymore
            Assert.assertFalse(mockNewsPagingView.isRefreshing)

            // MockNewsPresenter standard response is a ArticleResponse containing 20 items
            // Ensure our model received data from the presenter
            for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
                Assert.assertTrue(mockArticleListsModel.getArticleList(articleType).size == 20)
            }
        }
    }

    @Test
    fun testRefreshRequestWithIncompleteRefresh() {
        Assert.assertNotNull(newsPagingPresenter)

        val presenter = newsPagingPresenter
        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            // Have presenter to an initial article check which will call for a refresh
            if (presenter is NewsPagingPresenter) {
                presenter.initialArticleCheck()
            }
            presenter.attach(mockNewsPagingView)
            // First attachment, view should be displaying refreshing
            Assert.assertTrue(mockNewsPagingView.isRefreshing)

            // Simulate callbacks
            mockNewsService.fireCallbacks()
            // View should no longer be refreshing
            Assert.assertFalse(mockNewsPagingView.isRefreshing)


            // Add receiver to handler that will look for refresh complete events. This should only be received once
            // as we're going to request a bunch of refreshes without completing one
            val receiver = object : ArticleRefreshCompleteEventReceiver {
                private var refreshFlag = true
                override fun onReceive(event: BaseEvent) {
                    when {
                        event is ArticleRefreshCompleteEvent -> {
                            LogUtils.debug(TAG, "Received ArticleRefreshCompleteEvent")
                            Assert.assertTrue(refreshFlag)
                            refreshFlag = false
                        }
                        else -> {
                            LogUtils.debug(TAG, "")
                        }
                    }

                }
            }
            EventHandler.addEventReceiver(receiver)

            // Request a refresh from the presenter, above receiver should not be called
            presenter.requestArticleRefresh()
            // View should still be in refreshing state
            Assert.assertTrue(mockNewsPagingView.isRefreshing)

            // Request another refresh from the presenter, above receiver should not still not be called
            presenter.requestArticleRefresh()
            // View should still be in refreshing state
            Assert.assertTrue(mockNewsPagingView.isRefreshing)

            // Request another refresh 1000 times to ensure calls aren't getting through
            for (i in 0 until 1000) {
                presenter.requestArticleRefresh()
            }
            // Request another refresh from the presenter, above receiver should not still not be called
            Assert.assertTrue(mockNewsPagingView.isRefreshing)

            // Simulate callbacks, receiver should be called and view should not be in refreshing state
            mockNewsService.fireCallbacks()
            // View should no longer be refreshing
            Assert.assertFalse(mockNewsPagingView.isRefreshing)
        }
    }

    @Test
    fun testRefreshAllErrorCallbacks() {
        Assert.assertNotNull(newsPagingPresenter)

        val presenter = newsPagingPresenter
        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            // Ensure our beginning model lists are empty
            for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
                Assert.assertTrue(mockArticleListsModel.getArticleList(articleType).isEmpty())
            }

            // Set news service to provide errors instead of successes
            mockNewsService.responseType = MockNewsService.MockResponseType.API_KEY_ERROR

            // Have presenter initiate which will call for refresh
            if (presenter is NewsPagingPresenter) {
                presenter.initialArticleCheck()
            }
            presenter.attach(mockNewsPagingView)
            // View should be refreshing
            Assert.assertTrue(mockNewsPagingView.isRefreshing)

            // Simulate callbacks
            mockNewsService.fireCallbacks()

            // Presenter should only have received errors, which means articles lists should still be empty
            for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
                Assert.assertTrue(mockArticleListsModel.getArticleList(articleType).isEmpty())
            }
            // View should no longer be refreshing
            Assert.assertFalse(mockNewsPagingView.isRefreshing)
        }
    }

    @Test
    fun testRefreshWithDifferentResult() {
        Assert.assertNotNull(newsPagingPresenter)

        val presenter = newsPagingPresenter
        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            // Model should start off empty
            for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
                Assert.assertTrue(mockArticleListsModel.getArticleList(articleType).isEmpty())
            }

            // Have presenter check for initial articles (will do a refresh)
            if (presenter is NewsPagingPresenter) {
                presenter.initialArticleCheck()
            }
            presenter.attach(mockNewsPagingView)
            // First attachment, view should be displaying refreshing
            Assert.assertTrue(mockNewsPagingView.isRefreshing)

            //Simulate callbacks
            mockNewsService.fireCallbacks()
            // View should not be refreshing anymore
            Assert.assertFalse(mockNewsPagingView.isRefreshing)

            // Mock data model should contain 20 articles for each type
            for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
                Assert.assertTrue(mockArticleListsModel.getArticleList(articleType).size == 20)
            }

            // Setup mock service for new response (only gets 5 articles)
            mockNewsService.responseType = MockNewsService.MockResponseType.SUCCESS_2

            // Let's request a refresh
            presenter.requestArticleRefresh()
            // View should be refreshing
            Assert.assertTrue(mockNewsPagingView.isRefreshing)

            // Simulate callbacks
            mockNewsService.fireCallbacks()
            // View should not be refreshing anymore
            Assert.assertFalse(mockNewsPagingView.isRefreshing)

            // MockNewsPresenter new response should have 5 article items for each type
            for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
                Assert.assertTrue(mockArticleListsModel.getArticleList(articleType).size == 5)
            }
        }
    }

    @Test
    fun testRequestMoreArticles() {
        Assert.assertNotNull(newsPagingPresenter)

        val presenter = newsPagingPresenter
        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            // Numbers of times we've requested more articles
            var techMoreCount = 0
            var scienceMoreCount = 0
            var gamingMoreCount = 0

            // Model should start off empty
            for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
                Assert.assertTrue(mockArticleListsModel.getArticleList(articleType).isEmpty())
            }

            // Have presenter check for initial articles (will do a refresh)
            if (presenter is NewsPagingPresenter) {
                presenter.initialArticleCheck()
            }
            presenter.attach(mockNewsPagingView)
            // First attachment, view should be displaying refreshing
            Assert.assertTrue(mockNewsPagingView.isRefreshing)

            //Simulate callbacks
            mockNewsService.fireCallbacks()
            // View should not be refreshing anymore
            Assert.assertFalse(mockNewsPagingView.isRefreshing)

            // Model lists should have 20 articles each
            for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
                Assert.assertTrue(mockArticleListsModel.getArticleList(articleType).size == 20)
            }

            // Fire event for more articles tech type
            EventHandler.broadcast(RequestMoreArticleEvent(ArticleDisplayType.TECH))
            techMoreCount++
            // Simulate callbacks
            mockNewsService.fireCallbacks()

            // Should have added 20 articles to Tech list
            Assert.assertTrue(mockArticleListsModel.getArticleList(ArticleDisplayType.TECH).size == 40)


            // Let's fire a bunch of more article requests
            for (i in 0 until 1000) {
                EventHandler.broadcast(RequestMoreArticleEvent(ArticleDisplayType.TECH))
                mockNewsService.fireCallbacks()
                techMoreCount++

                // Should be multiple of 20 articles
                Assert.assertTrue(mockArticleListsModel.getArticleList(ArticleDisplayType.TECH).size == (techMoreCount + 1) * 20)
            }

            // Fire event for more articles science type
            EventHandler.broadcast(RequestMoreArticleEvent(ArticleDisplayType.SCIENCE))
            scienceMoreCount++
            // Simulate callbacks
            mockNewsService.fireCallbacks()

            // Should have added 20 articles to Science list
            Assert.assertTrue(mockArticleListsModel.getArticleList(ArticleDisplayType.SCIENCE).size == 40)


            // Let's fire a bunch of more article requests
            for (i in 0 until 1000) {
                EventHandler.broadcast(RequestMoreArticleEvent(ArticleDisplayType.SCIENCE))
                mockNewsService.fireCallbacks()
                scienceMoreCount++

                // Should be multiple of 20 articles
                Assert.assertTrue(mockArticleListsModel.getArticleList(ArticleDisplayType.SCIENCE).size == (scienceMoreCount + 1) * 20)
            }

            // Fire event for more articles gaming type
            EventHandler.broadcast(RequestMoreArticleEvent(ArticleDisplayType.GAMING))
            gamingMoreCount++
            // Simulate callbacks
            mockNewsService.fireCallbacks()

            // Should have added 20 articles to Gaming list
            Assert.assertTrue(mockArticleListsModel.getArticleList(ArticleDisplayType.GAMING).size == 40)


            // Let's fire a bunch of more article requests
            for (i in 0 until 1000) {
                EventHandler.broadcast(RequestMoreArticleEvent(ArticleDisplayType.GAMING))
                mockNewsService.fireCallbacks()
                gamingMoreCount++

                // Should be multiple of 20 articles
                Assert.assertTrue(mockArticleListsModel.getArticleList(ArticleDisplayType.GAMING).size == (gamingMoreCount + 1) * 20)
            }
        }
    }
}