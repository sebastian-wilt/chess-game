package app.stockfish;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;

public class Stockfish {
    private String path;
    private Process proc;
    private BufferedReader input;
    private BufferedWriter output;

    public Stockfish(String path) {
        this.path = path;
        start();
    }

    private void start() {
        var userDirectory = Paths.get("").toAbsolutePath().toString();
        var pb = new ProcessBuilder(path).directory(new File(userDirectory));
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        pb.redirectInput(ProcessBuilder.Redirect.PIPE);
        try {
            proc = pb.start();
            input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            output.write("uci\n");
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            String msg;
            do {
                msg = input.readLine();
            } while (!msg.equals("uciok"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Decide number of threads
        var threads = Runtime.getRuntime().availableProcessors();
        try {
            output.write(String.format("setoption name Threads value %d\n", threads / 2));
            output.flush();
            while (!input.ready());
            var msg = input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public String getMove(String moves) {
        try {
            // Start stockfish 
            output.write(String.format("position startpos moves %s\n", moves));
            output.write("go\n");
            output.flush();

            // Give time to think
            Thread.sleep(3000);
            output.write("stop\n");
            output.flush();

            // Read move
            String msg;
            do {
                msg = input.readLine();
            } while (!msg.startsWith("bestmove"));

            var parts = msg.split(" ");
                
            return parts[1];
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);;
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return "0000";
    }

    public void newGame(int strength) {
        try {
            output.write("ucinewgame\n");
            output.write("setoption name UCI_Limitstrength value true\n");
            output.write(String.format("setoption name UCI_Elo value %d\n", strength));
            output.write("isready\n");
            output.flush();
            while (!input.ready());
            var msg = input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
