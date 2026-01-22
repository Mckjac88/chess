package chess;

import java.util.Collection;
import java.util.HashSet;

/**
 * A Calculator to determine viable moves
 */
public abstract class PieceMovesCalculator {
    protected final ChessBoard board;
    protected final ChessPosition position;
    protected final ChessPiece piece;
    protected final HashSet<ChessMove> validMoves = new HashSet<>();
    protected enum MoveType {CLEAR, TAKE, BLOCKED}

    public PieceMovesCalculator(ChessBoard board, ChessPosition position){
        this.board = board;
        this.position = position;
        this.piece = board.getPiece(position);
    }

    public static PieceMovesCalculator create(ChessBoard board, ChessPosition position){
        ChessPiece piece = board.getPiece(position);
        if (piece.getPieceType() == ChessPiece.PieceType.QUEEN) return new QueenMovesCalculator(board, position);
        if (piece.getPieceType() == ChessPiece.PieceType.KING) return new KingMovesCalculator(board, position);
        if (piece.getPieceType() == ChessPiece.PieceType.ROOK) return new RookMovesCalculator(board, position);
        if (piece.getPieceType() == ChessPiece.PieceType.BISHOP) return new BishopMovesCalculator(board, position);
        if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT) return new KnightMovesCalculator(board, position);
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) return new PawnMovesCalculator(board, position);
        else throw new RuntimeException("Unrecognized piece");
    }

    MoveType validMove(ChessPosition targetPosition) {
        if (targetPosition.getRow() < 1 || targetPosition.getRow() > 8 || targetPosition.getColumn() < 1 || targetPosition.getColumn() > 8) {
            return MoveType.BLOCKED;
        } else if (board.getPiece(targetPosition) == null) {
            return MoveType.CLEAR;
        } else if (board.getPiece(targetPosition).getTeamColor() != piece.getTeamColor()) {
            return MoveType.TAKE;
        } else return MoveType.BLOCKED;
    }

    void moveDirection(int rowAdjust, int columnAdjust, boolean once){
        boolean stopped = once;
        ChessPosition targetPosition = new ChessPosition(position.getRow(), position.getColumn());
        MoveType targetMove;

        do {
            targetPosition = new ChessPosition(targetPosition.getRow()+rowAdjust, targetPosition.getColumn()+columnAdjust);
            targetMove = validMove(targetPosition);
            if (targetMove != MoveType.BLOCKED) {
                validMoves.add(new ChessMove(position, targetPosition, null));
            }
            if (targetMove != MoveType.CLEAR) {
                stopped = true;
            }
        } while (!stopped);
    }

    abstract Collection<ChessMove> pieceMoves();

    private static class QueenMovesCalculator extends PieceMovesCalculator {
        public QueenMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
        }

        @Override
        Collection<ChessMove> pieceMoves() {
            moveDirection(-1,-1, false);
            moveDirection(-1,0, false);
            moveDirection(-1,1, false);
            moveDirection(0,-1, false);
            moveDirection(0,1, false);
            moveDirection(1,-1, false);
            moveDirection(1,0, false);
            moveDirection(1,1, false);
            return validMoves;
        }
    }

    private static class KingMovesCalculator extends PieceMovesCalculator {
        public KingMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
        }

        @Override
        Collection<ChessMove> pieceMoves() {
            moveDirection(-1,-1, true);
            moveDirection(-1,0, true);
            moveDirection(-1,1, true);
            moveDirection(0,-1, true);
            moveDirection(0,1, true);
            moveDirection(1,-1, true);
            moveDirection(1,0, true);
            moveDirection(1,1, true);
            return validMoves;
        }
    }

    private static class RookMovesCalculator extends PieceMovesCalculator {
        public RookMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
        }

        @Override
        Collection<ChessMove> pieceMoves() {
            moveDirection(-1,0, false);
            moveDirection(0,-1, false);
            moveDirection(0,1, false);
            moveDirection(1,0, false);

            return validMoves;
        }
    }

    private static class BishopMovesCalculator extends PieceMovesCalculator {
        public BishopMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
        }

        @Override
        Collection<ChessMove> pieceMoves() {
            moveDirection(-1,-1, false);
            moveDirection(-1,1, false);
            moveDirection(1,-1, false);
            moveDirection(1,1, false);
            return validMoves;
        }
    }

    private static class KnightMovesCalculator extends PieceMovesCalculator {
        public KnightMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
        }

        @Override
        Collection<ChessMove> pieceMoves() {
            moveDirection(-2,-1, true);
            moveDirection(-2,1, true);
            moveDirection(-1,-2, true);
            moveDirection(-1,2, true);
            moveDirection(1,-2, true);
            moveDirection(1,2, true);
            moveDirection(2,-1, true);
            moveDirection(2,1, true);
            return validMoves;
        }
    }

    private static class PawnMovesCalculator extends PieceMovesCalculator {
        public PawnMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
        }

        void moveDirection(ChessGame.TeamColor color){
            int rowAdjust, startRow;
            if(color == ChessGame.TeamColor.WHITE) {
                rowAdjust = 1;
                startRow = 2;
            }
            else {
                rowAdjust = -1;
                startRow = 7;
            }
            ChessPosition targetPosition = new ChessPosition(position.getRow(), position.getColumn());

            MoveType targetMove;
            targetPosition = new ChessPosition(targetPosition.getRow() + rowAdjust, targetPosition.getColumn());
            ChessPosition pawnTakeLeft = new ChessPosition(targetPosition.getRow(), targetPosition.getColumn()-1);
            ChessPosition pawnTakeRight = new ChessPosition(targetPosition.getRow(), targetPosition.getColumn()+1);

            targetMove = validMove(targetPosition);
            if (targetMove == MoveType.CLEAR) {
                validMoves.add(new ChessMove(position, targetPosition, null));
                if (position.getRow() == startRow) {
                    ChessPosition doubleStep = new ChessPosition(targetPosition.getRow() + rowAdjust, targetPosition.getColumn());
                    targetMove = validMove(doubleStep);
                    if (targetMove == MoveType.CLEAR) {
                        validMoves.add(new ChessMove(position, doubleStep, null));
                    }
                }
            }

            targetMove = validMove(pawnTakeLeft);
            if (targetMove == MoveType.TAKE) {
                validMoves.add(new ChessMove(position, pawnTakeLeft, null));
            }

            targetMove = validMove(pawnTakeRight);
            if (targetMove == MoveType.TAKE) {
                validMoves.add(new ChessMove(position, pawnTakeRight, null));
            }
        }

        @Override
        Collection<ChessMove> pieceMoves() {
            moveDirection(piece.getTeamColor());
            return validMoves;
        }
    }
}