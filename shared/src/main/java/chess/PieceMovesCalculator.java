package chess;

import java.util.Collection;
import java.util.List;

/**
 * A Calculator to determine viable moves
 */
public class PieceMovesCalculator {

    private final ChessBoard board;
    private final ChessPosition position;
    private final ChessPiece piece;
    private ChessPosition targetPosition;

    public PieceMovesCalculator(ChessBoard board, ChessPosition position){
        this.board = board;
        this.position = position;
        this.piece = board.getPiece(position);
    }

    boolean validMove() {
        if (targetPosition.getRow() < 1 || targetPosition.getRow() > 8 || targetPosition.getColumn() < 1 || targetPosition.getColumn() > 8) {
            return false;
        } else if (board.getPiece(targetPosition) != null) {
            return board.getPiece(targetPosition).getTeamColor() == piece.getTeamColor();
        } else {
            return true;
        }
    }

    Collection<ChessMove> pieceMoves(){
        return List.of();
    };
}
