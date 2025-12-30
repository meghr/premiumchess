package com.attri.premiumchess.domain.models

/**
 * Represents a chess piece type and color.
 * Domain model for the game logic.
 */
enum class PieceType {
    PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
}

enum class PieceColor {
    WHITE, BLACK
}

data class ChessPiece(
    val type: PieceType,
    val color: PieceColor,
    val hasMoved: Boolean = false
)