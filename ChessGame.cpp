// ChessGame.cpp

#include "ChessGame.h"
#include "ChessAI.h"            // new: bring in the AI
#include <iostream>
#include <string>
#include <vector>
#include <regex>
#include <algorithm>
#include <cmath>

// static file masks
const uint64_t ChessGame::FILE_A = 0x0101010101010101ULL;
const uint64_t ChessGame::FILE_B = ChessGame::FILE_A << 1;
const uint64_t ChessGame::FILE_G = ChessGame::FILE_A << 6;
const uint64_t ChessGame::FILE_H = ChessGame::FILE_A << 7;

ChessGame::ChessGame() {
    whitePawns   = 0x000000000000FF00ULL;
    whiteRooks   = 0x0000000000000081ULL;
    whiteKnights = 0x0000000000000042ULL;
    whiteBishops = 0x0000000000000024ULL;
    whiteQueens  = 0x0000000000000008ULL;
    whiteKing    = 0x0000000000000010ULL;

    blackPawns   = 0x00FF000000000000ULL;
    blackRooks   = 0x8100000000000000ULL;
    blackKnights = 0x4200000000000000ULL;
    blackBishops = 0x2400000000000000ULL;
    blackQueens  = 0x0800000000000000ULL;
    blackKing    = 0x1000000000000000ULL;

    whiteToMove            = true;
    whiteCastleKingSide    = whiteCastleQueenSide = true;
    blackCastleKingSide    = blackCastleQueenSide = true;
    enPassantTarget        = -1;
}

// coordinate conversions
int ChessGame::algebraicToIndex(const std::string& sq) const {
    int file = sq[0] - 'A';
    int rank = sq[1] - '1';
    return rank * 8 + file;
}
char ChessGame::indexToFile(int idx) const { return char('A' + (idx % 8)); }
char ChessGame::indexToRank(int idx) const { return char('1' + (idx / 8)); }

// bitboard unions
uint64_t ChessGame::allWhite() const { return whitePawns|whiteKnights|whiteBishops|whiteRooks|whiteQueens|whiteKing; }
uint64_t ChessGame::allBlack() const { return blackPawns|blackKnights|blackBishops|blackRooks|blackQueens|blackKing; }
uint64_t ChessGame::occupied() const { return allWhite() | allBlack(); }
bool     ChessGame::isOccupied(uint64_t bb, int idx) const { return ((bb >> idx) & 1ULL) != 0; }

