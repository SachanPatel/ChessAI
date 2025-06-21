// ChessAI.cpp

#include "ChessAI.h"
#include <limits>
#include <algorithm>

static const int MAX_DEPTH = 5;

// minimax w/ alpha-beta
static int minimax(ChessGame game,
                   int depth,
                   int alpha,
                   int beta,
                   bool maximizing)
{
    if(depth==0) return game.evaluate();
    auto moves = game.generateMoves();
    if(maximizing){
        int maxEval = std::numeric_limits<int>::min();
        for(auto &mv: moves){
            if(mv.rfind("O-O",0)==0) continue;  // skip castling here
            ChessGame child = game;
            child.applyAlgebraicMove(mv);
            int eval = minimax(child, depth-1, alpha, beta, false);
            maxEval = std::max(maxEval, eval);
            alpha = std::max(alpha, eval);
            if(beta <= alpha) break;
        }
        return maxEval;
    } else {
        int minEval = std::numeric_limits<int>::max();
        for(auto &mv: moves){
            if(mv.rfind("O-O",0)==0) continue;
            ChessGame child = game;
            child.applyAlgebraicMove(mv);
            int eval = minimax(child, depth-1, alpha, beta, true);
            minEval = std::min(minEval, eval);
            beta = std::min(beta, eval);
            if(beta <= alpha) break;
        }
        return minEval;
    }
}

std::string ChessAI::chooseMove(const ChessGame &game) {
    ChessGame root = game;
    auto moves = root.generateMoves();
    std::string best;
    int bestScore = std::numeric_limits<int>::max();
    for(auto &mv: moves){
        ChessGame child = root;
        child.applyAlgebraicMove(mv);
        int score = minimax(child, MAX_DEPTH-1,
                            std::numeric_limits<int>::min(),
                            std::numeric_limits<int>::max(),
                            true);
        if(score < bestScore){
            bestScore = score;
            best = mv;
        }
    }
    return best;
}
