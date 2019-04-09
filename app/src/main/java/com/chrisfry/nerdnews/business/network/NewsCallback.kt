package com.chrisfry.nerdnews.business.network

import com.chrisfry.nerdnews.model.ResponseError
import com.chrisfry.nerdnews.utils.LogUtils
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

/**
 * Class to extend when you want to listen to callbacks from NewsService
 */
abstract class NewsCallback<T> : Callback<T> {
    companion object {
        private val TAG = NewsCallback::class.java.name
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (response.isSuccessful) {
            // Ensure body is non null before calling the response method, else throw an exception
            val body = response.body()
            if (body == null) {
                val exception = ResponseException("Response body is null")
                onFailure(call, exception)
            } else {
                onResponse(body)
            }
        } else {
            LogUtils.error(TAG, "${response.code()}: ${response.message()}")

            val errorBody = response.errorBody()
            if (errorBody == null) {
                // Ensure error body is non null before attempting to decode JSON, else throw an exception
                val exception = ResponseException("Could not decode JSON, error body is null")
                onFailure(call, exception)
            } else {
                try {
                    val gson = Gson()
                    val errorString = errorBody.string()
                    val responseError: ResponseError = gson.fromJson(errorString, ResponseError::class.java)
                    onFailure(responseError)
                } catch (exception: JsonSyntaxException) {
                    onFailure(call, exception)
                }
            }
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        LogUtils.error(TAG, "Error: ${t.message}")
    }

    abstract fun onResponse(response: T)

    abstract fun onFailure(error: ResponseError)

    class ResponseException(message: String) : Exception(message)
}