// apply a move by indices
void ChessGame::applyMove(int from, int to) {
    int oldEp = enPassantTarget;
    enPassantTarget = -1;

    uint64_t maskFrom = 1ULL<<from, maskTo = 1ULL<<to;
    uint64_t bbs[12] = {
        whitePawns, whiteKnights, whiteBishops, whiteRooks, whiteQueens, whiteKing,
        blackPawns, blackKnights, blackBishops, blackRooks, blackQueens, blackKing
    };

    // clear destination
    for(int i=0;i<12;i++) bbs[i] &= ~maskTo;

    // move piece
    int moved=-1;
    for(int i=0;i<12;i++){
        if((bbs[i]&maskFrom)!=0){
            moved=i;
            bbs[i] = (bbs[i]&~maskFrom)|maskTo;
            break;
        }
    }
    bool wm = moved<6;

    // double pawn → en passant
    if(moved==(wm?0:6) && std::abs(to-from)==16)
        enPassantTarget = (from+to)/2;

    // en passant capture
    if(to==oldEp && moved==(wm?0:6)){
        int cap = wm? to-8 : to+8;
        bbs[ wm?6:0 ] &= ~(1ULL<<cap);
    }

    // promotion
    if(moved==(wm?0:6)){
        int rank = to/8;
        if((wm&&rank==7) || (!wm&&rank==0)){
            bbs[moved] &= ~maskTo;
            int qIdx = wm?4:10;
            bbs[qIdx] |= maskTo;
        }
    }

    // revoke castling rights
    if(moved==5)  whiteCastleKingSide = whiteCastleQueenSide = false;
    if(moved==11) blackCastleKingSide = blackCastleQueenSide = false;
    int H1=algebraicToIndex("H1"), A1=algebraicToIndex("A1");
    int H8=algebraicToIndex("H8"), A8=algebraicToIndex("A8");
    if(from==H1||to==H1) whiteCastleKingSide = false;
    if(from==A1||to==A1) whiteCastleQueenSide= false;
    if(from==H8||to==H8) blackCastleKingSide = false;
    if(from==A8||to==A8) blackCastleQueenSide= false;

    // rook relocation on castling
    int E1=algebraicToIndex("E1"), F1=algebraicToIndex("F1"),
        G1=algebraicToIndex("G1"), C1=algebraicToIndex("C1"), D1=algebraicToIndex("D1");
    int E8=algebraicToIndex("E8"), F8=algebraicToIndex("F8"),
        G8=algebraicToIndex("G8"), C8=algebraicToIndex("C8"), D8=algebraicToIndex("D8");

    if(moved==5 && from==E1){
        if(to==G1){ bbs[3]&=~(1ULL<<H1); bbs[3]|=(1ULL<<F1); }
        else      { bbs[3]&=~(1ULL<<A1); bbs[3]|=(1ULL<<D1); }
    }
    if(moved==11 && from==E8){
        if(to==G8){ bbs[9]&=~(1ULL<<H8); bbs[9]|=(1ULL<<F8); }
        else      { bbs[9]&=~(1ULL<<A8); bbs[9]|=(1ULL<<D8); }
    }

    // write back
    whitePawns   = bbs[0];  whiteKnights = bbs[1];  whiteBishops = bbs[2];
    whiteRooks   = bbs[3];  whiteQueens  = bbs[4];  whiteKing    = bbs[5];
    blackPawns   = bbs[6];  blackKnights = bbs[7];  blackBishops = bbs[8];
    blackRooks   = bbs[9];  blackQueens  = bbs[10]; blackKing    = bbs[11];

    // flip
    whiteToMove = !whiteToMove;
}

// apply algebraic like "E2-E4" or "O-O"
void ChessGame::applyAlgebraicMove(const std::string& mv) {
    if(mv=="O-O"||mv=="O-O-O"){
        bool ks = (mv=="O-O");
        int from = algebraicToIndex(whiteToMove?"E1":"E8");
        int to   = algebraicToIndex(whiteToMove?(ks?"G1":"C1"):(ks?"G8":"C8"));
        applyMove(from,to);
    } else {
        int f = algebraicToIndex(mv.substr(0,2));
        int t = algebraicToIndex(mv.substr(3,2));
        applyMove(f,t);
    }
}

// generate pseudo-legal + castling, then filter out checks
std::vector<std::string> ChessGame::generateMoves() const {
    std::vector<std::string> pseudo;
    uint64_t bb = whiteToMove?allWhite():allBlack();

    // pseudo
    while(bb){
        uint64_t m = bb & -bb;
        int from  = __builtin_ctzll(m);
        bb &= bb-1;
        for(int to:generateMovesFrom(from)){
            std::string mv;
            mv += indexToFile(from);
            mv += indexToRank(from);
            mv += '-';
            mv += indexToFile(to);
            mv += indexToRank(to);
            pseudo.push_back(mv);
        }
    }

    // castling
    int E1=algebraicToIndex("E1"), F1=algebraicToIndex("F1"), G1=algebraicToIndex("G1"), C1=algebraicToIndex("C1"), D1=algebraicToIndex("D1");
    int E8=algebraicToIndex("E8"), F8=algebraicToIndex("F8"), G8=algebraicToIndex("G8"), C8=algebraicToIndex("C8"), D8=algebraicToIndex("D8");
    if(whiteToMove){
        if(whiteCastleKingSide && clearBetween(E1,G1)
           && !isSquareAttacked(E1,false)
           && !isSquareAttacked(F1,false)
           && !isSquareAttacked(G1,false))
            pseudo.push_back("O-O");
        if(whiteCastleQueenSide && clearBetween(E1,C1)
           && !isSquareAttacked(E1,false)
           && !isSquareAttacked(D1,false)
           && !isSquareAttacked(C1,false))
            pseudo.push_back("O-O-O");
    } else {
        if(blackCastleKingSide && clearBetween(E8,G8)
           && !isSquareAttacked(E8,true)
           && !isSquareAttacked(F8,true)
           && !isSquareAttacked(G8,true))
            pseudo.push_back("O-O");
        if(blackCastleQueenSide && clearBetween(E8,C8)
           && !isSquareAttacked(E8,true)
           && !isSquareAttacked(D8,true)
           && !isSquareAttacked(C8,true))
            pseudo.push_back("O-O-O");
    }

    // filter
    std::vector<std::string> legal;
    for(auto& mv:pseudo){
        ChessGame tmp = *this;
        tmp.applyAlgebraicMove(mv);
        if(!tmp.isInCheck(whiteToMove))
            legal.push_back(mv);
    }
    return legal;
}

