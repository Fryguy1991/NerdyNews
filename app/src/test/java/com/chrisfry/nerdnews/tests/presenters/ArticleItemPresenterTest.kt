package com.chrisfry.nerdnews.tests.presenters

import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.business.exceptions.LateArticleLoadException
import com.chrisfry.nerdnews.business.presenters.ArticleItemPresenter
import com.chrisfry.nerdnews.business.presenters.interfaces.IArticleItemPresenter
import com.chrisfry.nerdnews.model.ArticleDisplayModel
import com.chrisfry.nerdnews.tests.BaseTest
import com.chrisfry.nerdnews.utils.LogUtils
import org.junit.Assert
import org.junit.Test

class ArticleItemPresenterTest : BaseTest() {
    companion object {
        private val TAG = ArticleItemPresenterTest::class.java.name
    }

    private val normalArticle1 = ArticleDisplayModel(
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

    private val normalArticle2 = ArticleDisplayModel(
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

    class MockArticleItemView : ArticleItemPresenter.IArticleItemView {
        var sourceName = AppConstants.EMPTY_STRING
        var title = AppConstants.EMPTY_STRING
        var imageUrl = AppConstants.EMPTY_STRING
        var author = AppConstants.EMPTY_STRING
        var publishedAt = AppConstants.EMPTY_STRING
        var content = AppConstants.EMPTY_STRING
        var articleUrl = AppConstants.EMPTY_STRING
        var isViewClosing = false

        override fun displaySourceName(sourceName: String) {
            this.sourceName = sourceName
        }

        override fun displayTitle(title: String) {
            this.title = title
        }

        override fun displayImage(imageUrl: String) {
            this.imageUrl = imageUrl
        }

        override fun displayAuthor(author: String) {
            this.author = author
        }

        override fun displayPublishedAt(publishedAt: String) {
            this.publishedAt = publishedAt
        }

        override fun displayContent(content: String) {
            this.content = content
        }

        override fun displayLinkToArticle(articleUrl: String) {
            this.articleUrl = articleUrl
        }

        override fun closeView() {
            this.isViewClosing = true
        }
    }

    // Presenter instance we will be testing with
    private var articleItemPresenter: IArticleItemPresenter? = null

    // Mock for view that attaches to presenter
    private lateinit var mockArticleItemView: MockArticleItemView

    override fun setUp() {
        super.setUp()

        articleItemPresenter = ArticleItemPresenter.getInstance()
        mockArticleItemView = MockArticleItemView()
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
        // Attaching without an article should lead the presenter to try and close the view
        Assert.assertFalse(mockArticleItemView.isViewClosing)

        Assert.assertNotNull(articleItemPresenter)
        val presenter = articleItemPresenter
        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            presenter.attach(mockArticleItemView)

            // View should not have received any data and should be closing
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.sourceName)
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.title)
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.imageUrl)
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.author)
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.publishedAt)
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.content)
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.articleUrl)
            Assert.assertTrue(mockArticleItemView.isViewClosing)
        }
    }

    @Test
    fun testAttachWithNull() {
        // Attaching to presenter after sending null for article data
        Assert.assertFalse(mockArticleItemView.isViewClosing)

        Assert.assertNotNull(articleItemPresenter)
        val presenter = articleItemPresenter
        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            // Load presenter with null and attach mock view
            presenter.setArticleData(null)
            presenter.attach(mockArticleItemView)

            // View should not have received any data and should be closing
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.sourceName)
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.title)
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.imageUrl)
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.author)
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.publishedAt)
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.content)
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.articleUrl)
            Assert.assertTrue(mockArticleItemView.isViewClosing)
        }
    }

    @Test
    fun testAttachWithEmptyArticle() {
        // Create empty article model and load it into presenter
        val emptyArticle = ArticleDisplayModel(
            AppConstants.EMPTY_STRING, AppConstants.EMPTY_STRING,
            AppConstants.EMPTY_STRING, AppConstants.EMPTY_STRING, AppConstants.EMPTY_STRING, AppConstants.EMPTY_STRING,
            AppConstants.EMPTY_STRING
        )

        Assert.assertNotNull(articleItemPresenter)
        val presenter = articleItemPresenter
        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            // Load empty article into presenter and attach view
            presenter.setArticleData(emptyArticle)
            presenter.attach(mockArticleItemView)

            // View should not have received any data and should be closing
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.sourceName)
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.title)
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.imageUrl)
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.author)
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.publishedAt)
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.content)
            Assert.assertEquals(AppConstants.EMPTY_STRING, mockArticleItemView.articleUrl)
            Assert.assertTrue(mockArticleItemView.isViewClosing)
        }
    }

    @Test
    fun testAttachWithNormalArticle() {
        Assert.assertNotNull(articleItemPresenter)
        val presenter = articleItemPresenter
        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            // Load article in presenter and attach view
            presenter.setArticleData(normalArticle1)
            presenter.attach(mockArticleItemView)

            // View should be loaded with data and not closing
            Assert.assertEquals(normalArticle1.sourceName, mockArticleItemView.sourceName)
            Assert.assertEquals(normalArticle1.title, mockArticleItemView.title)
            Assert.assertEquals(normalArticle1.imageUrl, mockArticleItemView.imageUrl)
            Assert.assertEquals(normalArticle1.author, mockArticleItemView.author)
            Assert.assertEquals(normalArticle1.publishedAt, mockArticleItemView.publishedAt)
            Assert.assertEquals(normalArticle1.articleContent, mockArticleItemView.content)
            Assert.assertEquals(normalArticle1.articleUrl, mockArticleItemView.articleUrl)
            Assert.assertFalse(mockArticleItemView.isViewClosing)
        }
    }

    @Test
    fun testLoadTwoArticleBeforeAttach() {
        Assert.assertNotNull(articleItemPresenter)
        val presenter = articleItemPresenter
        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            // Load two different articles before attaching
            presenter.setArticleData(normalArticle1)
            presenter.setArticleData(normalArticle2)

            // Attach to view
            presenter.attach(mockArticleItemView)

            // View should be loaded with data from the latest article sent to presenter
            Assert.assertEquals(normalArticle2.sourceName, mockArticleItemView.sourceName)
            Assert.assertEquals(normalArticle2.title, mockArticleItemView.title)
            Assert.assertEquals(normalArticle2.imageUrl, mockArticleItemView.imageUrl)
            Assert.assertEquals(normalArticle2.author, mockArticleItemView.author)
            Assert.assertEquals(normalArticle2.publishedAt, mockArticleItemView.publishedAt)
            Assert.assertEquals(normalArticle2.articleContent, mockArticleItemView.content)
            Assert.assertEquals(normalArticle2.articleUrl, mockArticleItemView.articleUrl)
            Assert.assertFalse(mockArticleItemView.isViewClosing)
        }
    }

    @Test
    fun testLoadArticleAfterAttach() {
        Assert.assertNotNull(articleItemPresenter)
        val presenter = articleItemPresenter
        if (presenter == null) {
            Assert.assertTrue(false)
        } else {
            // Load presenter with article and attach
            presenter.setArticleData(normalArticle1)
            presenter.attach(mockArticleItemView)

            // View should be loaded with data from the latest article sent to presenter
            Assert.assertEquals(normalArticle1.sourceName, mockArticleItemView.sourceName)
            Assert.assertEquals(normalArticle1.title, mockArticleItemView.title)
            Assert.assertEquals(normalArticle1.imageUrl, mockArticleItemView.imageUrl)
            Assert.assertEquals(normalArticle1.author, mockArticleItemView.author)
            Assert.assertEquals(normalArticle1.publishedAt, mockArticleItemView.publishedAt)
            Assert.assertEquals(normalArticle1.articleContent, mockArticleItemView.content)
            Assert.assertEquals(normalArticle1.articleUrl, mockArticleItemView.articleUrl)
            Assert.assertFalse(mockArticleItemView.isViewClosing)

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
    }
}