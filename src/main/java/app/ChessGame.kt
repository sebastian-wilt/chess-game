package app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class ChessGame(private val controller: ChessApp) {
    private val positions: MutableMap<Position, Int> = emptyMap<Position, Int>().toMutableMap()
    private val _currentPosition = MutableStateFlow(Position())
    val currentPosition: StateFlow<Position> = _currentPosition.asStateFlow()


    init {
        positions[_currentPosition.value] = 1
    }

    fun makeMove(from: Pair<Int, Int>, to: Pair<Int, Int>) {
        val newPos = Position(_currentPosition.value)
        newPos.makeMove(from, to)
        _currentPosition.update { newPos }
    }
}