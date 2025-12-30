package com.attri.premiumchess.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.attri.premiumchess.R
import com.attri.premiumchess.domain.models.ChessPiece
import com.attri.premiumchess.domain.models.PieceColor
import com.attri.premiumchess.domain.models.PieceType

@Composable
fun ChessPieceComposable(
    piece: ChessPiece,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val resourceId = when (piece.color) {
        PieceColor.WHITE -> when (piece.type) {
            PieceType.PAWN -> R.drawable.ic_w_pawn
            PieceType.ROOK -> R.drawable.ic_w_rook
            PieceType.KNIGHT -> R.drawable.ic_w_knight
            PieceType.BISHOP -> R.drawable.ic_w_bishop
            PieceType.QUEEN -> R.drawable.ic_w_queen
            PieceType.KING -> R.drawable.ic_w_king
        }
        PieceColor.BLACK -> when (piece.type) {
            PieceType.PAWN -> R.drawable.ic_b_pawn
            PieceType.ROOK -> R.drawable.ic_b_rook
            PieceType.KNIGHT -> R.drawable.ic_b_knight
            PieceType.BISHOP -> R.drawable.ic_b_bishop
            PieceType.QUEEN -> R.drawable.ic_b_queen
            PieceType.KING -> R.drawable.ic_b_king
        }
    }

    Image(
        painter = painterResource(id = resourceId),
        contentDescription = "${piece.color} ${piece.type}",
        modifier = modifier.size(size)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF0D9B5)
@Composable
fun ChessPiecePreview() {
    Row {
        ChessPieceComposable(ChessPiece(PieceType.KING, PieceColor.WHITE), 64.dp)
        ChessPieceComposable(ChessPiece(PieceType.QUEEN, PieceColor.BLACK), 64.dp)
        ChessPieceComposable(ChessPiece(PieceType.KNIGHT, PieceColor.WHITE), 64.dp)
    }
}