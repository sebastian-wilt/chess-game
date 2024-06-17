package app

class ChessApp() {
    private val chessGame: ChessGame = ChessGame(this)
    private val gui: ChessView = ChessView(this)

    fun getCurrentPosition() = chessGame.currentPosition

    fun getTurnColor() = chessGame.turnColor

    fun getMoveCounter() = chessGame.moveCounter

    fun getGameState() = chessGame.gameState

    fun makeMove(from: Pair<Int, Int>, to: Pair<Int, Int>) = chessGame.makeMove(from, to)

    fun createNewGame() = chessGame.createNewGame()
}