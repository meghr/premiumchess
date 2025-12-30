package com.attri.premiumchess.domain.models

data class GameConfig(
    val player1Name: String,
    val player2Name: String,
    val timerSeconds: Long
) {
    companion object {
        const val NO_TIMER: Long = -1
    }
}