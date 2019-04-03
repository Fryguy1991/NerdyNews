package com.chrisfry.nerdnews.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Article (val source: ArticleSource,
               val author: String,
               val title: String,
               val description: String,
               val url: String,
               val urlToImage: String,
               val publishedAt: String,
               val content: String) : Parcelable