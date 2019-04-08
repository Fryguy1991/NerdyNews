package com.chrisfry.nerdnews.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Simpler model of Article used for displaying data retrieved from NewsAPI
 */
@Parcelize
data class ArticleDisplayModel(val title: String,
                               val sourceName: String,
                               val imageUrl: String,
                               val author: String,
                               val articleUrl: String,
                               val articleContent: String,
                               val publishedAt: String): Parcelable