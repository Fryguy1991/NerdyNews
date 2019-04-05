package com.chrisfry.nerdnews.business.network

import com.chrisfry.nerdnews.model.ArticleResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.QueryMap
import javax.inject.Inject

interface NewsService {
    companion object {
        private const val NEWS_WEB_API_ENDPOINT = "https://newsapi.org/v2/"

        @Volatile private var instance: NewsService? = null

        @Inject
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

                instance ?: retrofit.create(NewsService::class.java)
            }
        }

        // Request parameter keys
        const val KEY_CATEGORY = "category"
        const val KEY_COUNTRY = "country"
        const val KEY_DOMAINS = "domains"
        const val KEY_EXCLUDE_DOMAINS = "excludeDomains"
        const val KEY_FROM = "from"
        const val KEY_LANGUAGE = "language"
        const val KEY_PAGE = "page"
        const val KEY_PAGE_SIZE = "pageSize"
        const val KEY_SEARCH_TERM = "q"
        const val KEY_SORTBY = "sortby"
        const val KEY_SOURCES = "sources"
        const val KEY_TO = "to"

        // Possible category values
        const val BUSINESS_CATEGORY = "business"
        const val ENTERTAINMENT_CATEGORY = "entertainment"
        const val GENERAL_CATEGORY = "general"
        const val HEALTH_CATEGORY = "health"
        const val SCIENCE_CATEGORY = "science"
        const val SPORTS_CATEGORY = "sports"
        const val TECH_CATEGORY = "technology"

        // For possible language values see NewsApiLanguages enum

        // Possible sort by values
        const val SORTBY_RELEVANCY = "relevancy"
        const val SORTBY_POPULARITY = "popularity"
        const val SORTBY_PUBLISHED_AT = "publishedAt"
    }

    /**
     * Retrieve top headlines using query map
     * (see https://newsapi.org/docs/endpoints/top-headlines for request parameters)
     *
     * @param options: Map containing request parameters
     */
    @GET("top-headlines")
    fun getTopHeadlines(@QueryMap options: Map<String, String>): Call<ArticleResponse>

    /**
     * Retrieves all articles depending on options provided
     * (see https://newsapi.org/docs/endpoints/everything for request parameters)
     *
     * @param options: Map containing request parameters
     */
    @GET("everything")
    fun getEverything(@QueryMap options: Map<String, String>): Call<ArticleResponse>

    // TODO: Currently don't need to get sources, in order to use will need to write SourcesResponse
    // When writing SourcesResponse see https://newsapi.org/docs/endpoints/sources
//    @GET("sources")
//    fun getSources(@QueryMap options: Map<String, String>): Call<SourcesResponse>
}