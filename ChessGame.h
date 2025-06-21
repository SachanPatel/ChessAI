// ChessGame.h

#ifndef CHESSGAME_H
#define CHESSGAME_H

#include <string>
#include <vector>
#include <cstdint>
#include <initializer_list>

class ChessGame {
public:
    ChessGame();

    // coordinate conversions
    int  algebraicToIndex(const std::string& sq) const;
    char indexToFile(int idx)              const;
    char indexToRank(int idx)              const;

    // apply moves
    void applyMove(int from, int to);
    void applyAlgebraicMove(const std::string& mv);

    // move generation
    std::vector<std::string> generateMoves()    const;
    std::vector<int>         generateMovesFrom(int from) const;

    // game state queries
    bool isInCheck(bool white)    const;
    bool isCheckmate(bool white)  const;

    // evaluation (material + mobility + center + checks)
    int evaluate()               const;

    // simple ASCII UI
    void startGame();

private:
    // bitboards
    uint64_t whitePawns, whiteKnights, whiteBishops, whiteRooks, whiteQueens, whiteKing;
    uint64_t blackPawns, blackKnights, blackBishops, blackRooks, blackQueens, blackKing;
    bool     whiteToMove;
    bool     whiteCastleKingSide, whiteCastleQueenSide;
    bool     blackCastleKingSide, blackCastleQueenSide;
    int      enPassantTarget;

    // file masks
    static const uint64_t FILE_A;
    static const uint64_t FILE_B;
    static const uint64_t FILE_G;
    static const uint64_t FILE_H;

    // bitboard helpers
    uint64_t allWhite() const;
    uint64_t allBlack() const;
    uint64_t occupied() const;
    bool     isOccupied(uint64_t bb, int idx) const;

    // sliding + knight attacks
    std::vector<int> slide(int from, bool wm, const std::initializer_list<int>& dirs) const;
    uint64_t         knightAttacks(int sq) const;

    // castling / attack detection
    bool clearBetween(int a, int b) const;
    bool isSquareAttacked(int sq, bool byWhite) const;
};

#endif // CHESSGAME_H
