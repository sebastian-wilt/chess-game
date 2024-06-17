package app.model

import java.awt.Color

val STARTING_ROW =
    listOf(Piece.ROOK, Piece.KNIGHT, Piece.BISHOP, Piece.QUEEN, Piece.KING, Piece.BISHOP, Piece.KNIGHT, Piece.ROOK)

class Position {
    private val chessboard: Array<Array<ChessPiece?>> = Array(size = 8, init = { Array(size = 8, init = { null }) })

    constructor(position: Position) {
        for (i in 0..<8) {
            for (j in 0..<8) {
                chessboard[i][j] =
                    if (position.chessboard[i][j] != null) ChessPiece(position.chessboard[i][j]!!) else null
            }
        }

    }

    constructor() {
        for (i in STARTING_ROW.indices) {
            chessboard[0][i] = ChessPiece(STARTING_ROW[i], Color.BLACK)
            chessboard[1][i] = ChessPiece(Piece.PAWN, Color.BLACK)

            chessboard[6][i] = ChessPiece(Piece.PAWN, Color.WHITE)
            chessboard[7][i] = ChessPiece(STARTING_ROW[i], Color.WHITE)

            for (j in (2..<6)) {
                chessboard[j][i] = null
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Position) return false

        for (i in 0..<8) {
            for (j in 0..<8) {
                if (other.chessboard[i][j] != chessboard[i][j]) return false
            }
        }

        return true
    }


    fun makeMove(from: Pair<Int, Int>, to: Pair<Int, Int>) {
        chessboard[to.first][to.second] = chessboard[from.first][from.second]
        chessboard[from.first][from.second] = null
        chessboard[to.first][to.second]!!.move()
        chessboard.printReadable()
    }

    fun getChessBoard() = chessboard

    override fun hashCode(): Int {
        return chessboard.contentDeepHashCode()
    }
}

fun Array<Array<ChessPiece?>>.printReadable() {
    (0..2).forEach { _ -> println() }
    this.forEach { row -> println(row.contentToString()) }
}