package com.example.travelapp.ui.map
import android.Manifest
import android.app.AlertDialog
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
import com.example.travelapp.R
import com.example.travelapp.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

data class Location(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val category: String // 카테고리 필드 추가
)

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView
    private var fixedView: View? = null

    private val locations = listOf(
        Location("Eiffel Tower", 48.8584, 2.2945, "An iconic symbol of Paris.", "문화시설"),
        Location("Statue of Liberty", 40.6892, -74.0445, "A gift from France to the United States.", "문화시설"),
        Location("남산타워", 37.5512, 126.9882, "A major tourist attraction in Seoul.", "문화시설"),
        Location("Cafe de Paris", 48.8534, 2.3488, "Popular tourist cafe in Paris.", "카페"),
        Location("Starbucks Seoul", 37.5641, 126.9981, "Busy Starbucks coffee shop in Seoul.", "카페")
    )

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
                    mapView.setMapCenterPointAndZoomLevel(
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

    private fun addFixedViewToMap(name: String, description: String) {
        fixedView?.let {
            binding.mapView.removeView(it)
            fixedView = null
        }
        fixedView = FrameLayout(requireContext()).apply {
            setBackgroundResource(R.drawable.rounded_rectangle_shape)
            layoutParams = FrameLayout.LayoutParams(900, 800).apply {
                leftMargin = (mapView.width / 2) - 450
                topMargin = (mapView.height / 2) - 300
            }
        }

        val infoTextView = TextView(requireContext()).apply {
            text = "$name\n$description"
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
        binding.btnCafe.setOnClickListener {
            showCategory("카페")
        }
        binding.btnCultural.setOnClickListener {
            showCategory("문화시설")
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
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            if (query.isNotEmpty()) {
                showSearchResults(query)
            } else {
                Toast.makeText(requireContext(), "검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
            }

            // 키보드 숨기기
            val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(it.windowToken, 0)
            binding.searchEditText.clearFocus()
        }
    }

    private fun showCategory(category: String) {
        val results = locations.filter { it.category == category }
        mapView.removeAllPOIItems() // 기존 마커 제거
        results.forEach { location ->
            addMarkerAndShowInfo(location)
        }
    }

    private fun addMarkerAndShowInfo(location: Location) {
        val marker = MapPOIItem().apply {
            itemName = location.name
            mapPoint = MapPoint.mapPointWithGeoCoord(location.latitude, location.longitude) // 직접 할당
            markerType = MapPOIItem.MarkerType.BluePin
            selectedMarkerType = MapPOIItem.MarkerType.RedPin
            userObject = location
        }
        mapView.addPOIItem(marker)
    }

    private fun showSearchResults(query: String) {
        val results = locations.filter { it.name.contains(query, ignoreCase = true) }
        val names = results.map { it.name }.toTypedArray()

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Search Results")
            .setItems(names) { _, which ->
                val selectedLocation = results[which]
                val adjustedLatitude = selectedLocation.latitude - 0.005
                mapView.setMapCenterPointAndZoomLevel(
                    MapPoint.mapPointWithGeoCoord(adjustedLatitude, selectedLocation.longitude),
                    DEFAULT_ZOOM_LEVEL.toInt(),
                    true
                )
                addFixedViewToMap(selectedLocation.name, selectedLocation.description)
                addMarkerAndShowInfo(selectedLocation) // 마커 추가 및 정보 표시 함수 호출
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // 프래그먼트가 파괴될 때 고정된 뷰가 있다면 제거
        binding.mapView.removeView(fixedView)
    }

    companion object {
        private const val DEFAULT_ZOOM_LEVEL = 3.0
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}