package com.attri.premiumchess.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.attri.premiumchess.domain.models.*
import com.attri.premiumchess.ui.components.ChessPieceComposable
import com.attri.premiumchess.ui.components.GameEndDialog
import com.attri.premiumchess.ui.components.PlayerInfoBar
import com.attri.premiumchess.ui.theme.*
import com.attri.premiumchess.ui.viewmodels.AnimationState
import com.attri.premiumchess.ui.viewmodels.GameViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    onExitGame: () -> Unit,
    config: GameConfig
) {
    LaunchedEffect(key1 = config) {
        viewModel.initializeGame(config)
    }

    val boardState by viewModel.boardState.collectAsState()
    val whiteTime by viewModel.whiteTime.collectAsState()
    val blackTime by viewModel.blackTime.collectAsState()
    val selectedPosition by viewModel.selectedPosition.collectAsState()
    val legalMoves by viewModel.legalMoves.collectAsState()
    val hintsEnabled by viewModel.hintsEnabled.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val animationState by viewModel.animationState.collectAsState()
    
    var showSettings by remember { mutableStateOf(false) }
    var reviewMode by remember { mutableStateOf(false) }
    var showRematchDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val whiteCaptured = boardState.capturedPieces.filter { it.color == PieceColor.BLACK }
    val blackCaptured = boardState.capturedPieces.filter { it.color == PieceColor.WHITE }

    val whiteAdvantage = blackCaptured.sumOf { getPieceValue(it.type) } - whiteCaptured.sumOf { getPieceValue(it.type) }
    val blackAdvantage = whiteAdvantage * -1

    // Main layout: Row with 3 columns (Left Sidebar, Center Board, Right Sidebar)
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
    ) {
        // Left Sidebar (20%)
        Column(
            modifier = Modifier
                .weight(0.2f)
                .fillMaxHeight()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // White Player Info (Top Left)
            PlayerInfoBar(
                playerName = viewModel.gameConfig?.player1Name ?: "Player 1",
                playerColor = PieceColor.WHITE,
                timeSeconds = whiteTime,
                isMyTurn = boardState.turn == PieceColor.WHITE
            )
            
            // White Captured Pieces & Advantage
            Column(modifier = Modifier.weight(1f)) {
                if (whiteAdvantage > 0) {
                    Text("+$whiteAdvantage", color = Color.Green, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp))
                }
                CapturedPiecesBar(modifier = Modifier.fillMaxSize(), pieces = whiteCaptured)
            }

            // Exit Button (Bottom Left)
            Button(
                onClick = onExitGame,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = GoldAccent),
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text("EXIT")
            }
        }

        // Center Board (60%)
        BoxWithConstraints(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            // Calculate max square size based on available space
            val boardSize = minOf(maxHeight, maxWidth)
            
            ChessBoard(
                boardState = boardState,
                size = boardSize, // This size is now constrained and won't shrink unexpectedly
                selectedPosition = selectedPosition,
                legalMoves = if (reviewMode) emptyList() else legalMoves,
                hintsEnabled = hintsEnabled,
                animationState = animationState,
                onSquareClick = { pos -> if (!reviewMode) viewModel.onSquareClicked(pos) },
                onMove = { from, to -> if (!reviewMode) { viewModel.onSquareClicked(from); viewModel.onSquareClicked(to) } }
            )
        }

        // Right Sidebar (20%)
        Column(
            modifier = Modifier
                .weight(0.2f)
                .fillMaxHeight()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Black Player Info (Top Right)
            PlayerInfoBar(
                playerName = viewModel.gameConfig?.player2Name ?: "Player 2",
                playerColor = PieceColor.BLACK,
                timeSeconds = blackTime,
                isMyTurn = boardState.turn == PieceColor.BLACK
            )
            
            // Black Captured Pieces & Advantage
            Column(modifier = Modifier.weight(1f)) {
                if (blackAdvantage > 0) {
                    Text("+$blackAdvantage", color = Color.Green, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp))
                }
                CapturedPiecesBar(modifier = Modifier.fillMaxSize(), pieces = blackCaptured)
            }

            // Settings Button (Bottom Right)
            IconButton(
                onClick = { showSettings = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = GoldAccent)
            }
        }
    }

    if ((boardState.isCheckmate || boardState.isStalemate || whiteTime <= 0 || blackTime <= 0) && !reviewMode) {
        GameEndDialog(
            boardState = boardState,
            whiteTimeRemaining = whiteTime,
            blackTimeRemaining = blackTime,
            totalGameTime = config.timerSeconds,
            player1Name = viewModel.gameConfig?.player1Name ?: "White",
            player2Name = viewModel.gameConfig?.player2Name ?: "Black",
            onNewGame = {
                 showRematchDialog = true
            },
            onMainMenu = onExitGame,
            onReview = { reviewMode = true }
        )
    }
    
    if (showRematchDialog) {
        AlertDialog(
            onDismissRequest = { showRematchDialog = false },
            title = { Text("New Game") },
            text = { Text("Start a new game with the same players?") },
            confirmButton = {
                Button(
                    onClick = {
                        showRematchDialog = false
                        viewModel.initializeGame(config)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Yes", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRematchDialog = false
                        onExitGame() // Return to setup/menu
                    }
                ) {
                    Text("No, Change Setup")
                }
            }
        )
    }
    
    // Add review overlay UI if needed, or simply let user see the board
    if (reviewMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
             Button(
                onClick = onExitGame,
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
            ) {
                Text("Exit Review", color = Color.Black)
            }
        }
    }
    
    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            sheetState = sheetState
        ) {
             Column(modifier = Modifier.padding(24.dp).navigationBarsPadding()) {
                Text("Game Settings", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Move Hints")
                    Switch(checked = hintsEnabled, onCheckedChange = { viewModel.toggleHints() })
                }
                 Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Sound Effects")
                    Switch(checked = soundEnabled, onCheckedChange = { viewModel.toggleSound() })
                }
             }
        }
    }
}

