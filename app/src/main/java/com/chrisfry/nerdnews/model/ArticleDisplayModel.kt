package com.chrisfry.nerdnews.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Model containing the useful information for displaying an article. Calls to NewsAPI can retrieve null for values
 * but these are replaced with empty strings in this model (AppConstants.EMPTY_STRING)
 */
@Parcelize
data class ArticleDisplayModel(
    val title: String,
    val sourceName: String,
    val imageUrl: String,
    val author: String,
    val articleUrl: String,
    val articleContent: String,
    val publishedAt: String
) : Parcelable