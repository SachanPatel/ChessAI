import edu.princeton.cs.algs4.StdDraw;
import java.awt.Color;
import java.util.*;
import java.util.regex.*;

public class ChessGame {
    // Bitboards for each piece type and color
    private long whitePawns, whiteKnights, whiteBishops, whiteRooks, whiteQueens, whiteKing;
    private long blackPawns, blackKnights, blackBishops, blackRooks, blackQueens, blackKing;
    private boolean whiteToMove = true;

    // File masks for move generation
    private static final long FILE_A = 0x0101010101010101L;
    private static final long FILE_B = FILE_A << 1;
    private static final long FILE_G = FILE_A << 6;
    private static final long FILE_H = FILE_A << 7;

    public ChessGame() {
        initializeBitboards();
    }

    private void initializeBitboards() {
        whitePawns   = 0x000000000000FF00L;
        whiteRooks   = 0x0000000000000081L;
        whiteKnights = 0x0000000000000042L;
        whiteBishops = 0x0000000000000024L;
        whiteQueens  = 0x0000000000000008L;
        whiteKing    = 0x0000000000000010L;

        blackPawns   = 0x00FF000000000000L;
        blackRooks   = 0x8100000000000000L;
        blackKnights = 0x4200000000000000L;
        blackBishops = 0x2400000000000000L;
        blackQueens  = 0x0800000000000000L;
        blackKing    = 0x1000000000000000L;
    }

    private int algebraicToIndex(String sq) {
        sq = sq.toUpperCase();
        int file = sq.charAt(0) - 'A';
        int rank = sq.charAt(1) - '1';
        return rank * 8 + file;
    }

    private char indexToFile(int idx) {
        return (char) ('A' + (idx % 8));
    }

    private char indexToRank(int idx) {
        return (char) ('1' + (idx / 8));
    }

    private long allWhite() {
        return whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKing;
    }

    private long allBlack() {
        return blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKing;
    }

    private long occupied() {
        return allWhite() | allBlack();
    }

    private boolean isOccupied(long bb, int idx) {
        return ((bb >>> idx) & 1L) != 0;
    }

    // Generate knight attack bitmask from a square
    private long knightAttacks(int sq) {
        long bit = 1L << sq;
        long attacks = 0;
        attacks |= (bit << 17) & ~FILE_A;
        attacks |= (bit << 15) & ~FILE_H;
        attacks |= (bit << 10) & ~(FILE_A | FILE_B);
        attacks |= (bit << 6)  & ~(FILE_H | FILE_G);
        attacks |= (bit >>> 17) & ~FILE_H;
        attacks |= (bit >>> 15) & ~FILE_A;
        attacks |= (bit >>> 10) & ~(FILE_H | FILE_G);
        attacks |= (bit >>> 6)  & ~(FILE_A | FILE_B);
        return attacks;
    }

    // Generate sliding moves for bishops, rooks, queens
    private List<Integer> generateSlidingMoves(int from, boolean isWhite, int[] dirs) {
        List<Integer> moves = new ArrayList<>();
        for (int d : dirs) {
            int to = from;
            while (true) {
                int file = to % 8;
                if ((d == 9 || d == -7) && file == 7) break;
                if ((d == 7 || d == -9) && file == 0) break;
                if ((d == 1) && file == 7) break;
                if ((d == -1) && file == 0) break;
                to += d;
                if (to < 0 || to >= 64) break;
                if (isOccupied(isWhite ? allWhite() : allBlack(), to)) break;
                moves.add(to);
                if (isOccupied(occupied(), to)) break;
            }
        }
        return moves;
    }

    // Generate all pseudo-legal moves for a piece at 'from'
    private List<Integer> generateMovesFrom(int from) {
        List<Integer> moves = new ArrayList<>();
        long occ = occupied();
        boolean isWhite = isOccupied(allWhite(), from);
        // Pawn moves
        if (isOccupied(isWhite ? whitePawns : blackPawns, from)) {
            int dir = isWhite ? 8 : -8;
            int rank = from / 8;
            int to = from + dir;
            if (to >= 0 && to < 64 && !isOccupied(occ, to)) moves.add(to);
            if ((isWhite && rank == 1) || (!isWhite && rank == 6)) {
                int mid = from + dir;
                int to2 = from + 2 * dir;
                if (!isOccupied(occ, mid) && !isOccupied(occ, to2)) moves.add(to2);
            }
            int[] caps = isWhite ? new int[]{from + 7, from + 9} : new int[]{from - 7, from - 9};
            for (int c : caps) {
                if (c >= 0 && c < 64) {
                    int f1 = from % 8, f2 = c % 8;
                    if (Math.abs(f1 - f2) == 1) {
                        if (isWhite && isOccupied(allBlack(), c)) moves.add(c);
                        if (!isWhite && isOccupied(allWhite(), c)) moves.add(c);
                    }
                }
            }
        }
        // Knight moves
        if (isOccupied(isWhite ? whiteKnights : blackKnights, from)) {
            long att = knightAttacks(from);
            for (int to = 0; to < 64; to++) {
                if (((att >>> to) & 1L) != 0 && !isOccupied(isWhite ? allWhite() : allBlack(), to)) {
                    moves.add(to);
                }
            }
        }
        // Bishop moves
        if (isOccupied(isWhite ? whiteBishops : blackBishops, from)) {
            moves.addAll(generateSlidingMoves(from, isWhite, new int[]{9,7,-7,-9}));
        }
        // Rook moves
        if (isOccupied(isWhite ? whiteRooks : blackRooks, from)) {
            moves.addAll(generateSlidingMoves(from, isWhite, new int[]{1,-1,8,-8}));
        }
        // Queen moves
        if (isOccupied(isWhite ? whiteQueens : blackQueens, from)) {
            moves.addAll(generateSlidingMoves(from, isWhite, new int[]{9,7,-7,-9,1,-1,8,-8}));
        }
        // King moves
        if (isOccupied(isWhite ? whiteKing : blackKing, from)) {
            int[] ks = {9,7,-7,-9,1,-1,8,-8};
            for (int d : ks) {
                int to = from + d;
                if (to < 0 || to >= 64) continue;
                int f1 = from % 8, f2 = to % 8;
                if (Math.abs(f1 - f2) > 1) continue;
                if (!isOccupied(isWhite ? allWhite() : allBlack(), to)) moves.add(to);
            }
        }
        return moves;
    }

