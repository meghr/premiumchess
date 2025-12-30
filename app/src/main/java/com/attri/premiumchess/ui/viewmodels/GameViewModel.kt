package com.attri.premiumchess.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.attri.premiumchess.domain.logic.GameEngine
import com.attri.premiumchess.domain.models.*
import com.attri.premiumchess.utils.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AnimationState {
    object None : AnimationState()
    data class Capture(val piece: ChessPiece, val position: Position) : AnimationState()
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val soundManager = SoundManager(application)
    private var timerJob: Job? = null

    private val _boardState = MutableStateFlow(BoardState.initial())
    val boardState: StateFlow<BoardState> = _boardState.asStateFlow()

    private val _whiteTime = MutableStateFlow(600L)
    val whiteTime: StateFlow<Long> = _whiteTime.asStateFlow()

    private val _blackTime = MutableStateFlow(600L)
    val blackTime: StateFlow<Long> = _blackTime.asStateFlow()

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
        timerJob?.cancel()
    }

    fun initializeGame(config: GameConfig) {
        this.gameConfig = config
        _boardState.value = BoardState.initial()
        _whiteTime.value = config.timerSeconds
        _blackTime.value = config.timerSeconds
        resetSelection()
        startTimer()
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
                    viewModelScope.launch {
                        isAnimating = true
                        val capturedPiece = currentState.pieces[move.to]
                        if (capturedPiece != null) {
                            _animationState.value = AnimationState.Capture(capturedPiece, move.to)
                            delay(300)
                        }
                        commitMove(currentState, move)
                        _animationState.value = AnimationState.None
                        isAnimating = false
                    }
                } else {
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
        
        if (newState.isCheckmate || newState.isStalemate || newState.isDraw) {
            timerJob?.cancel()
        } else {
            startTimer()
        }
    }
    
    private fun startTimer() {
        timerJob?.cancel()
        if (gameConfig?.timerSeconds == GameConfig.NO_TIMER) return

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_boardState.value.turn == PieceColor.WHITE) {
                    val newTime = _whiteTime.value - 1
                    _whiteTime.value = newTime
                    
                    if (newTime == 30L || newTime == 10L) {
                        soundManager.playTimerNotify()
                    }
                    
                    if (newTime <= 0) {
                        soundManager.playIllegal() // Play sound on timeout
                        timerJob?.cancel()
                        break
                    }
                } else {
                    val newTime = _blackTime.value - 1
                    _blackTime.value = newTime
                    
                    if (newTime == 30L || newTime == 10L) {
                        soundManager.playTimerNotify()
                    }
                    
                    if (newTime <= 0) {
                        soundManager.playIllegal() // Play sound on timeout
                        timerJob?.cancel()
                        break
                    }
                }
            }
        }
    }
    
    private fun playSoundForMove(move: Move, newState: BoardState) {
        when {
            newState.isCheckmate -> soundManager.playCheckmate()
            newState.isStalemate -> soundManager.playStalemate()
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
