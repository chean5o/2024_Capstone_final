package com.example.travelapp.ui.makecourse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.travelapp.R
import com.example.travelapp.VoteResult
import com.example.travelapp.VoteService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Question2 : Fragment() {
    private lateinit var voteService: VoteService
    private val voteResults = mutableMapOf<Int, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 프래그먼트 레이아웃을 인플레이트합니다.
        return inflater.inflate(R.layout.fragment_question2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startQuestionsButton = view.findViewById<Button>(R.id.next_button2)
        startQuestionsButton.setOnClickListener {
            Log.d("MakeCourseFragment", "Next button clicked")
            goToQuestionsFragment()
        }

        // Retrofit 인스턴스 생성 및 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000/") // 본인의 서버 URL로 변경하세요
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        voteService = retrofit.create(VoteService::class.java)

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        val buttonIds = listOf(R.id.yesButton2, R.id.noButton2)
        buttonIds.forEach { id ->
            view?.findViewById<Button>(id)?.setOnClickListener { buttonView ->
                val answer = if (buttonView.tag.toString().startsWith("yes")) "Y" else "N"
                val questionId = buttonView.tag.toString().filter { it.isDigit() }.toInt()
                voteResults[questionId] = answer

                Toast.makeText(context, "질문 $questionId: $answer 선택됨", Toast.LENGTH_SHORT).show()

                if (voteResults.size == 1) {
                    sendVoteResults(voteResults)
                }
            }
        }
    }

    private fun sendVoteResults(voteResults: MutableMap<Int, String>) {
        val voteResultsList = voteResults.map { VoteResult(it.key, it.value) }
        val call = voteService.sendVoteResults(voteResultsList)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "투표 결과가 성공적으로 전송되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "투표 결과 전송.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "투표 결과 전송 중 오류가 발생했습니다: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun goToQuestionsFragment() {
        view?.findViewById<LinearLayout>(R.id.linearLayout3)?.visibility = View.GONE

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container1, Question3())
            .addToBackStack(null)  // 이전 프래그먼트로 돌아갈 수 있도록 백 스택에 추가
            .commit()
    }
}