// ChessAIB.java

import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class ChessAIB {
    private static final int MAX_DEPTH = 1;

    /**
     * Choose the best move for Black by searching each root move in parallel.
     * We generate all legal Black moves, apply each to a fresh copy of the game,
     * run minimax (with α–β pruning) from the resulting position (now White to move),
     * and then pick the move with the lowest evaluation (since Black wants to minimize).
     */
    public static String chooseMove(ChessGame game) {
        int depth = MAX_DEPTH - 1;

        return game.generateMoves()
                   .parallelStream()
                   .map(mv -> {
                       ChessGame copy = game.copy();
                       copy.applyAlgebraicMove(mv);
                       // After Black’s move, it's White’s turn → maximizing = true
                       int score = minimax(copy, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
                       return new SimpleEntry<>(mv, score);
                   })
                   // pick the entry with the smallest score
                   .min(Comparator.comparingInt(SimpleEntry::getValue))
                   .map(SimpleEntry::getKey)
                   .orElse(null);
    }

    /**
     * Standard minimax with alpha–beta pruning.
     *
     * @param game       current position
     * @param depth      remaining plies to search
     * @param alpha      best guaranteed value for the maximizer (White)
     * @param beta       best guaranteed value for the minimizer (Black)
     * @param maximizing true if it's White’s turn to move (maximize evaluation),
     *                   false if it's Black’s turn (minimize evaluation).
     */
    private static int minimax(ChessGame game, int depth, int alpha, int beta, boolean maximizing) {
        if (depth == 0) {
            return game.evaluate();
        }
        List<String> moves = game.generateMoves();
        if (maximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (String mv : moves) {
                game.applyAlgebraicMove(mv);
                int eval = minimax(game, depth - 1, alpha, beta, false);
                game.undoMove();
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;  // β cutoff
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (String mv : moves) {
                game.applyAlgebraicMove(mv);
                int eval = minimax(game, depth - 1, alpha, beta, true);
                game.undoMove();
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;  // α cutoff
            }
            return minEval;
        }
    }
}
