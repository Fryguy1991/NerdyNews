package com.chrisfry.nerdnews.business.network

import com.chrisfry.nerdnews.model.ArticleResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface NewsService {
    companion object {
        private const val NEWS_WEB_API_ENDPOINT = "https://newsapi.org/v2/"

        @Volatile private var instance: NewsService? = null

        @Synchronized
        fun getInstance(): NewsService {
            return instance ?: synchronized(this) {
                // Create OkHttpClient to add interceptor that adds headers to all our calls
                val client = OkHttpClient.Builder().addNetworkInterceptor(HeaderInterceptor()).build()

                val retrofit = Retrofit.Builder()
                    .client(client)
                    .baseUrl(NEWS_WEB_API_ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                instance
                    ?: retrofit.create(NewsService::class.java)
            }
        }
    }

    /**
     * Retrieve top headlines in the United States
     */
    @GET("top-headlines?country=us")
    fun getTopHeadlinesInUs(): Call<ArticleResponse>
}