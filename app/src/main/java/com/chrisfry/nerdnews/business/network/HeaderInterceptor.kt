package com.chrisfry.nerdnews.business.network

import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor : Interceptor {
    companion object {
        private val TAG = this::class.java.name
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val NERDY_NEWS_API_KEY = "7e9db9621b514351863841ea59dc84e4+"
    }
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Add authorization header and api key to every request
        val request = originalRequest.newBuilder()
            .header(AUTHORIZATION_HEADER, NERDY_NEWS_API_KEY)
            .build()

        return  chain.proceed(request)
    }
}