package com.example.bluffcardgame.data

// GameEngine.kt
class GameEngine {
    companion object {
        private val SUITS = listOf("♠", "♥", "♦", "♣")
        private val VALUES = listOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A")

        fun createDeck(): List<Card> {
            return SUITS.flatMap { suit ->
                VALUES.map { value -> Card(suit, value) }
            }
        }

        fun shuffleDeck(deck: List<Card>): List<Card> {
            return deck.shuffled()
        }

        fun distributeCards(deck: List<Card>, numPlayers: Int): List<Player> {
            val hands = List(numPlayers) { mutableListOf<Card>() }
            deck.forEachIndexed { index, card ->
                hands[index % numPlayers].add(card)
            }
            return hands.mapIndexed { index, hand ->
                Player(index + 1, hand = hand)
            }
        }
    }

    var players: List<Player> = emptyList()
    var currentPlayerIndex: Int = 0
    var playedCards: MutableList<Card> = mutableListOf()
    var claimedValue: String? = null
    var gameStatus: GameStatus = GameStatus.SETUP

    fun startGame(numPlayers: Int) {
        val newDeck = shuffleDeck(createDeck())
        players = distributeCards(newDeck, numPlayers)
        currentPlayerIndex = 0
        playedCards.clear()
        claimedValue = null
        gameStatus = GameStatus.PLAYING
    }

    fun playCards(player: Player, cardsToPlay: List<Card>, claimedCardValue: String): Boolean {
        if (gameStatus != GameStatus.PLAYING ||
            players[currentPlayerIndex].id != player.id) return false

        player.hand -= cardsToPlay.toSet()
        playedCards.addAll(cardsToPlay)
        claimedValue = claimedCardValue

        // Move to next player who still has cards
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        } while (players[currentPlayerIndex].isOut && players.any { !it.isOut })

        return true
    }

    fun challenge(challengingPlayer: Player): ChallengeResult {
        val challengedPlayer = players[currentPlayerIndex]
        val isBluffCalled = !challengedPlayer.hand.any { it.value == claimedValue }

        return if (isBluffCalled) {
            // Bluff was called successfully
            challengedPlayer.hand += playedCards
            challengedPlayer.isOut = challengedPlayer.hand.size > 52
            ChallengeResult(
                winner = challengingPlayer,
                loser = challengedPlayer,
                isBluff = true
            )
        } else {
            // Bluff was not called
            challengingPlayer.hand += playedCards
            challengingPlayer.isOut = challengingPlayer.hand.size > 52
            ChallengeResult(
                winner = challengedPlayer,
                loser = challengingPlayer,
                isBluff = false
            )
        }.also {
            playedCards.clear()
            claimedValue = null

            // Check if game ended
            if (players.count { !it.isOut } == 1) {
                gameStatus = GameStatus.ENDED
            }
        }
    }
}

data class ChallengeResult(
    val winner: Player,
    val loser: Player,
    val isBluff: Boolean
)