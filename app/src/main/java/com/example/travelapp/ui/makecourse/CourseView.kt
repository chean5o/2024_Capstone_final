package com.example.travelapp.ui.makecourse
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.travelapp.databinding.FragmentCourseViewBinding
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapPolyline
import net.daum.mf.map.api.MapView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class CourseView : Fragment() {

    private var _binding: FragmentCourseViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = MapView(requireContext())
        binding.mapView.addView(mapView)

        // API 호출을 수행하는 함수 호출
        getCarDirection()

        // 출발지 근처로 맵 뷰 설정
        val originLatitude = 33.44994698
        val originLongitude = 126.4875563
        val mapOption = MapPoint.mapPointWithGeoCoord(originLatitude, originLongitude)
        val mapLevel = 3 // 지도의 확대 레벨

        mapView.setMapCenterPoint(mapOption, true)
        mapView.setZoomLevel(mapLevel, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        binding.mapView.removeView(mapView)
    }

    private fun MutableList<List<Double>>.toMapPoints(): Array<MapPoint> {
        val mapPoints = mutableListOf<MapPoint>()
        for (list in this) {
            val mapPoint = MapPoint.mapPointWithGeoCoord(list[1], list[0]) // 위도, 경도 순서로 넣음
            mapPoints.add(mapPoint)
        }
        return mapPoints.toTypedArray()
    }

    private fun getCarDirection() {
        // 비동기로 HTTP 요청 보내기
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val restApiKey = "210bc2c33ecbf63c8f1539b7c0d31a4c"
                val origin = "126.4875563,33.44994698" // 출발지 좌표
                val destination = "126.4770068,33.49844111" // 목적지 좌표

                val headers = mapOf(
                    "Authorization" to "KakaoAK $restApiKey",
                    "Content-Type" to "application/json"
                )

                val queryParams = "origin=$origin&destination=$destination"
                val requestUrl = "https://apis-navi.kakaomobility.com/v1/directions?$queryParams"
                val url = URL(requestUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                // Set request headers
                headers.forEach { (key, value) ->
                    connection.setRequestProperty(key, value)
                }

                // Get response code
                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = StringBuilder()
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    // Process response data
                    val responseData = response.toString()
                    println(responseData)

                    val gson = Gson()
                    val route = gson.fromJson(responseData, Route::class.java)

                    val linePath = mutableListOf<List<Double>>()

                    route.routes.firstOrNull()?.let { routeInfo ->
                        routeInfo.sections.firstOrNull()?.let { section ->
                            section.roads.forEach { road ->
                                // 각 road의 vertexes를 순회하면서 중간 지점 추가
                                for (i in 0 until road.vertexes.size step 2) {
                                    val lat = road.vertexes[i]
                                    val lng = road.vertexes[i + 1]
                                    linePath.add(listOf(lat, lng))
                                }
                            }
                        }
                    }
                    val mapPoints = linePath.toMapPoints()

                    // UI 업데이트는 UI 스레드에서 처리
                    withContext(Dispatchers.Main) {
                        val polyline = MapPolyline().apply {
                            tag = 0
                            lineColor = Color.argb(179, 255, 174, 0) // ARGB
                            addPoints(mapPoints)
                        }
                        mapView.addPolyline(polyline)
                    }

                } else {
                    // Handle HTTP error
                    println("HTTP error! Status: $responseCode")
                }

                connection.disconnect()
            } catch (e: IOException) {
                // Handle IO exception
                println("Error: ${e.message}")
            }
        }
    }
}

data class Route(
    val transId: String,
    val routes: List<RouteInfo>
)

data class RouteInfo(
    val resultCode: Int,
    val resultMsg: String,
    val sections: List<Section>
)

data class Section(
    val roads: List<Road>
)

data class Road(
    val vertexes: List<Double>
)