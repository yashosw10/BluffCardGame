// File location: app/src/main/java/com/yourpackage/bluffcardgame/ui/components/CardView.kt
package com.example.bluffcardgame.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluffcardgame.data.Card

@Composable
fun CardView(
    card: Card,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    // Determine card color based on suit (red for hearts/diamonds)
    val cardColor = when (card.suit) {
        "♥", "♦" -> Color.Red
        else -> Color.Black
    }

    Box(
        modifier = Modifier
            .size(width = 80.dp, height = 120.dp)
            .padding(4.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.Blue else Color.Gray,
                shape = RoundedCornerShape(8.dp)
            )
            .background(Color.White, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card value (top)
            Text(
                text = card.value,
                color = cardColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Card suit (center)
            Text(
                text = card.suit,
                color = cardColor,
                fontSize = 32.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardViewPreview() {
    Column {
        // Preview a heart card
        CardView(
            card = Card(suit = "♥", value = "K"),
            isSelected = false
        )

        // Preview a selected spade card
        CardView(
            card = Card(suit = "♠", value = "A"),
            isSelected = true
        )
    }
}