// raw moves from one square
std::vector<int> ChessGame::generateMovesFrom(int from) const {
    std::vector<int> list;
    uint64_t occ = occupied();
    bool wm = isOccupied(allWhite(),from);

    // pawn
    if(isOccupied(wm?whitePawns:blackPawns,from)){
        int dir = wm?+8:-8, to = from+dir;
        if(to>=0&&to<64&&!isOccupied(occ,to)){
            list.push_back(to);
            int r=from/8;
            if((wm&&r==1)||(!wm&&r==6)){
                int to2=from+2*dir;
                if(!isOccupied(occ,to2)) list.push_back(to2);
            }
        }
        int caps[2]={wm?from+7:from-7, wm?from+9:from-9};
        for(int c:caps){
            if(c<0||c>=64) continue;
            if(std::abs((from%8)-(c%8))!=1) continue;
            if(wm&&isOccupied(allBlack(),c)) list.push_back(c);
            if(!wm&&isOccupied(allWhite(),c)) list.push_back(c);
            if(c==enPassantTarget) list.push_back(c);
        }
    }

    // knight
    if(isOccupied(wm?whiteKnights:blackKnights,from)){
        uint64_t att = knightAttacks(from);
        for(int t=0;t<64;t++){
            if(((att>>t)&1ULL) && !isOccupied(wm?allWhite():allBlack(),t))
                list.push_back(t);
        }
    }

    // bishop / rook / queen slides
    if(isOccupied(wm?whiteBishops:blackBishops,from)){
        for(int d: {9,7,-7,-9}){
            for(int t=from;;){
                int f=t%8;
                if(((d==9||d==-7)&&f==7)||((d==7||d==-9)&&f==0)) break;
                t+=d; if(t<0||t>=64) break;
                if(isOccupied(wm?allWhite():allBlack(),t)){
                    if(isOccupied(wm?whiteBishops:blackBishops,t) || isOccupied(wm?whiteQueens:blackQueens,t))
                        list.push_back(t);
                    break;
                }
                list.push_back(t);
            }
        }
    }
    if(isOccupied(wm?whiteRooks:blackRooks,from)){
        for(int d: {1,-1,8,-8}){
            for(int t=from;;){
                int f=t%8;
                if((d==1&&f==7)||(d==-1&&f==0)) break;
                t+=d; if(t<0||t>=64) break;
                if(isOccupied(wm?allWhite():allBlack(),t)){
                    if(isOccupied(wm?whiteRooks:blackRooks,t) || isOccupied(wm?whiteQueens:blackQueens,t))
                        list.push_back(t);
                    break;
                }
                list.push_back(t);
            }
        }
    }
    if(isOccupied(wm?whiteQueens:blackQueens,from)){
        for(int d: {9,7,-7,-9,1,-1,8,-8}){
            for(int t=from;;){
                int f=t%8;
                if(((d==9||d==-7)&&f==7)||((d==7||d==-9)&&f==0)||(d==1&&f==7)||(d==-1&&f==0)) break;
                t+=d; if(t<0||t>=64) break;
                if(isOccupied(wm?allWhite():allBlack(),t)){
                    if(isOccupied(wm?whiteQueens:blackQueens,t))
                        list.push_back(t);
                    break;
                }
                list.push_back(t);
            }
        }
    }

    // king moves
    if(isOccupied(wm?whiteKing:blackKing,from)){
        for(int d: {9,7,-7,-9,1,-1,8,-8}){
            int t=from+d;
            if(t<0||t>=64) continue;
            if(std::abs((from%8)-(t%8))>1) continue;
            if(!isOccupied(wm?allWhite():allBlack(),t))
                list.push_back(t);
        }
    }

    return list;
}

