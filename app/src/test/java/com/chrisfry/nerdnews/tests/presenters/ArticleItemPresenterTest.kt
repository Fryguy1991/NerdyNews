package com.chrisfry.nerdnews.tests.presenters

import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.business.exceptions.LateArticleLoadException
import com.chrisfry.nerdnews.business.presenters.ArticleItemPresenter
import com.chrisfry.nerdnews.business.presenters.interfaces.IArticleItemPresenter
import com.chrisfry.nerdnews.model.ArticleDisplayModelParcelable
import com.chrisfry.nerdnews.tests.BaseTest
import com.chrisfry.nerdnews.utils.LogUtils
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Class for isolating and testing ArticleItemPresenter
 */
class ArticleItemPresenterTest : BaseTest() {
    companion object {
        private val TAG = ArticleItemPresenterTest::class.java.simpleName
    }

    // Few demo article display models
    private val normalArticle1 = ArticleDisplayModelParcelable(
        "The one tiny thing that made me give up my iPhone for Android",
        "Bgr.com",
        "https://boygeniusreport.files.wordpress.com/2019/03/bgr-iphone-xs-2.jpg?quality=98&strip=all",
        "Mike Wehner",
        "https://bgr.com/2019/04/09/switching-to-android-reasons-chromecast-streaming/",
        "I’ve been an iPhone user since almost the very beginning. I didn’t own an original iPhone " +
                "at launch, but began using Apple’s smartphones with the iPhone 3G and never looked back. Until " +
                "recently, that is. A few days ago, I decided to make the switch to Android… [+6036 chars]",
        "April 9, 2019 12:30 PM"
    )
    private val normalArticle2 = ArticleDisplayModelParcelable(
        "SpaceX's Falcon Heavy rocket set for first commercial launch. Here's how to watch it live online. - NBC News",
        "NBC News",
        "https://media1.s-nbcnews.com/j/newscms/2018_06/2319226/180206-spacex-ac-835p_2dac8aaa78ac1eb66098d945a4b4c843.nbcnews-fp-1200-630.jpg",
        "Denise Chow",
        "https://www.nbcnews.com/mach/science/spacex-s-falcon-heavy-rocket-set-first-commercial-launch-here-ncna992446",
        "SUBSCRIBE April 9, 2019, 3:56 PM GMT Thirteen months after its maiden flight, SpaceXs " +
                "huge Falcon Heavy rocket is being readied for its first commercial launch on Wednesday. The " +
                "230-foot-tall rocket is scheduled to lift off at 6:35 p.m. ET from the Kennedy Sp… [+1828 chars]",
        "April 9, 2019 10:56 AM"
    )

    // Presenter instance we will be testing with
    private var articleItemPresenter: IArticleItemPresenter? = null
    // Mock for view that attaches to presenter
    @Mock
    private lateinit var mockArticleItemView: ArticleItemPresenter.IArticleItemView
    // Capture object for capturing strings sent to view
    @Captor
    private lateinit var stringCaptor: ArgumentCaptor<String>

    override fun setUp() {
        super.setUp()

        // Initialize new mock objects
        MockitoAnnotations.initMocks(this)

        articleItemPresenter = ArticleItemPresenter.getInstance()
    }

    override fun tearDown() {
        super.tearDown()

        articleItemPresenter?.detach()
    }

    @Test
    fun testPresenterNotNull() {
        Assert.assertNotNull(articleItemPresenter)
    }

    @Test
    fun testAttachWithNoArticle() {
        val presenter = articleItemPresenter!!

        // Attach view to presenter
        presenter.attach(mockArticleItemView)

        // Capture data that was sent to view
        verify(mockArticleItemView, never()).displaySourceName(capture(stringCaptor))
        verify(mockArticleItemView, never()).displayTitle(capture(stringCaptor))
        verify(mockArticleItemView, never()).displayImage(capture(stringCaptor))
        verify(mockArticleItemView, never()).displayAuthor(capture(stringCaptor))
        verify(mockArticleItemView, never()).displayPublishedAt(capture(stringCaptor))
        verify(mockArticleItemView, never()).displayContent(capture(stringCaptor))
        verify(mockArticleItemView, never()).displayLinkToArticle(capture(stringCaptor))

        // View should have only been sent empty strings since article model was not set, view should also be closing
        for (value: String in stringCaptor.allValues) {
            Assert.assertEquals(AppConstants.EMPTY_STRING, value)
        }
        verify(mockArticleItemView, times(1)).closeView()
    }

