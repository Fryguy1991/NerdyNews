package com.chrisfry.nerdnews.userinterface.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.chrisfry.nerdnews.business.network.NewsCallback
import com.chrisfry.nerdnews.business.network.NewsService
import com.chrisfry.nerdnews.model.ArticleResponse
import com.chrisfry.nerdnews.model.ResponseError
import com.chrisfry.nerdnews.userinterface.App
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = this::class.java.name
    }

    @Inject
    lateinit var newsService: NewsService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = application as App
        application.newsComponent.inject(this)

    }

    override fun onResume() {
        super.onResume()

        val headlinesCall = newsService.getTopHeadlinesInUs()
        headlinesCall.enqueue(headlinesCallback)
    }

    private val headlinesCallback = object : NewsCallback<ArticleResponse>() {
        override fun onFailure(error: ResponseError) {
            Log.e(TAG, "ERROR: ${error.code} \nMESSAGE: ${error.message}")

            // TODO: Handle failed case
        }

        override fun onResponse(response: ArticleResponse) {
            Log.d(TAG, "Retrieved ${response.articles.size} of ${response.totalResults} articles")
        }
    }
}