// ChessAI.h

#ifndef CHESSAI_H
#define CHESSAI_H

#include <string>
#include "ChessGame.h"

namespace ChessAI {
    std::string chooseMove(const ChessGame &game);
}

#endif
