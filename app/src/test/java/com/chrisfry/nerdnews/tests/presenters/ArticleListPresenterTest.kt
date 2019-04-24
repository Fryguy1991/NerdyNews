package com.chrisfry.nerdnews.tests.presenters

import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.events.RefreshCompleteEvent
import com.chrisfry.nerdnews.business.events.MoreArticleEvent
import com.chrisfry.nerdnews.business.network.INewsApi
import com.chrisfry.nerdnews.business.presenters.ArticleListPresenter
import com.chrisfry.nerdnews.business.presenters.interfaces.IArticleListPresenter
import com.chrisfry.nerdnews.model.Article
import com.chrisfry.nerdnews.model.ArticleDisplayModel
import com.chrisfry.nerdnews.model.ArticleSource
import com.chrisfry.nerdnews.model.IArticleListsModel
import com.chrisfry.nerdnews.tests.BaseTest
import com.nhaarman.mockitokotlin2.capture
import org.greenrobot.eventbus.EventBus
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.mockito.*
import org.mockito.Mockito.*

/**
 * Class for isolating and testing ArticleListPresenter
 */
class ArticleListPresenterTest : BaseTest() {
    companion object {
        private val TAG = ArticleListPresenterTest::class.java.simpleName
    }

    // Presenter instance we till use to test
    private var articleListPresenter: IArticleListPresenter? = null
    // Mock model to test presenter with
    @Mock
    private lateinit var mockArticleModel: IArticleListsModel
    // Mock news api object presenter will interract with
    @Mock
    private lateinit var mockNewsApi: INewsApi
    // Mock view object to interact with presenter
    @Mock
    private lateinit var mockArticleListView: ArticleListPresenter.IArticleListView
    // Capture object for seeing what articles are passed to view
    @Captor
    private lateinit var articleListCaptor: ArgumentCaptor<List<ArticleDisplayModel>>
    // Mock for interacting with event bus
    private var mockEventBus = EventBus.getDefault()

    override fun setUp() {
        super.setUp()

        // Initialize new mock objects
        MockitoAnnotations.initMocks(this)

        // TODO: Currently testing presenter with aricle type TECH, consider testing others
        // Not priority tho as function is the same no matter the article type

        // Instantiate presenter
        val presenter = ArticleListPresenter(ArticleDisplayType.TECH)
        presenter.articleModelInstance = mockArticleModel
        presenter.newsApiInstance = mockNewsApi
        presenter.eventBus = mockEventBus
        presenter.postDependencyInitiation()

        articleListPresenter = presenter
    }

    @After
    fun tearDown() {
        articleListPresenter?.detach()
        articleListPresenter?.breakDown()
    }

    @Test
    fun testPresenterNotNull() {
        Assert.assertNotNull(articleListPresenter)
    }

    @Test
    fun testAttachView() {
        val presenter = articleListPresenter!!

        presenter.attach(mockArticleListView)

        // Nothing happens on attach, values should be default
        verify(mockArticleListView, never()).noMoreArticlesAvailable()
        verify(mockArticleListView, never()).displayNoArticles()
        verify(mockArticleListView, never()).displayArticles(capture(articleListCaptor))
    }

    @Test
    fun testAttachWithNoArticles() {
        val presenter = articleListPresenter!!

        presenter.attach(mockArticleListView)

        // Nothing happpens on attach, values should be default
        verify(mockArticleListView, never()).noMoreArticlesAvailable()
        verify(mockArticleListView, never()).displayNoArticles()
        verify(mockArticleListView, never()).displayArticles(capture(articleListCaptor))

        // Request articles from presenter
        presenter.requestArticles()

        // No articles loaded in model. View should be displaying no articles
        verify(mockArticleListView, never()).noMoreArticlesAvailable()
        verify(mockArticleListView, times(1)).displayNoArticles()
        verify(mockArticleListView, never()).displayArticles(capture(articleListCaptor))
    }

    @Test
    fun testAttachWithArticles() {
        // Setup model get article list method to return 20 fake articles
        val fakeArticleList = getFakeArticleModelList(20)
        `when`(mockArticleModel.getArticleList(ArticleDisplayType.TECH)).thenReturn(fakeArticleList)

        val presenter = articleListPresenter!!
        presenter.attach(mockArticleListView)

        // Nothing happpens on attach, values should be default
        verify(mockArticleListView, never()).noMoreArticlesAvailable()
        verify(mockArticleListView, never()).displayNoArticles()
        verify(mockArticleListView, never()).displayArticles(capture(articleListCaptor))

        // Simulate view requesting articles
        presenter.requestArticles()

        // No articles loaded in model. View should be displaying no articles
        verify(mockArticleListView, never()).noMoreArticlesAvailable()
        verify(mockArticleListView, never()).displayNoArticles()
        verify(mockArticleListView, times(1)).displayArticles(capture(articleListCaptor))
        Assert.assertEquals(20, articleListCaptor.value.size)
    }


