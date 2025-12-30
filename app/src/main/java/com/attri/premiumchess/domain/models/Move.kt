package com.attri.premiumchess.domain.models

data class Move(
    val from: Position,
    val to: Position,
    val piece: ChessPiece,
    val capturedPiece: ChessPiece? = null,
    val moveType: MoveType = MoveType.NORMAL
)

enum class MoveType {
    NORMAL,
    CASTLE_KINGSIDE,
    CASTLE_QUEENSIDE,
    EN_PASSANT,
    PROMOTION
}