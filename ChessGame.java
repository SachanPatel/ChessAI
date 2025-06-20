import edu.princeton.cs.algs4.StdDraw;
import java.awt.Color;
import java.util.*;
import java.util.regex.*;

public class ChessGame {
    // Bitboards for each piece type and color
    private long whitePawns, whiteKnights, whiteBishops, whiteRooks, whiteQueens, whiteKing;
    private long blackPawns, blackKnights, blackBishops, blackRooks, blackQueens, blackKing;
    private boolean whiteToMove = true;

    // Castling rights
    private boolean whiteCastleKingSide = true;
    private boolean whiteCastleQueenSide = true;
    private boolean blackCastleKingSide = true;
    private boolean blackCastleQueenSide = true;

    // En passant target square index (-1 if none)
    private int enPassantTarget = -1;

    // File masks
    private static final long FILE_A = 0x0101010101010101L;
    private static final long FILE_B = FILE_A << 1;
    private static final long FILE_G = FILE_A << 6;
    private static final long FILE_H = FILE_A << 7;

    private final String[] imgs = {
        "pawn_white.png",   "knight_white.png", "bishop_white.png",
        "rook_white.png",   "queen_white.png",  "king_white.png",
        "pawn_black.png",   "knight_black.png", "bishop_black.png",
        "rook_black.png",   "queen_black.png",  "king_black.png"
    };
    
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

