package com.attri.premiumchess.domain.models

data class BoardState(
    val pieces: Map<Position, ChessPiece> = emptyMap(),
    val turn: PieceColor = PieceColor.WHITE,
    val lastMove: Move? = null,
    val capturedPieces: List<ChessPiece> = emptyList(),
    val whiteKingSideCastle: Boolean = true,
    val whiteQueenSideCastle: Boolean = true,
    val blackKingSideCastle: Boolean = true,
    val blackQueenSideCastle: Boolean = true,
    val enPassantTarget: Position? = null,
    val halfMoveClock: Int = 0,
    val fullMoveNumber: Int = 1,
    val isCheck: Boolean = false,
    val isCheckmate: Boolean = false,
    val isStalemate: Boolean = false,
    val isDraw: Boolean = false // Insufficient material, 50-move rule, etc.
) {
    companion object {
        fun initial(): BoardState {
            val pieces = mutableMapOf<Position, ChessPiece>()

            // White pieces
            pieces[Position(0, 0)] = ChessPiece(PieceType.ROOK, PieceColor.WHITE)
            pieces[Position(1, 0)] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE)
            pieces[Position(2, 0)] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE)
            pieces[Position(3, 0)] = ChessPiece(PieceType.QUEEN, PieceColor.WHITE)
            pieces[Position(4, 0)] = ChessPiece(PieceType.KING, PieceColor.WHITE)
            pieces[Position(5, 0)] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE)
            pieces[Position(6, 0)] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE)
            pieces[Position(7, 0)] = ChessPiece(PieceType.ROOK, PieceColor.WHITE)

            for (i in 0..7) {
                pieces[Position(i, 1)] = ChessPiece(PieceType.PAWN, PieceColor.WHITE)
            }

            // Black pieces
            pieces[Position(0, 7)] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)
            pieces[Position(1, 7)] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
            pieces[Position(2, 7)] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
            pieces[Position(3, 7)] = ChessPiece(PieceType.QUEEN, PieceColor.BLACK)
            pieces[Position(4, 7)] = ChessPiece(PieceType.KING, PieceColor.BLACK)
            pieces[Position(5, 7)] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
            pieces[Position(6, 7)] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
            pieces[Position(7, 7)] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)

            for (i in 0..7) {
                pieces[Position(i, 6)] = ChessPiece(PieceType.PAWN, PieceColor.BLACK)
            }

            return BoardState(pieces)
        }
    }
}