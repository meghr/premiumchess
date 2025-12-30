package com.attri.premiumchess.domain.logic

import com.attri.premiumchess.domain.models.BoardState
import com.attri.premiumchess.domain.models.ChessPiece
import com.attri.premiumchess.domain.models.Move
import com.attri.premiumchess.domain.models.MoveType
import com.attri.premiumchess.domain.models.PieceColor
import com.attri.premiumchess.domain.models.PieceType
import com.attri.premiumchess.domain.models.Position
import kotlin.math.abs

object GameEngine {

    fun getLegalMovesForPiece(state: BoardState, position: Position): List<Move> {
        val piece = state.pieces[position] ?: return emptyList()
        if (piece.color != state.turn) return emptyList()

        val pseudoMoves = getPseudoLegalMoves(state, position, piece)
        return pseudoMoves.filter { move ->
            !isKingInCheckAfterMove(state, move)
        }
    }

    // Helper function to execute move ONLY for validation purposes (lighter version)
    // Avoids infinite recursion by NOT calling checkmate/stalemate detection
    private fun makeMoveInternal(state: BoardState, move: Move): BoardState {
        val newPieces = state.pieces.toMutableMap()
        
        // Remove moving piece from source
        val movingPiece = newPieces.remove(move.from) ?: return state
        
        // Handle normal capture
        if (newPieces.containsKey(move.to)) {
             newPieces.remove(move.to)
        }
        
        // Handle Castling
        if (move.moveType == MoveType.CASTLE_KINGSIDE || move.moveType == MoveType.CASTLE_QUEENSIDE) {
            val isKingSide = move.moveType == MoveType.CASTLE_KINGSIDE
            val rank = move.from.rank
            val rookFromCol = if (isKingSide) 7 else 0
            val rookToCol = if (isKingSide) 5 else 3
            val rookPos = Position(rookFromCol, rank)
            val rook = newPieces.remove(rookPos)
            if (rook != null) {
                newPieces[Position(rookToCol, rank)] = rook.copy(hasMoved = true)
            }
        }
        
        // Handle En Passant
        if (move.moveType == MoveType.EN_PASSANT) {
             val captureRank = if (movingPiece.color == PieceColor.WHITE) move.to.rank - 1 else move.to.rank + 1
             newPieces.remove(Position(move.to.file, captureRank))
        }

        // Place moving piece
        var finalPiece = movingPiece.copy(hasMoved = true)
        
        // Handle Promotion
        if (move.moveType == MoveType.PROMOTION) {
            finalPiece = finalPiece.copy(type = PieceType.QUEEN)
        }

        newPieces[move.to] = finalPiece

        // Simple state update just for check validation (no history, castling rights update needed for this check)
        return state.copy(
            pieces = newPieces,
            turn = if (state.turn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        )
    }

    fun makeMove(state: BoardState, move: Move): BoardState {
        val newPieces = state.pieces.toMutableMap()
        
        // Remove moving piece from source
        val movingPiece = newPieces.remove(move.from) ?: return state
        
        // Handle normal capture
        val captured = if (newPieces.containsKey(move.to)) {
             newPieces.remove(move.to)
        } else null
        
        // Track captured pieces for UI
        val capturedList = state.capturedPieces.toMutableList()
        if (captured != null) capturedList.add(captured)

        // Handle Castling
        if (move.moveType == MoveType.CASTLE_KINGSIDE || move.moveType == MoveType.CASTLE_QUEENSIDE) {
            val isKingSide = move.moveType == MoveType.CASTLE_KINGSIDE
            val rank = move.from.rank
            val rookFromCol = if (isKingSide) 7 else 0
            val rookToCol = if (isKingSide) 5 else 3
            val rookPos = Position(rookFromCol, rank)
            val rook = newPieces.remove(rookPos)
            if (rook != null) {
                newPieces[Position(rookToCol, rank)] = rook.copy(hasMoved = true)
            }
        }
        
        // Handle En Passant
        if (move.moveType == MoveType.EN_PASSANT) {
             val captureRank = if (movingPiece.color == PieceColor.WHITE) move.to.rank - 1 else move.to.rank + 1
             val epPawn = newPieces.remove(Position(move.to.file, captureRank))
             if (epPawn != null) capturedList.add(epPawn)
        }

        // Place moving piece
        var finalPiece = movingPiece.copy(hasMoved = true)
        
        // Handle Promotion
        if (move.moveType == MoveType.PROMOTION) {
            finalPiece = finalPiece.copy(type = PieceType.QUEEN) // Auto-promote to Queen for now
        }

        newPieces[move.to] = finalPiece

        // Update Castling Rights
        var wK = state.whiteKingSideCastle
        var wQ = state.whiteQueenSideCastle
        var bK = state.blackKingSideCastle
        var bQ = state.blackQueenSideCastle

        // If king moves, lose all castling rights
        if (movingPiece.type == PieceType.KING) {
            if (movingPiece.color == PieceColor.WHITE) { wK = false; wQ = false }
            else { bK = false; bQ = false }
        }
        
        // If rook moves, lose specific right
        if (movingPiece.type == PieceType.ROOK) {
            if (movingPiece.color == PieceColor.WHITE) {
                if (move.from.file == 0) wQ = false
                if (move.from.file == 7) wK = false
            } else {
                if (move.from.file == 0) bQ = false
                if (move.from.file == 7) bK = false
            }
        }
        
        // If rook is captured, opponent loses specific right
        if (captured != null && captured.type == PieceType.ROOK) {
            if (captured.color == PieceColor.WHITE) {
                if (move.to.file == 0 && move.to.rank == 0) wQ = false
                if (move.to.file == 7 && move.to.rank == 0) wK = false
            } else {
                 if (move.to.file == 0 && move.to.rank == 7) bQ = false
                 if (move.to.file == 7 && move.to.rank == 7) bK = false
            }
        }

        // Update En Passant Target
        var epTarget: Position? = null
        if (movingPiece.type == PieceType.PAWN && abs(move.from.rank - move.to.rank) == 2) {
            val epRank = if (movingPiece.color == PieceColor.WHITE) 2 else 5
            epTarget = Position(move.from.file, epRank)
        }

        val nextTurn = if (state.turn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        
        // Check game termination conditions for NEXT state
        // Use makeMoveInternal logic here implicitly via checking isKingInCheck on new pieces
        // We construct a state just for checking check status
        val tempStateForCheck = state.copy(pieces = newPieces, turn = nextTurn) 
        val isCheck = isKingInCheck(tempStateForCheck, nextTurn)
        
        val newState = state.copy(
            pieces = newPieces,
            turn = nextTurn,
            lastMove = move,
            whiteKingSideCastle = wK,
            whiteQueenSideCastle = wQ,
            blackKingSideCastle = bK,
            blackQueenSideCastle = bQ,
            enPassantTarget = epTarget,
            capturedPieces = capturedList,
            isCheck = isCheck
        )
        
        // Check for mate only on the fully constructed new state
        val hasLegalMoves = hasAnyLegalMoves(newState)
        
        return newState.copy(
            isCheckmate = isCheck && !hasLegalMoves,
            isStalemate = !isCheck && !hasLegalMoves
        )
    }
    
    private fun hasAnyLegalMoves(state: BoardState): Boolean {
        for ((pos, piece) in state.pieces) {
            if (piece.color == state.turn) {
                // Optimization: if we find ONE legal move, return true
                if (getLegalMovesForPiece(state, pos).isNotEmpty()) return true
            }
        }
        return false
    }

    private fun getPseudoLegalMoves(state: BoardState, pos: Position, piece: ChessPiece): List<Move> {
        val moves = mutableListOf<Move>()
        when (piece.type) {
            PieceType.PAWN -> moves.addAll(getPawnMoves(state, pos, piece))
            PieceType.KNIGHT -> moves.addAll(getKnightMoves(state, pos, piece))
            PieceType.BISHOP -> moves.addAll(getSlidingMoves(state, pos, piece, listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1)))
            PieceType.ROOK -> moves.addAll(getSlidingMoves(state, pos, piece, listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)))
            PieceType.QUEEN -> moves.addAll(getSlidingMoves(state, pos, piece, listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1, 1 to 1, 1 to -1, -1 to 1, -1 to -1)))
            PieceType.KING -> moves.addAll(getKingMoves(state, pos, piece))
        }
        return moves
    }

    private fun getPawnMoves(state: BoardState, pos: Position, piece: ChessPiece): List<Move> {
        val moves = mutableListOf<Move>()
        val direction = if (piece.color == PieceColor.WHITE) 1 else -1
        val startRank = if (piece.color == PieceColor.WHITE) 1 else 6
        val promotionRank = if (piece.color == PieceColor.WHITE) 7 else 0

        // 1. Forward 1
        val fwd1 = Position(pos.file, pos.rank + direction)
        if (isValidPos(fwd1) && state.pieces[fwd1] == null) {
            val type = if (fwd1.rank == promotionRank) MoveType.PROMOTION else MoveType.NORMAL
            moves.add(Move(pos, fwd1, piece, moveType = type))
            
            // 2. Forward 2
            if (pos.rank == startRank) {
                val fwd2 = Position(pos.file, pos.rank + direction * 2)
                if (state.pieces[fwd2] == null) {
                    moves.add(Move(pos, fwd2, piece))
                }
            }
        }

        // 3. Captures
        val captureOffsets = listOf(-1, 1)
        for (offset in captureOffsets) {
            val capturePos = Position(pos.file + offset, pos.rank + direction)
            if (isValidPos(capturePos)) {
                val target = state.pieces[capturePos]
                
                // Normal capture
                if (target != null && target.color != piece.color) {
                    val type = if (capturePos.rank == promotionRank) MoveType.PROMOTION else MoveType.NORMAL
                    moves.add(Move(pos, capturePos, piece, capturedPiece = target, moveType = type))
                }
                
                // En Passant
                if (target == null && state.enPassantTarget == capturePos) {
                     // The actual pawn being captured is behind the target square
                     // Logic checks out for move gen, execution handles removal
                     moves.add(Move(pos, capturePos, piece, moveType = MoveType.EN_PASSANT))
                }
            }
        }
        return moves
    }

    private fun getKnightMoves(state: BoardState, pos: Position, piece: ChessPiece): List<Move> {
        val moves = mutableListOf<Move>()
        val offsets = listOf(
            1 to 2, 1 to -2, -1 to 2, -1 to -2,
            2 to 1, 2 to -1, -2 to 1, -2 to -1
        )
        for ((dx, dy) in offsets) {
            val target = Position(pos.file + dx, pos.rank + dy)
            if (isValidPos(target)) {
                val targetPiece = state.pieces[target]
                if (targetPiece == null || targetPiece.color != piece.color) {
                    moves.add(Move(pos, target, piece, capturedPiece = targetPiece))
                }
            }
        }
        return moves
    }

    private fun getSlidingMoves(state: BoardState, pos: Position, piece: ChessPiece, directions: List<Pair<Int, Int>>): List<Move> {
        val moves = mutableListOf<Move>()
        for ((dx, dy) in directions) {
            var current = Position(pos.file + dx, pos.rank + dy)
            while (isValidPos(current)) {
                val targetPiece = state.pieces[current]
                if (targetPiece == null) {
                    moves.add(Move(pos, current, piece))
                } else {
                    if (targetPiece.color != piece.color) {
                        moves.add(Move(pos, current, piece, capturedPiece = targetPiece))
                    }
                    break // Blocked
                }
                current = Position(current.file + dx, current.rank + dy)
            }
        }
        return moves
    }

    private fun getKingMoves(state: BoardState, pos: Position, piece: ChessPiece): List<Move> {
        val moves = mutableListOf<Move>()
        // Normal moves
        for (dx in -1..1) {
            for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                val target = Position(pos.file + dx, pos.rank + dy)
                if (isValidPos(target)) {
                    val targetPiece = state.pieces[target]
                    if (targetPiece == null || targetPiece.color != piece.color) {
                        moves.add(Move(pos, target, piece, capturedPiece = targetPiece))
                    }
                }
            }
        }
        
        // Castling
        // King must not be in check to start castling
        if (!state.isCheck) {
            val rank = if (piece.color == PieceColor.WHITE) 0 else 7
            
            // King Side
            val canCastleKingSide = if (piece.color == PieceColor.WHITE) state.whiteKingSideCastle else state.blackKingSideCastle
            if (canCastleKingSide) {
                val f1 = Position(5, rank) // f
                val g1 = Position(6, rank) // g
                if (state.pieces[f1] == null && state.pieces[g1] == null) {
                    // Cannot castle through check. 
                    // Square f1 must not be attacked. (g1 is destination, checked by isKingInCheckAfterMove)
                    if (!isSquareAttacked(state, f1, piece.color)) {
                         moves.add(Move(pos, g1, piece, moveType = MoveType.CASTLE_KINGSIDE))
                    }
                }
            }

            // Queen Side
            val canCastleQueenSide = if (piece.color == PieceColor.WHITE) state.whiteQueenSideCastle else state.blackQueenSideCastle
            if (canCastleQueenSide) {
                 val d1 = Position(3, rank) // d
                 val c1 = Position(2, rank) // c
                 val b1 = Position(1, rank) // b
                 if (state.pieces[d1] == null && state.pieces[c1] == null && state.pieces[b1] == null) {
                     // Cannot castle through check. d1 must not be attacked.
                     if (!isSquareAttacked(state, d1, piece.color)) {
                         moves.add(Move(pos, c1, piece, moveType = MoveType.CASTLE_QUEENSIDE))
                     }
                }
            }
        }
        
        return moves
    }

    private fun isValidPos(pos: Position): Boolean {
        return pos.file in 0..7 && pos.rank in 0..7
    }

    fun isSquareAttacked(state: BoardState, targetPos: Position, defendColor: PieceColor): Boolean {
        // Check knight jumps
        val knightOffsets = listOf(1 to 2, 1 to -2, -1 to 2, -1 to -2, 2 to 1, 2 to -1, -2 to 1, -2 to -1)
        for ((dx, dy) in knightOffsets) {
            val src = Position(targetPos.file + dx, targetPos.rank + dy)
            if (isValidPos(src)) {
                val p = state.pieces[src]
                if (p != null && p.color != defendColor && p.type == PieceType.KNIGHT) return true
            }
        }
        
        // Check sliding (Rook/Queen)
        val rookDirs = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)
        for ((dx, dy) in rookDirs) {
            var curr = Position(targetPos.file + dx, targetPos.rank + dy)
            while (isValidPos(curr)) {
                val p = state.pieces[curr]
                if (p != null) {
                    if (p.color != defendColor && (p.type == PieceType.ROOK || p.type == PieceType.QUEEN)) return true
                    break
                }
                curr = Position(curr.file + dx, curr.rank + dy)
            }
        }
        
        // Check sliding (Bishop/Queen)
        val bishopDirs = listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1)
        for ((dx, dy) in bishopDirs) {
             var curr = Position(targetPos.file + dx, targetPos.rank + dy)
            while (isValidPos(curr)) {
                val p = state.pieces[curr]
                if (p != null) {
                    if (p.color != defendColor && (p.type == PieceType.BISHOP || p.type == PieceType.QUEEN)) return true
                    break
                }
                curr = Position(curr.file + dx, curr.rank + dy)
            }
        }
        
        // Check Pawns
        val pawnDir = if (defendColor == PieceColor.WHITE) 1 else -1 
        // Pawns attack from 'pawnDir' relative to themselves.
        // So from target perspective, enemy pawns are at rank + pawnDir.
        val attackRank = targetPos.rank + pawnDir 
        if (attackRank in 0..7) {
             if (targetPos.file - 1 >= 0) {
                 val p = state.pieces[Position(targetPos.file - 1, attackRank)]
                 if (p != null && p.color != defendColor && p.type == PieceType.PAWN) return true
             }
             if (targetPos.file + 1 <= 7) {
                 val p = state.pieces[Position(targetPos.file + 1, attackRank)]
                 if (p != null && p.color != defendColor && p.type == PieceType.PAWN) return true
             }
        }
        
        // Check King (adjacent)
        for (dx in -1..1) {
            for (dy in -1..1) {
                if (dx==0 && dy==0) continue
                val src = Position(targetPos.file + dx, targetPos.rank + dy)
                if (isValidPos(src)) {
                    val p = state.pieces[src]
                    if (p != null && p.color != defendColor && p.type == PieceType.KING) return true
                }
            }
        }

        return false
    }

    fun isKingInCheck(state: BoardState, color: PieceColor): Boolean {
        // Find King
        val kingPos = state.pieces.entries.find { it.value.type == PieceType.KING && it.value.color == color }?.key ?: return false
        return isSquareAttacked(state, kingPos, color)
    }

    private fun isKingInCheckAfterMove(state: BoardState, move: Move): Boolean {
        // Use makeMoveInternal which does NOT trigger recursive checkmate scans
        val newState = makeMoveInternal(state, move)
        val myColor = state.turn
        return isKingInCheck(newState, myColor)
    }
}