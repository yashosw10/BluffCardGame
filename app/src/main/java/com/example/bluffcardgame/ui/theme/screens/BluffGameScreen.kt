// File location: app/src/main/java/com/yourpackage/bluffcardgame/ui/screens/BluffGameScreen.kt
package com.example.bluffcardgame.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluffcardgame.data.Card
import com.example.bluffcardgame.data.GameStatus
import com.example.bluffcardgame.data.Player
import com.example.bluffcardgame.ui.theme.components.CardView
import com.example.bluffcardgame.ui.theme.components.PlayerInfo
import com.example.bluffcardgame.viewmodels.BluffGameViewModel
import com.example.bluffcardgame.viewmodels.GameState

@Composable
fun BluffGameScreen(
    viewModel: BluffGameViewModel = viewModel(),
    onNavigateToSetup: () -> Unit = {}
) {
    val gameState by viewModel.gameState.collectAsState()

    when (gameState.status) {
        GameStatus.SETUP -> SetupScreen(
            onStartGame = { numPlayers -> viewModel.startGame(numPlayers) }
        )
        GameStatus.PLAYING -> PlayingScreen(
            gameState = gameState,
            onPlayCards = { player, cards, claimedValue ->
                viewModel.playCards(player, cards, claimedValue)
            },
            onCallBluff = { challenger -> viewModel.challenge(challenger) },
            onNewGame = onNavigateToSetup
        )
        GameStatus.ENDED -> GameOverScreen(
            winner = gameState.players.firstOrNull { !it.isOut },
            onNewGame = onNavigateToSetup
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupScreen(
    onStartGame: (Int) -> Unit
) {
    var numPlayers by remember { mutableIntStateOf(2) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Bluff Card Game") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Number of Players",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Slider(
                value = numPlayers.toFloat(),
                onValueChange = { numPlayers = it.toInt() },
                valueRange = 2f..4f,
                steps = 2,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Text(
                text = "$numPlayers Players",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Button(
                onClick = { onStartGame(numPlayers) },
                modifier = Modifier.width(200.dp)
            ) {
                Text("Start Game")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayingScreen(
    gameState: GameState,
    onPlayCards: (Player, List<Card>, String) -> Unit,
    onCallBluff: (Player) -> Unit,
    onNewGame: () -> Unit
) {
    val currentPlayer = gameState.players[gameState.currentPlayerIndex]
    var selectedCards by remember { mutableStateOf(setOf<Card>()) }
    var claimedValue by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Player ${currentPlayer.id}'s Turn") },
                actions = {
                    TextButton(onClick = onNewGame) {
                        Text("Quit")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Game status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                gameState.claimedValue?.let {
                    AssistChip(
                        onClick = {},
                        label = { Text("Claimed: $it") }
                    )
                }
                AssistChip(
                    onClick = {},
                    label = { Text("Cards Played: ${gameState.playedCards.size}") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current player's hand
            Text(
                text = "Your Hand (${currentPlayer.hand.size} cards)",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.height(140.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentPlayer.hand) { card ->
                    CardView(
                        card = card,
                        isSelected = selectedCards.contains(card),
                        onClick = {
                            selectedCards = if (selectedCards.contains(card)) {
                                selectedCards - card
                            } else {
                                selectedCards + card
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action area
            if (currentPlayer.id == 1) { // Human player
                Column {
                    OutlinedTextField(
                        value = claimedValue,
                        onValueChange = { claimedValue = it },
                        label = { Text("Claim these cards are...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onPlayCards(currentPlayer, selectedCards.toList(), claimedValue)
                            selectedCards = emptySet()
                            claimedValue = ""
                        },
                        enabled = selectedCards.isNotEmpty() && claimedValue.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Play ${selectedCards.size} Cards")
                    }
                }
            } else {
                Button(
                    onClick = { onCallBluff(currentPlayer) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Call Bluff!")
                }
            }

            // Other players
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Other Players",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(gameState.players.filter { it.id != currentPlayer.id }) { player ->
                    PlayerInfo(player = player)
                }
            }
        }
    }
}

@Composable
private fun GameOverScreen(
    winner: Player?,
    onNewGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Game Over!",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        winner?.let {
            Text(
                text = "${it.name} Wins!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Text(
            text = "Thanks for playing Bluff!",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onNewGame,
            modifier = Modifier.width(200.dp)
        ) {
            Text("New Game")
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun BluffGameScreenPreview() {
    MaterialTheme {
        BluffGameScreen()
    }
}