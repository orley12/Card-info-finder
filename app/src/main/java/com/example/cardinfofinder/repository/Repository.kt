package com.example.cardinfofinder.repository

import android.app.Activity

interface Repository {
    fun lookUpCardInfo(activity: Activity, cardNumber: String)
}
