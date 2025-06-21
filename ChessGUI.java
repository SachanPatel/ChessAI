// ChessGUI.java

import edu.princeton.cs.algs4.StdDraw;
import java.awt.Color;

public class ChessGUI {
    private final String[] imgs = {
        "pawn_white.png","knight_white.png","bishop_white.png",
        "rook_white.png","queen_white.png","king_white.png",
        "pawn_black.png","knight_black.png","bishop_black.png",
        "rook_black.png","queen_black.png","king_black.png"
    };

    /**
     * Draws the board and pieces to StdDraw exactly as you had in Java.
     * You can call this from JNI or from a plain Java main loop.
     */
    public void drawBoard(long whitePawns, long whiteKnights, long whiteBishops,
                          long whiteRooks, long whiteQueens, long whiteKing,
                          long blackPawns, long blackKnights, long blackBishops,
                          long blackRooks, long blackQueens, long blackKing) {
        StdDraw.setCanvasSize(600,600);
        StdDraw.setXscale(0,8); StdDraw.setYscale(0,8);
        StdDraw.clear();

        // draw squares
        for(int i=0;i<64;i++){
            int r=i/8, c=i%8;
            boolean light=(r+c)%2==0;
            StdDraw.setPenColor(light? new Color(240,217,181) : new Color(181,136,99));
            StdDraw.filledSquare(c+0.5, r+0.5, 0.5);
        }

        // draw pieces
        for(int i=0;i<64;i++){
            int r=i/8, c=i%8;
            String imgFile = null;
            if(((whitePawns>>i)&1L)==1)      imgFile=imgs[0];
            else if(((whiteKnights>>i)&1L)==1)imgFile=imgs[1];
            else if(((whiteBishops>>i)&1L)==1)imgFile=imgs[2];
            else if(((whiteRooks>>i)&1L)==1)  imgFile=imgs[3];
            else if(((whiteQueens>>i)&1L)==1) imgFile=imgs[4];
            else if(((whiteKing>>i)&1L)==1)   imgFile=imgs[5];
            else if(((blackPawns>>i)&1L)==1)  imgFile=imgs[6];
            else if(((blackKnights>>i)&1L)==1)imgFile=imgs[7];
            else if(((blackBishops>>i)&1L)==1)imgFile=imgs[8];
            else if(((blackRooks>>i)&1L)==1)  imgFile=imgs[9];
            else if(((blackQueens>>i)&1L)==1) imgFile=imgs[10];
            else if(((blackKing>>i)&1L)==1)   imgFile=imgs[11];

            if(imgFile!=null){
                StdDraw.picture(c+0.5, r+0.5, imgFile, 1, 1);
            }
        }

        // draw coordinates
        for(int i=0;i<64;i++){
            int r=i/8, c=i%8;
            StdDraw.setPenColor(Color.DARK_GRAY);
            StdDraw.text(c+0.1, r+0.1,
                "" + (char)('A'+c) + (char)('1'+r));
        }
    }
}
