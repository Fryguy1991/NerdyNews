package com.chrisfry.nerdnews.tests.presenters

import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.eventhandling.BaseEvent
import com.chrisfry.nerdnews.business.eventhandling.EventHandler
import com.chrisfry.nerdnews.business.eventhandling.events.RefreshCompleteEvent
import com.chrisfry.nerdnews.business.eventhandling.events.RefreshEvent
import com.chrisfry.nerdnews.business.eventhandling.receivers.RefreshEventReceiver
import com.chrisfry.nerdnews.business.presenters.NewsPagingPresenter
import com.chrisfry.nerdnews.business.presenters.interfaces.INewsPagingPresenter
import com.chrisfry.nerdnews.tests.BaseTest
import com.chrisfry.nerdnews.utils.LogUtils
import org.junit.*
import java.util.concurrent.TimeUnit

class NewsPagingPresenterTest: BaseTest() {
    companion object {
        private val TAG = NewsPagingPresenterTest::class.java.name
    }

    /**
     * Mock class for view that attaches to NewsPagingPresenter (INewsPagingView)
     */
    class MockNewsPagingView: NewsPagingPresenter.INewsPagingView {
        // Variable to represent if view is in refreshing state
        var isRefreshing = false

        override fun displayRefreshing() {
            isRefreshing = true
        }

        override fun refreshingComplete() {
            isRefreshing = false
        }
    }


    // Presenter instance we will be testing with
    private var newsPagingPresenter: INewsPagingPresenter? = null

    // Mock for view that attaches to presenter
    private lateinit var mockNewsPagingView: MockNewsPagingView

    override fun setUp() {
        super.setUp()

        newsPagingPresenter = NewsPagingPresenter.getInstance()
        mockNewsPagingView = MockNewsPagingView()
    }

    override fun tearDown() {
        super.tearDown()

        newsPagingPresenter?.detach()
    }

    @Test
    fun testPresenterNotNull() {
        Assert.assertNotNull(newsPagingPresenter)
    }

    @Test
    fun testAttachView() {
        newsPagingPresenter?.attach(mockNewsPagingView)

        // At first attachment view should be refreshing as we have no articles
        Assert.assertTrue(mockNewsPagingView.isRefreshing)
    }

    @Test
    fun testRefreshRequest() {
        // Ensure that when presenter requests a refresh we receive the request quickly (less than 1 second)
        val timeStamp = System.currentTimeMillis()

        // Add a mock item that will receiver the call to refresh from the presenter
        val refreshReceiver = object : RefreshEventReceiver {
            override fun onReceive(event: BaseEvent) {
                if (event is RefreshEvent) {
                    val eventTime = System.currentTimeMillis()
                    LogUtils.debug(TAG, "testRefreshRequest received refresh event")
                    Assert.assertTrue(eventTime - timeStamp < TimeUnit.SECONDS.toMicros(1))
                } else {
                    // Ignore other events
                }
            }
        }
        EventHandler.addRefreshReceiver(refreshReceiver)

        newsPagingPresenter?.requestArticleRefresh()
    }

    @Test
    fun testRefreshComplete() {
        Assert.assertNotNull(newsPagingPresenter)

        val presenter = newsPagingPresenter
        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            presenter.attach(mockNewsPagingView)

            // First attachment, view should be displaying refreshing
            Assert.assertTrue(mockNewsPagingView.isRefreshing)

            // Simulate all article types have completed refresh, presenter should tell view to stop refreshing
            for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
                EventHandler.broadcast(RefreshCompleteEvent(articleType))
            }
            // View should no longer be refreshing
            Assert.assertFalse(mockNewsPagingView.isRefreshing)

            // Let's request another refresh
            presenter.requestArticleRefresh()
            // View should be refreshing
            Assert.assertTrue(mockNewsPagingView.isRefreshing)

            // Complete all but one article type refresh
            for (i in 0..ArticleDisplayType.values().size - 2) {
                EventHandler.broadcast(RefreshCompleteEvent(ArticleDisplayType.values()[i]))
            }
            // View should still be in refreshing state
            Assert.assertTrue(mockNewsPagingView.isRefreshing)

            // Complete last article type
            EventHandler.broadcast(RefreshCompleteEvent(ArticleDisplayType.values()[ArticleDisplayType.values().size - 1]))
            // View should no longer be in refreshing state
            Assert.assertFalse(mockNewsPagingView.isRefreshing)
        }
    }

    @Test
    fun testRefreshRequestWithIncompleteRefresh() {
        Assert.assertNotNull(newsPagingPresenter)

        val presenter = newsPagingPresenter
        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            presenter.attach(mockNewsPagingView)

            // First attachment, view should be displaying refreshing
            Assert.assertTrue(mockNewsPagingView.isRefreshing)

            // Simulate all article types have completed refresh, presenter should tell view to stop refreshing
            for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
                EventHandler.broadcast(RefreshCompleteEvent(articleType))
            }
            // View should no longer be refreshing
            Assert.assertFalse(mockNewsPagingView.isRefreshing)

            // Add a mock item that will receive the call to refresh from the presenter, this will assert true once
            // and assert false for any other time (simulating multiple refresh calls without completion)
            val refreshReceiver = object : RefreshEventReceiver {
                private var shouldRefreshFlag = true
                override fun onReceive(event: BaseEvent) {
                    if (event is RefreshEvent) {
                        LogUtils.debug(TAG, "testRefreshRequest received refresh event, should receive no more as we're simulating not ending a refresh")
                        Assert.assertTrue(shouldRefreshFlag)
                        shouldRefreshFlag = false
                    } else {
                        // Ignore other events
                    }
                }
            }
            EventHandler.addRefreshReceiver(refreshReceiver)

            // Request a refresh from the presenter, above receiver should pass
            presenter.requestArticleRefresh()

            // View should still be in refreshing state
            Assert.assertTrue(mockNewsPagingView.isRefreshing)

            // Request another refresh from the presenter, above receiver
            // should not get a RefreshEvent call (or else it will fail)
            presenter.requestArticleRefresh()
            // View should still be in refreshing state
            Assert.assertTrue(mockNewsPagingView.isRefreshing)

            // Request another refresh 1000 times to ensure calls aren't getting through
            for (i in 0 until 1000) {
                presenter.requestArticleRefresh()
            }
            // View should still be in refreshing state
            Assert.assertTrue(mockNewsPagingView.isRefreshing)

            // Simulate all article types have completed refresh, presenter should tell view to stop refreshing
            for (articleType: ArticleDisplayType in ArticleDisplayType.values()) {
                EventHandler.broadcast(RefreshCompleteEvent(articleType))
            }
            // View should no longer be refreshing
            Assert.assertFalse(mockNewsPagingView.isRefreshing)
        }
    }
}