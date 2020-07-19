package com.example.cardinfofinder.repository

import android.app.Activity
import com.example.cardinfofinder.model.Card

interface Repository {
    fun lookUpCardInfo(activity: Activity, cardNumber: String)
    fun getCardInformation(): Card
}
