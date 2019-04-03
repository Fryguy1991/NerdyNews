package com.chrisfry.nerdnews.userinterface.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.business.enums.NewsApiCountrys
import com.chrisfry.nerdnews.business.enums.NewsApiLanguages
import com.chrisfry.nerdnews.business.network.NewsCallback
import com.chrisfry.nerdnews.business.network.NewsService
import com.chrisfry.nerdnews.model.Article
import com.chrisfry.nerdnews.model.ArticleResponse
import com.chrisfry.nerdnews.model.ResponseError
import com.chrisfry.nerdnews.userinterface.App
import java.util.*
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

        // Test retrieve top headlines for current country, current language, and technology category
        val queryParameters = HashMap<String, String>()
        queryParameters[NewsService.KEY_COUNTRY] = NewsApiCountrys.getCountry(Locale.getDefault().country).code
        queryParameters[NewsService.KEY_LANGUAGE] = NewsApiLanguages.getLanguage(Locale.getDefault().language).code
        queryParameters[NewsService.KEY_CATEGORY] = NewsService.TECH_CATEGORY

        val headlinesCall = newsService.getTopHeadlines(queryParameters)
        headlinesCall.enqueue(headlinesCallback)
    }

    private val headlinesCallback = object : NewsCallback<ArticleResponse>() {
        override fun onFailure(error: ResponseError) {
            Log.e(TAG, "ERROR: ${error.code} \nMESSAGE: ${error.message}")

            // TODO: Handle failed case
        }

        override fun onResponse(response: ArticleResponse) {
            Log.d(TAG, "Retrieved ${response.articles.size} of ${response.totalResults} articles")

            var articlesString = AppConstants.EMPTY_STRING
            for (article: Article in response.articles) {
                articlesString += "\n${article.title}"
            }

            Log.d(TAG, "Retrieved articles:$articlesString")
        }
    }
}