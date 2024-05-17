package com.example.travelapp.ui.makecourse

//import com.example.travelapp.databinding.FragmentMakecourseBinding
//import android.os.Bundle
//import android.view.View
//import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.travelapp.R
import com.example.travelapp.VoteResult
import com.example.travelapp.VoteService
import com.google.android.material.slider.RangeSlider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MakeCourseFragment : Fragment() {

//    private var voteResults = mutableMapOf<Int, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 프래그먼트 레이아웃을 인플레이트합니다.
        return inflater.inflate(R.layout.fragment_makecourse, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrofit 인스턴스 생성
        val retrofit = Retrofit.Builder()
            .baseUrl("http://172.30.137.145/") // 본인의 서버 URL로 변경하세요
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val voteService = retrofit.create(VoteService::class.java)

        val voteResults = mutableMapOf<Int, String>()

        val startQuestionsButton = view.findViewById<Button>(R.id.next_button)
        startQuestionsButton.setOnClickListener {
            Log.d("MakeCourseFragment", "Next button clicked")

            // 투표 결과의 크기가 3이면 투표 결과를 전송
            if (voteResults.size == 3) {
                // 투표 결과가 3개 이상일 때만 투표 결과를 전송하고 다음 프래그먼트로 이동
                sendVoteResults(voteResults, voteService)
                goToQuestionsFragment()
            } else {
                // 사용자에게 더 많은 입력이 필요하다고 알림
                Toast.makeText(context, "모든 항목을 선택해주세요.", Toast.LENGTH_LONG).show()
            }
        }

        val ageSpinner = view.findViewById<Spinner>(R.id.ageSpinner)
        // 10년 단위 나이 옵션 리스트 생성
        val ageOptions = listOf("10", "20", "30", "40", "50", "60")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ageOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        ageSpinner.adapter = adapter

        // 선택된 아이템에 대한 리스너 설정
        ageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // 선택된 나이 처리
                val selectedAge = ageOptions[position]
                voteResults[6] = selectedAge
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 아무것도 선택되지 않았을 때의 처리
            }
        }

        // Find the RangeSlider instance
        val budgetSlider: RangeSlider = view.findViewById(R.id.budgetSlider)
        val sliderValueText: TextView = view.findViewById(R.id.sliderValueText)

        // Set up a listener for when the user stops sliding
        budgetSlider.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: RangeSlider) {
                // 사용자가 슬라이더 조작을 시작할 때 실행될 코드 (필요한 경우)
            }

            override fun onStopTrackingTouch(slider: RangeSlider) {
                // 사용자가 슬라이더 조작을 마쳤을 때 실행될 코드g
                val value = slider.values[0] // 첫 번째 값을 가져옵니다. RangeSlider의 경우 범위를 가지므로, 필요에 따라 적절하게 값을 선택하세요.

                // Convert the slider value to a String and store it in the map
                voteResults[7] = String.format("%,.0f원", value)

                // Convert the slider values to a String and update the TextView
                sliderValueText.text = "${String.format("100,000원 - %,.0f원", value)}"
            }
        })

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioMale -> {
                    voteResults[5] = "0"
                }
                R.id.radioFemale -> {
                    voteResults[5] = "1"
                }
            }
        }
    }

    private fun sendVoteResults(voteResults: MutableMap<Int, String>, voteService: VoteService) {
        val voteResultsList = voteResults.map { VoteResult(it.key, it.value) }

        val call = voteService.sendVoteResults(voteResultsList)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "투표 결과가 성공적으로 전송되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "투표 결과가 성공적으로 전송.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "투표 결과 전송 중 오류가 발생했습니다: ${t.message}", Toast.LENGTH_SHORT).show()
                // 오류 로그 기록
                Log.e("QuestionsFragment", "투표 결과 전송 실패: ${t.message}", t)
            }
        })
    }

    private fun goToQuestionsFragment() {
        view?.findViewById<LinearLayout>(R.id.del)?.visibility = View.GONE

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container1, QuestionsFragment())
            .addToBackStack(null)  // 이전 프래그먼트로 돌아갈 수 있도록 백 스택에 추가
            .commit()
    }

//    override fun onDestroyView() {fh
//        super.onDestroyView()
//        _binding = null
//    }
}