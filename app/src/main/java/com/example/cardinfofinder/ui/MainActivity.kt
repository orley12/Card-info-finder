package com.example.cardinfofinder.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cardinfofinder.R
import com.example.cardinfofinder.repository.RepositoryImpl


class MainActivity : AppCompatActivity() {

    private val mRequestCode: Int = 104
    lateinit var cardNumberEditText: EditText;
    lateinit var scanCardBtn: Button;
    lateinit var proceedBtn: Button;
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        registerClickListeners()
    }

    private fun initViews() {
        cardNumberEditText = findViewById<EditText>(R.id.card_number_edit_text)
        scanCardBtn = findViewById<Button>(R.id.scan_card_btn)
        proceedBtn = findViewById<Button>(R.id.proceed_btn)
        progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        progressBar.visibility = View.GONE
    }

    private fun registerClickListeners() {
        scanCardBtn.setOnClickListener { gotoOcrCaptureActivity() }
        proceedBtn.setOnClickListener { gotoCardDetailActivity() }
    }

    fun getEditTextInput(editText: EditText): String {
        return editText.text.trim().toString()
    }

    private fun gotoOcrCaptureActivity() {
        val intent = Intent(this, OcrCaptureActivity::class.java)
        startActivityForResult(intent, mRequestCode)
    }

    fun gotoCardDetailActivity() {
        val cardNumber: String = getEditTextInput(cardNumberEditText);

        if (isNetworkAvailable() && !cardNumber.isEmpty() && isValidCard(cardNumber)) {
            RepositoryImpl.lookUpCardInfo(this, cardNumber)
            proceedBtn.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            return
        }

        proceedBtn.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    fun isValidCard(cardNumber: String): Boolean {
        if (cardNumber.matches(Regex("^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})\$"))) {
            return true
        }
        Toast.makeText(this, "Invalid card", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onResume() {
        super.onResume()
        progressBar.visibility = View.GONE
        proceedBtn.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === mRequestCode && resultCode == Activity.RESULT_OK) {

            val cardNumber: String? = data?.getStringExtra("cardNumber")

            if (cardNumber == null || cardNumber.toString().isEmpty()) {
                Toast.makeText(
                        this,
                        "Sorry unable to scan card, please try agin",
                        Toast.LENGTH_SHORT
                ).show()
                return;
            }
            cardNumberEditText.text = Editable.Factory.getInstance().newEditable(cardNumber)
        } else {
            Toast.makeText(
                    this,
                    "Sorry unable to scan card, please try agin",
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager: ConnectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).state == NetworkInfo.State.CONNECTED
                ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).state == NetworkInfo.State.CONNECTED) {
            return true
        }
        return false
    }
}

