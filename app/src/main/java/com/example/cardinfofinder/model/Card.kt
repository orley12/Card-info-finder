package com.example.cardinfofinder.model

data class Card (
        var brand: String,
        var type: String,
        var country: Country,
        var bank: Bank
)

data class Country (
        var name: String,
        var emoji: String
)

data class Bank (
        var name: String
)