// clear straight line
bool ChessGame::clearBetween(int a,int b) const {
    int step=(b>a?1:-1);
    for(int sq=a+step;sq!=b;sq+=step)
        if(isOccupied(occupied(),sq)) return false;
    return true;
}

// knight attack mask
uint64_t ChessGame::knightAttacks(int sq) const {
    uint64_t b=1ULL<<sq, a=0;
    a |= (b<<17)&~FILE_A; a |= (b<<15)&~FILE_H;
    a |= (b<<10)&~(FILE_A|FILE_B); a |= (b<<6)&~(FILE_H|FILE_G);
    a |= (b>>17)&~FILE_H;  a |= (b>>15)&~FILE_A;
    a |= (b>>10)&~(FILE_H|FILE_G); a |= (b>>6)&~(FILE_A|FILE_B);
    return a;
}

// is under attack?
bool ChessGame::isSquareAttacked(int sq,bool byWhite) const {
    // pawn
    if(byWhite){
        if(sq>=9  && ((whitePawns>>(sq-9))&1ULL) && sq%8!=0) return true;
        if(sq>=7  && ((whitePawns>>(sq-7))&1ULL) && sq%8!=7) return true;
    } else {
        if(sq<56 && ((blackPawns>>(sq+7))&1ULL) && sq%8!=0) return true;
        if(sq<56 && ((blackPawns>>(sq+9))&1ULL) && sq%8!=7) return true;
    }
    // knight
    auto km = knightAttacks(sq);
    if(((byWhite?whiteKnights:blackKnights)&km)!=0) return true;
    // sliders
    for(int d: {9,7,-7,-9}){
        int t=sq;
        while(true){
            int f=t%8;
            if(((d==9||d==-7)&&f==7)||((d==7||d==-9)&&f==0)) break;
            t+=d; if(t<0||t>=64) break;
            if(isOccupied(allWhite(),t)||isOccupied(allBlack(),t)){
                auto D = byWhite?(whiteBishops|whiteQueens):(blackBishops|blackQueens);
                if((D>>t)&1ULL) return true;
                break;
            }
        }
    }
    for(int d: {1,-1,8,-8}){
        int t=sq;
        while(true){
            int f=t%8;
            if((d==1&&f==7)||(d==-1&&f==0)) break;
            t+=d; if(t<0||t>=64) break;
            if(isOccupied(allWhite(),t)||isOccupied(allBlack(),t)){
                auto D = byWhite?(whiteRooks|whiteQueens):(blackRooks|blackQueens);
                if((D>>t)&1ULL) return true;
                break;
            }
        }
    }
    // king
    for(int d: {9,7,-7,-9,1,-1,8,-8}){
        int t=sq+d;
        if(t<0||t>=64) continue;
        if(std::abs((sq%8)-(t%8))>1) continue;
        if(byWhite && ((whiteKing>>t)&1ULL)) return true;
        if(!byWhite&& ((blackKing>>t)&1ULL)) return true;
    }
    return false;
}

// check / mate
bool ChessGame::isInCheck(bool white) const {
    int k = white?__builtin_ctzll(whiteKing):__builtin_ctzll(blackKing);
    return isSquareAttacked(k,!white);
}
bool ChessGame::isCheckmate(bool white) const {
    if(!isInCheck(white)) return false;
    ChessGame tmp = *this;
    tmp.whiteToMove = white;
    return tmp.generateMoves().empty();
}

