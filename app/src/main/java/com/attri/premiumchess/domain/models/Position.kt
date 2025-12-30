package com.attri.premiumchess.domain.models

data class Position(
    val file: Int, // 0-7 (a-h)
    val rank: Int  // 0-7 (1-8)
) {
    override fun toString(): String {
        return "${(file + 'a'.code).toChar()}${rank + 1}"
    }
}