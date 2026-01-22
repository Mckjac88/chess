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

    protected int[][] moveAdjusts;
    protected boolean moveLimit = false;
    protected ChessPiece.PieceType[] possiblePromotions = new ChessPiece.PieceType[] {
            ChessPiece.PieceType.QUEEN,
            ChessPiece.PieceType.ROOK,
            ChessPiece.PieceType.BISHOP,
            ChessPiece.PieceType.KNIGHT};

    public PieceMovesCalculator(ChessBoard board, ChessPosition position){
        this.board = board;
        this.position = position;
        this.piece = board.getPiece(position);
    }

    public static PieceMovesCalculator create(ChessBoard board, ChessPosition position){
        ChessPiece.PieceType type = board.getPiece(position).getPieceType();
        if (type == ChessPiece.PieceType.QUEEN) {return new QueenMovesCalculator(board, position);}
        else if (type == ChessPiece.PieceType.KING) {return new KingMovesCalculator(board, position);}
        else if (type == ChessPiece.PieceType.ROOK) {return new RookMovesCalculator(board, position);}
        else if (type == ChessPiece.PieceType.BISHOP) {return new BishopMovesCalculator(board, position);}
        else if (type == ChessPiece.PieceType.KNIGHT) {return new KnightMovesCalculator(board, position);}
        else if (type == ChessPiece.PieceType.PAWN) {return new PawnMovesCalculator(board, position);}
        else {throw new RuntimeException("Unrecognized piece");}
    }

    MoveType validMove(ChessPosition targetPosition) {
        if (targetPosition.getRow() < 1 || targetPosition.getRow() > 8 || targetPosition.getColumn() < 1 || targetPosition.getColumn() > 8) {
            return MoveType.BLOCKED;
        } else if (board.getPiece(targetPosition) == null) {
            return MoveType.CLEAR;
        } else if (board.getPiece(targetPosition).getTeamColor() != piece.getTeamColor()) {
            return MoveType.TAKE;
        } else {return MoveType.BLOCKED;}
    }

    void moveDirection(int rowAdjust, int columnAdjust, boolean once){
        boolean stopped = once;
        ChessPosition targetPosition = new ChessPosition(position.getRow(), position.getColumn());
        MoveType targetMove;

        do {
            targetPosition = new ChessPosition(targetPosition.getRow()+rowAdjust, targetPosition.getColumn()+columnAdjust);
            targetMove = validMove(targetPosition);
            if (targetMove != MoveType.BLOCKED) {
                addMove(position, targetPosition, false);
            }
            if (targetMove != MoveType.CLEAR) {
                stopped = true;
            }
        } while (!stopped);
    }

    void addMove(ChessPosition position, ChessPosition targetPosition, boolean promotion) {
        if(promotion) {
            for(ChessPiece.PieceType type : possiblePromotions){
                validMoves.add(new ChessMove(position, targetPosition, type));
            }
        }
        else {validMoves.add(new ChessMove(position, targetPosition, null));}
    }

    Collection<ChessMove> pieceMoves() {
        for(int[] adjust : moveAdjusts){
            int rowAdjust = adjust[0];
            int columnAdjust = adjust[1];
            moveDirection(rowAdjust, columnAdjust, moveLimit);
        }
        return validMoves;
    }

    private static class QueenMovesCalculator extends PieceMovesCalculator {
        public QueenMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.moveAdjusts = new int[][] {{-1,1},{-1,0},{-1,-1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
        }

    }

    private static class KingMovesCalculator extends PieceMovesCalculator {
        public KingMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.moveAdjusts = new int[][] {{-1,1},{-1,0},{-1,-1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
            this.moveLimit = true;
        }
    }

    private static class RookMovesCalculator extends PieceMovesCalculator {
        public RookMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.moveAdjusts = new int[][] {{-1,0},{0,-1},{0,1},{1,0}};
        }
    }

    private static class BishopMovesCalculator extends PieceMovesCalculator {
        public BishopMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.moveAdjusts = new int[][] {{-1,-1},{-1,1},{1,-1},{1,1}};
        }
    }

    private static class KnightMovesCalculator extends PieceMovesCalculator {
        public KnightMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.moveAdjusts = new int[][] {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
            this.moveLimit = true;
        }
    }

    private static class PawnMovesCalculator extends PieceMovesCalculator {
        public PawnMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
        }

        void moveDirection(ChessGame.TeamColor color){
            int rowAdjust, startRow, endRow;
            boolean promotion;

            if(color == ChessGame.TeamColor.WHITE) {
                rowAdjust = 1;
                startRow = 2;
                endRow = 8;
            }
            else {
                rowAdjust = -1;
                startRow = 7;
                endRow = 1;
            }

            MoveType targetMove;
            ChessPosition targetPosition = new ChessPosition(position.getRow() + rowAdjust, position.getColumn());
            ChessPosition pawnTakeLeft = new ChessPosition(targetPosition.getRow(), targetPosition.getColumn()-1);
            ChessPosition pawnTakeRight = new ChessPosition(targetPosition.getRow(), targetPosition.getColumn()+1);

            promotion = (targetPosition.getRow() == endRow);

            targetMove = validMove(targetPosition);
            if (targetMove == MoveType.CLEAR) {
                addMove(position, targetPosition, promotion);
                if (position.getRow() == startRow) {
                    ChessPosition doubleStep = new ChessPosition(targetPosition.getRow() + rowAdjust, targetPosition.getColumn());
                    targetMove = validMove(doubleStep);
                    if (targetMove == MoveType.CLEAR) {
                        addMove(position, doubleStep, false);
                    }
                }
            }

            targetMove = validMove(pawnTakeLeft);
            if (targetMove == MoveType.TAKE) {
                addMove(position, pawnTakeLeft, promotion);
            }

            targetMove = validMove(pawnTakeRight);
            if (targetMove == MoveType.TAKE) {
                addMove(position, pawnTakeRight, promotion);
            }
        }

        @Override
        Collection<ChessMove> pieceMoves() {
            moveDirection(piece.getTeamColor());
            return validMoves;
        }
    }
}