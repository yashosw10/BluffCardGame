package com.example.bluffcardgame.data

// Card.kt
data class Card(
    val suit: String,
    val value: String,
    val id: String = "$value$suit"
)
