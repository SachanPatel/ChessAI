#ifndef CHESSGAME_H
#define CHESSGAME_H

#include <cstdint>
#include <string>
#include <vector>

class ChessGame {
public:
    ChessGame();
    int  algebraicToIndex(const std::string &sq) const;
    char indexToFile(int idx) const;
    char indexToRank(int idx) const;

    void applyMove(int from, int to);
    void applyAlgebraicMove(const std::string &mv);

    std::vector<std::string> generateMoves() const;
    std::vector<int>         generateMovesFrom(int from) const;
    bool                     clearBetween(int a, int b) const;

    bool isInCheck(bool white) const;
    bool isCheckmate(bool white) const;

    int  evaluate() const;

    void startGame();  // <-- declare your input loop here

private:
    // bitboards
    uint64_t whitePawns, whiteKnights, whiteBishops, whiteRooks, whiteQueens, whiteKing;
    uint64_t blackPawns, blackKnights, blackBishops, blackRooks, blackQueens, blackKing;

    bool     whiteToMove;
    bool     whiteCastleKingSide, whiteCastleQueenSide;
    bool     blackCastleKingSide, blackCastleQueenSide;
    int      enPassantTarget;

    // file masks
    static const uint64_t FILE_A, FILE_B, FILE_G, FILE_H;

    uint64_t allWhite() const;
    uint64_t allBlack() const;
    uint64_t occupied() const;
    bool     isOccupied(uint64_t bb, int idx) const;
    uint64_t knightAttacks(int sq) const;

    bool isSquareAttacked(int sq, bool byWhite) const;
    std::vector<int> slide(int from, bool wm, const std::initializer_list<int> &dirs) const;
};

#endif // CHESSGAME_H
