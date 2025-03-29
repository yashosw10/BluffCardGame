// File location: app/src/main/java/com/yourpackage/bluffcardgame/viewmodels/BluffGameViewModel.kt
package com.example.bluffcardgame.viewmodels

import androidx.lifecycle.ViewModel
import com.example.bluffcardgame.data.Card
import com.example.bluffcardgame.data.GameEngine
import com.example.bluffcardgame.data.GameStatus
import com.example.bluffcardgame.data.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class GameState(
    val players: List<Player>,
    val currentPlayerIndex: Int,
    val playedCards: List<Card>,
    val claimedValue: String?,
    val status: GameStatus
)

class BluffGameViewModel : ViewModel() {
    private val gameEngine = GameEngine()

    private val _gameState = MutableStateFlow(
        GameState(
            players = emptyList(),
            currentPlayerIndex = 0,
            playedCards = emptyList(),
            claimedValue = null,
            status = GameStatus.SETUP
        )
    )
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    fun startGame(numPlayers: Int) {
        gameEngine.startGame(numPlayers)
        updateStateFromEngine()
    }

    fun playCards(player: Player, cards: List<Card>, claimedValue: String) {
        if (gameEngine.playCards(player, cards, claimedValue)) {
            updateStateFromEngine()
        }
    }

    fun challenge(challengingPlayer: Player): com.example.bluffcardgame.data.ChallengeResult {
        val result = gameEngine.challenge(challengingPlayer)
        updateStateFromEngine()
        return result
    }

    fun resetGame() {
        _gameState.update { it.copy(status = GameStatus.SETUP) }
    }

    private fun updateStateFromEngine() {
        _gameState.update {
            GameState(
                players = gameEngine.players,
                currentPlayerIndex = gameEngine.currentPlayerIndex,
                playedCards = gameEngine.playedCards,
                claimedValue = gameEngine.claimedValue,
                status = gameEngine.gameStatus
            )
        }
    }
}

// Challenge result sealed class for better type safety
sealed class ChallengeResult {
    data class Success(val challenger: Player, val challenged: Player) : ChallengeResult()
    data class Failure(val challenger: Player, val challenged: Player) : ChallengeResult()
}