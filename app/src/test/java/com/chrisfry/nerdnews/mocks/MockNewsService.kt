package com.chrisfry.nerdnews.mocks

import com.chrisfry.nerdnews.TestUtils
import com.chrisfry.nerdnews.business.network.NewsCallback
import com.chrisfry.nerdnews.business.network.NewsService
import com.chrisfry.nerdnews.model.ArticleResponse
import com.chrisfry.nerdnews.model.ResponseError
import com.chrisfry.nerdnews.utils.LogUtils
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
    companion object {
        private val TAG = MockNewsService::class.java.simpleName
    }

    // Add variable so we can change the responses type that will be received
    var responseType = MockResponseType.SUCCESS_1

    // Fake retrofit used for converting responses
    private val fakeRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(NewsService.NEWS_WEB_API_ENDPOINT)
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient())
        .build()

    // Some mock responses we want to provide
    private val mockSuccessResponse1: ArticleResponse
    private val mockSuccessResponse2: ArticleResponse
    private val mockApiKeyErrorResponse: ResponseError

    // List of callbacks
    private val callbackList= mutableListOf<CallbackObject>()

    init {
        // Setup converters
        val articleResponseConverter =
            fakeRetrofit.responseBodyConverter<ArticleResponse>(ArticleResponse::class.java, arrayOf())
        val responseErrorConverter =
            fakeRetrofit.responseBodyConverter<ResponseError>(ResponseError::class.java, arrayOf())

        // Setup mock success 1
        var responseBody =
            ResponseBody.create(MediaType.parse("application/json"), TestUtils.readJsonFile("SuccessResponse1.json"))
        var successConvertOutput = articleResponseConverter.convert(responseBody)
        if (successConvertOutput == null) {
            throw Exception("Test Setup Failed")
        } else {
            mockSuccessResponse1 = successConvertOutput
        }

        // Setup mock success 2
        responseBody =
            ResponseBody.create(MediaType.parse("application/json"), TestUtils.readJsonFile("SuccessResponse2.json"))
        successConvertOutput = articleResponseConverter.convert(responseBody)
        if (successConvertOutput == null) {
            throw Exception("Test Setup Failed")
        } else {
            mockSuccessResponse2 = successConvertOutput
        }

        // Setup mock api key error
        responseBody =
            ResponseBody.create(MediaType.parse("application/json"), TestUtils.readJsonFile("ApiKeyErrorResponse.json"))
        val errorConvertOutput = responseErrorConverter.convert(responseBody)
        if (errorConvertOutput == null) {
            throw Exception("Test Setup Failed")
        } else {
            mockApiKeyErrorResponse = errorConvertOutput
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
            LogUtils.debug(TAG, "Not currently handling in this test")
            return true
        }

        override fun clone(): Call<ArticleResponse> {
            LogUtils.debug(TAG, "Not currently handling in this test")
            return this
        }

        override fun isCanceled(): Boolean {
            LogUtils.debug(TAG, "Not currently handling in this test")
            return false
        }

        override fun cancel() {
            LogUtils.debug(TAG, "Not currently handling in this test")
        }

        override fun execute(): Response<ArticleResponse> {
            TODO("Not currently handling in this test")
        }

        override fun request(): Request {
            TODO("Not currently handling in this test")
        }
    }

    /**
     * Response types that this service will serve
     */
    enum class MockResponseType {
        SUCCESS_1,
        SUCCESS_2,
        API_KEY_ERROR
    }

    /**
     * Data class for containing a callback object and provided response
     *
     * @param callback: Callback that will receive the response when callbacks are fired
     * @param responseType: Response type to be provided by the callback
     */
    data class CallbackObject(val callback: NewsCallback<ArticleResponse>, val responseType: MockResponseType)

    /**
     * Mock news services fires all stored callbacks
     */
    fun fireCallbacks() {
        for (callbackEvent: CallbackObject in callbackList) {
            when (callbackEvent.responseType) {
                MockResponseType.SUCCESS_1 -> callbackEvent.callback.onResponse(mockSuccessResponse1)
                MockResponseType.SUCCESS_2 -> callbackEvent.callback.onResponse(mockSuccessResponse2)
                MockResponseType.API_KEY_ERROR -> callbackEvent.callback.onFailure(mockApiKeyErrorResponse)
            }
        }
        clearCallbacks()
    }

    /**
     * Mock news service removes all stored callbacks (without firing them)
     */
    fun clearCallbacks() {
        callbackList.clear()
    }
}

