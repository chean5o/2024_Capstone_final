package com.example.travelapp
import com.example.travelapp.ui.home.MainInfoResponse
import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.GET
import com.example.travelapp.ui.map.Location
import okhttp3.ResponseBody

interface PlaceService {
    @GET("/find_map/{name}")
    fun getPlaceDetails(@Path("name") name: String): Call<List<Location>>

    @GET("/places/{category}")
    fun getPlaces(@Path("category") category: Int): Call<List<Location>> // Location은 앞서 정의한 데이터 모델입니다.

    @GET("/main_imageurl")
    fun getMainInfoAndImage(): Call<MainInfoResponse>

    @GET("/find_map_img/{name}")
    fun ImageData(@Path("name") name: String): Call<ResponseBody>
}