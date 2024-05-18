package com.example.travelapp
import com.example.travelapp.ui.map.Location
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface PlaceService {
    @GET("/find_map/{name}")
    fun getPlaceDetails(@Path("name") name: String): Call<Location>

    @GET("/places/{category}")
    fun getPlaces(@Path("category") category: Int): Call<List<Location>> // Location은 앞서 정의한 데이터 모델입니다.
}
