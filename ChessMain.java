public class ChessMain {
    public static void main(String[] args) {
        // Create a chessboard
        ChessBoard board = new ChessBoard();
        
        // Initialize pieces
        board.initializePieces();
        
        // Display the initial state of the board
        board.displayBoard();
        
        // Example move: Move a pawn from e2 to e4
        //board.movePiece("e2", "e4");
        
        // Display the board after the move
        board.displayBoard();
    }
}