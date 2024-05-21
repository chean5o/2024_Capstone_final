package com.example.travelapp.ui.home

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.travelapp.PlaceService
import com.example.travelapp.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class MainInfoResponse(
    val name: String,
    val star: Double,
    val tot_review: String,
    val address: String,
    val image: String // Base64 인코딩된 문자열
)

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    object RetrofitClient {
        private const val BASE_URL = "http://172.30.41.65:3000/"

        val instance: PlaceService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PlaceService::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupUI()
        return binding.root
    }

    private fun setupUI() {
        fetchMainInfoAndImage() // 메인 정보 및 이미지 가져오기
    }

    private fun fetchMainInfoAndImage() {
        RetrofitClient.instance.getMainInfoAndImage().enqueue(object : Callback<MainInfoResponse> {
            override fun onResponse(call: Call<MainInfoResponse>, response: Response<MainInfoResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { info ->
                        binding.textViewName.text = info.name
                        binding.textViewStar.text = "별점: ${info.star}"
                        binding.textViewAddress.text = info.address
                        binding.textViewTotalReview.text = info.tot_review
                        Log.d("ImageData", "Image data received: ${info.image}") // 이미지 데이터 로그 출력
                        loadAndDisplayImage(info.image)
                    }
                } else {
                    Log.e("API Error", "Response not successful")
                    Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MainInfoResponse>, t: Throwable) {
                Log.e("API Error", "Network error", t)
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadAndDisplayImage(imageData: String) {
        try {
            // Base64 문자열에서 프리픽스를 제거
            val base64Data = imageData.substringAfter("base64,")
            Log.d("Base64Data", base64Data)
            val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            binding.imageView.setImageBitmap(bitmap)
        } catch (e: IllegalArgumentException) {
            Log.e("ImageError", "Base64 decode error", e)
            Toast.makeText(context, "Failed to decode image", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("ImageError", "Failed to load image", e)
            Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
