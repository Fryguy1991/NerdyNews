package com.chrisfry.nerdnews.business.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class NewsApi @Inject constructor() {
    private val service: NewsService

    init {
        // Create OkHttpClient to add interceptor that adds headers to all our calls
        val client = OkHttpClient.Builder().addNetworkInterceptor(HeaderInterceptor()).build()
        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(NewsService.NEWS_WEB_API_ENDPOINT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(NewsService::class.java)
    }

    fun getService(): NewsService {
        return service
    }
}