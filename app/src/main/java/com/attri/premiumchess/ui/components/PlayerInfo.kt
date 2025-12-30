package com.attri.premiumchess.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attri.premiumchess.domain.models.PieceColor
import java.util.Locale

@Composable
fun PlayerInfoBar(
    modifier: Modifier = Modifier,
    playerName: String,
    playerColor: PieceColor,
    timeSeconds: Long,
    isMyTurn: Boolean
) {
    val activeAlpha by animateFloatAsState(targetValue = if (isMyTurn) 1f else 0.6f, label = "activeAlpha")

    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .alpha(activeAlpha),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Piece Icon
        Text(
            text = if (playerColor == PieceColor.WHITE) "♔" else "♚",
            fontSize = 24.sp,
            color = if (playerColor == PieceColor.WHITE) Color.White else Color.LightGray
        )
        
        // Name
        Text(
            text = playerName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        // Timer
        ChessTimer(timeSeconds = timeSeconds, isMyTurn = isMyTurn)
    }
}

@Composable
fun ChessTimer(timeSeconds: Long, isMyTurn: Boolean) {
    val minutes = timeSeconds / 60
    val seconds = timeSeconds % 60
    val timeString = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    val color by animateColorAsState(
        targetValue = when {
            timeSeconds <= 0 -> Color.Red
            timeSeconds <= 10 -> Color.Red
            timeSeconds <= 30 -> Color(0xFFFF9800) // Orange
            else -> Color.White
        },
        label = "timerColor"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "TimerFlash")
    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (timeSeconds <= 0) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TimerFlashAlpha"
    )

    Box(
        modifier = Modifier
            .background(
                color = Color.Black.copy(alpha = if (isMyTurn) 0.4f else 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = timeString,
            fontFamily = FontFamily.Monospace,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.alpha(flashAlpha)
        )
    }
}
