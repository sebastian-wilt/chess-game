package app.model

import java.awt.Color
import kotlin.math.abs

const val printDebug = false
val STARTING_ROW =
    listOf(Piece.ROOK, Piece.KNIGHT, Piece.BISHOP, Piece.QUEEN, Piece.KING, Piece.BISHOP, Piece.KNIGHT, Piece.ROOK)

class Position {
    private val chessboard: Array<Array<ChessPiece?>> = Array(size = 8, init = { Array(size = 8, init = { null }) })
    private var turnColor: Color
    private val lastMove: Pair<Pair<Int, Int>, Pair<Int, Int>>?
    private val possibleMoves: MutableList<Pair<Pair<Int, Int>, Pair<Int, Int>>> = mutableListOf()
    var isCheck: Boolean = false

    constructor(position: Position, lastMove: Pair<Pair<Int, Int>, Pair<Int, Int>>?, findMoves: Boolean = true) {
        for (i in 0..<8) {
            for (j in 0..<8) {
                chessboard[i][j] =
                    if (position.chessboard[i][j] != null) ChessPiece(position.chessboard[i][j]!!) else null
            }
        }

        turnColor = position.turnColor
        this.lastMove = lastMove

        if (findMoves) {
            findPossibleMoves()
            if (possibleMoves.isEmpty()) {
                isCheck = true
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

        turnColor = Color.WHITE
        lastMove = null
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

    fun checkValidMove(from: Pair<Int, Int>, to: Pair<Int, Int>) = (Pair(from, to) in possibleMoves)

    fun makeMove(from: Pair<Int, Int>, to: Pair<Int, Int>, simulation: Boolean = false) {

        val isPawnMoveToEmpty =
            chessboard[from.first][from.second]?.type == Piece.PAWN && chessboard[to.first][to.second] == null

        val isDiagonalMove =
            abs(to.first - from.first) == 1 && abs(to.second - from.second) == 1

        val isEnPassant = isPawnMoveToEmpty && isDiagonalMove

        if (isEnPassant) {
            val direction = if (turnColor == Color.WHITE) -1 else 1
            chessboard[to.first - direction][to.second] = null
        }

        val isLongKingMove = chessboard[from.first][from.second]?.type == Piece.KING && abs(to.second - from.second) > 1

        if (isLongKingMove) {
            val isShortCastle = to.second == 6
            if (isShortCastle) {
                chessboard[from.first][to.second - 1] = chessboard[from.first][to.second + 1]
                chessboard[from.first][to.second + 1] = null
            } else {
                chessboard[from.first][to.second + 1] = chessboard[from.first][to.second - 2]
                chessboard[from.first][to.second - 2] = null
            }
        }

        chessboard[to.first][to.second] = chessboard[from.first][from.second]
        chessboard[from.first][from.second] = null
        chessboard[to.first][to.second]?.move()


        if (!simulation) {
            chessboard.printReadable()
        }

        changeTurn()
    }

    fun getChessBoard() = chessboard

    override fun hashCode(): Int {
        return chessboard.contentDeepHashCode()
    }

    private fun findPossibleMoves(ignoreCheck: Boolean = false) {
        for (i in 0..<8) {
            for (j in 0..<8) {
                val piece = chessboard[i][j] ?: continue

                if (piece.color != turnColor) {
                    continue
                }

                when (piece.type) {
                    Piece.PAWN -> findPawnMoves(i, j, ignoreCheck)
                    Piece.ROOK -> findRookMoves(i, j, ignoreCheck)
                    Piece.BISHOP -> findBishopMoves(i, j, ignoreCheck)
                    Piece.KING -> findKingMoves(i, j, ignoreCheck)
                    Piece.KNIGHT -> findKnightMoves(i, j, ignoreCheck)
                    Piece.QUEEN -> {
                        findRookMoves(i, j, ignoreCheck)
                        findBishopMoves(i, j, ignoreCheck)
                    }
                }
            }
        }

        if (!ignoreCheck) {
            printValidMoves()
        }
    }

    private fun findKnightMoves(row: Int, col: Int, ignoreCheck: Boolean = false) {
        for (i in -2..2) {
            for (j in -2..2) {
                if (!(abs(i) == 1 && abs(j) == 2 || abs(i) == 2 && abs(j) == 1)) {
                    continue
                }

                if ((row + i) !in (0..7) || (col + j) !in (0..7)) {
                    continue
                }

                if (chessboard[row + i][col + j]?.color == turnColor) {
                    continue
                }

                checkLegalMove(Pair(row, col), Pair(row + i, col + j), ignoreCheck)
            }
        }
    }

    private fun findKingMoves(row: Int, col: Int, ignoreCheck: Boolean = false) {
        if (!ignoreCheck && printDebug) {
            println("Checking king moves for ($row, $col)")
        }

        for (i in (-1..1)) {
            for (j in (-1..1)) {
                if (i == 0 && j == 0) {
                    continue
                }

                if ((row + i) in (0..7) && (col + j) in (0..7) && chessboard[row + i][col + j]?.color != turnColor) {
                    checkLegalMove(Pair(row, col), Pair(row + i, col + j), ignoreCheck)
                }
            }
        }

        // Castling

        if (chessboard[row][col]!!.hasMoved()) {
            return
        }

        val rightSquaresEmpty = chessboard[row][col + 1] == null && chessboard[row][col + 2] == null
        val rightRookValid =
            chessboard[row][col + 3]?.type == Piece.ROOK && chessboard[row][col + 3]?.color == turnColor && chessboard[row][col + 3]?.hasMoved() == false

        val from = Pair(row, col)

        if (rightSquaresEmpty && rightRookValid) {
            if (simulateMove(from, Pair(row, col + 1)) && simulateMove(from, Pair(row, col + 2))) {
                possibleMoves.add(Pair(from, Pair(row, col + 2)))
            }
        }

        val leftSquaresEmpty =
            chessboard[row][col - 1] == null && chessboard[row][col - 2] == null && chessboard[row][col - 3] == null
        val leftRookValid =
            chessboard[row][col - 4]?.type == Piece.ROOK && chessboard[row][col - 4]?.color == turnColor && chessboard[row][col - 4]?.hasMoved() == false

        if (leftRookValid && leftSquaresEmpty) {
            if (simulateMove(from, Pair(row, col - 1)) && simulateMove(from, Pair(row, col - 2))) {
                possibleMoves.add(Pair(from, Pair(row, col - 2)))
            }
        }

    }

    private fun findBishopMoves(row: Int, col: Int, ignoreCheck: Boolean = false) {
        if (!ignoreCheck && printDebug) {
            println("Checking bishop moves for ($row, $col)")
        }

        val opposition = if (turnColor == Color.WHITE) Color.BLACK else Color.WHITE

        for (i in (1..7)) {
            val isValidSquare = ((row + i) < 8 && (col + i) < 8)
            val isValidTarget = isValidSquare && chessboard[row + i][col + i]?.color != turnColor
            if (isValidTarget) {
                checkLegalMove(Pair(row, col), Pair(row + i, col + i), ignoreCheck)

                val isOpposition = chessboard[row + i][col + i]?.color == opposition
                if (isOpposition) break else continue
            }

            break
        }

        for (i in (1..7)) {
            val isValidSquare = ((row - i) >= 0 && (col - i) >= 0)
            val isValidTarget = isValidSquare && chessboard[row - i][col - i]?.color != turnColor
            if (isValidTarget) {
                checkLegalMove(Pair(row, col), Pair(row - i, col - i), ignoreCheck)

                val isOpposition = chessboard[row - i][col - i]?.color == opposition
                if (isOpposition) break else continue
            }

            break
        }

        for (i in (1..7)) {
            val isValidSquare = ((row - i) >= 0 && (col + i) < 8)
            val isValidTarget = isValidSquare && chessboard[row - i][col + i]?.color != turnColor
            if (isValidTarget) {
                checkLegalMove(Pair(row, col), Pair(row - i, col + i), ignoreCheck)

                val isOpposition = chessboard[row - i][col + i]?.color == opposition
                if (isOpposition) break else continue
            }

            break
        }

        for (i in (1..7)) {
            val isValidSquare = ((row + i) < 8 && (col - i) >= 0)
            val isValidTarget = isValidSquare && chessboard[row + i][col - i]?.color != turnColor
            if (isValidTarget) {
                checkLegalMove(Pair(row, col), Pair(row + i, col - i), ignoreCheck)

                val isOpposition = chessboard[row + i][col - i]?.color == opposition
                if (isOpposition) break else continue
            }
            break
        }
    }

    private fun findRookMoves(row: Int, col: Int, ignoreCheck: Boolean = false) {
        if (!ignoreCheck && printDebug) {
            println("Checking rook moves for ($row, $col)")
        }

        val opposition = if (turnColor == Color.WHITE) Color.BLACK else Color.WHITE

        // Move down
        for (i in (1..7)) {
            val isValidSquare = (row + i) < 8
            val isValidTarget = isValidSquare && chessboard[row + i][col]?.color != turnColor
            if (isValidTarget) {
                checkLegalMove(Pair(row, col), Pair(row + i, col), ignoreCheck)

                val isOpposition = chessboard[row + i][col]?.color == opposition
                if (isOpposition) break else continue
            }

            break
        }

        // Move up
        for (i in (1..7)) {
            val isValidSquare = (row - i) >= 0
            val isValidTarget = isValidSquare && chessboard[row - i][col]?.color != turnColor
            if (isValidTarget) {
                checkLegalMove(Pair(row, col), Pair(row - i, col), ignoreCheck)

                val isOpposition = chessboard[row - i][col]?.color == opposition
                if (isOpposition) break else continue
            }

            break
        }

        // Move right
        for (i in (1..7)) {
            val isValidSquare = (col + i) < 8
            val isValidTarget = isValidSquare && chessboard[row][col + i]?.color != turnColor
            if (isValidTarget) {
                checkLegalMove(Pair(row, col), Pair(row, col + i), ignoreCheck)

                val isOpposition = chessboard[row][col + i]?.color == opposition
                if (isOpposition) break else continue
            }

            break
        }

        // Move left
        for (i in (1..7)) {
            val isValidSquare = (col - i) >= 0
            val isValidTarget = isValidSquare && chessboard[row][col - i]?.color != turnColor
            if (isValidTarget) {
                checkLegalMove(Pair(row, col), Pair(row, col - i), ignoreCheck)

                val isOpposition = chessboard[row][col - i]?.color == opposition
                if (isOpposition) break else continue
            }

            break
        }
    }

    private fun findPawnMoves(row: Int, col: Int, ignoreCheck: Boolean = false) {
        if (!ignoreCheck && printDebug) {
            println("Checking pawn moves for ($row, $col)")
        }

        val opposition = if (turnColor == Color.WHITE) Color.BLACK else Color.WHITE
        val direction = if (turnColor == Color.WHITE) -1 else 1
        val pawn = chessboard[row][col]!!

        if ((row + direction) in (0..7)) {

            // Move 1 forward
            if (chessboard[row + direction][col] == null) {
                checkLegalMove(Pair(row, col), Pair(row + direction, col), ignoreCheck, canCaptureKing = false)
            }

            // Move 2 forward
            if ((row + direction * 2) in (0..7) && chessboard[row + direction * 2][col] == null && !pawn.hasMoved()) {
                checkLegalMove(Pair(row, col), Pair(row + direction * 2, col), ignoreCheck, canCaptureKing = false)
            }

            // Take left
            if ((col - 1) >= 0 && chessboard[row + direction][col - 1] != null && chessboard[row + direction][col - 1]!!.color == opposition) {
                checkLegalMove(Pair(row, col), Pair(row + direction, col - 1), ignoreCheck)
            }

            // Take right
            if ((col + 1) < 8 && chessboard[row + direction][col + 1] != null && chessboard[row + direction][col + 1]!!.color == opposition) {
                checkLegalMove(Pair(row, col), Pair(row + direction, col + 1), ignoreCheck)
            }
        }


        val lastFrom = lastMove?.first ?: return
        val lastTo = lastMove.second

        println("\n\nLast Move: $lastFrom -> $lastTo")

        if (abs(lastTo.first - lastFrom.first) == 2 && chessboard[lastTo.first][lastTo.second]?.type == Piece.PAWN) {
            // Left
            if (row == lastTo.first && col == lastTo.second - 1) {
                checkLegalMove(Pair(row, col), Pair(row + direction, col + 1), ignoreCheck)
            }

            // Right
            if (row == lastTo.first && col == lastTo.second + 1) {
                checkLegalMove(Pair(row, col), Pair(row + direction, col - 1), ignoreCheck)
            }
        }
    }

    private fun checkLegalMove(
        from: Pair<Int, Int>,
        to: Pair<Int, Int>,
        ignoreCheck: Boolean,
        canCaptureKing: Boolean = true
    ) {
        if (!ignoreCheck && simulateMove(from, to)) {
            possibleMoves.add(Pair(from, to))
        }

        if (ignoreCheck && canCaptureKing) {
            if (chessboard[to.first][to.second]?.type == Piece.KING) {
                isCheck = true
            }
        }
    }

    private fun simulateMove(from: Pair<Int, Int>, to: Pair<Int, Int>): Boolean {
        if (printDebug) {
            println("Simulating move: $from -> $to")
        }
        val pos = Position(this, lastMove = null, findMoves = false)
        pos.makeMove(from, to, true)
        pos.findPossibleMoves(ignoreCheck = true)
        return !pos.isCheck
    }

    private fun printValidMoves() {
        (0..2).forEach { _ -> println() }
        println("Possible moves: ")
        for ((from, to) in possibleMoves) {
            println("$from -> $to")
        }
    }
}

fun Array<Array<ChessPiece?>>.printReadable() {
    (0..2).forEach { _ -> println() }
    this.forEach { row -> println(row.contentToString()) }
}