    @Test
    fun testAttachWithNull() {
        val presenter = articleItemPresenter!!

        presenter.setArticleData(null)
        // Attach view to presenter
        presenter.attach(mockArticleItemView)

        // Capture data that was sent to view
        verify(mockArticleItemView, never()).displaySourceName(capture(stringCaptor))
        verify(mockArticleItemView, never()).displayTitle(capture(stringCaptor))
        verify(mockArticleItemView, never()).displayImage(capture(stringCaptor))
        verify(mockArticleItemView, never()).displayAuthor(capture(stringCaptor))
        verify(mockArticleItemView, never()).displayPublishedAt(capture(stringCaptor))
        verify(mockArticleItemView, never()).displayContent(capture(stringCaptor))
        verify(mockArticleItemView, never()).displayLinkToArticle(capture(stringCaptor))

        // View should have only been sent empty strings since article model was not set, view should also be closing
        for (value: String in stringCaptor.allValues) {
            Assert.assertEquals(AppConstants.EMPTY_STRING, value)
        }
        verify(mockArticleItemView, times(1)).closeView()
    }

    @Test
    fun testAttachWithEmptyArticle() {
        // Create empty article model and load it into presenter
        val emptyArticle = ArticleDisplayModelParcelable(
            AppConstants.EMPTY_STRING, AppConstants.EMPTY_STRING,
            AppConstants.EMPTY_STRING, AppConstants.EMPTY_STRING, AppConstants.EMPTY_STRING, AppConstants.EMPTY_STRING,
            AppConstants.EMPTY_STRING
        )

        val presenter = articleItemPresenter!!

        presenter.setArticleData(emptyArticle)
        // Attach view to presenter
        presenter.attach(mockArticleItemView)

        // Capture data that was sent to view
        verify(mockArticleItemView, never()).displaySourceName(capture(stringCaptor))
        verify(mockArticleItemView, never()).displayTitle(capture(stringCaptor))
        verify(mockArticleItemView, never()).displayImage(capture(stringCaptor))
        verify(mockArticleItemView, never()).displayAuthor(capture(stringCaptor))
        verify(mockArticleItemView, never()).displayPublishedAt(capture(stringCaptor))
        verify(mockArticleItemView, never()).displayContent(capture(stringCaptor))
        verify(mockArticleItemView, never()).displayLinkToArticle(capture(stringCaptor))

        // View should have only been sent empty strings since article model was not set, view should also be closing
        for (value: String in stringCaptor.allValues) {
            Assert.assertEquals(AppConstants.EMPTY_STRING, value)
        }
        verify(mockArticleItemView, times(1)).closeView()
    }

    @Test
    fun testAttachWithNormalArticle() {
        val presenter = articleItemPresenter!!

        presenter.setArticleData(normalArticle1)
        // Attach view to presenter
        presenter.attach(mockArticleItemView)

        // Capture data that was sent to view
        verify(mockArticleItemView, times(1)).displaySourceName(capture(stringCaptor))
        verify(mockArticleItemView, times(1)).displayTitle(capture(stringCaptor))
        verify(mockArticleItemView, times(1)).displayImage(capture(stringCaptor))
        verify(mockArticleItemView, times(1)).displayAuthor(capture(stringCaptor))
        verify(mockArticleItemView, times(1)).displayPublishedAt(capture(stringCaptor))
        verify(mockArticleItemView, times(1)).displayContent(capture(stringCaptor))
        verify(mockArticleItemView, times(1)).displayLinkToArticle(capture(stringCaptor))

        // Captured view data should match data in normal article
        Assert.assertEquals(normalArticle1.sourceName, stringCaptor.allValues[0])
        Assert.assertEquals(normalArticle1.title, stringCaptor.allValues[1])
        Assert.assertEquals(normalArticle1.imageUrl, stringCaptor.allValues[2])
        Assert.assertEquals(normalArticle1.author, stringCaptor.allValues[3])
        Assert.assertEquals(normalArticle1.publishedAt, stringCaptor.allValues[4])
        Assert.assertEquals(normalArticle1.articleContent, stringCaptor.allValues[5])
        Assert.assertEquals(normalArticle1.articleUrl, stringCaptor.allValues[6])
        // View should not be "closing"
        verify(mockArticleItemView, never()).closeView()
    }


