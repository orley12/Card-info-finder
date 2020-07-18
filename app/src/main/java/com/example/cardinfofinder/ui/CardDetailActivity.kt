package com.example.cardinfofinder.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.example.cardinfofinder.R
import com.example.cardinfofinder.repository.RepositoryImpl

class CardDetailActivity : AppCompatActivity() {


    lateinit var cardBrandTextView: TextView
    lateinit var cardTypeTextView: TextView
    lateinit var bankTextView: TextView
    lateinit var CountryTextView: TextView
    lateinit var emojiTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_detail)

        initViews()
        setViewText()
    }

    private fun initViews(){
        cardBrandTextView  = findViewById<TextView>(R.id.card_brand)
        cardTypeTextView  = findViewById<TextView>(R.id.card_type)
        bankTextView = findViewById<TextView>(R.id.bank)
        CountryTextView  = findViewById<TextView>(R.id.country)
        emojiTextView = findViewById<TextView>(R.id.emoji)
    }

    private fun setViewText(){
        val (brand, type, country, bank) = RepositoryImpl.getCardInformation()
        cardBrandTextView.text = brand
        cardTypeTextView.text = type
        bankTextView.text = country.name
        CountryTextView.text = bank.name
        emojiTextView.text = country.emoji
    }

}
