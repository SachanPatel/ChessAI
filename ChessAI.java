// ChessAI.java

import java.util.*;

public class ChessAI {
    private static int MAX_DEPTH = 5;

    public static String chooseMove(ChessGame game) {
        ChessGame root = game.copy();
        List<String> moves = root.generateMoves();
        
        String best = null;
        int bestScore = Integer.MAX_VALUE;
        for (String mv : moves) {
            
            ChessGame child = root.copy();
            child.applyAlgebraicMove(mv);
            int score = minimax(child, MAX_DEPTH-1, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
            if (score <= bestScore) {
                bestScore = score;
                best = mv;
            }
        }
        return best;
    }

    private static int minimax(ChessGame game, int depth, int alpha, int beta, boolean maximizing) {
        if (depth == 0) return game.evaluate();
        List<String> moves = game.generateMoves();
        if (maximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (String mv : moves) {
                if (mv.startsWith("O-O")) continue;
                ChessGame child = game.copy();
                int from = child.algebraicToIndex(mv.substring(0,2));
                int to   = child.algebraicToIndex(mv.substring(3,5));
                child.applyMove(from,to);
                int eval = minimax(child, depth-1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (String mv : moves) {
                if (mv.startsWith("O-O")) continue;
                ChessGame child = game.copy();
                int from = child.algebraicToIndex(mv.substring(0,2));
                int to   = child.algebraicToIndex(mv.substring(3,5));
                child.applyMove(from,to);
                int eval = minimax(child, depth-1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }
}
