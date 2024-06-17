package app;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

class ChessView {
    private final ChessApp controller;
    private JPanel mainPanel;
    private JLabel currentMoveColor;
    private final JButton[][] chessBoardSquares = new JButton[8][8];
    private final Image[][] chessPieceImages = new Image[2][6];
    private static final int KING = 0, QUEEN = 1, ROOK = 2, KNIGHT = 3, BISHOP = 4, PAWN = 5;
    private static final int[] STARTING_ROW = {ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK};
    private static final int BLACK = 0, WHITE = 1;
    private final ImageIcon defaultIcon = new ImageIcon(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB));
    private int counter = 0;
    private final int[] moveFrom = new int[2];
    private final int[] moveTo = new int[2];
    private final HashMap<String, String> images = new HashMap<>();

    public ChessView(ChessApp controller) {
        this.controller = controller;
        createImages();
        init();
        loadPosition(new Position());
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
    }

    private void createMenu() {
        JPanel menuPanel = new JPanel(new GridLayout(1, 3));

        JButton newGame = new JButton("New game");
        newGame.addActionListener((e) -> createNewGame());

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
                System.out.println(string);
                var path = images.getOrDefault(string, null);
                ImageIcon icon;
                if (path != null) {
                    try {
                        var file = new File("./src/main/java/app/" + path);
                        icon = new ImageIcon(ImageIO.read(file));
                        System.out.println("Fetching" + path);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        icon = null;
                    }
                } else {
                    icon = defaultIcon;
                }

                chessBoardSquares[i][j].setIcon(icon);
            }
        }
    }

    private void createNewGame() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                chessBoardSquares[i][j].setIcon(defaultIcon);
            }
        }

        for (int i = 0; i < STARTING_ROW.length; i++) {
            chessBoardSquares[0][i].setIcon(new ImageIcon(chessPieceImages[BLACK][STARTING_ROW[i]]));
            chessBoardSquares[1][i].setIcon(new ImageIcon(chessPieceImages[BLACK][PAWN]));

            chessBoardSquares[6][i].setIcon(new ImageIcon(chessPieceImages[WHITE][PAWN]));
            chessBoardSquares[7][i].setIcon(new ImageIcon(chessPieceImages[WHITE][STARTING_ROW[i]]));
        }

        currentMoveColor.setText("1");
        currentMoveColor.setBackground(Color.WHITE);
        currentMoveColor.setForeground(Color.BLACK);

        // controller.createNewGame();
    }

    private void makeMove() {
//        JButton from = chessBoardSquares[moveFrom[0]][moveFrom[1]];
//        JButton to = chessBoardSquares[moveTo[0]][moveTo[1]];
//
//        counter = 0;
//        if (!controller.validMove(moveFrom, moveTo)) {
//            return;
//        }
//
//        to.setIcon(from.getIcon());
//        from.setIcon(defaultIcon);
//
//        controller.makeMove(moveFrom, moveTo);
    }

    public void endGame(String result, String message) {
        currentMoveColor.setText(message);
    }

    public void updateCounter(int counter) {
        currentMoveColor.setText(String.valueOf(counter / 2));
        Color bg = (counter & 1) == 1 ? Color.BLACK : Color.WHITE;
        currentMoveColor.setBackground(bg);
        currentMoveColor.setForeground(bg == Color.BLACK ? Color.WHITE : Color.BLACK);
    }

    class ChessButton implements ActionListener {
        int row, col;

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
}
