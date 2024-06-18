package app.model

import java.awt.Color

val STARTING_ROW =
    listOf(Piece.ROOK, Piece.KNIGHT, Piece.BISHOP, Piece.QUEEN, Piece.KING, Piece.BISHOP, Piece.KNIGHT, Piece.ROOK)

class Position {
    private val chessboard: Array<Array<ChessPiece?>> = Array(size = 8, init = { Array(size = 8, init = { null }) })
    private var turnColor: Color
    private val possibleMoves: MutableList<Pair<Pair<Int, Int>, Pair<Int, Int>>> = mutableListOf()
    private var isCheck: Boolean = false

    constructor(position: Position) {
        for (i in 0..<8) {
            for (j in 0..<8) {
                chessboard[i][j] =
                    if (position.chessboard[i][j] != null) ChessPiece(position.chessboard[i][j]!!) else null
            }
        }

        turnColor = position.turnColor

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

        turnColor = Color.WHITE
    }

    private fun changeTurn() {
        turnColor = if (turnColor == Color.WHITE) Color.BLACK else Color.WHITE
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

        changeTurn()
    }

    fun getChessBoard() = chessboard

    override fun hashCode(): Int {
        return chessboard.contentDeepHashCode()
    }

    fun findPossibleMoves(ignoreCheck: Boolean = false) {
        for (i in 0..<8) {
            for (j in 0..<8) {
                val piece = chessboard[i][j] ?: continue

                if (piece.color != turnColor) {
                    continue
                }

                when (piece.type) {
                    Piece.PAWN -> findPawnMoves(i, j)
                    Piece.ROOK -> findRookMoves(i, j)
                    Piece.BISHOP -> findBishopMoves(i, j)
                    Piece.KING -> findKingMoves(i, j)
                    Piece.KNIGHT -> findKnightMoves(i, j)
                    Piece.QUEEN -> {
                        findRookMoves(i, j)
                        findBishopMoves(i, j)
                    }
                }
            }
        }
    }

    private fun findKnightMoves(i: Int, j: Int) {

    }

    private fun findKingMoves(i: Int, j: Int) {
        TODO("Not yet implemented")
    }

    private fun findBishopMoves(i: Int, j: Int) {
        TODO("Not yet implemented")
    }

    private fun findRookMoves(i: Int, j: Int) {
        TODO("Not yet implemented")
    }

    private fun findPawnMoves(row: Int, col: Int, ignoreCheck: Boolean = false) {
        val opposition = if (turnColor == Color.WHITE) Color.BLACK else Color.WHITE
        val direction = if (turnColor == Color.WHITE) 1 else -1

        // Move 1 forward
        if ((row + direction) < 8 && (row + direction) >= 0 && chessboard[row + direction][col] == null) {
            val move = Pair(Pair(row, col), Pair(row + direction, col))

            if (!ignoreCheck && simulateMove(move.first, move.second)) {
                possibleMoves.add(move)
            }
        }

        // Move 2 forward
        if ((row + direction * 2) < 8 && (row + direction * 2) >= 0 && chessboard[row + direction][col] == null && chessboard[row + direction * 2][col] == null && !chessboard[row][col]!!.hasMoved()) {
            val move = Pair(Pair(row, col), Pair(row + direction * 2, col))

            if (!ignoreCheck && simulateMove(move.first, move.second)) {
                possibleMoves.add(move)
            }
        }

        // Take left
        if ((row + direction) < 8 && (row + direction) >= 0 && (col - 1) >= 0 && chessboard[row + direction][col - 1] != null) {
        }
    }

    private fun simulateMove(from: Pair<Int, Int>, to: Pair<Int, Int>): Boolean {
        val pos = Position(this)
        pos.makeMove(from, to)
        pos.findPossibleMoves(ignoreCheck = true)
        return !pos.isCheck
    }
}

fun Array<Array<ChessPiece?>>.printReadable() {
    (0..2).forEach { _ -> println() }
    this.forEach { row -> println(row.contentToString()) }
}