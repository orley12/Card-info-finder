package com.example.cardinfofinder.service

import com.example.cardinfofinder.model.Card
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Path


interface CardInfoFinderService {
    @GET("/{cardNumber}")
    fun lookUpCardInfo(@Path("cardNumber") cardNumber: String): Call<Card>
}