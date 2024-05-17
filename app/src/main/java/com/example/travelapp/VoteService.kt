package com.example.travelapp
import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

interface VoteService {
    @POST("/vote")
    fun sendVoteResults(@Body voteResultsList: List<VoteResult>): Call<Void> // 이 부분이 변경되었습니다.
}

