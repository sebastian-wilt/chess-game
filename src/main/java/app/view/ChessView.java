package app.view;

import app.controller.ChessApp;
import app.model.GameState;
import app.model.Position;
import kotlin.Pair;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

public class ChessView {
    private final ChessApp controller;
    private JPanel mainPanel;
    private JLabel currentMoveColor;
    private final JButton[][] chessBoardSquares = new JButton[8][8];
    private final ImageIcon defaultIcon = new ImageIcon(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB));
    private int counter = 0;
    private final int[] moveFrom = new int[2];
    private final int[] moveTo = new int[2];
    private final HashMap<String, String> images = new HashMap<>();

    public ChessView(ChessApp controller) {
        this.controller = controller;
        var pos = controller.getCurrentPosition();
        createImages();
        init();
        loadPosition(pos);
    }

    private void init() {
        JFrame frame = new JFrame("Chess");
        mainPanel = new JPanel(new BorderLayout());
        frame.add(mainPanel);

        createMenu();
        createChessBoard();

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        createUpdateThread();
    }

    private void createMenu() {
        JPanel menuPanel = new JPanel(new GridLayout(1, 3));

        JButton newGame = new JButton("New game");
        newGame.addActionListener((e) -> {
            controller.createNewGame();
            loadPosition(controller.getCurrentPosition());
            updateCounter(controller.getMoveCounter(), controller.getTurnColor());
        });

        currentMoveColor = new JLabel("1", JLabel.CENTER);
        currentMoveColor.setOpaque(true);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener((e) -> System.exit(0));

        mainPanel.add(menuPanel, BorderLayout.NORTH);
        menuPanel.add(newGame);
        menuPanel.add(currentMoveColor);
        menuPanel.add(exitButton);
    }

    private void createChessBoard() {
        JPanel chessPanel = new JPanel(new GridLayout(8, 8));
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                JButton newButton = new JButton();
                newButton.setIcon(defaultIcon);
                newButton.addActionListener(new ChessButton(i, j));

                if ((i % 2 == 0 && j % 2 == 0) || (i % 2 == 1 && j % 2 == 1)) {
                    newButton.setBackground(Color.WHITE);
                } else {
                    newButton.setBackground(new Color(184, 142, 114));
                }

                chessBoardSquares[i][j] = newButton;
                chessPanel.add(newButton);
            }
        }

        mainPanel.add(chessPanel, BorderLayout.CENTER);
    }

    private void createImages() {
        images.put("BLACK_KING", "assets/black_king.png");
        images.put("BLACK_PAWN", "assets/black_pawn.png");
        images.put("BLACK_QUEEN", "assets/black_queen.png");
        images.put("BLACK_ROOK", "assets/black_rook.png");
        images.put("BLACK_KNIGHT", "assets/black_knight.png");
        images.put("BLACK_BISHOP", "assets/black_bishop.png");
        images.put("WHITE_KING", "assets/white_king.png");
        images.put("WHITE_PAWN", "assets/white_pawn.png");
        images.put("WHITE_QUEEN", "assets/white_queen.png");
        images.put("WHITE_ROOK", "assets/white_rook.png");
        images.put("WHITE_KNIGHT", "assets/white_knight.png");
        images.put("WHITE_BISHOP", "assets/white_bishop.png");
    }

    private void loadPosition(Position position) {
        var board = position.getChessBoard();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                var string = board[i][j] == null ? null : board[i][j].toString();
                var path = images.getOrDefault(string, null);
                ImageIcon icon;
                if (path != null) {
                    try {
                        var file = new File("./src/main/java/app/" + path);
                        icon = new ImageIcon(ImageIO.read(file));
                    } catch (Exception e) {
                        icon = null;
                    }
                } else {
                    icon = defaultIcon;
                }

                chessBoardSquares[i][j].setIcon(icon);
            }
        }
    }

    private void makeMove() {
        counter = 0;
        new MoveThread(new Pair<>(moveFrom[0], moveFrom[1]), new Pair<>(moveTo[0], moveTo[1])).execute();
        update();
    }

    private void update() {
        loadPosition(controller.getCurrentPosition());
        updateCounter(controller.getMoveCounter(), controller.getTurnColor());

        var gameState = controller.getGameState();
        switch (gameState) {
            case BLACK, WHITE, DRAW -> endGame(gameState);
        }
    }

    public void endGame(GameState state) {
        String message = "";
        switch (state) {
            case BLACK -> message = "Black won";
            case WHITE -> message = "White won";
            case DRAW -> message = "Draw";
        }
        currentMoveColor.setText(message);
    }

    public void updateCounter(int counter, Color turnColor) {
        currentMoveColor.setText(String.valueOf(counter));
        currentMoveColor.setBackground(turnColor);
        currentMoveColor.setForeground(turnColor == Color.BLACK ? Color.WHITE : Color.BLACK);
    }

    private void createUpdateThread() {
        new UpdateThread().execute();
    }

    class ChessButton implements ActionListener {
        final int row, col;

        public ChessButton(int i, int j) {
            row = i;
            col = j;
        }

        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            if (counter == 0) {
                if (button.getIcon() == defaultIcon) {
                    return;
                }
                moveFrom[0] = row;
                moveFrom[1] = col;
                counter++;
            } else {
                moveTo[0] = row;
                moveTo[1] = col;
                if (Arrays.equals(moveFrom, moveTo)) {
                    counter = 0;
                    return;
                }
                makeMove();
            }
        }
    }

    // Thread triggering update every second
    class UpdateThread extends SwingWorker {
        protected Object doInBackground() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void done() {
            System.out.println("Update thread done");
            update();
            createUpdateThread();
        }
    }

    // Thread for making moves
    // Much processing, especially if playing against stockfish
    // So needs own thread
    class MoveThread extends SwingWorker {
        private Pair<Integer, Integer> from, to;

        MoveThread(Pair<Integer, Integer> from, Pair<Integer, Integer> to) {
            this.from = from;
            this.to = to;
        }

        protected Object doInBackground() {
            controller.makeMove(from, to);
            return null;
        }
    }

}
