package com.attri.premiumchess.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.attri.premiumchess.domain.models.BoardState
import com.attri.premiumchess.domain.models.PieceColor
import com.attri.premiumchess.ui.theme.GoldAccent
import com.attri.premiumchess.ui.theme.GoldDim
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GameEndDialog(
    boardState: BoardState,
    whiteTimeRemaining: Long,
    blackTimeRemaining: Long,
    totalGameTime: Long, // Total configured time per player
    player1Name: String, // White
    player2Name: String, // Black
    onNewGame: () -> Unit,
    onMainMenu: () -> Unit,
    onReview: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(300) // Delay for board update
        showDialog = true
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = { /* Cannot be dismissed */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                GameEndContent(
                    boardState = boardState,
                    whiteTimeRemaining = whiteTimeRemaining,
                    blackTimeRemaining = blackTimeRemaining,
                    totalGameTime = totalGameTime,
                    player1Name = player1Name,
                    player2Name = player2Name,
                    onNewGame = onNewGame,
                    onMainMenu = onMainMenu,
                    onReview = onReview
                )
                
                // Confetti for wins
                val winnerExists = (whiteTimeRemaining <= 0 || blackTimeRemaining <= 0 || boardState.isCheckmate) && !boardState.isStalemate
                
                if (winnerExists) {
                    ConfettiEffect()
                }
            }
        }
    }
}

@Composable
fun GameEndContent(
    boardState: BoardState,
    whiteTimeRemaining: Long,
    blackTimeRemaining: Long,
    totalGameTime: Long,
    player1Name: String,
    player2Name: String,
    onNewGame: () -> Unit,
    onMainMenu: () -> Unit,
    onReview: () -> Unit
) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(300))
        scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }

    // Determine result
    val isTimeout = whiteTimeRemaining <= 0 || blackTimeRemaining <= 0
    val winnerColor = when {
        whiteTimeRemaining <= 0 -> PieceColor.BLACK
        blackTimeRemaining <= 0 -> PieceColor.WHITE
        boardState.isCheckmate -> if (boardState.turn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        else -> null // Draw
    }
    
    val isDraw = winnerColor == null
    
    val winnerName = if (winnerColor == PieceColor.WHITE) player1Name else if (winnerColor == PieceColor.BLACK) player2Name else null
    
    val titleText = when {
        winnerName != null && isTimeout -> "$winnerName Wins!"
        winnerName != null -> "$winnerName Wins!"
        boardState.isStalemate -> "Draw - Stalemate"
        else -> "Draw"
    }
    
    val subTitleText = when {
        isTimeout -> "Time ran out"
        boardState.isCheckmate -> "By Checkmate"
        boardState.isStalemate -> "No legal moves available"
        else -> "Game Over"
    }

    val icon = when {
        boardState.isCheckmate -> Icons.Rounded.EmojiEvents 
        boardState.isStalemate -> Icons.Default.Handshake 
        isTimeout -> Icons.Default.Alarm
        else -> Icons.Default.Flag 
    }

    val iconColor = if (winnerColor == PieceColor.WHITE) Color.White else if (winnerColor == PieceColor.BLACK) Color.Gray else GoldAccent

    // Animations for pulses
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    
    val drawBorderAlpha by if (isDraw) {
        infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
            label = "DrawPulse"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }
    
    val timeoutIconScale by if (isTimeout) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "TimeoutPulse"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }
    
    val borderColor = if (isDraw) GoldAccent.copy(alpha = drawBorderAlpha) else GoldAccent
    val timeoutColor = if (isTimeout) Color.Red else iconColor
    val finalIconColor = if (isTimeout) timeoutColor else iconColor

    Card(
        modifier = Modifier
            .width(500.dp) // Enlarged dialog width
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            }
            .shadow(24.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(Color(0xFF2D2D2D), Color(0xFF1A1A1A))))
                .padding(32.dp), // Increased padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp) // Increased spacing
        ) {
            // Header Icon
            val iconScale = remember { Animatable(0.5f) }
            val iconRotation = remember { Animatable(-45f) }
            LaunchedEffect(Unit) {
                launch { iconScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.6f)) }
                launch { iconRotation.animateTo(0f, animationSpec = tween(500)) }
            }
            
            Box(
                modifier = Modifier
                    .size(96.dp) // Larger icon container
                    .scale(iconScale.value)
                    .rotate(iconRotation.value)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                 Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp) // Larger icon
                        .scale(if (isTimeout) timeoutIconScale else 1f),
                    tint = finalIconColor
                )
            }

            // Winner Announcement
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = titleText,
                    fontSize = 36.sp, // Larger font
                    fontWeight = FontWeight.Bold,
                    color = if (winnerColor != null) (if (winnerColor == PieceColor.WHITE) Color.White else Color.LightGray) else GoldAccent,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                var subTitleAlpha by remember { mutableFloatStateOf(0f) }
                LaunchedEffect(Unit) {
                    delay(300)
                    animate(0f, 0.7f, animationSpec = tween(500)) { value, _ -> subTitleAlpha = value }
                }
                
                Text(
                    text = subTitleText,
                    fontSize = 24.sp, // Larger font
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = subTitleAlpha),
                    textAlign = TextAlign.Center
                )
            }
            
            // Game Statistics
            GameStatsCard(
                totalMoves = boardState.fullMoveNumber,
                gameDuration = calculateDuration(totalGameTime, whiteTimeRemaining, blackTimeRemaining),
                whiteRemaining = whiteTimeRemaining,
                blackRemaining = blackTimeRemaining,
                boardState = boardState
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Main Menu
                ScaleButton(
                    onClick = onMainMenu,
                    modifier = Modifier
                        .height(56.dp) // Increased height
                        .weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.Gray),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Menu", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White) // Larger font and set color
                }
                
                Spacer(Modifier.width(16.dp))
                
                // New Game
                ScaleButton(
                    onClick = onNewGame,
                    modifier = Modifier
                        .height(64.dp) // Increased height (larger than others)
                        .weight(1.3f), // More weight
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("New Game", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp) // Larger font
                }

                Spacer(Modifier.width(16.dp))

                // View Board
                ScaleButton(
                    onClick = onReview,
                    modifier = Modifier
                        .height(56.dp) // Increased height
                        .weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.Gray),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Review", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White) // Larger font and set color
                }
            }
        }
    }
}

