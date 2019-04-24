package com.chrisfry.nerdnews.tests.network

import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.events.MoreArticleEvent
import com.chrisfry.nerdnews.business.events.RefreshCompleteEvent
import com.chrisfry.nerdnews.business.network.INewsApi
import com.chrisfry.nerdnews.business.network.NewsApi
import com.chrisfry.nerdnews.tests.BaseTest
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.greenrobot.eventbus.EventBus
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class NewsApiTest: BaseTest() {

    // TODO: Currently testing with actual NewsService and ArticleDataModel. Suggesting injecting these into API so we
    // can mock them and not rely on testing while pulling actual data.

    // NewsApi instance we will be testing with
    private var newsApi: INewsApi? = null
    // Mock for interacting with event bus
    @Mock
    private lateinit var mockEventBus: EventBus
    @Captor
    private lateinit var eventCaptor: ArgumentCaptor<Any>

    override fun setUp() {
        super.setUp()

        // Initialize mockito mocks
        MockitoAnnotations.initMocks(this)

        newsApi = NewsApi(mockEventBus)
    }

    @After
    fun tearDown() {
        newsApi = null
    }

    @Test
    fun testApiNotNull() {
        Assert.assertNotNull(newsApi)
    }

    @Test
    fun testArticleRefresh() {
        val api = newsApi!!

        // Request article refresh from news API
        api.requestArticleRefresh()

        // Add a sleep so actual article data is retrieved THIS IS FRAGILE!!!!
        Thread.sleep(2000)

        // Should have received a refresh complete event
        verify(mockEventBus, times(1)).post(capture(eventCaptor))
        Assert.assertTrue(eventCaptor.value is RefreshCompleteEvent)

        // Request another article refresh from news API
        api.requestArticleRefresh()

        // Add a sleep so actual article data is retrieved THIS IS FRAGILE!!!!
        Thread.sleep(2000)

        // Should have received a refresh complete event
        verify(mockEventBus, times(2)).post(capture(eventCaptor))
        Assert.assertTrue(eventCaptor.value is RefreshCompleteEvent)

        // Request one more article refresh from news API
        api.requestArticleRefresh()

        // Add a sleep so actual article data is retrieved THIS IS FRAGILE!!!!
        Thread.sleep(2000)

        // Should have received a refresh complete event
        verify(mockEventBus, times(3)).post(capture(eventCaptor))
        Assert.assertTrue(eventCaptor.value is RefreshCompleteEvent)
    }

    @Test
    fun testMoreArticleRequest() {
        val api = newsApi!!

        // Request article refresh from news API
        api.requestArticleRefresh()

        // Add a sleep so actual article data is retrieved THIS IS FRAGILE!!!!
        Thread.sleep(2000)

        // Should have received a refresh complete event
        verify(mockEventBus, times(1)).post(capture(eventCaptor))
        Assert.assertTrue(eventCaptor.value is RefreshCompleteEvent)

        // Request more TECH articles
        api.requestMoreArticles(ArticleDisplayType.TECH)

        // Add a sleep so actual article data is retrieved THIS IS FRAGILE!!!!
        Thread.sleep(2000)

        // Should have received more article event for tech articles
        verify(mockEventBus, times(2)).post(capture(eventCaptor))
        Assert.assertTrue(eventCaptor.value is MoreArticleEvent)
        Assert.assertEquals(ArticleDisplayType.TECH, (eventCaptor.value as MoreArticleEvent).articleType)

        // Request more SCIENCE articles
        api.requestMoreArticles(ArticleDisplayType.SCIENCE)

        // Add a sleep so actual article data is retrieved THIS IS FRAGILE!!!!
        Thread.sleep(2000)

        // Should have received more article event for science articles
        verify(mockEventBus, times(3)).post(capture(eventCaptor))
        Assert.assertTrue(eventCaptor.value is MoreArticleEvent)
        Assert.assertEquals(ArticleDisplayType.SCIENCE, (eventCaptor.value as MoreArticleEvent).articleType)

        // Request more GAMING articles
        api.requestMoreArticles(ArticleDisplayType.GAMING)

        // Add a sleep so actual article data is retrieved THIS IS FRAGILE!!!!
        Thread.sleep(2000)

        // Should have received more article event for gaming articles
        verify(mockEventBus, times(4)).post(capture(eventCaptor))
        Assert.assertTrue(eventCaptor.value is MoreArticleEvent)
        Assert.assertEquals(ArticleDisplayType.GAMING, (eventCaptor.value as MoreArticleEvent).articleType)
    }

    @Test
    fun testMoreArticleRequestWithoutRefresh() {
        // NewsApi is designed to have a refresh called before more articles are requested, but this is currently possible
        // TODO: Eat more article requests if we haven't had a successful refresh?
        val api = newsApi!!

        // Request more TECH articles
        api.requestMoreArticles(ArticleDisplayType.TECH)

        // Add a sleep so actual article data is retrieved THIS IS FRAGILE!!!!
        Thread.sleep(2000)

        // Should have received more article event for tech articles
        verify(mockEventBus, times(1)).post(capture(eventCaptor))
        Assert.assertTrue(eventCaptor.value is MoreArticleEvent)
        Assert.assertEquals(ArticleDisplayType.TECH, (eventCaptor.value as MoreArticleEvent).articleType)

        // Request more SCIENCE articles
        api.requestMoreArticles(ArticleDisplayType.SCIENCE)

        // Add a sleep so actual article data is retrieved THIS IS FRAGILE!!!!
        Thread.sleep(2000)

        // Should have received more article event for science articles
        verify(mockEventBus, times(2)).post(capture(eventCaptor))
        Assert.assertTrue(eventCaptor.value is MoreArticleEvent)
        Assert.assertEquals(ArticleDisplayType.SCIENCE, (eventCaptor.value as MoreArticleEvent).articleType)

        // Request more GAMING articles
        api.requestMoreArticles(ArticleDisplayType.GAMING)

        // Add a sleep so actual article data is retrieved THIS IS FRAGILE!!!!
        Thread.sleep(2000)

        // Should have received more article event for gaming articles
        verify(mockEventBus, times(3)).post(capture(eventCaptor))
        Assert.assertTrue(eventCaptor.value is MoreArticleEvent)
        Assert.assertEquals(ArticleDisplayType.GAMING, (eventCaptor.value as MoreArticleEvent).articleType)
    }
}