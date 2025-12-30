package com.attri.premiumchess.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.attri.premiumchess.utils.Constants

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        val animDuration = 1000
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = animDuration)
        )
        // Ensure total time matches SPLASH_DELAY (2000ms)
        val remainingTime = Constants.SPLASH_DELAY - animDuration
        if (remainingTime > 0) {
            delay(remainingTime)
        }
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Placeholder for logo
            Text(
                text = "♔",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 100.sp,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.alpha(alpha.value)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "PREMIUM CHESS",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.alpha(alpha.value)
            )
        }
    }
}