package com.example.travelapp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

//interface VoteService {
//    @POST("/vote")
//    fun sendVoteResults(@Body voteResultsJson: String): Call<Void>
//}
interface VoteService {
    @POST("/vote")
    fun sendVoteResults(@Body voteResultsList: List<VoteResult>): Call<Void> // 이 부분이 변경되었습니다.
}