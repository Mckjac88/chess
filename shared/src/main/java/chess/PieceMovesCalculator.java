package chess;

import java.util.Collection;
import java.util.HashSet;

public abstract class PieceMovesCalculator {
    protected final ChessBoard board;
    protected final ChessPosition position;
    protected final ChessPiece piece;
    protected final Collection<ChessMove> pieceMoves = new HashSet<>();
    protected int[][] pieceDirections;

    public PieceMovesCalculator(ChessBoard board, ChessPosition position) {
        this.board = board;
        this.position = position;
        this.piece = board.getPiece(position);
    }

    public static PieceMovesCalculator create(ChessBoard board, ChessPosition position) {
        ChessPiece piece = board.getPiece(position);
        return switch(piece.getPieceType()) {
            case KING -> new KingMovesCalculator(board, position);
            case QUEEN -> new QueenMovesCalculator(board, position);
            case BISHOP -> new BishopMovesCalculator(board, position);
            case KNIGHT -> new KnightMovesCalculator(board, position);
            case ROOK -> new RookMovesCalculator(board, position);
            case PAWN -> new PawnMovesCalculator(board, position);
        };
    }

    public Collection<ChessMove> moveAll() {
        return pieceMoves;
    }

    private static class PawnMovesCalculator extends PieceMovesCalculator {
        public PawnMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
        }
    }

    private static class RookMovesCalculator extends PieceMovesCalculator {
        public RookMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.pieceDirections = new int[][] {{0,-1},{1,0},{0,1},{-1,0}};
        }
    }

    private static class KnightMovesCalculator extends PieceMovesCalculator {
        public KnightMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.pieceDirections = new int[][] {
                    {1,-2},{2,-1},{2,1},{1,2},
                    {-1,2},{-2,1},{-2,-1},{-1,-2}
            };
        }
    }

    private static class BishopMovesCalculator extends PieceMovesCalculator {
        public BishopMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.pieceDirections = new int[][] {{1,-1},{1,1},{-1,1},{-1,-1}};
        }
    }

    private static class QueenMovesCalculator extends PieceMovesCalculator {
        public QueenMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.pieceDirections = new int[][] {
                    {0,-1},{1,0},{0,1},{-1,0},
                    {1,-1},{1,1},{-1,1},{-1,-1}
            };
        }
    }

    private static class KingMovesCalculator extends PieceMovesCalculator {
        public KingMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.pieceDirections = new int[][] {
                    {0,-1},{1,0},{0,1},{-1,0},
                    {1,-1},{1,1},{-1,1},{-1,-1}
            };
        }
    }
}
