package com.chrisfry.nerdnews.tests.presenters

import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.events.RefreshCompleteEvent
import com.chrisfry.nerdnews.business.network.INewsApi
import com.chrisfry.nerdnews.business.presenters.NewsPagingPresenter
import com.chrisfry.nerdnews.business.presenters.interfaces.INewsPagingPresenter
import com.chrisfry.nerdnews.model.Article
import com.chrisfry.nerdnews.model.ArticleSource
import com.chrisfry.nerdnews.model.IArticleListsModel
import com.chrisfry.nerdnews.tests.BaseTest
import org.greenrobot.eventbus.EventBus
import org.junit.*
import org.mockito.*
import org.mockito.Mockito.*

/**
 * Class for isolating and testing NewsPagingPresenter
 */
class NewsPagingPresenterTest : BaseTest() {
    companion object {
        private val TAG = NewsPagingPresenterTest::class.java.simpleName
    }

    // Presenter instance we will be testing with
    private var newsPagingPresenter: INewsPagingPresenter? = null

    // Mock for view that attaches to presenter
    @Mock
    private lateinit var mockNewsPagingView: NewsPagingPresenter.INewsPagingView
    // Object for capturing boolean arguments sent to view
    @Captor
    private lateinit var booleanCaptor: ArgumentCaptor<Boolean>
    // Mock for api that retrieves data and notifies presenter of data changes
    @Mock
    private lateinit var mockNewsApi: INewsApi
    // Mock model that presenter will use
    @Mock
    private lateinit var mockArticleListsModel: IArticleListsModel
    // Mock for interacting with event handler
    private var mockEventBus = EventBus.getDefault()

    override fun setUp() {
        super.setUp()

        // Initialize a new set of mock objects
        MockitoAnnotations.initMocks(this)

        // Create presenter
        val presenter = NewsPagingPresenter()

        // Inject mocks into presenter
        presenter.newsApiInstance = mockNewsApi
        presenter.articleModelInstance = mockArticleListsModel
        presenter.eventBus = mockEventBus
        presenter.postDependencyInitiation()


        newsPagingPresenter = presenter
    }

    @After
    fun tearDown() {
        newsPagingPresenter?.detach()
        newsPagingPresenter?.breakDown()
    }

    @Test
    fun testPresenterNotNull() {
        Assert.assertNotNull(newsPagingPresenter)
    }

    @Test
    fun testAttachView() {
        // Tell presenter to pull initial article list
        val presenter = newsPagingPresenter!!
        presenter.initialArticleCheck()

        setupDummyArticles()

        // Attach mock view to presenter
        newsPagingPresenter?.attach(mockNewsPagingView)

        // Haven't simulated refresh complete, view should still be in refreshing state
        verify(mockNewsPagingView, times(1)).displayRefreshing(booleanCaptor.capture())
        verify(mockNewsPagingView, never()).refreshingComplete()
        Assert.assertTrue(booleanCaptor.value)

        // Simulate a refresh completing
        mockEventBus.post(RefreshCompleteEvent())

        // View should no longer be in "refreshing" state
        verify(mockNewsPagingView, times(2)).displayRefreshing(booleanCaptor.capture())
        verify(mockNewsPagingView, times(1)).refreshingComplete()
        Assert.assertFalse(booleanCaptor.value)
    }

    @Test
    fun testRefreshRequest() {
        // Tell presenter to pull initial article list
        val presenter = newsPagingPresenter!!
        presenter.initialArticleCheck()

        // Attach mock view to presenter
        newsPagingPresenter?.attach(mockNewsPagingView)

        // Haven't simulated refresh complete, view should still be in refreshing state
        verify(mockNewsPagingView, times(1)).displayRefreshing(booleanCaptor.capture())
        verify(mockNewsPagingView, never()).refreshingComplete()
        Assert.assertTrue(booleanCaptor.value)

        // Simulate a refresh completing
        setupDummyArticles()
        mockEventBus.post(RefreshCompleteEvent())

        // View should no longer be in "refreshing" state
        verify(mockNewsPagingView, times(2)).displayRefreshing(booleanCaptor.capture())
        verify(mockNewsPagingView, times(1)).refreshingComplete()
        Assert.assertFalse(booleanCaptor.value)

        // Now let's simulate the view requesting a refresh
        presenter.requestArticleRefresh()

        // Haven't simulated refresh complete, view should be in refreshing state
        verify(mockNewsPagingView, times(3)).displayRefreshing(booleanCaptor.capture())
        verify(mockNewsPagingView, times(1)).refreshingComplete()
        Assert.assertTrue(booleanCaptor.value)

        // Simulate a refresh complete event
        mockEventBus.post(RefreshCompleteEvent())

        // View should no longer be in "refreshing" state
        verify(mockNewsPagingView, times(4)).displayRefreshing(booleanCaptor.capture())
        verify(mockNewsPagingView, times(2)).refreshingComplete()
        Assert.assertFalse(booleanCaptor.value)
    }

