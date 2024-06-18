package app.model

import java.awt.Color

class ChessPiece {
    val type: Piece
    val color: Color
    private var hasMoved = false

    constructor(type: Piece, color: Color) {
        this.type = type
        this.color = color
    }

    constructor(other: ChessPiece) {
        type = other.type
        hasMoved = other.hasMoved
        color = other.color
    }

    override fun toString(): String {
        return "${if (color == Color.BLACK) "BLACK" else "WHITE"}_${type}"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ChessPiece) {
            return false
        }
        return type == other.type && hasMoved == other.hasMoved() && color == other.color
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + hasMoved.hashCode()
        result = 31 * result + color.hashCode()
        return result
    }

    fun move() {
        hasMoved = true
    }

    fun hasMoved(): Boolean {
        return hasMoved
    }

}