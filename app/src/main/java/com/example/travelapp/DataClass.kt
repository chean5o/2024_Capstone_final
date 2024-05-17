package com.example.travelapp

import android.os.Parcel
import android.os.Parcelable

data class DataClass(
    val AREA: String,
    val X_COORD_x: String,
    val Y_COORD_x: String,
    val price_x: String,
    val price_y: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(AREA)
        parcel.writeString(X_COORD_x)
        parcel.writeString(Y_COORD_x)
        parcel.writeString(price_x)
        parcel.writeString(price_y)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataClass> {
        override fun createFromParcel(parcel: Parcel): DataClass {
            return DataClass(parcel)
        }

        override fun newArray(size: Int): Array<DataClass?> {
            return arrayOfNulls(size)
        }
    }
}
