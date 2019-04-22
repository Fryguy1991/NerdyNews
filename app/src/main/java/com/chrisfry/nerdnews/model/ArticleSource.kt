package com.chrisfry.nerdnews.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Model we receive from calls to NewsAPI to represent an article source. Receiving null for values is a possibility
 */
@Parcelize
data class ArticleSource(val id: String?, val name: String?) : Parcelable