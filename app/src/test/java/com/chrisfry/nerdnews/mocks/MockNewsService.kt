package com.chrisfry.nerdnews.mocks

import com.chrisfry.nerdnews.TestUtils
import com.chrisfry.nerdnews.business.network.NewsCallback
import com.chrisfry.nerdnews.business.network.NewsService
import com.chrisfry.nerdnews.model.ArticleResponse
import com.chrisfry.nerdnews.model.ResponseError
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception


/**
 * Mock class for NewsService that is injected into the presenter
 */
class MockNewsService : NewsService {
    // Add variable so we can change the responses type that will be received
    var responseType = MockResponseType.SUCCESS

    // Fake retrofit used for converting responses
    private val fakeRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(NewsService.NEWS_WEB_API_ENDPOINT)
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient())
        .build()

    // Some mock responses we want to provide
    private val mockSuccessResponse: ArticleResponse
    private val mockErrorResponse: ResponseError

    // List of callbacks
    private val callbackList= mutableListOf<CallbackObject>()

    init {
        // Setup converters
        val articleResponseConverter =
            fakeRetrofit.responseBodyConverter<ArticleResponse>(ArticleResponse::class.java, arrayOf())
        val responseErrorConverter =
            fakeRetrofit.responseBodyConverter<ResponseError>(ResponseError::class.java, arrayOf())

        // Setup mock success
        var responseBody =
            ResponseBody.create(MediaType.parse("application/json"), TestUtils.readJsonFile("successResponse1.json"))
        var successConvertOutput = articleResponseConverter.convert(responseBody)
        if (successConvertOutput == null) {
            throw Exception("Test Setup Failed")
        } else {
            mockSuccessResponse = successConvertOutput
        }

        // Setup mock error
        responseBody =
            ResponseBody.create(MediaType.parse("application/json"), TestUtils.readJsonFile("errorResponse1.json"))
        val errorConvertOutput = responseErrorConverter.convert(responseBody)
        if (errorConvertOutput == null) {
            throw Exception("Test Setup Failed")
        } else {
            mockErrorResponse = errorConvertOutput
        }
    }

    override fun getEverything(options: Map<String, String>): Call<ArticleResponse> {
        return ResponseMockNewsCall()
    }

    override fun getTopHeadlines(options: Map<String, String>): Call<ArticleResponse> {
        return ResponseMockNewsCall()
    }

    /**
     * Mock call class to simulate responses provided by NewsService
     */
    inner class ResponseMockNewsCall : Call<ArticleResponse> {
        override fun enqueue(callback: Callback<ArticleResponse>) {
            // Hold onto callbacks until we're asked to fire them
            if (callback is NewsCallback) {
                callbackList.add(CallbackObject(callback, responseType))
            }
        }

        override fun isExecuted(): Boolean {
            return true
        }

        override fun clone(): Call<ArticleResponse> {
            return this
        }

        override fun isCanceled(): Boolean {
            return false
        }

        override fun cancel() {
            // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun execute(): Response<ArticleResponse> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun request(): Request {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    enum class MockResponseType(val isSuccessful: Boolean) {
        SUCCESS(true),
        ERROR(false)
    }

    data class CallbackObject(val callback: NewsCallback<ArticleResponse>, val responseType: MockResponseType)

    fun fireCallbacks() {
        for (callbackEvent: CallbackObject in callbackList) {
            when (callbackEvent.responseType) {
                MockResponseType.SUCCESS -> callbackEvent.callback.onResponse(mockSuccessResponse)
                MockResponseType.ERROR -> callbackEvent.callback.onFailure(mockErrorResponse)
            }
        }
        clearCallbacks()
    }

    fun clearCallbacks() {
        callbackList.clear()
    }
}

