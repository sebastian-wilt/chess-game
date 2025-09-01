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

    // Create new position from previous position
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

    // Create default starting position
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

    // Override equals and hashCode for use in hashmap

    override fun equals(other: Any?): Boolean {
        if (other !is Position) return false

        for (i in 0..<8) {
            for (j in 0..<8) {
                if (other.chessboard[i][j] != chessboard[i][j]) return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        return chessboard.contentDeepHashCode()
    }

    fun checkValidMove(from: Pair<Int, Int>, to: Pair<Int, Int>) = (Pair(from, to) in possibleMoves)

    // Move must be legal
    // use checkValidMove first
    fun makeMove(move: String, simulation: Boolean = false) {
        val movePair = fromLongAlgebraicNotation(move)
        val from = movePair.first
        val to = movePair.second

        val isPromotion = move.length == 5

        val isPawnMoveToEmpty =
            chessboard[from.first][from.second]?.type == Piece.PAWN && chessboard[to.first][to.second] == null

        val isDiagonalMove =
            abs(to.first - from.first) == 1 && abs(to.second - from.second) == 1

        val isEnPassant = isPawnMoveToEmpty && isDiagonalMove

        if (isEnPassant) {
            val direction = if (turnColor == Color.WHITE) -1 else 1
            chessboard[to.first - direction][to.second] = null
        }

        // Also known as castling, but alas
        val isLongKingMove = chessboard[from.first][from.second]?.type == Piece.KING && abs(to.second - from.second) > 1

        if (isLongKingMove) {
            val isShortCastle = to.second == 6

            // Also move rook as castling moves both king and rook
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

        if (isPromotion) {
            val piece = when (move[4]) {
                'q' -> Piece.QUEEN
                'r' -> Piece.ROOK
                'b' -> Piece.BISHOP
                'n' -> Piece.KNIGHT
                else -> Piece.QUEEN
            }

            chessboard[to.first][to.second] = ChessPiece(piece, turnColor)
        }

        changeTurn()
    }

    private fun fromLongAlgebraicNotation(move: String): Pair<Pair<Int, Int>, Pair<Int, Int>> {
        val fromCol = squares.indexOf(move[0])
        val fromRow = 8 - move[1].toString().toInt()
        val from = Pair(fromRow, fromCol)
        val toCol = squares.indexOf(move[2])
        val toRow = 8 - move[3].toString().toInt()
        val to = Pair(toRow, toCol)

        return Pair(from, to)
    }

    private fun toLongAlgebricNotation(from: Pair<Int, Int>, to: Pair<Int, Int>): String {
        var src = squares[from.second] + (8 - from.first).toString()
        var dest = squares[to.second] + (8 - to.first).toString()
        return "$src$dest"
    }

    fun getChessBoard() = chessboard


    // Find all legal moves for position
    //
    // ignoreCheck used for simulation to check if move would put player themself in check
    // useful for not allowing move where king would move into check by pinned piece
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
    }

    private fun findKnightMoves(row: Int, col: Int, ignoreCheck: Boolean = false) {
        for (i in -2..2) {
            for (j in -2..2) {
                // Only consider squares that knight can move to (2 + 1)
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
        // Check 3x3 around king
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
            // Need to check both 1 and 2 squares right as it is not legal to castle through check
            if (simulateMove(from, Pair(row, col + 1)) && simulateMove(from, Pair(row, col + 2))) {
                possibleMoves.add(Pair(from, Pair(row, col + 2)))
            }
        }

        val leftSquaresEmpty =
            chessboard[row][col - 1] == null && chessboard[row][col - 2] == null && chessboard[row][col - 3] == null
        val leftRookValid =
            chessboard[row][col - 4]?.type == Piece.ROOK && chessboard[row][col - 4]?.color == turnColor && chessboard[row][col - 4]?.hasMoved() == false

        if (leftRookValid && leftSquaresEmpty) {
            // Need to check both 1 and 2 squares left as it is not legal to castle through check
            if (simulateMove(from, Pair(row, col - 1)) && simulateMove(from, Pair(row, col - 2))) {
                possibleMoves.add(Pair(from, Pair(row, col - 2)))
            }
        }

    }

    private fun findBishopMoves(row: Int, col: Int, ignoreCheck: Boolean = false) {
        val opposition = if (turnColor == Color.WHITE) Color.BLACK else Color.WHITE

        // Move down right
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

        // Move up left
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

        // Move up right
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

        // Move down left
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

        // Check en passant
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

    // Check if move is legal
    // Add to list of possible moves if legal and not simulation (ignoreCheck)
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

    // Check if move would put own king in check
    // Returns true if legal move
    private fun simulateMove(from: Pair<Int, Int>, to: Pair<Int, Int>): Boolean {
        val pos = Position(this, lastMove = null, findMoves = false)
        pos.makeMove(toLongAlgebricNotation(from, to), true)
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