    @Test
    fun testRefreshRequestWithIncompleteRefresh() {
        // Tell presenter to pull initial article list
        val presenter = newsPagingPresenter!!
        presenter.initialArticleCheck()

        // Attach mock view to presenter
        newsPagingPresenter?.attach(mockNewsPagingView)

        // Haven't simulated refresh complete, view should still be in refreshing state
        verify(mockNewsPagingView, times(1)).displayRefreshing(booleanCaptor.capture())
        verify(mockNewsPagingView, never()).refreshingComplete()
        Assert.assertTrue(booleanCaptor.value)

        // Simulate a refresh completing
        setupDummyArticles()
        mockEventBus.post(RefreshCompleteEvent())

        // View should no longer be in "refreshing" state
        verify(mockNewsPagingView, times(2)).displayRefreshing(booleanCaptor.capture())
        verify(mockNewsPagingView, times(1)).refreshingComplete()
        Assert.assertFalse(booleanCaptor.value)

        // Simulate a view requested refresh
        presenter.requestArticleRefresh()

        // Haven't simulated refresh complete, view should still be in refreshing state
        verify(mockNewsPagingView, times(3)).displayRefreshing(booleanCaptor.capture())
        verify(mockNewsPagingView, times(1)).refreshingComplete()
        Assert.assertTrue(booleanCaptor.value)

        // Simulate another view requested refresh (should be eaten since a refresh is in progress)
        presenter.requestArticleRefresh()
        // Haven't simulated refresh complete, view should still be in refreshing state
        verify(mockNewsPagingView, times(3)).displayRefreshing(booleanCaptor.capture())
        verify(mockNewsPagingView, times(1)).refreshingComplete()
        Assert.assertTrue(booleanCaptor.value)

        // Request another refresh 1000 times to ensure calls aren't getting through
        for (i in 0 until 1000) {
            presenter.requestArticleRefresh()

            // Haven't simulated refresh complete, view should still be in refreshing state
            verify(mockNewsPagingView, times(3)).displayRefreshing(booleanCaptor.capture())
            verify(mockNewsPagingView, times(1)).refreshingComplete()
            Assert.assertTrue(booleanCaptor.value)
        }

        // Simulate a refresh complete event
        mockEventBus.post(RefreshCompleteEvent())
        // View should no longer be in "refreshing" state
        verify(mockNewsPagingView, times(4)).displayRefreshing(booleanCaptor.capture())
        verify(mockNewsPagingView, times(2)).refreshingComplete()
        Assert.assertFalse(booleanCaptor.value)
    }

    @Test
    fun testNonEmptyModelInitiation() {
        val mockArticleList = mutableListOf<Article>()
        for (i in 0 until 20) {
            // Create a list of fake articles
            mockArticleList.add(Article(ArticleSource("test", "test"), "test", "test", "test", "test", "test", "test", "test"))
        }

        `when`(mockArticleListsModel.getArticleList(ArticleDisplayType.TECH)).thenReturn(mockArticleList)
        `when`(mockArticleListsModel.getArticleList(ArticleDisplayType.SCIENCE)).thenReturn(mockArticleList)
        `when`(mockArticleListsModel.getArticleList(ArticleDisplayType.GAMING)).thenReturn(mockArticleList)

        // Tell presenter to pull initial article list, they are not empty so a refresh should NOT be started
        val presenter = newsPagingPresenter!!
        presenter.initialArticleCheck()

        // Attach mock view to presenter
        newsPagingPresenter?.attach(mockNewsPagingView)

        // Since we already have some article data in each type we should not be refreshing
        verify(mockNewsPagingView, times(1)).displayRefreshing(booleanCaptor.capture())
        verify(mockNewsPagingView, never()).refreshingComplete()
        Assert.assertFalse(booleanCaptor.value)
    }

