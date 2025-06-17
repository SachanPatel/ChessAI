import edu.princeton.cs.algs4.StdDraw;
import java.awt.Color;

public class ChessBoard {
    private ChessPiece[] board;
    private int moveCount = 0;
    private boolean whiteInCheck = false;
    private boolean blackInCheck = false;
    private boolean kingMovedW = false;
    private boolean kingMovedB = false;
    private boolean rookMovedW1 = false;
    private boolean rookMovedW2 = false;
    private boolean rookMovedB1 = false;
    private boolean rookMovedB2 = false;

    public ChessBoard() {
        board = new ChessPiece[64]; // 8x8 = 64 squares
    }

    public void initializePieces() {
        for (int i = 0; i < 8; i++) {
            board[getIndex(1, i)] = new ChessPiece('P', 'W');
            board[getIndex(6, i)] = new ChessPiece('P', 'B');
        }

        // Rooks
        board[getIndex(0, 0)] = new ChessPiece('R', 'W');
        board[getIndex(0, 7)] = new ChessPiece('R', 'W');
        board[getIndex(7, 0)] = new ChessPiece('R', 'B');
        board[getIndex(7, 7)] = new ChessPiece('R', 'B');

        // Knights
        board[getIndex(0, 1)] = new ChessPiece('N', 'W');
        board[getIndex(0, 6)] = new ChessPiece('N', 'W');
        board[getIndex(7, 1)] = new ChessPiece('N', 'B');
        board[getIndex(7, 6)] = new ChessPiece('N', 'B');

        // Bishops
        board[getIndex(0, 2)] = new ChessPiece('B', 'W');
        board[getIndex(0, 5)] = new ChessPiece('B', 'W');
        board[getIndex(7, 2)] = new ChessPiece('B', 'B');
        board[getIndex(7, 5)] = new ChessPiece('B', 'B');

        // Queens and Kings
        board[getIndex(0, 3)] = new ChessPiece('Q', 'W');
        board[getIndex(0, 4)] = new ChessPiece('K', 'W');
        board[getIndex(7, 3)] = new ChessPiece('Q', 'B');
        board[getIndex(7, 4)] = new ChessPiece('K', 'B');
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
        StdDraw.setCanvasSize(600, 600);
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
            StdDraw.setPenColor(Color.BLACK);
            StdDraw.text(col + 0.15, row + 0.1, String.valueOf(rowLetter(col + 1)) + String.valueOf(row + 1));
            if (board[index] != null) {
                ChessPiece piece = board[index];
                char symbol = piece.getName();
                StdDraw.setPenColor(piece.getColor() == 'W' ? Color.WHITE : Color.BLACK);
                StdDraw.text(col + 0.5, row + 0.5, String.valueOf(symbol));
            }
        }
    }

    public void CastleKingSide() {
        if (moveCount % 2 == 0) {
            // White's kingside castling logic
            if (whiteInCheck) {
                System.out.println("White is in check, cannot castle kingside.");
                return;
            }
            if (kingMovedW || rookMovedW2) {
                System.out.println("White's king or rook has already moved, cannot castle kingside.");
                return;
            }
            if (board[getIndex(0,5)] != null || board[getIndex(0,6)] != null) {
                System.out.println("Squares between king and rook are not empty, cannot castle kingside.");
                return;
            }
            // Move the king and rook
            board[getIndex(0,6)] = board[getIndex(0,4)]; // Move king to g1
            board[getIndex(0,4)] = null; // Clear original king position
            board[getIndex(0,5)] = board[getIndex(0,7)]; // Move rook to f1
            board[getIndex(0,7)] = null; // Clear original rook position
            kingMovedW = true;
            rookMovedW2 = true;
            moveCount++;

        } else {
            // Black's kingside castling logic
            if (blackInCheck) {
                System.out.println("Black is in check, cannot castle kingside");
                return;
            }
            if (kingMovedB || rookMovedB2) {
                System.out.println("Black's king or rook has already moved, cannot castle kingside.");
                return;
            }
            if (board[getIndex(7,5)] != null || board[getIndex(7,6)] != null) {
                System.out.println("Squares between king and rook are not empty, cannot castle kingside.");
                return;
            }
            // Move the king and rook
            board[getIndex(7,6)] = board[getIndex(7,4)]; // Move king to g1
            board[getIndex(7,4)] = null; // Clear original king position
            board[getIndex(7,5)] = board[getIndex(7,7)]; // Move rook to f1
            board[getIndex(7,7)] = null; // Clear original rook position
            kingMovedW = true;
            rookMovedW2 = true;
            moveCount++;
        }
    }

    public void CastleQueenSide() {
        if (moveCount % 2 == 0) {
            // White's queenside castling logic
            if (whiteInCheck) {
                System.out.println("White is in check, cannot castle queenside.");
                return;
            }
            if (kingMovedW || rookMovedW1) {
                System.out.println("White's king or rook has already moved, cannot castle queenside.");
                return;
            }
            if (board[getIndex(0,1)] != null || board[getIndex(0,2)] != null  || board[getIndex(0,3)] != null ) {
                System.out.println("Squares between king and rook are not empty, cannot castle queenside.");
                return;
            }
            // Move the king and rook
            board[getIndex(0,2)] = board[getIndex(0,4)]; // Move king to g1
            board[getIndex(0,4)] = null; // Clear original king position
            board[getIndex(0,3)] = board[getIndex(0,0)]; // Move rook to f1
            board[getIndex(0,0)] = null; // Clear original rook position
            kingMovedW = true;
            rookMovedW2 = true;
            moveCount++;

        } else {
            // Black's queenside castling logic
            if (blackInCheck) {
                System.out.println("Black is in check, cannot castle queenside");
                return;
            }
            if (kingMovedB || rookMovedB2) {
                System.out.println("Black's king or rook has already moved, cannot castle queenside.");
                return;
            }
            if (board[getIndex(7,1)] != null || board[getIndex(7,2)] != null  || board[getIndex(7,3)] != null ) {
                System.out.println("Squares between king and rook are not empty, cannot castle kingside.");
                return;
            }
            // Move the king and rook
            board[getIndex(7,2)] = board[getIndex(7,4)]; // Move king to g1
            board[getIndex(7,4)] = null; // Clear original king position
            board[getIndex(7,3)] = board[getIndex(7,0)]; // Move rook to f1
            board[getIndex(7,0)] = null; // Clear original rook position
            kingMovedW = true;
            rookMovedW2 = true;
            moveCount++;
        }
    }
    // Placeholder for move logic
    public void movePiece(String command) {
        // e.g., "Pe2-e4"
        if (command.equals("O-O")) {
            CastleKingSide();
            return;
        }
        if (command.equals("O-O-O")) {
            CastleQueenSide();
            return;
        }
        char pieceType = command.charAt(0);
        String trimmedCommand = command.substring(1);
        String from = trimmedCommand.substring(0,2);
        String to = trimmedCommand.substring(3,5);
        int fromRow = Integer.parseInt(String.valueOf(from.charAt(1))) - 1;
        int fromCol = rowCoord(from.charAt(0));
        int toRow = Integer.parseInt(String.valueOf(to.charAt(1))) - 1;
        int toCol = rowCoord(to.charAt(0));
        int fromIndex = getIndex(fromRow, fromCol);
        int toIndex = getIndex(toRow, toCol);
        ChessPiece piece = board[fromIndex];
        if (piece == null) {
            System.out.println("No piece at " + from);
            return;
        }
        if (toIndex < 0 || toIndex >= board.length) {
            System.out.println("Invalid move to " + to);
            return;
        }
        if (fromIndex < 0 || fromIndex >= board.length) {
            System.out.println("Invalid move from " + from);
            return;
        }
        if (board[toIndex] != null && board[toIndex].getColor() == piece.getColor()) {
            System.out.println("Cannot move to " + to + ", own piece there.");
            return;
        }
        if (moveCount % 2 == 0 && piece.getColor() == 'B') {
            System.out.println("It's White's turn.");
            return;
        } else if (moveCount % 2 == 1 && piece.getColor() == 'W') {
            System.out.println("It's Black's turn.");
            return;
        }
        pieceType = board[fromIndex].getName();

        
        
        if (pieceType == 'P') {
            if (moveCount%2 == 0) {
                if (fromRow == 1) {
                    if (fromIndex + 8 == toIndex || fromIndex + 16 == toIndex) {
                        executeMove(fromRow, fromCol, toRow, toCol);
                    }
                }
                else if (fromRow != 1) {
                    if (fromIndex + 8 == toIndex) {
                        executeMove(fromRow, fromCol, toRow, toCol);
                    }
                }
            }
            else {
                if (fromRow == 6) {
                    if (fromIndex - 8 == toIndex || fromIndex - 16 == toIndex) {
                        executeMove(fromRow, fromCol, toRow, toCol);
                    }
                }
                else if (fromRow != 6) {
                    if (fromIndex - 8 == toIndex) {
                        executeMove(fromRow, fromCol, toRow, toCol);
                    }
                }
            }
            
        } 
        else if (pieceType == 'R') {
            
            if (fromIndex % 8 == toIndex % 8) {
                // Vertical move
                if (fromRow < toRow) {
                    for (int i = fromRow + 1; i < toRow; i++) {
                        if (board[getIndex(i, fromCol)] != null) {
                            System.out.println("Path is blocked by another piece.");
                            return;
                        }
                    }
                } else {
                    for (int i = toRow + 1; i < fromRow; i++) {
                        if (board[getIndex(i, fromCol)] != null) {
                            System.out.println("Path is blocked by another piece.");
                            return;
                        }
                    }
                }
                executeMove(fromRow, fromCol, toRow, toCol);
                if (fromRow == 0 && fromCol == 0) {
                    rookMovedW1 = true; // White's rook on a1 has moved
                } else if (fromRow == 0 && fromCol == 7) {
                    rookMovedW2 = true; // White's rook on h1 has moved
                } else if (fromRow == 7 && fromCol == 0) {
                    rookMovedB1 = true;
                } else if (fromRow == 7 && fromCol == 7) {
                    rookMovedB2 = true;
                }
            } else if (fromCol == toCol) {
                // Horizontal move
                if (fromCol < toCol) {
                    for (int i = fromCol + 1; i < toCol; i++) {
                        if (board[getIndex(fromRow, i)] != null) {
                            System.out.println("Path is blocked by another piece.");
                            return;
                        }
                    }
                } else {
                    for (int i = toCol + 1; i < fromCol; i++) {
                        if (board[getIndex(fromRow, i)] != null) {
                            System.out.println("Path is blocked by another piece.");
                            return;
                        }
                    }
                }
                executeMove(fromRow, fromCol, toRow, toCol);
            } else {
                System.out.println("Invalid rook move.");
            }
        } 
        else if (pieceType == 'N') {
            if (Math.abs(fromRow - toRow) == 2 && Math.abs(fromCol - toCol) == 1 ||
                Math.abs(fromRow - toRow) == 1 && Math.abs(fromCol - toCol) == 2) {
                // Valid knight move
                executeMove(fromRow, fromCol, toRow, toCol);
            } else {
                System.out.println("Invalid knight move.");
            }
        }
        else if (pieceType == 'B') {
            if (Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol)) {
                // Diagonal move
                int rowStep = (toRow - fromRow) > 0 ? 1 : -1;
                int colStep = (toCol - fromCol) > 0 ? 1 : -1;
                int r = fromRow + rowStep;
                int c = fromCol + colStep;
                while (r != toRow && c != toCol) {
                    if (board[getIndex(r, c)] != null) {
                        System.out.println("Path is blocked by another piece.");
                        return;
                    }
                    r += rowStep;
                    c += colStep;
                }
                executeMove(fromRow, fromCol, toRow, toCol);
            } else {
                System.out.println("Invalid bishop move.");
            }
        }
        else if (pieceType == 'Q') {
            if (Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol)) {
                // Diagonal move
                int rowStep = (toRow - fromRow) > 0 ? 1 : -1;
                int colStep = (toCol - fromCol) > 0 ? 1 : -1;
                int r = fromRow + rowStep;
                int c = fromCol + colStep;
                while (r != toRow && c != toCol) {
                    if (board[getIndex(r, c)] != null) {
                        System.out.println("Path is blocked by another piece.");
                        return;
                    }
                    r += rowStep;
                    c += colStep;
                }
                executeMove(fromRow, fromCol, toRow, toCol);
            } else if (fromIndex % 8 == toIndex % 8 || fromCol == toCol) {
                // Vertical or horizontal move
                if (fromRow < toRow) {
                    for (int i = fromRow + 1; i < toRow; i++) {
                        if (board[getIndex(i, fromCol)] != null) {
                            System.out.println("Path is blocked by another piece.");
                            return;
                        }
                    }
                } else {
                    for (int i = toRow + 1; i < fromRow; i++) {
                        if (board[getIndex(i, fromCol)] != null) {
                            System.out.println("Path is blocked by another piece.");
                            return;
                        }
                    }
                }
                executeMove(fromRow, fromCol, toRow, toCol);
            } 
            else {
                System.out.println("Invalid queen move.");
            }
        }
        else if (pieceType == 'K') {
            if (Math.abs(fromRow - toRow) <= 1 && Math.abs(fromCol - toCol) <= 1) {
                // Valid king move
                if (moveCount % 2 == 0 && whiteInCheck) {
                    System.out.println("White is in check, cannot move king.");
                    return;
                } else if (moveCount % 2 == 1 && blackInCheck) {
                    System.out.println("Black is in check, cannot move king.");
                    return;
                }
                executeMove(fromRow, fromCol, toRow, toCol);
            } else {
                System.out.println("Invalid king move.");
            }
        }
        

    }

    public void executeMove(int fromRow, int fromCol, int toRow, int toCol) {
        // This method is a placeholder for the actual move logic
        // It should handle the movement of pieces based on the game rules
        // For now, it just prints the move
        board[getIndex(toRow, toCol)] = board[getIndex(fromRow, fromCol)];
        board[getIndex(fromRow,fromCol)] = null;
        moveCount++;
        evaluateCheck();
        
    }
    public void evaluateCheck() {
        // This method should evaluate if either player is in check
        // For now, it does nothing
        // You can implement the logic to check for check conditions here

    }
    int rowCoord (char c) {
        switch (c) {
            case 'a': return 0;
            case 'b': return 1;
            case 'c': return 2;
            case 'd': return 3;
            case 'e': return 4;
            case 'f': return 5;
            case 'g': return 6;
            case 'h': return 7;
            default: throw new IllegalArgumentException("Invalid row: " + c);
        }
    }
    char rowLetter(int n) {
        switch (n) {
            case 1 : return 'A';
            case 2 : return 'B';
            case 3 : return 'C';
            case 4 : return 'D';
            case 5 : return 'E';
            case 6 : return 'F';
            case 7 : return 'G';
            case 8 : return 'H';
            default: throw new IllegalArgumentException("Invalid row: " + n);
        }
    }
    public boolean isWhiteInCheck() {
        return false;
    }

    public boolean isBlackInCheck() {
        return false;
    }
}