    @Test
    fun testRequestMoreArticles() {
        // Setup model get article list method to return 50 fake articles
        var fakeArticleList = getFakeArticleModelList(50)
        `when`(mockArticleModel.getArticleList(ArticleDisplayType.TECH)).thenReturn(fakeArticleList)

        val presenter = articleListPresenter!!
        presenter.attach(mockArticleListView)

        // Nothing happpens on attach, values should be default
        verify(mockArticleListView, never()).noMoreArticlesAvailable()
        verify(mockArticleListView, never()).displayNoArticles()
        verify(mockArticleListView, never()).displayArticles(capture(articleListCaptor))

        // Simulate view requesting articles
        presenter.requestArticles()

        // View should be displaying 50 articles
        verify(mockArticleListView, never()).noMoreArticlesAvailable()
        verify(mockArticleListView, never()).displayNoArticles()
        verify(mockArticleListView, times(1)).displayArticles(capture(articleListCaptor))
        Assert.assertEquals(50, articleListCaptor.value.size)

        // Setup model get article list method to return 50 MORE fake articles
        fakeArticleList = getFakeArticleModelList(100)
        `when`(mockArticleModel.getArticleList(ArticleDisplayType.TECH)).thenReturn(fakeArticleList)

        // Simulate view requesting more articles
        presenter.requestMoreArticles()

        // Notify presenter more articles are available
        mockEventBus.post(MoreArticleEvent(ArticleDisplayType.TECH))

        // View should be displaying 100 articles
        verify(mockArticleListView, never()).noMoreArticlesAvailable()
        verify(mockArticleListView, never()).displayNoArticles()
        verify(mockArticleListView, times(2)).displayArticles(capture(articleListCaptor))
        Assert.assertEquals(100, articleListCaptor.value.size)

        // Let's do this a bunch of times (as if we've requested 5000 articles)
        for (i in 3..100) {
            fakeArticleList = getFakeArticleModelList(50 * i)
            `when`(mockArticleModel.getArticleList(ArticleDisplayType.TECH)).thenReturn(fakeArticleList)

            // Simulate view requesting more articles
            presenter.requestMoreArticles()
            // Notify presenter more articles are available
            mockEventBus.post(MoreArticleEvent(ArticleDisplayType.TECH))

            // View should be displaying i * 50 articles
            verify(mockArticleListView, never()).noMoreArticlesAvailable()
            verify(mockArticleListView, never()).displayNoArticles()
            verify(mockArticleListView, times(i)).displayArticles(capture(articleListCaptor))
            Assert.assertEquals(i * 50, articleListCaptor.value.size)
        }
    }

    @Test
    fun testRefreshArticles() {
        // Setup model get article list method to return 50 fake articles
        var fakeArticleList = getFakeArticleModelList(50)
        `when`(mockArticleModel.getArticleList(ArticleDisplayType.TECH)).thenReturn(fakeArticleList)

        val presenter = articleListPresenter!!
        presenter.attach(mockArticleListView)

        // Nothing happpens on attach, values should be default
        verify(mockArticleListView, never()).noMoreArticlesAvailable()
        verify(mockArticleListView, never()).displayNoArticles()
        verify(mockArticleListView, never()).displayArticles(capture(articleListCaptor))

        // Simulate view requesting articles
        presenter.requestArticles()

        // View should be displaying 50 articles
        verify(mockArticleListView, never()).noMoreArticlesAvailable()
        verify(mockArticleListView, never()).displayNoArticles()
        verify(mockArticleListView, times(1)).displayArticles(capture(articleListCaptor))
        Assert.assertEquals(50, articleListCaptor.value.size)

        // Setup model get article list method to return 20 fake articles (different size to test refresh)
        fakeArticleList = getFakeArticleModelList(20)
        `when`(mockArticleModel.getArticleList(ArticleDisplayType.TECH)).thenReturn(fakeArticleList)

        // Simulate refresh complete event
        mockEventBus.post(RefreshCompleteEvent())

        // View should be displaying the 20 new fake articles
        // (presenter receives event and since the view is attached pushes data to view)
        verify(mockArticleListView, never()).noMoreArticlesAvailable()
        verify(mockArticleListView, never()).displayNoArticles()
        verify(mockArticleListView, times(2)).displayArticles(capture(articleListCaptor))
        Assert.assertEquals(20, articleListCaptor.value.size)
    }


