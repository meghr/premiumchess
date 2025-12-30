package com.attri.premiumchess.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attri.premiumchess.domain.models.GameConfig

@Composable
fun PlayerSetupScreen(
    onBack: () -> Unit,
    onStartGame: (GameConfig) -> Unit
) {
    var player1Name by remember { mutableStateOf("") }
    var player2Name by remember { mutableStateOf("") }
    var selectedTimerOption by remember { mutableStateOf<TimerOption>(TimerOption.Blitz5) }
    var showCustomTimerDialog by remember { mutableStateOf(false) }
    var customMinutes by remember { mutableStateOf("10") }
    var customSeconds by remember { mutableStateOf("00") }
    var showExitDialog by remember { mutableStateOf(false) }
    var customTimerSeconds by remember { mutableStateOf(600L) }

    val isValidPlayer1 = player1Name.isNotBlank()
    val isValidPlayer2 = player2Name.isNotBlank()
    val canStart = isValidPlayer1 && isValidPlayer2

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val startGameButtonRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (player1Name.isNotEmpty() || player2Name.isNotEmpty()) {
                        showExitDialog = true
                    } else {
                        onBack()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "NEW GAME SETUP",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(48.dp)) // Balance for back button
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(48.dp)
            ) {
                // Left Column: Player Names & Start Button
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PlayerInputSection(
                        label = "Player 1 (White)",
                        name = player1Name,
                        onNameChange = { if (it.length <= 20) player1Name = it },
                        isWhite = true,
                        errorMessage = if (!isValidPlayer1 && player1Name.isNotEmpty()) "Name cannot be empty" else null,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    PlayerInputSection(
                        label = "Player 2 (Black)",
                        name = player2Name,
                        onNameChange = { if (it.length <= 20) player2Name = it },
                        isWhite = false,
                        errorMessage = if (!isValidPlayer2 && player2Name.isNotEmpty()) "Name cannot be empty" else null,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            startGameButtonRequester.requestFocus()
                        })
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = {
                            val finalSeconds = if (selectedTimerOption == TimerOption.Custom) customTimerSeconds else selectedTimerOption.seconds
                            val config = GameConfig(
                                player1Name = player1Name.trim(),
                                player2Name = player2Name.trim(),
                                timerSeconds = finalSeconds
                            )
                            onStartGame(config)
                        },
                        enabled = canStart,
                        modifier = Modifier
                            .focusRequester(startGameButtonRequester)
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "START GAME",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }

                // Right Column: Timer Selection
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = "GAME TIMER",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(TimerOption.values()) { option ->
                            TimerOptionCard(
                                option = option,
                                isSelected = selectedTimerOption == option,
                                onSelect = {
                                    if (it == TimerOption.Custom) {
                                        selectedTimerOption = it
                                        showCustomTimerDialog = true
                                    } else {
                                        selectedTimerOption = it
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val displayTime = if (selectedTimerOption == TimerOption.Custom) {
                        val m = customTimerSeconds / 60
                        val s = customTimerSeconds % 60
                        String.format("%02d:%02d", m, s)
                    } else {
                        selectedTimerOption.label
                    }

                    Text(
                        text = "Selected: $displayTime",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Discard Setup?") },
            text = { Text("Are you sure you want to go back? Your current setup will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onBack()
                }) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCustomTimerDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTimerDialog = false },
            title = { Text("Custom Timer") },
            text = {
                Column {
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { if (it.all { c -> c.isDigit() }) customMinutes = it },
                        label = { Text("Minutes (0-90)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customSeconds,
                        onValueChange = { if (it.all { c -> c.isDigit() }) customSeconds = it },
                        label = { Text("Seconds (0-59)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val mins = customMinutes.toIntOrNull() ?: 0
                    val secs = customSeconds.toIntOrNull() ?: 0
                    val totalSeconds = (mins * 60L) + secs
                    
                    if (totalSeconds in 30..5400) { // 30s to 90m
                        customTimerSeconds = totalSeconds
                        showCustomTimerDialog = false
                    }
                }) {
                    Text("Set")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTimerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PlayerInputSection(
    label: String,
    name: String,
    onNameChange: (String) -> Unit,
    isWhite: Boolean,
    errorMessage: String?,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isWhite) Color.White else Color.Black)
                    .then(
                        if (isWhite) Modifier else Modifier
                            .border(BorderStroke(1.dp, Color.White), CircleShape)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text("Enter Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = errorMessage != null
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun TimerOptionCard(
    option: TimerOption,
    isSelected: Boolean,
    onSelect: (TimerOption) -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "border"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onSelect(option) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            )
            if (option.subLabel.isNotEmpty()) {
                Text(
                    text = option.subLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

enum class TimerOption(val label: String, val subLabel: String, val seconds: Long) {
    Bullet1("1 min", "Bullet", 60),
    Blitz3("3 min", "Blitz", 180),
    Blitz5("5 min", "Blitz", 300),
    Rapid10("10 min", "Rapid", 600),
    Rapid15("15 min", "Rapid", 900),
    Classical30("30 min", "Classical", 1800),
    Casual("No Timer", "Casual", GameConfig.NO_TIMER),
    Custom("Custom", "Set Time", 0)
}