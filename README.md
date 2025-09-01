# Chess App

Simple Chess App made with Java Swing written in Java and Kotlin.

Allows playing player vs player and player vs stockfish (or another UCI compliant chess engine).

There is a provided stockfish binary for Linux x86-64.

If not using linux, or if you want to use another Universal Chess Interface (UCI) compliant chess engine,
download a chess engine and change modify the path 
in the constructor located in ./src/main/java/app/model/ChessGame.kt:14.

Stockfish can be downloaded [here](https://stockfishchess.org/download/).


## Usage

With kotlin:

```sh
kotlin -cp build/libs/Chess-1.0-SNAPSHOT.jar app.Main
```

With provided makefile (requires kotlin installed):

```sh
make run
```

