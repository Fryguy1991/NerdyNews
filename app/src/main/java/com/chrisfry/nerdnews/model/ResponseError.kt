package com.chrisfry.nerdnews.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ResponseError(val status: String, val code: String, val message: String) : Parcelable