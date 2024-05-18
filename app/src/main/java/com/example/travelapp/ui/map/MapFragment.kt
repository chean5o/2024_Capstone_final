package com.example.travelapp.ui.map
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
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
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import android.os.Parcelable
import android.widget.LinearLayout
import kotlinx.parcelize.Parcelize

@Parcelize
data class Location(
    val name: String,
    val y_coord: Double, // latitude 위도
    val x_coord: Double, // longitude 경도
    val description: String,
    val category: Int, // 카테고리 필드 추가
    val address: String
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
    private val currentMarkers = mutableListOf<MapPOIItem>()


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
        fixedView?.let {
            binding.mapView.removeView(it)
            fixedView = null
        }
        fixedView = FrameLayout(requireContext()).apply {
            setBackgroundResource(R.drawable.rounded_rectangle_shape)
            layoutParams = FrameLayout.LayoutParams(900, 800).apply {
                leftMargin = (mapView?.width ?: 0) / 2 - 450
                topMargin = (mapView?.height ?: 0) / 2 - 300
            }
        }

        val infoTextView = TextView(requireContext()).apply {
            text = "$name\n$address"
            textSize = 16f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
        }

        val closeButton = ImageView(requireContext()).apply {
            setImageResource(R.drawable.baseline_close_24)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = 20
                rightMargin = 30
            }
            setOnClickListener {
                binding.mapView.removeView(fixedView)
                fixedView = null
            }
        }

        // 뷰에 컴포넌트 추가
        (fixedView as FrameLayout).apply {
            addView(infoTextView)
            addView(closeButton)
            binding.mapView.addView(this)
        }
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
                Toast.makeText(requireContext(), "찾고싶은 장소를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun showCategory(category: Int) {
        mapView?.removePOIItems(currentMarkers.toTypedArray())
        currentMarkers.clear()

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
        currentMarkers.add(marker)
    }

    private fun showSearchResults(query: String) {
        RetrofitClient.instance.getPlaceDetails(query).enqueue(object : Callback<List<Location>> {
            override fun onResponse(call: Call<List<Location>>, response: Response<List<Location>>) {
                if (response.isSuccessful) {
                    response.body()?.let { locations ->
                        if (locations.isNotEmpty()) {
                            // 검색 결과가 있을 때
                            val bundle = Bundle().apply {
                                putParcelableArrayList("locations", ArrayList(locations))
                            }
                            val fragment = SearchResultFragment().apply {
                                arguments = bundle
                            }
                            mapView?.removeAllPOIItems()
                            binding.mapView.removeView(mapView)  // mapView 뷰를 부모로부터 안전하게 제거
                            mapView = null
                            view?.findViewById<FrameLayout>(R.id.map_view)?.visibility = View.GONE
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit()
                        } else {
                            Toast.makeText(context, "No results found.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Error: Server returned an error ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Location>>, t: Throwable) {
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