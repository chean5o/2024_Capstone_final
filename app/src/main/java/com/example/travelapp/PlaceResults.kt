import android.os.Parcel
import android.os.Parcelable

data class PlaceResults(val area: String, val xCoord: String, val yCoord: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(area)
        parcel.writeString(xCoord)
        parcel.writeString(yCoord)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlaceResults> {
        override fun createFromParcel(parcel: Parcel): PlaceResults {
            return PlaceResults(parcel)
        }

        override fun newArray(size: Int): Array<PlaceResults?> {
            return arrayOfNulls(size)
        }
    }
}