// evaluation
int ChessGame::evaluate() const {
    if(isCheckmate(true))  return -10'000'000;
    if(isCheckmate(false)) return  10'000'000;
    int score = 0;
    // material
    for(int i=0;i<64;i++){
        if(isOccupied(whitePawns,i))   score+=1;
        if(isOccupied(whiteKnights,i)) score+=3;
        if(isOccupied(whiteBishops,i)) score+=3;
        if(isOccupied(whiteRooks,i))   score+=5;
        if(isOccupied(whiteQueens,i))  score+=9;
        if(isOccupied(whiteKing,i))    score+=1000;
        if(isOccupied(blackPawns,i))   score-=1;
        if(isOccupied(blackKnights,i)) score-=3;
        if(isOccupied(blackBishops,i)) score-=3;
        if(isOccupied(blackRooks,i))   score-=5;
        if(isOccupied(blackQueens,i))  score-=9;
        if(isOccupied(blackKing,i))    score-=1000;
    }
    // check pressure
    if(isInCheck(true))  score-=50;
    if(isInCheck(false)) score+=50;
    // center bonus
    for(int sq: {27,28,35,36}){
        if(isOccupied(allWhite(),sq)) score+=1;
        if(isOccupied(allBlack(),sq)) score-=1;
    }
    // mobility
    auto myM = generateMoves().size();
    ChessGame tmp = *this;
    tmp.whiteToMove = !whiteToMove;
    auto opM = tmp.generateMoves().size();
    score += int(myM)*2;
    score -= int(opM)*2;
    return score;
}

// ASCII UI

void ChessGame::startGame() {
    std::regex mvRx(R"(^([A-H][1-8]-[A-H][1-8])$|^(O-O|O-O-O)$)");
    std::string input;

    while (true) {
        // --- 1) draw ASCII board ---
        for (int r = 7; r >= 0; --r) {
            std::cout << r+1 << ' ';
            for (int f = 0; f < 8; ++f) {
                int idx = r*8 + f;
                char c = '.';
                if      (isOccupied(whitePawns,   idx)) c='P';
                else if (isOccupied(whiteKnights, idx)) c='N';
                else if (isOccupied(whiteBishops, idx)) c='B';
                else if (isOccupied(whiteRooks,   idx)) c='R';
                else if (isOccupied(whiteQueens,  idx)) c='Q';
                else if (isOccupied(whiteKing,    idx)) c='K';
                else if (isOccupied(blackPawns,   idx)) c='p';
                else if (isOccupied(blackKnights, idx)) c='n';
                else if (isOccupied(blackBishops, idx)) c='b';
                else if (isOccupied(blackRooks,   idx)) c='r';
                else if (isOccupied(blackQueens,  idx)) c='q';
                else if (isOccupied(blackKing,    idx)) c='k';
                std::cout << c << ' ';
            }
            std::cout << "\n";
        }
        std::cout << "  A B C D E F G H\n\n";

        // --- 2) generate legal moves ---
        auto legal = generateMoves();
        if (legal.empty()) {
            if (isCheckmate(whiteToMove))
                std::cout << (whiteToMove ? "Black" : "White") << " wins!\n";
            else
                std::cout << "Stalemate!\n";
            return;
        }

        // DEBUG: see whose turn it is
        // std::cout << "[DEBUG] whiteToMove=" << whiteToMove << "\n";

        // --- 3) AI plays on Black’s turn ---
        if (!whiteToMove) {
            std::string aiMv = ChessAI::chooseMove(*this);
            std::cout << "Black (AI) plays: " << aiMv << "\n\n";
            applyAlgebraicMove(aiMv);
            continue;   // go back to top, redraw board
        }

        // --- 4) Human plays on White’s turn ---
        std::cout << "White to move.  Legal:";
        for (auto &m : legal) std::cout << ' ' << m;
        std::cout << "\nEnter move or EXIT: ";
        std::getline(std::cin, input);

        if (input == "EXIT") return;
        if (!std::regex_match(input, mvRx) ||
            std::find(legal.begin(), legal.end(), input) == legal.end())
        {
            std::cout << "Illegal move, try again.\n\n";
            continue;
        }

        applyAlgebraicMove(input);
        std::cout << "\n";
    }
}