    @Test
    fun testRefreshFailed() {
        // Tell presenter to pull initial article list
        val presenter = newsPagingPresenter!!
        presenter.initialArticleCheck()

        // Attach mock view to presenter
        newsPagingPresenter?.attach(mockNewsPagingView)

        // Haven't simulated refresh complete, view should still be in refreshing state
        verify(mockNewsPagingView, times(1)).displayRefreshing(booleanCaptor.capture())
        verify(mockNewsPagingView, never()).refreshingComplete()
        verify(mockNewsPagingView, never()).refreshingFailed()
        Assert.assertTrue(booleanCaptor.value)

        // Simulate a refresh completing without setting up dummy data
        mockEventBus.post(RefreshCompleteEvent())

        // View should no longer be in "refreshing" state
        verify(mockNewsPagingView, times(2)).displayRefreshing(booleanCaptor.capture())
        verify(mockNewsPagingView, never()).refreshingComplete()
        verify(mockNewsPagingView, times(1)).refreshingFailed()
        Assert.assertFalse(booleanCaptor.value)

        // Now let's simulate the view requesting a refresh successfully
        presenter.requestArticleRefresh()

        // Haven't simulated refresh complete, view should be in refreshing state
        verify(mockNewsPagingView, times(3)).displayRefreshing(booleanCaptor.capture())
        verify(mockNewsPagingView, never()).refreshingComplete()
        verify(mockNewsPagingView, times(1)).refreshingFailed()
        Assert.assertTrue(booleanCaptor.value)

        // Simulate a refresh complete event
        setupDummyArticles()
        mockEventBus.post(RefreshCompleteEvent())

        // View should no longer be in "refreshing" state
        verify(mockNewsPagingView, times(4)).displayRefreshing(booleanCaptor.capture())
        verify(mockNewsPagingView, times(1)).refreshingComplete()
        verify(mockNewsPagingView, times(1)).refreshingFailed()
        Assert.assertFalse(booleanCaptor.value)

        // Now let's simulate the view requesting a refresh and it failing again
        presenter.requestArticleRefresh()

        // Haven't simulated refresh complete, view should be in refreshing state
        verify(mockNewsPagingView, times(5)).displayRefreshing(booleanCaptor.capture())
        verify(mockNewsPagingView, times(1)).refreshingComplete()
        verify(mockNewsPagingView, times(1)).refreshingFailed()
        Assert.assertTrue(booleanCaptor.value)

        // Setup a failed refresh
        `when`(mockArticleListsModel.getArticleList(ArticleDisplayType.TECH)).thenReturn(mutableListOf())
        `when`(mockArticleListsModel.getArticleList(ArticleDisplayType.SCIENCE)).thenReturn(mutableListOf())
        `when`(mockArticleListsModel.getArticleList(ArticleDisplayType.GAMING)).thenReturn(mutableListOf())

        // Simulate a refresh complete event
        mockEventBus.post(RefreshCompleteEvent())

        // View should no longer be in "refreshing" state, and should have called refreshing failed
        verify(mockNewsPagingView, times(6)).displayRefreshing(booleanCaptor.capture())
        verify(mockNewsPagingView, times(1)).refreshingComplete()
        verify(mockNewsPagingView, times(2)).refreshingFailed()
        Assert.assertFalse(booleanCaptor.value)
    }


    private fun setupDummyArticles() {
        // Setup mock model to return some dummy data
        val dummyArticles = getFakeArticleModelList(10)
        `when`(mockArticleListsModel.getArticleList(ArticleDisplayType.TECH)).thenReturn(dummyArticles)
        `when`(mockArticleListsModel.getArticleList(ArticleDisplayType.SCIENCE)).thenReturn(dummyArticles)
        `when`(mockArticleListsModel.getArticleList(ArticleDisplayType.GAMING)).thenReturn(dummyArticles)
    }
}
