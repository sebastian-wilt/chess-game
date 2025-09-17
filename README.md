# Chess App

Simple Chess App made with Java Swing written in Java and Kotlin.

Allows playing player vs player and player vs stockfish (or another UCI compliant chess engine).

## Usage

Requires a UCI chess engine to play against an engine.

Stockfish can be downloaded [here](https://stockfishchess.org/download/).

Rename stockfish to ```stockfish-avx2```, or modify the file path
in the constructor located in ./src/main/java/app/model/ChessGame.kt:14.

Requires kotlin and java

With kotlin:

```sh
kotlin -cp build/libs/Chess-1.0-SNAPSHOT.jar app.Main
```

With provided makefile:

```sh
make run
```

