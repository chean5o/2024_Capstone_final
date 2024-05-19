package com.example.travelapp
import com.example.travelapp.ui.map.Location
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import okhttp3.ResponseBody

interface PlaceService {
    @GET("/find_map/{name}")
    fun getPlaceDetails(@Path("name") name: String): Call<List<Location>>

    @GET("/places/{category}")
    fun getPlaces(@Path("category") category: Int): Call<List<Location>> // Location은 앞서 정의한 데이터 모델입니다.

    @GET("imageurl") // 여기에 노드 서버의 이미지 URL을 제공하는 API 경로를 입력하세요.
    fun getImageUrl(): Call<ResponseBody>
}
