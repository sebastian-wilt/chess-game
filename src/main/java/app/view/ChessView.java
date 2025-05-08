package app.view;

import app.controller.ChessApp;
import app.model.GameState;
import app.model.Position;
import app.model.GameMode;
import app.model.PlayerVsPlayer;
import app.model.PlayerVsStockfish;
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
    private final String squares = "abcdefgh";

    private boolean flipped = false;

    private final ChessApp controller;
    private JPanel mainPanel;
    private JLabel currentMoveColor;
    private final JButton[][] chessBoardSquares = new JButton[8][8];
    private final ImageIcon defaultIcon = new ImageIcon(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB));

    private String promotionSuffix = "";
    private volatile boolean waitForPromotion = false;

    private int counter = 0;
    private final int[] moveFrom = new int[2];
    private final int[] moveTo = new int[2];
    private final HashMap<String, ImageIcon> images = new HashMap<>();

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

        var gamePanel = new JFrame();
        createGameMenu(gamePanel);
        gamePanel.setAlwaysOnTop(true);
        gamePanel.pack();
        gamePanel.setLocationRelativeTo(null);


        JButton newGame = new JButton("New game");
        newGame.addActionListener((e) -> {
            gamePanel.setVisible(true);
        });

        currentMoveColor = new JLabel("1", JLabel.CENTER);
        currentMoveColor.setOpaque(true);

        var flipBoard  = new JButton("Flip board");
        flipBoard.addActionListener((e) -> {
            flipped = !flipped;
        });

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener((e) -> System.exit(0));

        mainPanel.add(menuPanel, BorderLayout.NORTH);
        menuPanel.add(newGame);
        menuPanel.add(currentMoveColor);
        menuPanel.add(flipBoard);
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

    private void createGameMenu(Container parent) {
        final String pvp = "Player vs Player";
        final String pvsf = "Player vs Stockfish";

        var cards = new JPanel(new CardLayout());

        // Player vs player
        var pvpCard = new JPanel();
        var pvpButton = new JButton("Start game");
        pvpButton.addActionListener((e) -> {
            controller.createNewGame(PlayerVsPlayer.INSTANCE);
            loadPosition(controller.getCurrentPosition());
            updateCounter(controller.getMoveCounter(), controller.getTurnColor());
            parent.setVisible(false);
        });
        pvpCard.add(pvpButton);

        // Player vs stockfish
        var pvsfCard = new JPanel(new GridLayout(3, 1, 10, 10));

        // Select difficulty
        var defaultDifficulty = 2200;
        var difficultyLabel = new JLabel(String.valueOf(defaultDifficulty), JLabel.CENTER);
        pvsfCard.add(difficultyLabel);

        var difficultySlider = new JSlider(1320, 3190, defaultDifficulty);
        difficultySlider.setMajorTickSpacing(200);
        difficultySlider.setPaintTicks(true);
        difficultySlider.addChangeListener((evt) -> {
            var value = difficultySlider.getValue();
            difficultyLabel.setText(String.valueOf(value));
        });
        pvsfCard.add(difficultySlider);

        var pvsfButton = new JButton("Start game");
        pvsfButton.addActionListener((e) -> {
            controller.createNewGame(new PlayerVsStockfish(difficultySlider.getValue()));
            loadPosition(controller.getCurrentPosition());
            updateCounter(controller.getMoveCounter(), controller.getTurnColor());
            parent.setVisible(false);
        });
        pvsfCard.add(pvsfButton);

        cards.add(pvpCard, pvp);
        cards.add(pvsfCard, pvsf);

        var comboBoxPane = new JPanel();
        String comboBoxItems[] = {pvp, pvsf};

        var cb = new JComboBox(comboBoxItems);
        cb.setEditable(false);
        cb.addItemListener((evt) -> {
            var cl = (CardLayout)(cards.getLayout());
            cl.show(cards, (String)evt.getItem());
        });
        comboBoxPane.add(cb);

        parent.add(comboBoxPane, BorderLayout.PAGE_START);
        parent.add(cards, BorderLayout.CENTER);
    }

    private void createPromotionPanel(JFrame parent) {
        // Q R B N 
        var panel = new JPanel();

        String suffixes = "qrbn";

        String prefix;
        String pieces[] = {"_QUEEN", "_ROOK", "_BISHOP", "_KNIGHT"};
        var turnColor = controller.getTurnColor();
        
        if (turnColor == Color.WHITE) {
            prefix = "WHITE";
        } else {
            prefix = "BLACK";
        }

        for (int i = 0; i < pieces.length; i++) {
            var suffix = String.valueOf(suffixes.charAt(i));
            var button = new PromotionButton(suffix);
            var icon = images.get(prefix + pieces[i]);
            button.setIcon(icon);

            button.addActionListener((e) -> {
                final PromotionButton b = (PromotionButton) (e.getSource());
                promotionSuffix = b.suffix;
                parent.dispose();
                waitForPromotion = false;
            });

            panel.add(button);
        }

        parent.add(panel);
    }

    private void createImages() {
        images.put("BLACK_KING", readIcon("assets/black_king.png"));
        images.put("BLACK_PAWN", readIcon("assets/black_pawn.png"));
        images.put("BLACK_QUEEN", readIcon("assets/black_queen.png"));
        images.put("BLACK_ROOK", readIcon("assets/black_rook.png"));
        images.put("BLACK_KNIGHT", readIcon("assets/black_knight.png"));
        images.put("BLACK_BISHOP", readIcon("assets/black_bishop.png"));
        images.put("WHITE_KING", readIcon("assets/white_king.png"));
        images.put("WHITE_PAWN", readIcon("assets/white_pawn.png"));
        images.put("WHITE_QUEEN", readIcon("assets/white_queen.png"));
        images.put("WHITE_ROOK", readIcon("assets/white_rook.png"));
        images.put("WHITE_KNIGHT", readIcon("assets/white_knight.png"));
        images.put("WHITE_BISHOP", readIcon("assets/white_bishop.png"));
    }

    private ImageIcon readIcon(String suffix) {
        String path = "./src/main/java/app/" + suffix;
        try {
            return new ImageIcon(ImageIO.read(new File(path)));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        return null;
    }

    private void loadPosition(Position position) {
        if (flipped) {
            loadFlippedPosition(position);
        } else {
            loadNormalPosition(position);
        }
    }

    private void loadNormalPosition(Position position) {
        var board = position.getChessBoard();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                var string = board[i][j] == null ? null : board[i][j].toString();
                var icon = images.getOrDefault(string, defaultIcon);
                chessBoardSquares[i][j].setIcon(icon);
            }
        }
    }
    
    private void loadFlippedPosition(Position position) {
        var board = position.getChessBoard();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                var string = board[i][j] == null ? null : board[i][j].toString();
                var icon = images.getOrDefault(string, defaultIcon);
                var coords = translateCoordinates(i, j);
                chessBoardSquares[coords.getFirst()][coords.getSecond()].setIcon(icon);
            }
        }
    }

    private Pair<Integer, Integer> translateCoordinates(int y, int x) {
        var a = 7 - y; 
        var b = 7 - x;
        return new Pair(a, b);
    }

    private void makeMove() {
        counter = 0;
        var move = toLongAlgebricNotation(new Pair<>(moveFrom[0], moveFrom[1]), new Pair<>(moveTo[0], moveTo[1]));
    
        System.out.format("Move: %d %d -> %d %d\n", moveFrom[0], moveFrom[1], moveTo[0], moveTo[1]);
        var wait = false;
        if (controller.getTurnColor() == Color.WHITE) {
            if (chessBoardSquares[moveFrom[0]][moveFrom[1]].getIcon() == images.get("WHITE_PAWN") && moveTo[0] == 0) {
                var promotionPanel = new JFrame();
                createPromotionPanel(promotionPanel);
                promotionPanel.setAlwaysOnTop(true);
                promotionPanel.pack();
                promotionPanel.setLocationRelativeTo(null);
                promotionPanel.setVisible(true);
                waitForPromotion = true;
                wait = true;
            }
        } else {
            if (chessBoardSquares[moveFrom[0]][moveFrom[1]].getIcon() == images.get("BLACK_PAWN") && moveTo[0] == 7) {
                var promotionPanel = new JFrame();
                createPromotionPanel(promotionPanel);
                promotionPanel.setAlwaysOnTop(true);
                promotionPanel.pack();
                promotionPanel.setLocationRelativeTo(null);
                promotionPanel.setVisible(true);
                waitForPromotion = true;
                wait = true;
            }
        }

        if (wait) {
            while (waitForPromotion) {
                try {
                    Thread.sleep(100);
                    System.out.println("Waiting");
                } catch (InterruptedException e) {
                }
            }

            move += promotionSuffix;
        }

        System.out.println("Making move: " + move);
        controller.makeMove(move);
    }

    private String toLongAlgebricNotation(Pair<Integer, Integer> from, Pair<Integer, Integer> to) {
        var src = squares.charAt(from.getSecond()) + Integer.toString((8 - from.getFirst()));
        var dest = squares.charAt(to.getSecond()) + Integer.toString((8 - to.getFirst()));
        return String.format("%s%s", src, dest);
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
        final int i, j;
        int row, col;

        public ChessButton(int i, int j) {
            this.i = i;
            this.j = j;
        }

        public void actionPerformed(ActionEvent e) {
            if (flipped) {
                var coords = translateCoordinates(i, j);
                row = coords.getFirst();
                col = coords.getSecond();
            } else {
                row = i;
                col = j;
            }

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

                counter = 0;
                new MoveThread().execute();
            }
        }
    }

    class PromotionButton extends JButton {
        String suffix;
        
        PromotionButton(String suffix) {
            this.suffix = suffix;
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
        protected Object doInBackground() {
            makeMove();
            return null;
        }

        protected void done() {
            update();
        }
    }

}
