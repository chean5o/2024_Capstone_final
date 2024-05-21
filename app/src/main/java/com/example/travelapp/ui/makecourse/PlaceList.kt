package com.example.travelapp.ui.makecourse

import PlaceResults
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.travelapp.DataClass
import com.example.travelapp.R

class PlaceList : Fragment() {
    private lateinit var linearLayout: LinearLayout
    private var selectedPlaces = mutableListOf<DataClass>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        // 버튼 찾기 및 클릭 리스너 설정
        val startQuestionsButton = view.findViewById<Button>(R.id.next_button5)
        startQuestionsButton.setOnClickListener {
            if (selectedPlaces.size >= 3) {
                goToQuestionsFragment()
            } else {
                Toast.makeText(context, "장소를 3개 이상 선택하세요.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun populatePlaceViews(places: List<DataClass>) {
        places.forEach { place ->
            val layoutInflater = LayoutInflater.from(context)
            val placeView = layoutInflater.inflate(R.layout.item_place, linearLayout, false)

            // 스피너와 관련된 TextView 설정
            val areaTextView: TextView = placeView.findViewById(R.id.nameTextView)
            val priceTextView: TextView = placeView.findViewById(R.id.descriptionTextView)
            val reviewTextView: TextView = placeView.findViewById(R.id.reviewTextView)
            val checkBox: CheckBox = placeView.findViewById(R.id.placeCheckBox)

            // 장소 이름과 설명 설정
            areaTextView.text = place.AREA
            priceTextView.text = "평균 금액: ${place.price_x}원"
            reviewTextView.text = place.tot_review_x

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedPlaces.add(place)
                } else {
                    selectedPlaces.remove(place)
                }
                // 디버깅용 로그 추가
                Log.d("PlaceList", "Selected places: ${selectedPlaces.size}")
            }

            linearLayout.addView(placeView)
        }
    }

    private fun goToQuestionsFragment() {
        // 디버깅용 로그 추가
        Log.d("PlaceList", "Navigating to CourseView with ${selectedPlaces.size} places")

        val placeResultsList = selectedPlaces.map {
            PlaceResults(it.AREA, it.X_COORD_x, it.Y_COORD_x, it.price_x)
        }

        val bundle = Bundle().apply {
            putParcelableArrayList("selectedPlaces", ArrayList(placeResultsList))
        }

        val questionsFragment = CourseView().apply {
            arguments = bundle
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container1, questionsFragment)
            .addToBackStack(null)
            .commit()
    }
}
