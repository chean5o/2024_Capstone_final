package com.example.travelapp.ui.home

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.travelapp.R
import com.example.travelapp.PlaceService
import com.example.travelapp.databinding.FragmentHomeBinding
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class Review(
    val name: String,
    val star: Double,
    val tot_review: String,
    val address: String
)

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    object RetrofitClient {
        private const val BASE_URL = "http://192.168.50.109:3000/"

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
        val imageView = binding.imageView // imageView를 바인딩에서 직접 참조
        loadAndDisplayImage(requireContext(), imageView) // 이미지 로드 함수 호출 시 imageView 전달
        fetchMainInfo() // 메인 정보 가져오기
    }

    private fun fetchMainInfo() {
        RetrofitClient.instance.getMainInfo().enqueue(object : Callback<Review> {
            override fun onResponse(call: Call<Review>, response: Response<Review>) {
                if (response.isSuccessful) {
                    response.body()?.let { review ->
                        binding.textViewName.text = review.name
                        binding.textViewStar.text = "별점: ${review.star}"
                        binding.textViewAddress.text = review.address
                        binding.textViewTotalReview.text = review.tot_review
                    }
                } else {
                    Log.e("API Error", "Response not successful")
                    Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Review>, t: Throwable) {
                Log.e("API Error", "Network error", t)
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun loadAndDisplayImage(context: Context, imageView: ImageView) {
        RetrofitClient.instance.fetchImageData().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val inputStream = it.byteStream()
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        imageView.setImageBitmap(bitmap)
                    }
                } else {
                    // Handle the error
                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Handle the failure
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
