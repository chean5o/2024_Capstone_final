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
import com.example.travelapp.R
import com.example.travelapp.VoteResult
import com.example.travelapp.VoteService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlaceList : Fragment() {
    private lateinit var linearLayout: LinearLayout
    private val selectedSpinnerValues = HashMap<String, String>()
    private lateinit var voteService: VoteService
    private val voteResults = mutableMapOf<String, String>()

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
            .baseUrl("http://10.0.2.2:5000/") // 본인의 서버 URL로 변경하세요
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        voteService = retrofit.create(VoteService::class.java)

        // 버튼 찾기 및 클릭 리스너 설정
        val startQuestionsButton = view.findViewById<Button>(R.id.next_button5)
        startQuestionsButton.setOnClickListener {
            if (selectedSpinnerValues.size >= 5 && isSequential(selectedSpinnerValues.values.toList())) {
                sendVoteResults(selectedSpinnerValues)
                goToQuestionsFragment()
            } else {
                Toast.makeText(context, "적어도 5개의 스피너에서 1부터 순서대로 선택해야 합니다.", Toast.LENGTH_LONG).show()
            }
        }

        /*startQuestionsButton.setOnClickListener {
            Log.d("MakeCourseFragment", "Next button clicked")
            goToQuestionsFragment()

            if (voteResults.size > 4) { // 필요한 질문의 수에 따라 변경 가능
                sendVoteResults(voteResults)
                goToQuestionsFragment()
            }
            else {
                Toast.makeText(context, "방문 장소를 5개 이상 선택해주세요.", Toast.LENGTH_LONG).show()
            }
        }*/
    }

    private fun isSequential(selectedValues: List<String>): Boolean {
        // 선택된 값들을 정렬
        val sortedValues = selectedValues.sorted()
        // '선택하세요' 제거
        val filteredValues = sortedValues.filterNot { it == "선택하세요" }
        // 순서대로 선택되었는지 검사
        for (i in filteredValues.indices) {
            if (filteredValues[i] != (i + 1).toString()) return false
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

            setupSpinner(spinner, place.AREA)
            linearLayout.addView(placeView)
        }
    }


    private fun setupSpinner(spinner: Spinner, placeArea: String) {
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
                    selectedSpinnerValues.remove(placeArea)
                } else {
                    // 중복 선택 검사
                    if (selectedSpinnerValues.containsValue(selectedOption)) {
                        Toast.makeText(context, "이미 선택된 옵션입니다.", Toast.LENGTH_SHORT).show()
                        spinner.setSelection(0)  // 다시 '선택하세요'로 리셋
                    } else {
                        selectedSpinnerValues[placeArea] = selectedOption
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedSpinnerValues.remove(placeArea)
            }
        }
    }

    private fun sendVoteResults(voteResults: HashMap<String, String>) {
        val voteResultsList = voteResults.map { VoteResult(it.key.toInt(), it.value) }
        val call = voteService.sendVoteResults(voteResultsList)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    //Toast.makeText(context, "투표 결과가 성공적으로 전송되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    //Toast.makeText(context, "투표 결과 전송.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "투표 결과 전송 중 오류가 발생했습니다: ${t.message}", Toast.LENGTH_SHORT).show()
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
