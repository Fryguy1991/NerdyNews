package com.chrisfry.nerdnews.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Parcelable version of ArticleDisplayModel so it can be sent in Android bundles.
 * ELEMENTS SHOULD MATCH ArticleDisplayModel
 */
@Parcelize
data class ArticleDisplayModelParcelable(
    val title: String,
    val sourceName: String,
    val imageUrl: String,
    val author: String,
    val articleUrl: String,
    val articleContent: String,
    val publishedAt: String
) : Parcelable