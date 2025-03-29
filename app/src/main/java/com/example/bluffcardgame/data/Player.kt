package com.example.bluffcardgame.data

// Player.kt
data class Player(
    val id: Int,
    val name: String = "Player $id",
    var hand: List<Card> = emptyList(),
    var isOut: Boolean = false
)