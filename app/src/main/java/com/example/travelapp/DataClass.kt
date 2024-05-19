package com.example.travelapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataClass(
    val AREA: String,
    val X_COORD_x: String,
    val Y_COORD_x: String,
    val price_x: String,
    val price_y: String,
    val tot_review_x: String
) : Parcelable