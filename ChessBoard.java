import edu.princeton.cs.algs4.StdDraw;
import java.awt.Color;
public class ChessBoard  {
    private ChessPiece[][] board;

    public ChessBoard() {
        board = new ChessPiece[8][8];
    }

    public void initializePieces() {
        // Initialize pawns
        for (int i = 0; i < 8; i++) {
            board[1][i] = new ChessPiece("Pawn", "White");
            board[6][i] = new ChessPiece("Pawn", "Black");
        }
        // Initialize rooks
        board[0][0] = new ChessPiece("Rook", "White");
        board[0][7] = new ChessPiece("Rook", "White");
        board[7][0] = new ChessPiece("Rook", "Black");
        board[7][7] = new ChessPiece("Rook", "Black");
        // Initialize knights
        board[0][1] = new ChessPiece("Knight", "White");
        board[0][6] = new ChessPiece("Knight", "White");
        board[7][1] = new ChessPiece("Knight", "Black");
        board[7][6] = new ChessPiece("Knight", "Black");
        // Initialize bishops
        board[0][2] = new ChessPiece("Bishop", "White");
        board[0][5] = new ChessPiece("Bishop", "White");
        board[7][2] = new ChessPiece("Bishop", "Black");
        board[7][5] = new ChessPiece("Bishop", "Black");
        // Initialize queens and kings
        board[0][3] = new ChessPiece("Queen", "White");
        board[0][4] = new ChessPiece("King", "White");
        board[7][3] = new ChessPiece("Queen", "Black");
        board[7][4] = new ChessPiece("King", "Black");
    }

    public void displayBoard() {
        StdDraw.setCanvasSize(800, 800);
        StdDraw.setXscale(0, 8);
        StdDraw.setYscale(0, 8);
        StdDraw.clear();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                // Alternate colors for the board squares
                if ((i + j) % 2 == 0) {
                    StdDraw.setPenColor(new Color(240, 217, 181)); // Light color
                } else {
                    StdDraw.setPenColor(new Color(181, 136, 99)); // Dark color
                }
                StdDraw.filledSquare(j + 0.5, i + 0.5, 0.5);

                // Draw pieces if present
                if (board[i][j] != null) {
                    ChessPiece piece = board[i][j];
                    String symbol = piece.getName().substring(0, 1);
                    if (piece.getName().equals("Knight")) symbol = "N";
                    if (piece.getColor().equals("White")) {
                        StdDraw.setPenColor(Color.WHITE);
                    } else {
                        StdDraw.setPenColor(Color.BLACK);
                    }
                    StdDraw.text(j + 0.5, i + 0.5, symbol);
                }
            }
        }
    }

    //public void movePiece(String from
}