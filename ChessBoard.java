import edu.princeton.cs.algs4.StdDraw;
import java.awt.Color;

public class ChessBoard {
    private ChessPiece[] board;

    public ChessBoard() {
        board = new ChessPiece[64]; // 8x8 = 64 squares
    }

    public void initializePieces() {
        for (int i = 0; i < 8; i++) {
            board[getIndex(1, i)] = new ChessPiece("Pawn", "White");
            board[getIndex(6, i)] = new ChessPiece("Pawn", "Black");
        }

        // Rooks
        board[getIndex(0, 0)] = new ChessPiece("Rook", "White");
        board[getIndex(0, 7)] = new ChessPiece("Rook", "White");
        board[getIndex(7, 0)] = new ChessPiece("Rook", "Black");
        board[getIndex(7, 7)] = new ChessPiece("Rook", "Black");

        // Knights
        board[getIndex(0, 1)] = new ChessPiece("Knight", "White");
        board[getIndex(0, 6)] = new ChessPiece("Knight", "White");
        board[getIndex(7, 1)] = new ChessPiece("Knight", "Black");
        board[getIndex(7, 6)] = new ChessPiece("Knight", "Black");

        // Bishops
        board[getIndex(0, 2)] = new ChessPiece("Bishop", "White");
        board[getIndex(0, 5)] = new ChessPiece("Bishop", "White");
        board[getIndex(7, 2)] = new ChessPiece("Bishop", "Black");
        board[getIndex(7, 5)] = new ChessPiece("Bishop", "Black");

        // Queens and Kings
        board[getIndex(0, 3)] = new ChessPiece("Queen", "White");
        board[getIndex(0, 4)] = new ChessPiece("King", "White");
        board[getIndex(7, 3)] = new ChessPiece("Queen", "Black");
        board[getIndex(7, 4)] = new ChessPiece("King", "Black");
    }

    private int getIndex(int row, int col) {
        return row * 8 + col;
    }

    private int getRow(int index) {
        return index / 8;
    }

    private int getCol(int index) {
        return index % 8;
    }

    public void displayBoard() {
        StdDraw.setCanvasSize(800, 800);
        StdDraw.setXscale(0, 8);
        StdDraw.setYscale(0, 8);
        StdDraw.clear();

        for (int index = 0; index < 64; index++) {
            int row = index / 8;
            int col = index % 8;

            if ((row + col) % 2 == 0) {
                StdDraw.setPenColor(new Color(240, 217, 181));
            } else {
                StdDraw.setPenColor(new Color(181, 136, 99));
            }
            StdDraw.filledSquare(col + 0.5, row + 0.5, 0.5);

            if (board[index] != null) {
                ChessPiece piece = board[index];
                String symbol = piece.getName().equals("Knight") ? "N" : piece.getName().substring(0, 1);
                StdDraw.setPenColor(piece.getColor().equals("White") ? Color.WHITE : Color.BLACK);
                StdDraw.text(col + 0.5, row + 0.5, symbol);
            }
        }
    }

    // Placeholder for move logic
    public void movePiece(String command) {
        // e.g., "Pe2-e4"
    }

    public boolean isWhiteInCheck() {
        return false;
    }

    public boolean isBlackInCheck() {
        return false;
    }
}
