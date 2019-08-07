package com.kode.travelmantics

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TravelDeal(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var price: String = "",
    var imageUrl: String = "",
    var imageName: String = ""
) : Parcelable
