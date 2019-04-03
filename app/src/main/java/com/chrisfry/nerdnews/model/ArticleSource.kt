package com.chrisfry.nerdnews.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ArticleSource(val id: String,
                         val name: String): Parcelable