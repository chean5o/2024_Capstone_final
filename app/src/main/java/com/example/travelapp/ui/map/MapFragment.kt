package com.example.travelapp.ui.map
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.travelapp.R
import com.example.travelapp.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView
    private lateinit var fixedView: View // 고정된 뷰

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

    private fun addFixedViewToMap(x: Float, y: Float) {
        // 고정된 뷰를 생성하고 추가
        fixedView = View(requireContext())
        fixedView.setBackgroundResource(R.drawable.rounded_rectangle_shape)
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // 뷰 크기를 작게 조절
        layoutParams.width = 800
        layoutParams.height = 600
        layoutParams.leftMargin = x.toInt() - (layoutParams.width / 2)
        layoutParams.topMargin = y.toInt() - (layoutParams.height / 2)
        binding.mapView.addView(fixedView, layoutParams)

        // 닫기 버튼을 추가하고 클릭 이벤트 처리
        val closeButton = ImageView(requireContext())
        closeButton.setImageResource(R.drawable.baseline_close_24)
        val closeLayoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // closeButton을 fixedView 내부에 추가
        closeLayoutParams.gravity = Gravity.TOP and Gravity.END
        binding.mapView.addView(closeButton, closeLayoutParams)

        closeButton.setOnClickListener {
            // 뷰와 닫기 버튼 제거
            binding.mapView.removeView(fixedView)
            binding.mapView.removeView(closeButton)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 지도에 터치 이벤트 처리
        mapView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                mapView.post {
                    val centerX = mapView.width / 2f
                    val centerY = mapView.height * 0.75f
                    // 화면 중앙에 뷰 추가
                    addFixedViewToMap(centerX, centerY)
                }
                true
            } else {
                false
            }
        }
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