        whiteToMove = true;
        whiteCastleKingSide = whiteCastleQueenSide = true;
        blackCastleKingSide = blackCastleQueenSide = true;
        enPassantTarget = -1;
    }

    public int algebraicToIndex(String sq) {
        sq = sq.toUpperCase();
        int file = sq.charAt(0) - 'A';
        int rank = sq.charAt(1) - '1';
        return rank * 8 + file;
    }

    public char indexToFile(int idx) {
        return (char)('A' + (idx % 8));
    }

    public char indexToRank(int idx) {
        return (char)('1' + (idx / 8));
    }

    private long allWhite()  { return whitePawns|whiteKnights|whiteBishops|whiteRooks|whiteQueens|whiteKing; }
    private long allBlack()  { return blackPawns|blackKnights|blackBishops|blackRooks|blackQueens|blackKing; }
    private long occupied()  { return allWhite()|allBlack(); }
    private boolean isOccupied(long bb, int idx) { return ((bb >>> idx) & 1L) != 0; }

    public ChessGame copy() {
        ChessGame g = new ChessGame();
        g.whitePawns   = this.whitePawns;
        g.whiteKnights = this.whiteKnights;
        g.whiteBishops = this.whiteBishops;
        g.whiteRooks   = this.whiteRooks;
        g.whiteQueens  = this.whiteQueens;
        g.whiteKing    = this.whiteKing;
        g.blackPawns   = this.blackPawns;
        g.blackKnights = this.blackKnights;
        g.blackBishops = this.blackBishops;
        g.blackRooks   = this.blackRooks;
        g.blackQueens  = this.blackQueens;
        g.blackKing    = this.blackKing;
        g.whiteToMove  = this.whiteToMove;
        g.whiteCastleKingSide  = this.whiteCastleKingSide;
        g.whiteCastleQueenSide = this.whiteCastleQueenSide;
        g.blackCastleKingSide  = this.blackCastleKingSide;
        g.blackCastleQueenSide = this.blackCastleQueenSide;
        g.enPassantTarget      = this.enPassantTarget;
        return g;
    }

    public void applyMove(int from, int to) {
        int oldEp = enPassantTarget;
        enPassantTarget = -1;

        long maskFrom = 1L << from;
        long maskTo   = 1L << to;

        long[] bbs = {
            whitePawns, whiteKnights, whiteBishops, whiteRooks, whiteQueens, whiteKing,
            blackPawns, blackKnights, blackBishops, blackRooks, blackQueens, blackKing
        };

        // 1) Clear destination (capture or en passant)
        for (int i = 0; i < bbs.length; i++) {
            bbs[i] &= ~maskTo;
        }

        // 2) Move piece
        int moved = -1;
        for (int i = 0; i < bbs.length; i++) {
            if ((bbs[i] & maskFrom) != 0) {
                moved = i;
                bbs[i] = (bbs[i] & ~maskFrom) | maskTo;
                break;
            }
        }

        boolean wm = (moved < 6);

        // 3) Double pawn push → set en passant target
        if (moved == (wm ? 0 : 6) && Math.abs(to - from) == 16) {
            enPassantTarget = (from + to) / 2;
        }

        // 4) En passant capture
        if (to == oldEp && moved == (wm ? 0 : 6)) {
            int cap = wm ? to - 8 : to + 8;
            bbs[ wm ? 6 : 0 ] &= ~(1L << cap);
        }

        // 5) Pawn promotion to queen
        if (moved == (wm ? 0 : 6)) {
            int rank = to / 8;
            if ((wm && rank == 7) || (!wm && rank == 0)) {
                bbs[moved] &= ~maskTo;            // remove pawn
                int qIdx = wm ? 4 : 10;           // queen index
                bbs[qIdx] |= maskTo;
            }
        }

        // 6) Invalidate castling rights if rook or king moved/captured
        if (moved == 5)  whiteCastleKingSide = whiteCastleQueenSide = false;
        if (moved == 11) blackCastleKingSide = blackCastleQueenSide = false;
        if (from == algebraicToIndex("H1") || to == algebraicToIndex("H1")) whiteCastleKingSide = false;
        if (from == algebraicToIndex("A1") || to == algebraicToIndex("A1")) whiteCastleQueenSide = false;
        if (from == algebraicToIndex("H8") || to == algebraicToIndex("H8")) blackCastleKingSide = false;
        if (from == algebraicToIndex("A8") || to == algebraicToIndex("A8")) blackCastleQueenSide = false;

        // 7) Rook relocation for castling only when appropriate
        // White
        if (moved == 5 && from == algebraicToIndex("E1")) {
            if (to == algebraicToIndex("G1")) {
                // White king-side
                bbs[3] &= ~(1L << algebraicToIndex("H1"));
                bbs[3] |=  (1L << algebraicToIndex("F1"));
            } else if (to == algebraicToIndex("C1")) {
                // White queen-side
                bbs[3] &= ~(1L << algebraicToIndex("A1"));
                bbs[3] |=  (1L << algebraicToIndex("D1"));
            }
        }
        // Black
        if (moved == 11 && from == algebraicToIndex("E8")) {
            if (to == algebraicToIndex("G8")) {
                // Black king-side
                bbs[9] &= ~(1L << algebraicToIndex("H8"));
                bbs[9] |=  (1L << algebraicToIndex("F8"));
            } else if (to == algebraicToIndex("C8")) {
                // Black queen-side
                bbs[9] &= ~(1L << algebraicToIndex("A8"));
                bbs[9] |=  (1L << algebraicToIndex("D8"));
            }
        }

        // 8) Write back bitboards
        whitePawns   = bbs[0];  whiteKnights = bbs[1];  whiteBishops = bbs[2];
        whiteRooks   = bbs[3];  whiteQueens  = bbs[4];  whiteKing    = bbs[5];
        blackPawns   = bbs[6];  blackKnights = bbs[7];  blackBishops = bbs[8];
        blackRooks   = bbs[9];  blackQueens  = bbs[10]; blackKing    = bbs[11];

        // 9) Flip turn
        whiteToMove = !whiteToMove;
    }

    /** Generate all legal pseudo-moves plus castling */
   public List<String> generateMoves() {
    // 1) Build up all pseudo-legal moves
    List<String> pseudo = new ArrayList<>();
    long bb = whiteToMove ? allWhite() : allBlack();
    while (bb != 0) {
        long m = bb & -bb;
        int from = Long.numberOfTrailingZeros(m);
        bb &= bb - 1;
        for (int to : generateMovesFrom(from)) {
            pseudo.add("" 
                + indexToFile(from) 
                + indexToRank(from)
                + '-' 
                + indexToFile(to) 
                + indexToRank(to));
        }
    }

    // 2) Add castling in exactly the same way you had before
    if (whiteToMove) {
        if (whiteCastleKingSide
            && clearBetween("E1","G1")
            && !isOccupied(occupied(), algebraicToIndex("F1"))
            && !isOccupied(occupied(), algebraicToIndex("G1"))
            && !isSquareAttacked(algebraicToIndex("E1"), false)
            && !isSquareAttacked(algebraicToIndex("F1"), false)
            && !isSquareAttacked(algebraicToIndex("G1"), false))
        {
            pseudo.add("O-O");
        }
        if (whiteCastleQueenSide
            && clearBetween("E1","C1")
            && !isOccupied(occupied(), algebraicToIndex("D1"))
            && !isOccupied(occupied(), algebraicToIndex("C1"))
            && !isSquareAttacked(algebraicToIndex("E1"), false)
            && !isSquareAttacked(algebraicToIndex("D1"), false)
            && !isSquareAttacked(algebraicToIndex("C1"), false))
        {
            pseudo.add("O-O-O");
        }
    } else {
        if (blackCastleKingSide
            && clearBetween("E8","G8")
            && !isOccupied(occupied(), algebraicToIndex("F8"))
            && !isOccupied(occupied(), algebraicToIndex("G8"))
            && !isSquareAttacked(algebraicToIndex("E8"), true)
            && !isSquareAttacked(algebraicToIndex("F8"), true)
            && !isSquareAttacked(algebraicToIndex("G8"), true))
        {
            pseudo.add("O-O");
        }
        if (blackCastleQueenSide
            && clearBetween("E8","C8")
            && !isOccupied(occupied(), algebraicToIndex("D8"))
            && !isOccupied(occupied(), algebraicToIndex("C8"))
            && !isSquareAttacked(algebraicToIndex("E8"), true)
            && !isSquareAttacked(algebraicToIndex("D8"), true)
            && !isSquareAttacked(algebraicToIndex("C8"), true))
        {
            pseudo.add("O-O-O");
        }
    }

    // 3) Now filter out any move that would leave your king in check
    List<String> legal = new ArrayList<>();
    boolean me = whiteToMove;
    for (String mv : pseudo) {
        ChessGame copy = this.copy();
        copy.applyAlgebraicMove(mv);
        if (!copy.isInCheck(me)) {
            legal.add(mv);
        }
    }

    return legal;
}


