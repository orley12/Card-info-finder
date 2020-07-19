package com.example.cardinfofinder.repository

import android.app.Activity
import android.widget.Toast
import com.example.cardinfofinder.service.ApiClient
import com.example.cardinfofinder.service.CardInfoFinderService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import android.content.Intent
import com.example.cardinfofinder.model.Card
import com.example.cardinfofinder.ui.CardDetailActivity


object RepositoryImpl : Repository {

    private val cardFinderService: CardInfoFinderService =
            ApiClient.buildService(CardInfoFinderService::class.java)

    lateinit var cardInfo: Card;


    override fun lookUpCardInfo(activity: Activity, cardNumber: String) {

        val call = cardFinderService.lookUpCardInfo(cardNumber)
        call.enqueue(object : Callback<Card> {
            override fun onResponse(call: Call<Card>, response: Response<Card>) {
                assert(response.body() != null)
                try {
                    cardInfo = response.body()!!
                    gotoCardDetailActivity(activity);
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            override fun onFailure(call: Call<Card>, t: Throwable) {
                Toast.makeText(activity, "Unable to get card information.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun getCardInformation(): Card {
        return cardInfo;
    }

    private fun gotoCardDetailActivity(activity: Activity) {
        val intentCardDetailActivity = Intent(activity, CardDetailActivity::class.java)
        intentCardDetailActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        activity.getApplicationContext()
                .startActivity(intentCardDetailActivity)
    }

}