    @Test
    fun testLoadTwoArticleBeforeAttach() {
        val presenter = articleItemPresenter!!

        // Set article data twice
        presenter.setArticleData(normalArticle1)
        presenter.setArticleData(normalArticle2)
        // Attach view to presenter
        presenter.attach(mockArticleItemView)

        // Capture data that was sent to view
        verify(mockArticleItemView, times(1)).displaySourceName(capture(stringCaptor))
        verify(mockArticleItemView, times(1)).displayTitle(capture(stringCaptor))
        verify(mockArticleItemView, times(1)).displayImage(capture(stringCaptor))
        verify(mockArticleItemView, times(1)).displayAuthor(capture(stringCaptor))
        verify(mockArticleItemView, times(1)).displayPublishedAt(capture(stringCaptor))
        verify(mockArticleItemView, times(1)).displayContent(capture(stringCaptor))
        verify(mockArticleItemView, times(1)).displayLinkToArticle(capture(stringCaptor))

        // Captured view data should match data in last article sent to presenter
        Assert.assertEquals(normalArticle2.sourceName, stringCaptor.allValues[0])
        Assert.assertEquals(normalArticle2.title, stringCaptor.allValues[1])
        Assert.assertEquals(normalArticle2.imageUrl, stringCaptor.allValues[2])
        Assert.assertEquals(normalArticle2.author, stringCaptor.allValues[3])
        Assert.assertEquals(normalArticle2.publishedAt, stringCaptor.allValues[4])
        Assert.assertEquals(normalArticle2.articleContent, stringCaptor.allValues[5])
        Assert.assertEquals(normalArticle2.articleUrl, stringCaptor.allValues[6])
        // View should not be "closing"
        verify(mockArticleItemView, never()).closeView()
    }

    @Test
    fun testLoadArticleAfterAttach() {
        val presenter = articleItemPresenter!!

        // Set article data in presenter
        presenter.setArticleData(normalArticle1)
        // Attach view to presenter
        presenter.attach(mockArticleItemView)

        // Capture data that was sent to view
        verify(mockArticleItemView, times(1)).displaySourceName(capture(stringCaptor))
        verify(mockArticleItemView, times(1)).displayTitle(capture(stringCaptor))
        verify(mockArticleItemView, times(1)).displayImage(capture(stringCaptor))
        verify(mockArticleItemView, times(1)).displayAuthor(capture(stringCaptor))
        verify(mockArticleItemView, times(1)).displayPublishedAt(capture(stringCaptor))
        verify(mockArticleItemView, times(1)).displayContent(capture(stringCaptor))
        verify(mockArticleItemView, times(1)).displayLinkToArticle(capture(stringCaptor))

        // Captured view data should match data in last article sent to presenter
        Assert.assertEquals(normalArticle1.sourceName, stringCaptor.allValues[0])
        Assert.assertEquals(normalArticle1.title, stringCaptor.allValues[1])
        Assert.assertEquals(normalArticle1.imageUrl, stringCaptor.allValues[2])
        Assert.assertEquals(normalArticle1.author, stringCaptor.allValues[3])
        Assert.assertEquals(normalArticle1.publishedAt, stringCaptor.allValues[4])
        Assert.assertEquals(normalArticle1.articleContent, stringCaptor.allValues[5])
        Assert.assertEquals(normalArticle1.articleUrl, stringCaptor.allValues[6])
        // View should not be "closing"
        verify(mockArticleItemView, never()).closeView()

        // Attempt to load an article after view has been attached
        var lateArticleLoadException: LateArticleLoadException? = null
        try {
            presenter.setArticleData(normalArticle2)
        } catch (exception: LateArticleLoadException) {
            LogUtils.error(TAG, exception.message.toString())
            lateArticleLoadException = exception
        }
        // Method should have thrown exception, it should be not null
        Assert.assertNotNull(lateArticleLoadException)
    }

    @Test
    fun testNavigateToArticle() {
        val presenter = articleItemPresenter!!

        presenter.setArticleData(normalArticle1)
        // Attach view to presenter
        presenter.attach(mockArticleItemView)

        // Method to navigate shouldn't have been called
        verify(mockArticleItemView, never()).navigateToArticleSource(capture(stringCaptor))

        // Simulate view clicked the navigate to full article button
        presenter.goToArticleClicked()

        // Method to navigate to full article should have been called, capture URL
        verify(mockArticleItemView, times(1)).navigateToArticleSource(capture(stringCaptor))
        Assert.assertEquals(normalArticle1.articleUrl, stringCaptor.value)

        // Call this method ~1000 more times just to ensure we're always getting the correct value
        for (i in 2 until 1000) {
            // Simulate view clicked the navigate to full article button
            presenter.goToArticleClicked()

            // Method to navigate to full article should have been called, capture URL
            verify(mockArticleItemView, times(i)).navigateToArticleSource(capture(stringCaptor))
            Assert.assertEquals(normalArticle1.articleUrl, stringCaptor.value)
        }
    }
}