package com.attri.premiumchess.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.attri.premiumchess.domain.logic.GameEngine
import com.attri.premiumchess.domain.models.BoardState
import com.attri.premiumchess.domain.models.ChessPiece
import com.attri.premiumchess.domain.models.GameConfig
import com.attri.premiumchess.domain.models.Move
import com.attri.premiumchess.domain.models.MoveType
import com.attri.premiumchess.domain.models.Position
import com.attri.premiumchess.utils.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Sealed class to represent the animation state
sealed class AnimationState {
    object None : AnimationState()
    data class Capture(val piece: ChessPiece, val position: Position) : AnimationState()
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val soundManager = SoundManager(application)

    private val _boardState = MutableStateFlow(BoardState.initial())
    val boardState: StateFlow<BoardState> = _boardState.asStateFlow()

    private val _selectedPosition = MutableStateFlow<Position?>(null)
    val selectedPosition: StateFlow<Position?> = _selectedPosition.asStateFlow()

    private val _legalMoves = MutableStateFlow<List<Move>>(emptyList())
    val legalMoves: StateFlow<List<Move>> = _legalMoves.asStateFlow()

    private val _animationState = MutableStateFlow<AnimationState>(AnimationState.None)
    val animationState: StateFlow<AnimationState> = _animationState.asStateFlow()

    private var isAnimating = false
    
    private val _hintsEnabled = MutableStateFlow(true)
    val hintsEnabled: StateFlow<Boolean> = _hintsEnabled.asStateFlow()
    
    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    var gameConfig: GameConfig? = null

    init {
        soundManager.setSoundEnabled(_soundEnabled.value)
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }

    fun initializeGame(config: GameConfig) {
        this.gameConfig = config
        _boardState.value = BoardState.initial()
        resetSelection()
    }
    
    fun toggleHints() {
        _hintsEnabled.value = !_hintsEnabled.value
    }
    
    fun toggleSound() {
        _soundEnabled.value = !_soundEnabled.value
        soundManager.setSoundEnabled(_soundEnabled.value)
    }

    fun onSquareClicked(pos: Position) {
        if (isAnimating) return

        val currentState = _boardState.value
        val selected = _selectedPosition.value

        if (selected != null) {
            val move = _legalMoves.value.find { it.to == pos }
            if (move != null) {
                val isCapture = currentState.pieces[move.to] != null || move.moveType == MoveType.EN_PASSANT
                
                if (isCapture) {
                    // Start capture animation
                    viewModelScope.launch {
                        isAnimating = true
                        val capturedPiece = currentState.pieces[move.to]
                        if (capturedPiece != null) {
                            _animationState.value = AnimationState.Capture(capturedPiece, move.to)
                            delay(300) // Duration of slide-off animation
                        }
                        
                        // After animation, commit the move
                        commitMove(currentState, move)
                        _animationState.value = AnimationState.None
                        isAnimating = false
                    }
                } else {
                    // If not a capture, commit immediately
                    commitMove(currentState, move)
                }
                return
            }
        }

        val piece = currentState.pieces[pos]
        if (piece != null && piece.color == currentState.turn) {
            _selectedPosition.value = pos
            _legalMoves.value = GameEngine.getLegalMovesForPiece(currentState, pos)
        } else {
            resetSelection()
        }
    }

    private fun commitMove(currentState: BoardState, move: Move) {
        val newState = GameEngine.makeMove(currentState, move)
        playSoundForMove(move, newState)
        _boardState.value = newState
        resetSelection()
    }
    
    private fun playSoundForMove(move: Move, newState: BoardState) {
        when {
            newState.isCheckmate -> soundManager.playCheckmate()
            newState.isCheck -> soundManager.playCheck()
            move.moveType == MoveType.CASTLE_KINGSIDE || move.moveType == MoveType.CASTLE_QUEENSIDE -> soundManager.playCastle()
            move.capturedPiece != null || move.moveType == MoveType.EN_PASSANT -> soundManager.playCapture()
            else -> soundManager.playMove()
        }
    }

    private fun resetSelection() {
        _selectedPosition.value = null
        _legalMoves.value = emptyList()
    }
}