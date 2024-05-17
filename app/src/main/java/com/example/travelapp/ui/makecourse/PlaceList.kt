package com.example.travelapp.ui.makecourse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.travelapp.DataClass
import com.example.travelapp.PlaceResults
import com.example.travelapp.PlaceService
import com.example.travelapp.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlaceList : Fragment() {
    private lateinit var linearLayout: LinearLayout
    private val selectedSpinnerValues = HashMap<String, Pair<DataClass, Int>>()
    private lateinit var voteService: PlaceService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_place_list, container, false)
        linearLayout = view.findViewById(R.id.linear_layout_places)

        // dataList를 가져와 populatePlaceViews에 전달
        val dataList: List<DataClass>? = arguments?.getParcelableArrayList("dataList")
        if (dataList != null) {
            populatePlaceViews(dataList)
        } else {
            Log.e("PlaceList", "No data received")
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.50.164:3000/") // 본인의 서버 URL로 변경하세요
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        voteService = retrofit.create(PlaceService::class.java)

        // 버튼 찾기 및 클릭 리스너 설정
        val startQuestionsButton = view.findViewById<Button>(R.id.next_button5)
        startQuestionsButton.setOnClickListener {
            if (selectedSpinnerValues.size >= 5 && isSequential(selectedSpinnerValues.values.map { it.second })) {
                sendPlaceResults(selectedSpinnerValues)
                goToQuestionsFragment()
            } else {
                Toast.makeText(context, "적어도 5개의 스피너에서 1부터 순서대로 선택해야 합니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isSequential(selectedValues: List<Int>): Boolean {
        // 선택된 값들을 정렬
        val sortedValues = selectedValues.sorted()
        // 순서대로 선택되었는지 검사
        for (i in sortedValues.indices) {
            if (sortedValues[i] != (i + 1)) return false
        }
        return true
    }

    private fun populatePlaceViews(places: List<DataClass>) {
        places.forEach { place ->
            val layoutInflater = LayoutInflater.from(context)
            val placeView = layoutInflater.inflate(R.layout.item_place, linearLayout, false)

            // 스피너와 관련된 TextView 설정
            val areaTextView: TextView = placeView.findViewById(R.id.nameTextView)
            val priceTextView: TextView = placeView.findViewById(R.id.descriptionTextView)
            val spinner: Spinner = placeView.findViewById(R.id.placeSpinner)

            // 장소 이름과 설명 설정
            areaTextView.text = place.AREA
            priceTextView.text = place.price_x

            setupSpinner(spinner, place)
            linearLayout.addView(placeView)
        }
    }

    private fun setupSpinner(spinner: Spinner, place: DataClass) {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.place_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.setSelection(0)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedOption = parent.getItemAtPosition(position).toString()

                if (position == 0) {
                    selectedSpinnerValues.remove(place.AREA)
                } else {
                    val selectedNumber = selectedOption.toInt()
                    selectedSpinnerValues[place.AREA] = Pair(place, selectedNumber)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedSpinnerValues.remove(place.AREA)
            }
        }
    }

    private fun sendPlaceResults(selectedValues: HashMap<String, Pair<DataClass, Int>>) {
        val placeResultsList = selectedValues.values.map { PlaceResults(it.first.AREA, it.first.X_COORD_x, it.first.Y_COORD_x) }
        val call = voteService.sendPlaceResults(placeResultsList)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("PlaceList", "Place results successfully sent")
                } else {
                    Log.d("PlaceList", "Failed to send place results")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("PlaceList", "Error sending place results: ${t.message}")
                Toast.makeText(context, "데이터 전송 중 오류가 발생했습니다: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun goToQuestionsFragment() {
        view?.findViewById<LinearLayout>(R.id.linear_layout_places)?.visibility = View.GONE
        view?.findViewById<Button>(R.id.next_button5)?.visibility = View.GONE

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container1, CourseView())
            .addToBackStack(null)  // 이전 프래그먼트로 돌아갈 수 있도록 백 스택에 추가
            .commit()
    }
}