@Composable
fun ScaleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    border: BorderStroke? = null,
    shape: Shape = ButtonDefaults.shape,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "buttonScale")
    
    Button(
        onClick = onClick,
        modifier = modifier.scale(scale),
        interactionSource = interactionSource,
        colors = colors,
        border = border,
        shape = shape,
        content = content
    )
}

@Composable
fun GameStatsCard(
    totalMoves: Int,
    gameDuration: String,
    whiteRemaining: Long,
    blackRemaining: Long,
    boardState: BoardState
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(icon = Icons.Default.Timeline, label = "Moves", value = "$totalMoves")
                StatItem(icon = Icons.Default.Timer, label = "Duration", value = gameDuration)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(icon = Icons.Default.AccessTime, label = "White", value = formatTime(whiteRemaining))
                StatItem(icon = Icons.Default.AccessTimeFilled, label = "Black", value = formatTime(blackRemaining))
            }
            
            // Material captured could be added here
            val whiteCaptured = boardState.capturedPieces.count { it.color == PieceColor.BLACK }
            val blackCaptured = boardState.capturedPieces.count { it.color == PieceColor.WHITE }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Captured: White $whiteCaptured | Black $blackCaptured", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = GoldDim, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 10.sp)
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun calculateDuration(totalPerPlayer: Long, whiteRem: Long, blackRem: Long): String {
    if (totalPerPlayer <= 0) return "N/A"
    val elapsed = (totalPerPlayer * 2) - (whiteRem + blackRem)
    return formatTime(elapsed)
}

fun formatTime(seconds: Long): String {
    if (seconds < 0) return "00:00"
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.US, "%02d:%02d", m, s)
}

data class Particle(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float,
    val speedY: Float,
    val speedX: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

@Composable
fun ConfettiEffect() {
    val particles = remember {
        List(100) {
            Particle(
                x = Random.nextFloat(), // relative 0..1
                y = Random.nextFloat() * -0.5f, // start above
                color = listOf(GoldAccent, Color.White, Color.LightGray, GoldDim).random(),
                size = Random.nextFloat() * 10f + 5f,
                speedY = Random.nextFloat() * 0.01f + 0.005f,
                speedX = (Random.nextFloat() - 0.5f) * 0.01f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 10f
            )
        }
    }
    
    var particleState by remember { mutableStateOf(particles) }
    var isActiveAnimation by remember { mutableStateOf(true) }
    
    // Auto-stop after 2 seconds (fade out effect can be added if needed)
    LaunchedEffect(Unit) {
        delay(2500)
        isActiveAnimation = false
    }

    if (isActiveAnimation) {
        LaunchedEffect(Unit) {
            while (isActive) {
                withFrameNanos { time ->
                    particleState = particleState.map { p ->
                        var newY = p.y + p.speedY
                        var newX = p.x + p.speedX + (sin(time / 1000000000f + p.y * 10) * 0.002f).toFloat()
                        
                        // Respawn if out of bounds, but only if we want continuous flow for 2 seconds
                        if (newY > 1.2f) {
                            newY = -0.1f
                            newX = Random.nextFloat()
                        }
                        
                        p.copy(
                            x = newX,
                            y = newY,
                            rotation = p.rotation + p.rotationSpeed
                        )
                    }
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            particleState.forEach { p ->
                withTransform({
                    translate(p.x * width, p.y * height)
                    rotate(p.rotation)
                }) {
                    drawRect(
                        color = p.color,
                        topLeft = Offset(-p.size/2, -p.size/2),
                        size = androidx.compose.ui.geometry.Size(p.size, p.size)
                    )
                }
            }
        }
    }
}
