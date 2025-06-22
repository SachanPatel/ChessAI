// ChessAI.java

import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class ChessAI {
    private static final int MAX_DEPTH = 6;

    /**
     * Choose the best move by searching each root move in parallel.
     */
    public static String chooseMove(ChessGame game) {
        int depth = MAX_DEPTH - 1;
        return game.generateMoves()
            .parallelStream()
            .map(mv -> {
                // for each candidate move, make a copy, apply the move, run minimax
                ChessGame copy = game.copy();
                copy.applyAlgebraicMove(mv);
                int score = minimax(copy, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
                return new SimpleEntry<>(mv, score);
            })
            .min(Comparator.comparingInt(SimpleEntry::getValue))
            .map(SimpleEntry::getKey)
            .orElse(null);
    }

    /**
     * Standard minimax with alpha–beta pruning.
     */
    private static int minimax(ChessGame game, int depth, int alpha, int beta, boolean maximizing) {
        if (depth == 0) {
            return game.evaluate();
        }
        List<String> moves = game.generateMoves();
        if (maximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (String mv : moves) {
                ChessGame child = game.copy();
                child.applyAlgebraicMove(mv);
                int eval = minimax(child, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;  // α–β cutoff
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (String mv : moves) {
                ChessGame child = game.copy();
                child.applyAlgebraicMove(mv);
                int eval = minimax(child, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;  // α–β cutoff
            }
            return minEval;
        }
    }
}
