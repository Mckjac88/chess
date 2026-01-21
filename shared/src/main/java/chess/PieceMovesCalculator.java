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
    protected enum MoveType {CLEAR, TAKE, BLOCKED};

    public PieceMovesCalculator(ChessBoard board, ChessPosition position){
        this.board = board;
        this.position = position;
        this.piece = board.getPiece(position);
    }

    public static PieceMovesCalculator create(ChessBoard board, ChessPosition position){
        ChessPiece piece = board.getPiece(position);
        if (piece.getPieceType() == ChessPiece.PieceType.QUEEN) return new QueenMovesCalculator(board, position);
        else throw new RuntimeException("Other Pieces not implemented yet");
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
}