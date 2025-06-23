// ChessAIW.java

import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class ChessAIW {
    private static final int MAX_DEPTH = 5;

    /**
     * Choose the best move for White by searching each root move in parallel.
     * We generate all legal White moves, apply each to a fresh copy of the game,
     * run minimax (with α–β pruning) from the resulting position, and then pick
     * the move with the highest returned score.
     */
    public static String chooseMove(ChessGame game) {
        int depth = MAX_DEPTH - 1;  // we've already made one half-move at the root

        return game.generateMoves()
                   .parallelStream()
                   .map(mv -> {
                       ChessGame copy = game.copy();
                       copy.applyAlgebraicMove(mv);
                       // after White’s move, it's Black’s turn => maximizing = false
                       int score = minimax(copy, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
                       return new SimpleEntry<>(mv, score);
                   })
                   // pick the entry with the largest score
                   .max(Comparator.comparingInt(SimpleEntry::getValue))
                   .map(SimpleEntry::getKey)
                   .orElse(null);
    }

    /**
     * Standard minimax with alpha–beta pruning.
     *
     * @param game       the current position
     * @param depth      how many half-moves to look ahead
     * @param alpha      the best already guaranteed value for the maximizer
     * @param beta       the best already guaranteed value for the minimizer
     * @param maximizing true if it's White’s turn to move (maximize evaluation),
     *                   false if it's Black’s turn (minimize evaluation).
     */
    private static int minimax(ChessGame game, int depth, int alpha, int beta, boolean maximizing) {
        if (depth == 0)
            return game.evaluate();

        List<String> moves = game.generateMoves();
        if (maximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (String mv : moves) {
                game.applyAlgebraicMove(mv);
                int eval = minimax(game, depth - 1, alpha, beta, false);
                game.undoMove();  // assumes ChessGame provides undoMove()
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