fun getPieceValue(type: PieceType): Int {
    return when (type) {
        PieceType.PAWN -> 1
        PieceType.KNIGHT, PieceType.BISHOP -> 3
        PieceType.ROOK -> 5
        PieceType.QUEEN -> 9
        PieceType.KING -> 0
    }
}

@Composable
fun CapturedPiecesBar(modifier: Modifier, pieces: List<ChessPiece>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(pieces) { piece ->
            ChessPieceComposable(piece = piece, size = 40.dp)
        }
    }
}

@Composable
fun ChessBoard(
    boardState: BoardState,
    size: Dp,
    selectedPosition: Position?,
    legalMoves: List<Move>,
    hintsEnabled: Boolean,
    animationState: AnimationState,
    onSquareClick: (Position) -> Unit,
    onMove: (Position, Position) -> Unit
) {
    val squareSize = size / 8
    val textMeasurer = rememberTextMeasurer()
    
    var draggedPiece by remember { mutableStateOf<Pair<Position, ChessPiece>?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var hoverPosition by remember { mutableStateOf<Position?>(null) }
    val squareSizePx = with(LocalDensity.current) { squareSize.toPx() }

    val checkPulse = remember { Animatable(0f) }
    LaunchedEffect(boardState.isCheck) {
        if (boardState.isCheck) {
            checkPulse.animateTo(1f, animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse))
        } else {
            checkPulse.snapTo(0f)
        }
    }
    
    val selectionPulse = remember { Animatable(0.8f) }
    LaunchedEffect(selectedPosition) {
        if (selectedPosition != null && draggedPiece == null) { // Only pulse when not dragging
            selectionPulse.animateTo(1f, animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse))
        } else {
            selectionPulse.snapTo(0.8f)
        }
    }
    
    Box(
        modifier = Modifier
            .size(size)
            .border(4.dp, BoardBorder)
            .background(BoardBorder)
            .padding(4.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    val file = (it.x / squareSizePx).toInt().coerceIn(0, 7)
                    val rank = 7 - (it.y / squareSizePx).toInt().coerceIn(0, 7)
                    onSquareClick(Position(file, rank))
                })
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (rank in 0..7) {
                for (file in 0..7) {
                    val pos = Position(file, rank)
                    val color = if ((rank + file) % 2 != 0) BoardLightSquare else BoardDarkSquare
                    val left = file * squareSizePx
                    val top = (7 - rank) * squareSizePx
                    drawRect(color, topLeft = Offset(left, top), size = Size(squareSizePx + 1f, squareSizePx + 1f))

                    if (boardState.lastMove?.from == pos || boardState.lastMove?.to == pos) {
                         drawRect(GoldAccent.copy(alpha = 0.6f), topLeft = Offset(left, top), size = Size(squareSizePx, squareSizePx), style = Stroke(width = 3.dp.toPx()))
                    }
                    if (selectedPosition == pos && draggedPiece == null) {
                        drawRect(Color(0xFF03A9F4).copy(alpha = selectionPulse.value), topLeft = Offset(left, top), size = Size(squareSizePx, squareSizePx), style = Stroke(width = 4.dp.toPx()))
                    }
                    if (boardState.isCheck && boardState.pieces[pos]?.type == PieceType.KING && boardState.pieces[pos]?.color == boardState.turn) {
                         drawRect(Color.Red.copy(alpha = 0.5f * checkPulse.value + 0.2f), topLeft = Offset(left, top), size = Size(squareSizePx, squareSizePx))
                         drawRect(Color.Red.copy(alpha = checkPulse.value), topLeft = Offset(left, top), size = Size(squareSizePx, squareSizePx), style = Stroke(width = 6.dp.toPx()))
                    }
                    
                    // Hover highlight
                    if (hoverPosition == pos && draggedPiece != null) {
                        val isValid = legalMoves.any{ it.to == pos }
                        val hoverColor = if(isValid) Color.Green.copy(alpha = 0.3f) else Color.Red.copy(alpha = 0.3f)
                        drawRect(hoverColor, topLeft = Offset(left,top), size=Size(squareSizePx, squareSizePx))
                    }

                    if (hintsEnabled && draggedPiece == null) {
                        val move = legalMoves.find { it.to == pos }
                        if (move != null) {
                            val isCapture = boardState.pieces[pos] != null || move.moveType == MoveType.EN_PASSANT
                            val hintColor = if (isCapture) Color(0xFFF44336).copy(alpha = 0.5f) else Color(0xFF4CAF50).copy(alpha = 0.5f)
                            if (isCapture) {
                                drawCircle(hintColor, radius = squareSizePx * 0.4f, center = Offset(left + squareSizePx/2, top + squareSizePx/2), style = Stroke(width = 4.dp.toPx()))
                            } else {
                                drawCircle(hintColor, radius = squareSizePx * 0.15f, center = Offset(left + squareSizePx/2, top + squareSizePx/2))
                            }
                        }
                    }
                }
            }
            
            val textStyle = TextStyle(color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.SansSerif)
            for (rank in 0..7) {
                val text = (rank + 1).toString()
                val layoutResult = textMeasurer.measure(text, textStyle)
                drawText(layoutResult, topLeft = Offset(x = 4.dp.toPx(), y = (7 - rank) * squareSizePx + 4.dp.toPx()))
            }
            for (file in 0..7) {
                val text = ('a' + file).toString()
                val layoutResult = textMeasurer.measure(text, textStyle)
                drawText(layoutResult, topLeft = Offset(x = (file + 1) * squareSizePx - layoutResult.size.width - 4.dp.toPx(), y = 8 * squareSizePx - layoutResult.size.height - 2.dp.toPx()))
            }
        }

        // Render board pieces
        boardState.pieces.forEach { (position, piece) ->
            key(position) {
                val isAnimatingOut = animationState is AnimationState.Capture && (animationState as AnimationState.Capture).position == position
                val isBeingDragged = draggedPiece?.first == position
                
                if (!isAnimatingOut) {
                    val xOffset = squareSize * position.file
                    val yOffset = squareSize * (7 - position.rank)
                    
                    val isLosingKing = boardState.isCheckmate && piece.type == PieceType.KING && piece.color == boardState.turn
                    val isWinnerPiece = boardState.isCheckmate && piece.color != boardState.turn
                    
                    val rotation = remember { Animatable(0f) }
                    LaunchedEffect(isLosingKing) {
                        if (isLosingKing) {
                            delay(200)
                            rotation.animateTo(90f, animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow))
                        } else {
                            rotation.snapTo(0f)
                        }
                    }
                    
                    val glowAlpha = remember { Animatable(0f) }
                    LaunchedEffect(isWinnerPiece) {
                        if (isWinnerPiece) {
                            glowAlpha.animateTo(1f, animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse))
                        } else {
                            glowAlpha.snapTo(0f)
                        }
                    }

                    if (isWinnerPiece) {
                        Box(
                            modifier = Modifier
                                .offset(x = xOffset, y = yOffset)
                                .size(squareSize)
                                .alpha(glowAlpha.value)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(GoldAccent.copy(alpha = 0.6f), Color.Transparent),
                                        center = Offset.Unspecified,
                                        radius = Float.POSITIVE_INFINITY
                                    ), 
                                    CircleShape
                                )
                        )
                    }

                    ChessPieceComposable(
                        piece, 
                        squareSize, 
                        Modifier
                            .offset(x = xOffset, y = yOffset)
                            .rotate(rotation.value)
                            .pointerInput(position, piece) { // Use key to restart gesture detection for this specific piece
                                if (piece.color != boardState.turn) return@pointerInput
                                detectDragGestures(
                                    onDragStart = { offset -> 
                                        onSquareClick(position) // Select piece
                                        draggedPiece = position to piece
                                        dragOffset = offset
                                    },
                                    onDragEnd = { 
                                        hoverPosition?.let { onMove(position, it) }
                                        draggedPiece = null
                                        hoverPosition = null
                                    }
                                ) { change, dragAmount -> 
                                    change.consume()
                                    dragOffset += dragAmount
                                    
                                    val currentX = (position.file * squareSizePx) + dragOffset.x
                                    val currentY = ((7-position.rank) * squareSizePx) + dragOffset.y
                                    
                                    val file = (currentX / squareSizePx).toInt().coerceIn(0, 7)
                                    val rank = 7 - (currentY / squareSizePx).toInt().coerceIn(0, 7)
                                    hoverPosition = Position(file, rank)
                                }
                            }
                            .alpha(if(isBeingDragged) 0.3f else 1f) // Ghost piece
                    )
                }
            }
        }

        // Render animating capture piece
        if (animationState is AnimationState.Capture) {
            val animState = animationState as AnimationState.Capture
            val targetX = if (animState.piece.color == PieceColor.WHITE) size.value + 40.dp.value else -40.dp.value
            val animatableX = remember { Animatable( (squareSize * animState.position.file).value ) }
            val animatableY = remember { Animatable( (squareSize * (7 - animState.position.rank)).value ) }

            LaunchedEffect(animationState) {
                animatableX.animateTo(targetX, animationSpec = tween(300, easing = LinearEasing))
            }
            ChessPieceComposable(animState.piece, squareSize, Modifier.offset(x = animatableX.value.dp, y = animatableY.value.dp))
        }
        
        // Render dragged piece
        draggedPiece?.let { (pos, piece) ->
            val currentX = (pos.file * squareSizePx) + dragOffset.x - (squareSizePx / 2)
            val currentY = ((7 - pos.rank) * squareSizePx) + dragOffset.y - (squareSizePx / 2)
            ChessPieceComposable(
                piece, 
                squareSize, 
                Modifier
                    .offset { IntOffset(currentX.roundToInt(), currentY.roundToInt()) }
                    .shadow(8.dp, CircleShape)
            )
        }
    }
}