/** Returns true if the given side’s king is under attack. */
public boolean isInCheck(boolean white) {
    int kingSq = white
        ? Long.numberOfTrailingZeros(whiteKing)
        : Long.numberOfTrailingZeros(blackKing);
    return isSquareAttacked(kingSq, !white);
}

/** Returns true if side to move is in checkmate. */
public boolean isCheckmate(boolean white) {
    // check that king is in check, and has no legal move out
    if (!isInCheck(white)) return false;
    // temporarily flip side so generateMoves() filters correctly
    boolean save = whiteToMove;
    whiteToMove = white;
    List<String> moves = generateMoves();
    whiteToMove = save;
    return moves.isEmpty();
}

    /** Generate moves for a single square (no castling) */
    private List<Integer> generateMovesFrom(int from) {
        List<Integer> list = new ArrayList<>();
        long occ = occupied();
        boolean wm = isOccupied(allWhite(), from);

        // Pawn pushes & captures & en passant
        if (isOccupied(wm ? whitePawns : blackPawns, from)) {
            int dir = wm ? 8 : -8;
            int to = from + dir;
            if (to >= 0 && to < 64 && !isOccupied(occ, to)) {
                list.add(to);
                int rank = from/8;
                if ((wm && rank==1) || (!wm && rank==6)) {
                    int to2 = from + 2*dir;
                    if (!isOccupied(occ, to2)) list.add(to2);
                }
            }
            int[] caps = wm ? new int[]{from+7, from+9} : new int[]{from-7, from-9};
            for (int c : caps) {
                if (c<0||c>=64) continue;
                if (Math.abs((from%8)-(c%8))!=1) continue;
                if (wm && isOccupied(allBlack(), c)) list.add(c);
                if (!wm && isOccupied(allWhite(), c)) list.add(c);
                if (c == enPassantTarget) list.add(c);
            }
        }

        // Knight
        if (isOccupied(wm ? whiteKnights : blackKnights, from)) {
            long att = knightAttacks(from);
            for (int t=0; t<64; t++) {
                if (((att>>>t)&1L)!=0 && !isOccupied(wm?allWhite():allBlack(), t))
                    list.add(t);
            }
        }

        // Bishops
        if (isOccupied(wm ? whiteBishops : blackBishops, from))
            list.addAll(slide(from, wm, new int[]{9,7,-7,-9}));
        // Rooks
        if (isOccupied(wm ? whiteRooks : blackRooks, from))
            list.addAll(slide(from, wm, new int[]{1,-1,8,-8}));
        // Queens
        if (isOccupied(wm ? whiteQueens : blackQueens, from))
            list.addAll(slide(from, wm, new int[]{9,7,-7,-9,1,-1,8,-8}));
        // King
        if (isOccupied(wm ? whiteKing : blackKing, from)) {
            for (int d : new int[]{9,7,-7,-9,1,-1,8,-8}) {
                int t = from + d;
                if (t<0||t>=64) continue;
                if (Math.abs((from%8)-(t%8))>1) continue;
                if (!isOccupied(wm?allWhite():allBlack(), t))
                    list.add(t);
            }
        }

        return list;
    }

    /** Check if squares between a→b (exclusive) are empty */
    private boolean clearBetween(String a, String b) {
        int ia = algebraicToIndex(a), ib = algebraicToIndex(b);
        int step = (ib>ia?1:-1);
        for (int sq=ia+step; sq!=ib; sq+=step) {
            if (isOccupied(occupied(), sq)) return false;
        }
        return true;
    }

    /** Sliding moves for bishops/rooks/queens */
    private List<Integer> slide(int from, boolean wm, int[] dirs) {
        List<Integer> moves = new ArrayList<>();
        long occ = occupied(), own = wm ? allWhite() : allBlack();
        for (int d : dirs) {
            int t = from;
            while (true) {
                int f = t % 8;
                if ((d==9||d==-7) && f==7) break;
                if ((d==7||d==-9) && f==0) break;
                if (d==1 && f==7) break;
                if (d==-1 && f==0) break;
                t += d;
                if (t<0||t>=64) break;
                if (isOccupied(own, t)) break;
                moves.add(t);
                if (isOccupied(occ, t)) break;
            }
        }
        return moves;
    }

    /** Knight attack bitmask */
    private long knightAttacks(int sq) {
        long b=1L<<sq, a=0;
        a|=(b<<17)&~FILE_A; a|=(b<<15)&~FILE_H;
        a|=(b<<10)&~(FILE_A|FILE_B); a|=(b<<6)&~(FILE_H|FILE_G);
        a|=(b>>>17)&~FILE_H; a|=(b>>>15)&~FILE_A;
        a|=(b>>>10)&~(FILE_H|FILE_G); a|=(b>>>6)&~(FILE_A|FILE_B);
        return a;
    }

    /** Is square under attack by side? */
    private boolean isSquareAttacked(int sq, boolean byWhite) {
        // Pawn
        if (byWhite) {
            if (sq>=9 && ((whitePawns>>(sq-9))&1L)==1 && sq%8!=0) return true;
            if (sq>=7 && ((whitePawns>>(sq-7))&1L)==1 && sq%8!=7) return true;
        } else {
            if (sq<56 && ((blackPawns>>(sq+7))&1L)==1 && sq%8!=0) return true;
            if (sq<56 && ((blackPawns>>(sq+9))&1L)==1 && sq%8!=7) return true;
        }
        // Knight
        long km = knightAttacks(sq);
        if (((byWhite?whiteKnights:blackKnights)&km)!=0) return true;
        // Diagonal sliders
        for (int d: new int[]{9,7,-7,-9}) {
            int t=sq;
            while (true) {
                int f=t%8;
                if ((d==9||d==-7)&&f==7) break;
                if ((d==7||d==-9)&&f==0) break;
                t+=d; if (t<0||t>=64) break;
                if (isOccupied(allWhite(),t)||isOccupied(allBlack(),t)) {
                    long diag = byWhite?(whiteBishops|whiteQueens):(blackBishops|blackQueens);
                    if (((diag>>>t)&1L)==1) return true;
                    break;
                }
            }
        }
        // Orthogonal sliders
        for (int d: new int[]{1,-1,8,-8}) {
            int t=sq;
            while (true) {
                int f=t%8;
                if (d==1 && f==7) break;
                if (d==-1 && f==0) break;
                t+=d; if (t<0||t>=64) break;
                if (isOccupied(allWhite(),t)||isOccupied(allBlack(),t)) {
                    long ort = byWhite?(whiteRooks|whiteQueens):(blackRooks|blackQueens);
                    if (((ort>>>t)&1L)==1) return true;
                    break;
                }
            }
        }
        // King
        for (int d: new int[]{9,7,-7,-9,1,-1,8,-8}) {
            int t = sq + d;
            if (t<0||t>=64) continue;
            if (Math.abs((sq%8)-(t%8))>1) continue;
            if (byWhite && ((whiteKing>>>t)&1L)==1) return true;
            if (!byWhite && ((blackKing>>>t)&1L)==1) return true;
        }
        return false;
    }

    /** Evaluate: material + check + checkmate */
    public int evaluate() {
        if (isCheckmate(true))  return -10000000;
        if (isCheckmate(false)) return  10000000;
        int score=0;
        for(int i=0;i<64;i++){
            if (isOccupied(whitePawns,i))   score+=1;
            if (isOccupied(whiteKnights,i)) score+=3;
            if (isOccupied(whiteBishops,i)) score+=3;
            if (isOccupied(whiteRooks,i))   score+=5;
            if (isOccupied(whiteQueens,i))  score+=9;
            if (isOccupied(whiteKing,i))    score+=200000;
            if (isOccupied(blackPawns,i))   score-=1;
            if (isOccupied(blackKnights,i)) score-=3;
            if (isOccupied(blackBishops,i)) score-=3;
            if (isOccupied(blackRooks,i))   score-=5;
            if (isOccupied(blackQueens,i))  score-=9;
            if (isOccupied(blackKing,i))    score-=200000;
        }
        if (isInCheck(false)) score -= 50;
        if (isInCheck(true))  score += 50;
        return score;
    }

    


     private void drawBoard() {
        StdDraw.setCanvasSize(600,600);
        StdDraw.setXscale(0,8); StdDraw.setYscale(0,8);
        StdDraw.clear();

        // draw squares
        for(int i=0;i<64;i++){
            int r=i/8, c=i%8;
            boolean light=(r+c)%2==0;
            StdDraw.setPenColor(light?
                new Color(240,217,181):
                new Color(181,136,99));
            StdDraw.filledSquare(c+0.5, r+0.5, 0.5);
        }

        // draw pieces
        for(int i=0;i<64;i++){
            int r=i/8, c=i%8;
            String imgFile = null;
            if(isOccupied(whitePawns,i))   imgFile=imgs[0];
            else if(isOccupied(whiteKnights,i)) imgFile=imgs[1];
            else if(isOccupied(whiteBishops,i)) imgFile=imgs[2];
            else if(isOccupied(whiteRooks,i))   imgFile=imgs[3];
            else if(isOccupied(whiteQueens,i))  imgFile=imgs[4];
            else if(isOccupied(whiteKing,i))    imgFile=imgs[5];
            else if(isOccupied(blackPawns,i))   imgFile=imgs[6];
            else if(isOccupied(blackKnights,i)) imgFile=imgs[7];
            else if(isOccupied(blackBishops,i)) imgFile=imgs[8];
            else if(isOccupied(blackRooks,i))   imgFile=imgs[9];
            else if(isOccupied(blackQueens,i))  imgFile=imgs[10];
            else if(isOccupied(blackKing,i))    imgFile=imgs[11];

            if(imgFile!=null) {
                // draw at center of square, size 1x1
                StdDraw.picture(c+0.5, r+0.5, imgFile, 1, 1);
            }
            
        }
        for(int i=0;i<64;i++){
            int r=i/8, c=i%8;
            StdDraw.setPenColor(Color.DARK_GRAY);
            StdDraw.text(c+0.1, r+0.1, ""+indexToFile(i)+indexToRank(i));
        }
    }


    // Helper to apply either O-O/O-O-O or normal moves
    private void applyAlgebraicMove(String mv) {
        if (mv.equals("O-O") || mv.equals("O-O-O")) {
            boolean ks = mv.equals("O-O");
            int from = algebraicToIndex(whiteToMove ? "E1" : "E8");
            String dest = whiteToMove
                ? (ks ? "G1" : "C1")
                : (ks ? "G8" : "C8");
            applyMove(from, algebraicToIndex(dest));
        } else {
            int from = algebraicToIndex(mv.substring(0,2));
            int to   = algebraicToIndex(mv.substring(3,5));
            applyMove(from, to);
        }
    }

    public void startGame() {
        Scanner sc = new Scanner(System.in);
        Pattern p = Pattern.compile("^([A-H][1-8]-[A-H][1-8])$|^(O-O|O-O-O)$");

        while (true) {
            drawBoard();
            List<String> legal = generateMoves();
            if (legal.isEmpty()) {
                System.out.println("Game over! " + (isCheckmate(whiteToMove) ? (whiteToMove ? "Black wins!" : "White wins!") : "Stalemate!"));
                break;
            }
            if (!whiteToMove) {
                System.out.println("Black to move. Legal: " + legal);
                String ai = ChessAI.chooseMove(this);
                System.out.println("AI plays: " + ai);
                applyAlgebraicMove(ai);
            } else {
                System.out.println("White to move. Legal: " + legal);
                System.out.print("Enter move or EXIT: ");
                String in = sc.nextLine().trim().toUpperCase();
                if (in.equals("EXIT")) break;
                Matcher m = p.matcher(in);
                if (!m.matches() || !legal.contains(in)) {
                    System.out.println("Illegal, try again.");
                    continue;
                }
                applyAlgebraicMove(in);
            }
        }
        sc.close();
    }

    public static void main(String[] args) {
        new ChessGame().startGame();
    }
}
