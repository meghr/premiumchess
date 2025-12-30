package com.attri.premiumchess.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.attri.premiumchess.domain.models.GameConfig

/**
 * ViewModel for the main activity navigation state.
 * Follows MVVM pattern to separate UI logic from business logic.
 */
class MainViewModel : ViewModel() {

    // Using a sealed class for type-safe screen state
    sealed class ScreenState {
        object Splash : ScreenState()
        object Menu : ScreenState()
        object Setup : ScreenState()
        data class Game(val config: GameConfig) : ScreenState()
    }

    private val _currentScreen = MutableStateFlow<ScreenState>(ScreenState.Splash)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    fun onSplashFinished() {
        _currentScreen.value = ScreenState.Menu
    }

    fun onStartSetup() {
        _currentScreen.value = ScreenState.Setup
    }
    
    fun onCancelSetup() {
        _currentScreen.value = ScreenState.Menu
    }

    fun onStartGame(config: GameConfig) {
        _currentScreen.value = ScreenState.Game(config)
    }

    fun onBackToMenu() {
        _currentScreen.value = ScreenState.Menu
    }
}