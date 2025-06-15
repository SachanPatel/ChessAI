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
        for (int i = 7; i  >= 0; i--) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] != null) {
                    String name = board[i][j].getName();
                    if (name == "Knight") {
                        name = "N"; // Use 'N' for Knight to avoid confusion with King
                    }
                    System.out.print(name.charAt(0) + "\t");
                } else {
                    System.out.print(".\t"); // Empty square
                }
            }
            System.out.println();
        }
    }

    //public void movePiece(String from
}