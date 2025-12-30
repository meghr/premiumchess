package com.attri.premiumchess

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.attri.premiumchess.ui.screens.GameScreen
import com.attri.premiumchess.ui.screens.MainMenuScreen
import com.attri.premiumchess.ui.screens.PlayerSetupScreen
import com.attri.premiumchess.ui.screens.SplashScreen
import com.attri.premiumchess.ui.theme.PremiumChessTheme
import com.attri.premiumchess.ui.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Enable edge-to-edge
        enableEdgeToEdge()
        
        // Configure immersive mode
        hideSystemUI()

        setContent {
            PremiumChessTheme {
                val viewModel: MainViewModel = viewModel()
                val screenState by viewModel.currentScreen.collectAsState()

                when (screenState) {
                    is MainViewModel.ScreenState.Splash -> {
                        SplashScreen {
                            viewModel.onSplashFinished()
                        }
                    }
                    is MainViewModel.ScreenState.Menu -> {
                        MainMenuScreen {
                            viewModel.onStartSetup()
                        }
                    }
                    is MainViewModel.ScreenState.Setup -> {
                        PlayerSetupScreen(
                            onBack = { viewModel.onCancelSetup() },
                            onStartGame = { config -> viewModel.onStartGame(config) }
                        )
                    }
                    is MainViewModel.ScreenState.Game -> {
                        GameScreen {
                            viewModel.onBackToMenu()
                        }
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}