    // Restrict move to pseudo-legal set
    public boolean restrictMove(int from, int to) {
        return generateMovesFrom(from).contains(to);
    }

    // Generate all moves for side to move
    public List<String> generateMoves() {
        List<String> list = new ArrayList<>();
        long bb = whiteToMove ? allWhite() : allBlack();
        while (bb != 0) {
            long mask = bb & -bb;
            int from = Long.numberOfTrailingZeros(mask);
            bb &= bb - 1;
            for (int to : generateMovesFrom(from)) {
                list.add("" + indexToFile(from) + indexToRank(from) + "-" + indexToFile(to) + indexToRank(to));
            }
        }
        return list;
    }

    // Get piece symbol at square
    private char getPieceSymbolAt(int idx) {
        if (isOccupied(whitePawns, idx))   return 'P';
        if (isOccupied(whiteKnights, idx)) return 'N';
        if (isOccupied(whiteBishops, idx)) return 'B';
        if (isOccupied(whiteRooks, idx))   return 'R';
        if (isOccupied(whiteQueens, idx))  return 'Q';
        if (isOccupied(whiteKing, idx))    return 'K';
        if (isOccupied(blackPawns, idx))   return 'P';
        if (isOccupied(blackKnights, idx)) return 'N';
        if (isOccupied(blackBishops, idx)) return 'B';
        if (isOccupied(blackRooks, idx))   return 'R';
        if (isOccupied(blackQueens, idx))  return 'Q';
        if (isOccupied(blackKing, idx))    return 'K';
        return ' ';
    }

    // Execute the move on bitboards
    private void movePiece(int from, int to) {
        long maskFrom = 1L << from;
        long maskTo   = 1L << to;
        long[] bbs = { whitePawns, whiteKnights, whiteBishops, whiteRooks, whiteQueens, whiteKing,
                       blackPawns, blackKnights, blackBishops, blackRooks, blackQueens, blackKing };
        for (int i = 0; i < bbs.length; i++) {
            if ((bbs[i] & maskFrom) != 0) {
                bbs[i] &= ~maskFrom;
                bbs[i] |= maskTo;
                break;
            }
        }
        whitePawns   = bbs[0]; whiteKnights = bbs[1]; whiteBishops = bbs[2]; whiteRooks = bbs[3]; whiteQueens = bbs[4]; whiteKing = bbs[5];
        blackPawns   = bbs[6]; blackKnights = bbs[7]; blackBishops = bbs[8]; blackRooks = bbs[9]; blackQueens = bbs[10]; blackKing = bbs[11];
        whiteToMove = !whiteToMove;
    }

    // Draw board and pieces
    private void drawBoard() {
        StdDraw.setCanvasSize(600, 600);
        StdDraw.setXscale(0, 8);
        StdDraw.setYscale(0, 8);
        StdDraw.clear();
        for (int idx = 0; idx < 64; idx++) {
            int row = idx / 8;
            int col = idx % 8;
            boolean light = (row + col) % 2 == 0;
            StdDraw.setPenColor(light ? new Color(240, 217, 181) : new Color(181, 136, 99));
            StdDraw.filledSquare(col + 0.5, row + 0.5, 0.5);
            StdDraw.setPenColor(Color.DARK_GRAY);
            StdDraw.text(col + 0.1, row + 0.1, "" + indexToFile(idx) + indexToRank(idx));
        }
        for (int idx = 0; idx < 64; idx++) {
            char sym = getPieceSymbolAt(idx);
            if (sym != ' ') {
                int row = idx / 8;
                int col = idx % 8;
                boolean isWhitePiece = isOccupied(allWhite(), idx);
                StdDraw.setPenColor(isWhitePiece ? Color.WHITE : Color.BLACK);
                StdDraw.text(col + 0.5, row + 0.5, String.valueOf(sym));
            }
        }
    }

    // Game loop
    public void startGame() {
        Scanner scanner = new Scanner(System.in);
        Pattern cmdPattern = Pattern.compile("^([KQRBNP]?)([A-H][1-8])[-]?([A-H][1-8])$");
        while (true) {
            drawBoard();
            List<String> moves = generateMoves();
            System.out.println((whiteToMove ? "White" : "Black") + " to move. Legal moves: " + moves);
            System.out.print("Enter move (e.g., PA2-A4 or e2e4), or EXIT: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("EXIT")) break;
            Matcher m = cmdPattern.matcher(input);
            if (!m.matches()) { System.out.println("Invalid format"); continue; }
            String piece = m.group(1);
            int from = algebraicToIndex(m.group(2));
            int to   = algebraicToIndex(m.group(3));
            if (!restrictMove(from, to)) { System.out.println("Illegal move"); continue; }
            if (!piece.isEmpty()) {
                char at = getPieceSymbolAt(from);
                if (at != piece.charAt(0)) { System.out.println("No such piece at " + m.group(2)); continue; }
            }
            movePiece(from, to);
        }
        scanner.close();
    }

    public static void main(String[] args) {
        ChessGame game = new ChessGame();
        game.startGame();
    }
}