    @Test
    fun testNoMoreArticlesAvailable() {
        // Setup model get article list method to return 50 fake articles
        var fakeArticleList = getFakeArticleModelList(50)
        `when`(mockArticleModel.getArticleList(ArticleDisplayType.TECH)).thenReturn(fakeArticleList)

        val presenter = articleListPresenter!!
        presenter.attach(mockArticleListView)

        // Nothing happens on attach, values should be default
        verify(mockArticleListView, never()).noMoreArticlesAvailable()
        verify(mockArticleListView, never()).displayNoArticles()
        verify(mockArticleListView, never()).displayArticles(capture(articleListCaptor))

        // Simulate view requesting articles
        presenter.requestArticles()

        // View should be displaying 50 articles
        verify(mockArticleListView, never()).noMoreArticlesAvailable()
        verify(mockArticleListView, never()).displayNoArticles()
        verify(mockArticleListView, times(1)).displayArticles(capture(articleListCaptor))
        Assert.assertEquals(50, articleListCaptor.value.size)

        // Setup model get article list method to return 50 MORE fake articles
        fakeArticleList = getFakeArticleModelList(100)
        `when`(mockArticleModel.getArticleList(ArticleDisplayType.TECH)).thenReturn(fakeArticleList)

        // Simulate view requesting more articles
        presenter.requestMoreArticles()
        // Notify presenter more articles are available
        mockEventBus.post(MoreArticleEvent(ArticleDisplayType.TECH))

        // View should be displaying 100 articles
        verify(mockArticleListView, never()).noMoreArticlesAvailable()
        verify(mockArticleListView, never()).displayNoArticles()
        verify(mockArticleListView, times(2)).displayArticles(capture(articleListCaptor))
        Assert.assertEquals(100, articleListCaptor.value.size)

        // Simulate view requesting more articles
        presenter.requestMoreArticles()
        // Notify presenter more articles are available (even though there aren't)
        mockEventBus.post(MoreArticleEvent(ArticleDisplayType.TECH))

        // View should still be displaying 100 articles, no more articles available should have been called
        verify(mockArticleListView, times(1)).noMoreArticlesAvailable()
        verify(mockArticleListView, never()).displayNoArticles()
        verify(mockArticleListView, times(2)).displayArticles(capture(articleListCaptor))
        Assert.assertEquals(100, articleListCaptor.value.size)

        // Let's request a bunch more times to ensure calls aren't getting through
        // (Presenter should ignore more article requests when it knows there are no more articles to display)
        for (i in 2 until 1000) {
            // Simulate view requesting more articles
            presenter.requestMoreArticles()
            // Notify presenter more articles are available (even though there aren't)
            mockEventBus.post(MoreArticleEvent(ArticleDisplayType.TECH))

            // View should still be displaying 100 articles, no more articles available should have been called
            verify(mockArticleListView, times(i)).noMoreArticlesAvailable()
            verify(mockArticleListView, never()).displayNoArticles()
            verify(mockArticleListView, times(2)).displayArticles(capture(articleListCaptor))
            Assert.assertEquals(100, articleListCaptor.value.size)
        }
    }

    @Test
    fun testRealisticArticleData() {
        // Create some mock data that looks like an actual article and set to be returned
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
        val articleList = mutableListOf(article1, article2, article3)
        `when`(mockArticleModel.getArticleList(ArticleDisplayType.TECH)).thenReturn(articleList)

        val presenter = articleListPresenter!!
        presenter.attach(mockArticleListView)

        // Nothing happens on attach, values should be default
        verify(mockArticleListView, never()).noMoreArticlesAvailable()
        verify(mockArticleListView, never()).displayNoArticles()
        verify(mockArticleListView, never()).displayArticles(capture(articleListCaptor))

        // Simulate view requesting articles
        presenter.requestArticles()

        // View should be displaying our 3 "realistic" articles
        verify(mockArticleListView, never()).noMoreArticlesAvailable()
        verify(mockArticleListView, never()).displayNoArticles()
        verify(mockArticleListView, times(1)).displayArticles(capture(articleListCaptor))
        Assert.assertEquals(3, articleListCaptor.value.size)

        // Titles have source trimmed from the end. They should now appear as:
        val dataList = articleListCaptor.value
        Assert.assertEquals("Here's What Could Sink Uber, According to Uber ", dataList[0].title)
        Assert.assertEquals("Android-Dev-Takes-On-Different-Kind-of-Writing ", dataList[1].title)
        Assert.assertEquals("Disney CEO calls social media a ‘powerful marketing tool’ for extremism ", dataList[2].title)

        // Test article data view has received
        for(i in 0 until 3) {
            val demoData = articleList[i]
            val viewData = articleListCaptor.value[i]

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