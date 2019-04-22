package com.chrisfry.nerdnews.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Model we receive from calls to NewsAPI to represent an article. Receiving null for values is a possibility
 */
@Parcelize
data class Article(
    val source: ArticleSource,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?
) : Parcelable