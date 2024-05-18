package com.example.travelapp

import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

interface VoteService {
    @POST("/vote")
    fun sendVoteResults(@Body voteResultsList: List<VoteResult>): Call<Void> // 투표 결과를 보내는 메서드

    @GET("/data")
    fun getData(): Call<List<DataClass>> // 서버에서 데이터를 받아오는 메서드
}