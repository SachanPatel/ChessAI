public class ChessMain {
    public static void main(String[] args) {
        // Create a chessboard
        ChessBoard board = new ChessBoard();
        
        // Initialize pieces
        board.initializePieces();
        
        // Display the initial state of the board
        board.displayBoard();
        
        
        
        while (true) {
            System.out.println("Enter a command: ");
            String move = System.console().readLine();
            if (move.equals("exit")) {
                break;
            }
            board.movePiece(move);
            board.displayBoard();

        }
    }
}