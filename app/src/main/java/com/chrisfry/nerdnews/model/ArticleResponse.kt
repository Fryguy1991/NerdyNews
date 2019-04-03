package com.chrisfry.nerdnews.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ArticleResponse(val status: String,
                      val totalResults: Int,
                      val articles: List<Article>) : Parcelable