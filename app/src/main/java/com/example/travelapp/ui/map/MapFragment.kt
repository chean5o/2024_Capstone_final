package com.example.travelapp.ui.map
import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.travelapp.PlaceService
import com.example.travelapp.R
import com.example.travelapp.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.parcel.Parcelize
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Parcelize
data class Location(
    val name: String,
    val y_coord: Double, // latitude 위도
    val x_coord: Double, // longitude 경도
    val category: Int, // 카테고리 필드 추가
    val adress: String
) : Parcelable

object RetrofitClient {
    private const val BASE_URL = "http://192.168.50.34:3000/"

    val instance: PlaceService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlaceService::class.java)
    }
}
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mapView: MapView? = null
    private var fixedView: View? = null


//    private val locations = listOf(
//        Location("Eiffel Tower", 48.8584, 2.2945, "An iconic symbol of Paris.", "문화시설"),
//        Location("Statue of Liberty", 40.6892, -74.0445, "A gift from France to the United States.", "문화시설"),
//        Location("남산타워", 37.5512, 126.9882, "A major tourist attraction in Seoul.", "문화시설"),
//        Location("Cafe de Paris", 48.8534, 2.3488, "Popular tourist cafe in Paris.", "카페"),
//        Location("Starbucks Seoul", 37.5641, 126.9981, "Busy Starbucks coffee shop in Seoul.", "카페"),
//        Location("Hallasan Mountain", 33.3617, 126.5292, "The highest mountain in South Korea, located in Jeju.", "문화시설"),
//        Location("Seongsan Ilchulbong", 33.4581, 126.9426, "Volcanic cone with a huge crater, popular for sunrise views.", "레저시설"),
//        Location("Jeongbang Waterfall", 33.2411, 126.5594, "A waterfall that falls directly into the sea, a unique feature in Jeju.", "음식점"),
//        Location("Osulloc Tea Museum", 33.3058, 126.2895, "Museum dedicated to Korean tea culture, located in Jeju.", "문화시설"),
//        Location("Manjanggul Cave", 33.5281, 126.7717, "One of the finest lava tunnels in the world, found in Jeju.", "산책로")
//    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 위치 권한이 있는지 확인
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 없으면 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // 권한이 있으면 현재 위치 가져오기
            fetchCurrentLocation()
        }

        // 지도 초기화
        mapView = MapView(requireContext())
        binding.mapView.addView(mapView)

        return root
    }

    private fun fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // 위치 권한이 허용된 경우 현재 위치 가져오기
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                // 위치 정보 가져오기 성공
                location?.let {
                    // 지도에 현재 위치 표시
                    mapView?.setMapCenterPointAndZoomLevel(
                        MapPoint.mapPointWithGeoCoord(
                            it.latitude,
                            it.longitude
                        ),
                        DEFAULT_ZOOM_LEVEL.toInt(),
                        true
                    )
                }
            }.addOnFailureListener { e ->
                // 위치 정보 가져오기 실패
                Toast.makeText(requireContext(), "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // 위치 권한이 거부된 경우 사용자에게 다시 권한 요청
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun addFixedViewToMap(name: String, address: String) {
        // 기존의 뷰를 제거하고 새로운 LinearLayout을 설정합니다.
        fixedView?.let {
            binding.mapView.removeView(it)
            fixedView = null
        }
        val inflater = LayoutInflater.from(context)
        fixedView = inflater.inflate(R.layout.fixed_view, binding.mapView, false).apply {
            findViewById<TextView>(R.id.infoTextView).text = "$name"
            findViewById<TextView>(R.id.addressTextView).text = "$address"
            val imageView = findViewById<ImageView>(R.id.imageView)  // ImageView 찾기
            loadImageIntoImageView(imageView)  // 이미지 로드 및 설정

            findViewById<ImageView>(R.id.closeButton).setOnClickListener {
                binding.mapView.removeView(fixedView)
                fixedView = null
            }
        }

        // 설정된 위치에 따라 뷰 위치 조정
        fixedView?.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            topMargin = (mapView?.height ?: 0) / 2 - 300
            leftMargin = 50  // 왼쪽 여백 추가
            rightMargin = 50 // 오른쪽 여백 추가
        }

        binding.mapView.addView(fixedView)
    }

    private fun loadImageIntoImageView(imageView: ImageView) {
        RetrofitClient.instance.getImageUrl().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.byteStream()?.let { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        activity?.runOnUiThread {
                            imageView.setImageBitmap(bitmap)
                        }
                    }
                } else {
                    Log.e("API Error", "Response not successful")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("API Error", "Network error", t)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.btnCafe.setOnClickListener {
//            showCategory(11) //음식점 , 산책로, 문화, 레저
//        }
//        binding.btnCultural.setOnClickListener {
//            showCategory(3)

        binding.btnRestaurant.apply {
            background = ContextCompat.getDrawable(context, R.drawable.rounded_button_background)
            backgroundTintList = null
            setPadding(0, 0, 0, 0) // 패딩 제거
            minHeight = 0 // 최소 높이 제거

            // 클릭 리스너 설정
            setOnClickListener {
                showCategory(11)
            }
        }
        binding.btnScenic.apply {
            background = ContextCompat.getDrawable(context, R.drawable.rounded_button_background)
            backgroundTintList = null
            setPadding(0, 0, 0, 0) // 패딩 제거
            minHeight = 0 // 최소 높이 제거

            // 클릭 리스너 설정
            setOnClickListener {
                showCategory(7)
            }
        }
        binding.btnCultural.apply {
            background = ContextCompat.getDrawable(context, R.drawable.rounded_button_background)
            backgroundTintList = null
            setPadding(0, 0, 0, 0) // 패딩 제거
            minHeight = 0 // 최소 높이 제거

            // 클릭 리스너 설정
            setOnClickListener {
                showCategory(3)
            }
        }
        binding.btnSports.apply {
            background = ContextCompat.getDrawable(context, R.drawable.rounded_button_background)
            backgroundTintList = null
            setPadding(0, 0, 0, 0) // 패딩 제거
            minHeight = 0 // 최소 높이 제거

            // 클릭 리스너 설정
            setOnClickListener {
                showCategory(5)
            }
        }

        /*mapView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // 맵의 너비와 높이에 대한 중앙 좌표 계산
                val centerX = mapView.width / 2f
                val centerY = mapView.height / 2f

                // 클릭된 위치가 중앙의 일정 범위 내에 있는지 확인
                if (Math.abs(event.x - centerX) < 100 && Math.abs(event.y - centerY) < 100) {
                    mapView.post {
                        // 중앙 근처에서 클릭되었을 때만 뷰 추가
                        addFixedViewToMap(centerX, mapView.height * 0.65f)
                    }
                    true // 이벤트 처리 완료
                } else {
                    false // 다른 지점 클릭시 이벤트 무시
                }
            } else {
                false // ACTION_DOWN 이외의 액션은 처리하지 않음
            }
        }*/
        //수빈이 코드
//        binding.searchButton.setOnClickListener {
//            val query = binding.searchEditText.text.toString()
//            if (query.isNotEmpty()) {
//                showSearchResults(query)
//            } else {
//                Toast.makeText(requireContext(), "검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
//            }
//
//            // 키보드 숨기기
//            val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
//            inputMethodManager?.hideSoftInputFromWindow(it.windowToken, 0)
//            binding.searchEditText.clearFocus()
//        }
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                showSearchResults(query)
                // 키보드 숨기기 및 포커스 제거
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                inputMethodManager?.hideSoftInputFromWindow(it.windowToken, 0)
                binding.searchEditText.clearFocus()
            } else {
                Toast.makeText(requireContext(), "Please enter a search term.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun showCategory(category: Int) {
        RetrofitClient.instance.getPlaces(category).enqueue(object : Callback<List<Location>> {
            override fun onResponse(call: Call<List<Location>>, response: Response<List<Location>>) {
                if (response.isSuccessful) {
                    val places = response.body()
                    // 받아온 장소 데이터를 처리하고 지도에 표시하는 로직을 작성합니다.
                    places?.forEach { location ->
                        addMarkerAndShowInfo(location)
                    }
                } else {
                    println("Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Location>>, t: Throwable) {
                println("Failure: ${t.message}")
            }
        })
    }

    private fun addMarkerAndShowInfo(location: Location) {
        val marker = MapPOIItem().apply {
            itemName = location.name
            mapPoint = MapPoint.mapPointWithGeoCoord(location.y_coord, location.x_coord) // 직접 할당
            markerType = MapPOIItem.MarkerType.BluePin
            selectedMarkerType = MapPOIItem.MarkerType.RedPin
            userObject = location
        }
        mapView?.addPOIItem(marker)
    }

    private fun showSearchResults(query: String) {
        RetrofitClient.instance.getPlaceDetails(query).enqueue(object : Callback<List<Location>> {
            override fun onResponse(call: Call<List<Location>>, response: Response<List<Location>>) {
                if (response.isSuccessful) {
                    val locations = response.body()
                    if (locations.isNullOrEmpty()) {
                        Toast.makeText(context, "No results found.", Toast.LENGTH_SHORT).show()
                    } else {
                        // 입력된 검색어를 통해 필터링
                        val results = locations.filter { it.name.contains(query, ignoreCase = true) }
                        val names = results.map { it.name }.toTypedArray()

                        // 검색 결과 다이얼로그 생성 및 표시
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle("Search Results")
                            .setItems(names) { _, which ->
                                val selectedLocation = results[which]
                                val adjustedLatitude = selectedLocation.y_coord - 0.005
                                mapView?.setMapCenterPointAndZoomLevel(
                                    MapPoint.mapPointWithGeoCoord(adjustedLatitude, selectedLocation.x_coord),
                                    DEFAULT_ZOOM_LEVEL.toInt(),
                                    true
                                )
                                addFixedViewToMap(selectedLocation.name, selectedLocation.adress)
                                addMarkerAndShowInfo(selectedLocation) // 마커 추가 및 정보 표시 함수 호출
                            }
                            .setNegativeButton("Cancel", null)
                            .create()

                        dialog.show()
                    }
                } else {
                    // 서버 응답은 있으나 성공적이지 않은 경우 (예: 404, 500 등)
                    Toast.makeText(context, "Error: Server returned an error ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Location>>, t: Throwable) {
                // 네트워크 요청 실패 (예: 연결 문제, 타임아웃 등)
                Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // 고정된 뷰가 있다면 제거
        fixedView?.let {
            binding.mapView.removeView(it)
            fixedView = null
        }
        mapView?.removeAllPOIItems()
        binding.mapView.removeView(mapView)  // mapView 뷰를 부모로부터 안전하게 제거
        mapView = null
        _binding = null
    }

    companion object {
        private const val DEFAULT_ZOOM_LEVEL = 3.0
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}